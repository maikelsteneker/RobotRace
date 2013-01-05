
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
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
        for (int i = 0; i < numbers.length; i++) {
            Number.draw(numbers[i], gl);
            gl.glTranslatef(Number.width() + Number.LINE_WIDTH, 0, 0);
            if (i == M - 1) {
                drawColon(gl);
            }
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

    private static void drawColon(GL2 gl) {
        float w = Number.LINE_WIDTH;
        float l = Number.LINE_LENGTH;
        float d = (l - w) / 2;
        drawTopDot(gl);
        gl.glPushMatrix();
        gl.glTranslatef(0, w + l, 0);
        drawTopDot(gl);
        gl.glPopMatrix();
        gl.glTranslatef(3 * w, 0, 0);
    }

    private static void drawTopDot(GL2 gl) {
        float w = Number.LINE_WIDTH;
        float l = Number.LINE_LENGTH;
        float d = (l - w) / 2;

        gl.glBegin(GL_QUADS);
        gl.glVertex3f(w, 2 * w + d, 0);
        gl.glVertex3f(2 * w, 2 * w + d, 0);
        gl.glVertex3f(2 * w, w + d, 0);
        gl.glVertex3f(w, w + d, 0);
        gl.glEnd();
    }
}
