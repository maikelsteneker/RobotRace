
import javax.media.opengl.GL2;
import static java.lang.Math.*;

/**
 *
 * @author maikel
 */
public class Clock {

    final static public int M = 2; // number of digits before comma
    final static public int N = 4; // number of digits in total

    public static void draw(GL2 gl, int... numbers) {
        gl.glPushMatrix();
        gl.glScalef(1, -1, 1);
        for (int number : numbers) {
            Number.draw(number, gl);
            gl.glTranslatef(Number.width() + Number.LINE_WIDTH, 0, 0);
        }
        gl.glPopMatrix();
    }

    public static void draw(GL2 gl, float time) {
        int round = (int) round((time % pow(10, M)) * pow(10, N - M));
        String f = Integer.toString(round);
        int[] numbers = new int[N];
        int i = 0;
        while (f.length() < N) {
            f = "0".concat(f);
        }

        for (char c : f.toCharArray()) {
            if (Character.isDigit(c)) {
                int n = Integer.parseInt(c + "");
                numbers[i++] = n;
            }
        }
        draw(gl, numbers);
    }

    public static void main(String[] args) {
        draw(null, 5.1234234f);
    }
}
