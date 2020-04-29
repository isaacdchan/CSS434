import java.util.Date;
import mpi.*;   // for mpiJava

public class Heat2D {
  private static double a = 1.0;  // heat speed
  private static double dt = 1.0; // time quantum
  private static double dd = 2.0; // change in system

  public int flatten(int p, int x, int y, int size) {
    return (p * size * size) + (x * size) + y;
  }
  public int calculateSlice(int size, int rank) {

  }

  public static void main(String[] args) {
    // verify arguments
    if (args.length != 9) {
      System.out.
	println("usage: " +
	    "java Heat2D size max_time heat_time interval");
      System.exit(-1);
    }

    MPI.Init(args);
    int numNodes = MPI.COMM_WORLD.Size();
    int myRank = MPI.COMM_WORLD.Rank();

    // number of coordinates to calculate
    int[] sizeArray = new int[1];
    int sizeArray;
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
      size[0] = Integer.parseInt(args[1]);
      maxTime[0] = Integer.parseInt(args[2]);
      heatTime[0] = Integer.parseInt(args[3]);
      interval[0] = Integer.parseInt(args[4]);
    }

    MPI.COMM_WORLD.Bcast(sizeArray,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(maxTime,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(heatTime,0,1,MPI.INT,0);
    MPI.COMM_WORLD.Bcast(interval,0,1,MPI.INT,0);

    size = sizeArray[0];
    // computation space that will be distributed
    double[] matrix = new double[2 * size[0] * size[0]];
    for (int i = 0; i < p * size * size; i ++)
      matrix[i] = 0;

    // start a timer
    Date startTime = new Date();
    int stripeSize = size/numNodes;

    // simulate heat diffusion
    for (int t = 0; t < max_time; t++) {
      int p = t % 2; // p = 0 or 1: indicates the phase

      synchronize(size, t, heatTime, p, matrix);

      int leftmost = flatten(p, rank * portionSize, 0);
      int leftmostMinusOne = leftmost - size;

      int rightmost = flatten(p, ((rank+1) * stripeSize) - 1, 0);
      int rightmostPlusOne = rightmost + size;

      if (leftmostMinusOne > 0) {
	MPI.COMM_WORLD.Recv(matrix, leftmostMinusOne, size, MPI.DOUBLE, rank - 1, 0); 
	MPI.COMM_WORLD.Send(matrix, leftmost, size, MPI.DOUBLE, rank - 1, 0); 
      }
      if (rightmostPlusOne < (p * size * size)) {
	MPI.COMM_WORLD.Send(matrix, rightmost, size, MPI.DOUBLE, rank - 1, 0); 
	MPI.COMM_WORLD.Recv(matrix, rightmostPlusOne, size, MPI.DOUBLE, rank - 1, 0); 
      }

      if (rank == 0) {
	for (int i = 1; i < numNodes; i++) {
	  int receivingIndex = flatten(p, i, 0);	  
	  MPI.COMM_WORLD.Recv(matrix, receivingIndex, size * stripeSize, MPI.DOUBLE, i, 0);
	}

	// display intermediate results
	if (interval != 0 && (t % interval == 0 || t == max_time - 1)) {
	  System.out.println("time = " + t);
	  for (int y = 0; y < size; y++) {
	    for (int x = 0; x < size; x++)
	      System.out.println((int) (Math.floor(z[p][x][y] / 2)) + " ");
	  }
	  System.out.println();
	}

      } else {
	int sendingIndex = flatten(p, rank, 0);
	MPI.COMM_WORLD.SEND(matrix, sendingIndex, size * stripeSize, MPI.DOUBLE, 0, 0); 
      }

      // perform forward Euler method
      int p2 = (p + 1) % 2;
      // ignore the outermost border
      for (int x = 1; x < size - 1; x++) {
	for (int y = 1; y < size - 1; y++) {
	  // new temp at the location equals 
	  // ((right_temp - 2) * current_temp) + left_temp
	  // ((upper_temp - 2) * current_temp) + lower_temp
	  z[p2][x][y] = z[p][x][y] +
	    r * (z[p][x + 1][y] - 2 * z[p][x][y] + z[p][x - 1][y]) +
	    r * (z[p][x][y + 1] - 2 * z[p][x][y] + z[p][x][y - 1]);
	}
      }	
    } // end of simulation

    // finish the timer
    Date endTime = new Date();
    System.out.println("Elapsed time = " +
	(endTime.getTime() - startTime.getTime()));
  }

  private static void synchronize(int size, int time, int heatTime, int p, double[] matrix) {
    // two left-most and two right-most columns are identical
    for (int y = 0; y < size; y++) {
      matrix[flatten(p, 0, y)] = matrix[flatten(p, 1, y)]; 
      matrix[flatten(p, size-1, y)] = matrix[flatten(p, size-2, y)]; 
      matrix[flatten(p, x, size-1)] = matrix[flatten(p, x, size-2)];
    }
    // two upper-most and lower-most rows are identical
    for (int y = 0; y < size; y++) {
      matrix[flatten(p, x, 0)] = matrix[flatten(p, x, 1)];
      matrix[flatten(p, x, size-1)] = matrix[flatten(p, x, size-2)];
    }
    // heat middle bottom third
    if (t < heat_time) {
      for (int y = size / 3; x < (size / 3) * 2; y++)
	matrix[flatten(p, x, 0)] = 19.0; 
    }
  }


}

