import mpi.*;

class Arg {
    static public void main(String[] args) throws MPIException {

        MPI.Init(args);

        int myrank = MPI.COMM_WORLD.Rank();
        int p = MPI.COMM_WORLD.Size();

        for (int i = 0; i < args.length; i++)
            System.out.println("myrank[" + myrank + "]'s args[" + i + "] = " + args[i]);

        MPI.Finalize();
    }
}
