
import robotrace.Vector;

/**
 *
 * @author s102877
 */
public class Matrix {
    final static private double[][] identity = {{1,0,0,},{0,1,0,},{0,0,1,}};
    final static Matrix I = new Matrix(identity);
    double[][] numbers;
    
    public Matrix(double[][] numbers) {
        this.numbers = numbers;
    }
    
    public Vector times(Vector v) {
        Vector h1 = new Vector(numbers[0][0], numbers[0][1], numbers[0][2]);
        double x = v.dot(h1);
        
        Vector h2 = new Vector(numbers[1][0], numbers[1][1], numbers[1][2]);
        double y = v.dot(h2);
        
        Vector h3 = new Vector(numbers[2][0], numbers[2][1], numbers[2][2]);
        double z = v.dot(h3);
        
        return new Vector(x, y, z);        
    }
    
    public Matrix transposed() {
        double[][] result = new double[numbers.length][numbers.length];
        
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers.length; j++) {
                result[j][i] = numbers[i][j];
            }
        }
        
        return new Matrix(result);
        
    }
}
