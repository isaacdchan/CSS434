import mpi.*;   // for mpiJava

public class MyProgram {
   private final static int arraySize = 100; // the size of dArray
   private final static int master = 0;  // the master rank
   private final static int tag = 0;     // Send/Recv's tag is always 0.

   public static void main(String[] args) throws MPIException {
      // Start the MPI library.
      MPI.Init(args);
      int numNodes = MPI.COMM_WORLD.Size();
      int myRank = MPI.COMM_WORLD.Rank();

      // compute my own stripe
      int sliceLength = arraySize / numNodes; // each portion of array
      double[] arrayToCompute = null;

      if (myRank == 0) { // master

	 // initialize dArray[100].
	 arrayToCompute = new double[arraySize];
	 for (int i = 0; i < arraySize; i++)
	    arrayToCompute[i] = i;

	 // send a portion of dArray[100] to each slave
	 for (int rank = 1; rank < numNodes; rank++)
	    // arguments are data, offset, size, datatype, rank_to_send_to, tag (rarely used)
	    MPI.COMM_WORLD.Send(arrayToCompute, sliceLength * rank, sliceLength, MPI.DOUBLE, rank, tag);

      } else { // slaves: rank 1 to rank n - 1
	 // allocates dArray[25].
	 arrayToCompute = new double[sliceLength];

	 // receive a portion of dArray[100] from the master
	 MPI.COMM_WORLD.Recv(arrayToCompute, 0, sliceLength, MPI.DOUBLE, master, tag);
      }

      // both master and slave compute the square root of each array element
      for (int i = 0; i < sliceLength; i++)
	 arrayToCompute[i] = Math.sqrt(arrayToCompute[i]);

      if (myRank == 0) { // master

	 // receive answers from each slave
	 for (int rank = 1; rank < numNodes; rank++)
	    MPI.COMM_WORLD.Recv(arrayToCompute, rank * sliceLength, sliceLength, MPI.DOUBLE, rank, tag);
	 // print out the results
	 for (int i = 0; i < arraySize; i++)
	    System.out.println("Results[ " + i + " ] = " + arrayToCompute[i]);
      } else { // slaves: rank 0 to rank n - 1
	 // send the results back to the master
	 MPI.COMM_WORLD.Send(arrayToCompute, 0, sliceLength, MPI.DOUBLE, 0, tag);
      }

      // Terminate the MPI library.
      MPI.Finalize();
   }
}
