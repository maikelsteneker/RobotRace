
import robotrace.Vector;

/**
 *
 * @author s102877
 */
public class Matrix {

    final static private double[][] identity = {{1, 0, 0,}, {0, 1, 0,}, {0, 0, 1,}};
    final static Matrix I = new Matrix(identity);
    double[][] numbers;

    public Matrix(double[][] numbers) {
        this.numbers = numbers;
    }

    public Vector times(Vector v) {
        Vector h1 = new Vector(numbers[0][0], numbers[0][1], numbers[0][2]);
        double x = v.dot(h1) + numbers[0][3];

        Vector h2 = new Vector(numbers[1][0], numbers[1][1], numbers[1][2]);
        double y = v.dot(h2) + numbers[1][3];

        Vector h3 = new Vector(numbers[2][0], numbers[2][1], numbers[2][2]);
        double z = v.dot(h3) + numbers[2][3];

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
    
    public Matrix inverseCheating() {
        double[][] result = new double[numbers.length][numbers.length];
        
        //transposed for first three columns, rows
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = numbers[j][i];
            }
        }
        
        //negative values for right-most column
        for (int i = 0; i < 3; i++) {
            result[i][3] = -numbers[i][3];
        }
        
        //1 in right-bottom corner
        result[3][3] = 1;
        
        return new Matrix(result);
    }

    public Matrix inverse() {
        double[][] result = new double[numbers.length][numbers.length];
        if (isTranslationMatrix()) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    result[i][j] = (i == j) ? 1 : 0;
                }
            }
            for (int i = 0; i < 4; i++) {
                result[i][3] = -numbers[i][3];
                result[3][i] = (i == 3) ? 1 : 0;
            }
        } else {
            throw new UnsupportedOperationException("Only works for translation matrix");
        }
        return new Matrix(result);
    }

    private boolean isTranslationMatrix() {
        boolean result = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i==j) {
                    result = result && numbers[i][j] == 1;
                } else {
                    if (j!=3) {
                        result = result && numbers[i][j] == 0;
                    }
                }
            }
        }
        return result;
    }
    
    public boolean equalsTo(Matrix m) {
        boolean result = true;
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[0].length; j++) {
                result = result && (numbers[i][j] == m.numbers[i][j]);
            }
        }
        return result;
    }
    
    public void print() {
        final double EPS = Math.pow(10, -13);
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[0].length; j++) {
                double n = numbers[i][j];
                System.out.print((Math.abs(n) < EPS ? 0 : n) + ",");
            }
            System.out.println();
        }
        System.out.println();
    }
}
