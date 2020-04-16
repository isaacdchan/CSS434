import java.io.Serializable;

public class DoubleArray implements Serializable {

    public double[] data;

    public DoubleArray(int size) {
        data = new double[size];
        for (int i = 0; i < size;i++)
            data[i] = (double) i;
    }

    public void print() {
        for (double i : data)
            System.out.println(i);
    }
}