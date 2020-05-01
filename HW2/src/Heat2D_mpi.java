import java.util.Date;
import mpi.*;   // for mpiJava

public class Heat2D_mpi {
  private static double a = 1.0;  // heat speed
  private static double dt = 1.0; // time quantum
  private static double dd = 2.0; // change in system

  public static int flatten(int p, int x, int y, int size) {
    return (p * size * size) + (x * size) + y;
  }
  public static int[] calculateStripeWidths(int size, int numNodes) {
    int[] stripeWidths = new int[numNodes];
    int remainder = size % numNodes;

    for (int rank = 0; rank < numNodes; rank++) {
      int length = size / numNodes + ((rank < remainder) ? 1 : 0);
      stripeWidths[rank] = length;
    }

    return stripeWidths;
  }
  
  public static int[] calculateStartingIndices(int[] stripeWidths, int numNodes) {
    int startingIndices[] = new int[numNodes];
    int counter = 0;

    for (int rank = 0; rank < stripeWidths.length; rank++) {
      startingIndices[rank] = counter;
      counter += stripeWidths[rank];
    }

    return startingIndices;
  }

  public static void main(String[] args) throws MPIException {
    // verify arguments
    if (args.length != 4) {
      System.out.
	println("usage: " +
	    "java Heat2D size max_time heat_time interval");
      System.exit(-1);
    }

    MPI.Init(args);
    int numNodes = MPI.COMM_WORLD.Size();
    int myRank = MPI.COMM_WORLD.Rank();

    // size of the heating area
    int [] sizeArray = new int[1];
    int size;
    // number of coordinates to calculate
    int[] startingIndices = new int[numNodes];
    // how many columns each node is responsible for
    int [] stripeWidths = new int[numNodes];
    // how long to initially heat the bottom center third
    int[] maxTime = new int[1];
    // how long to initially heat the bottom center third
    int[] heatTime = new int[1];
    // how often to display results
    int[] interval = new int[1];
    // something with Euler's number?	
    double r = a * dt / (dd * dd);


    // if master: read and broadcast args to slaves
    if (myRank == 0) {
      sizeArray[0] = Integer.parseInt(args[0]);
      stripeWidths = calculateStripeWidths(sizeArray[0], numNodes);
      startingIndices = calculateStartingIndices(stripeWidths, numNodes);
      maxTime[0] = Integer.parseInt(args[1]);
      heatTime[0] = Integer.parseInt(args[2]);
      interval[0] = Integer.parseInt(args[3]);
    }

    MPI.COMM_WORLD.Bcast(sizeArray,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(stripeWidths,0,numNodes,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(startingIndices,0,numNodes,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(maxTime,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(heatTime,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(interval,0,1,MPI.INT,0);

    size = sizeArray[0];
    int stripeWidth = stripeWidths[myRank];
    int startingIndex = startingIndices[myRank];

    // computation space that will be distributed
    double[] matrix = new double[2 * size * size];
    for (int i = 0; i < 2 * size * size; i ++)
      matrix[i] = 0;

    // start a timer
    Date startTime = new Date();

    // simulate heat diffusion
    for (int t = 0; t < maxTime[0]; t++) {
      int p = t % 2; // p = 0 or 1: indicates the phase

      synchronize(size, t, heatTime[0], p, matrix);

      int leftmost = flatten(p, startingIndex, 0, size);
      int leftmostMinusOne = flatten(p, startingIndex - 1, 0, size);

      int rightmost = flatten(p, startingIndex + stripeWidth - 1, 0, size);
      int rightmostPlusOne = flatten(p, startingIndex + stripeWidth, 0, size);

      // receive data only from the column directly to the left
      // column length will be the size
      
      if (myRank == 0 && numNodes > 1) {
	MPI.COMM_WORLD.Send(matrix, rightmost, size, MPI.DOUBLE, myRank + 1, 0); 
	MPI.COMM_WORLD.Recv(matrix, rightmostPlusOne, size, MPI.DOUBLE, myRank + 1, 0); 

      }
      else if (myRank == numNodes - 1) {
	MPI.COMM_WORLD.Recv(matrix, leftmostMinusOne, size, MPI.DOUBLE, myRank - 1, 0); 
	MPI.COMM_WORLD.Send(matrix, leftmost, size, MPI.DOUBLE, myRank - 1, 0); 
      }
      else {
	MPI.COMM_WORLD.Recv(matrix, leftmostMinusOne, size, MPI.DOUBLE, myRank - 1, 0); 
	MPI.COMM_WORLD.Send(matrix, leftmost, size, MPI.DOUBLE, myRank - 1, 0); 
	MPI.COMM_WORLD.Send(matrix, rightmost, size, MPI.DOUBLE, myRank + 1, 0); 
	MPI.COMM_WORLD.Recv(matrix, rightmostPlusOne, size, MPI.DOUBLE, myRank + 1, 0); 
      }

      if (interval[0] != 0 && (t % interval[0] == 0 || t == maxTime[0] - 1)) {
	// if master, collect data from slaves
	if (myRank == 0) {
	  for (int i = 1; i < numNodes; i++) {
	    int receivingIndex = flatten(p, startingIndices[i], 0, size);	  
	    int receivingStripeLength = stripeWidths[i] * size;
	    MPI.COMM_WORLD.Recv(matrix, receivingIndex, receivingStripeLength, MPI.DOUBLE, i, 0);
	  }

	  // display intermediate results
	  log(matrix, size, t, p);
	  logTime(startTime);

	} else {
	  int sendingIndex = flatten(p, startingIndex, 0, size);
	  MPI.COMM_WORLD.Send(matrix, sendingIndex, stripeWidth * size, MPI.DOUBLE, 0, 0); 
	}
      }
      // perform forward Euler method
      int p2 = (p + 1) % 2;
      // ignore the outermost border if leftmost or rightmost slice
      int start = startingIndex; 
      if (myRank == 0) {start++;}
      int end = startingIndex + stripeWidth;
      if (myRank == numNodes - 1) {end--;}

      for (int x = start; x < end; x++) {
	for (int y = 1; y < size - 1; y++) {
	  matrix[flatten(p2, x, y, size)] = matrix[flatten(p, x, y, size)] +
	    r * (matrix[flatten(p, x+1, y, size)] - 2 * matrix[flatten(p, x, y, size)] +
	       	matrix[flatten(p,x-1,y,size)]) +
	    r * (matrix[flatten(p, x, y+1, size)] - 2 * matrix[flatten(p, x, y, size)] +
	       	matrix[flatten(p, x, y-1,size)]);
	}
      }
    }

    // finish the timer
    if (myRank == 0)
      logTime(startTime);
    MPI.Finalize();
  }

  private static void synchronize(int size, int time, int heatTime, int p, double[] matrix) {
    // two left-most and two right-most columns are identical
    for (int y = 0; y < size; y++) {
      matrix[flatten(p, 0, y, size)] = matrix[flatten(p, 1, y, size)]; 
      matrix[flatten(p, size-1, y, size)] = matrix[flatten(p, size-2, y, size)]; 
    }
    // two upper-most and lower-most rows are identical
    for (int x = 0; x < size; x++) {
      matrix[flatten(p, x, 0, size)] = matrix[flatten(p, x, 1, size)];
      matrix[flatten(p, x, size-1, size)] = matrix[flatten(p, x, size-2, size)];
    }
    // heat middle bottom third
    if (time < heatTime) {
      for (int x = size / 3; x < (size / 3) * 2; x++)
	matrix[flatten(p, x, 0, size)] = 19.0; 
    }
  }

  private static void logTime(Date startTime) {
    Date endTime = new Date();
    System.out.println("Elapsed time = " +
	(endTime.getTime() - startTime.getTime()));
  }

  private static void log(double[] matrix, int size, int t, int p) {
    System.out.println("time = " + t);
      for (int y = 0; y < size; y++) {
	for (int x = 0; x < size; x++) {
	  System.out.print((int) (Math.floor(matrix[flatten(p, x, y, size)] / 2)) + " ");
	}
	System.out.println();
      }
      System.out.println();
  }

}

