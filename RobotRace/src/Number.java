
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;

/**
 *
 * @author maikel
 */
public class Number {
    
    public final static boolean[] ZERO = {true, true, true, false, true, true, true};
    public final static boolean[] ONE = {false, false, true, false, false, true, false};
    public final static boolean[] TWO = {true, false, true, true, true, false, true};
    public final static boolean[] THREE = {true, false, true, true, false, true, true};
    public final static boolean[] FOUR = {false, true, true, true, false, true, false};
    public final static boolean[] FIVE = {true, true, false, true, false, true, true};
    public final static boolean[] SIX = {true, true, false, true, true, true, true};
    public final static boolean[] SEVEN = {true, false, true, false, false, true, false};
    public final static boolean[] EIGHT = {true, true, true, true, true, true, true};
    public final static boolean[] NINE = {true, true, true, true, false, true, true};
    public final static float LINE_LENGTH = 1f;
    public final static float LINE_WIDTH = 0.3f;
    
    public static boolean[] booleanArray(int number) {
        boolean[] result;
        
        switch (number) {
            case 0:
                result = ZERO;
                break;
            case 1:
                result = ONE;
                break;
            case 2:
                result = TWO;
                break;
            case 3:
                result = THREE;
                break;
            case 4:
                result = FOUR;
                break;
            case 5:
                result = FIVE;
                break;
            case 6:
                result = SIX;
                break;
            case 7:
                result = SEVEN;
                break;
            case 8:
                result = EIGHT;
                break;
            case 9:
                result = NINE;
                break;
            default:
                // IMPOSSIBRU!
                return null;
        }
        
        return result;
    }
    
    public static void draw(int number, GL2 gl) {
        draw(booleanArray(number), gl);
    }
    
    public static void draw(boolean[] number, GL2 gl) {
        for (int i = 0; i < number.length; i++) {
            if (number[i]) {
                gl.glPushMatrix();
                drawLine(i, gl);
                gl.glPopMatrix();
            }
        }
    }
    
    private static void drawLine(int line, GL2 gl) {
        final float w = LINE_WIDTH, l = LINE_LENGTH;
        switch (line) {
            case 0:
                gl.glTranslatef(w, w, 0);
                gl.glRotatef(-90, 0, 0, 1);
                drawLine(gl);
                break;
            case 1:
                gl.glTranslatef(0, w, 0);
                drawLine(gl);
                break;
            case 2:
                gl.glTranslatef(l + w, w, 0);
                drawLine(gl);
                break;
            case 3:
                gl.glTranslatef(w, 2 * w + l, 0);
                gl.glRotatef(-90, 0, 0, 1);
                drawLine(gl);
                break;
            case 4:
                gl.glTranslatef(0, 2 * w + l, 0);
                drawLine(gl);
                break;
            case 5:
                gl.glTranslatef(l + w, 2 * w + l, 0);
                drawLine(gl);
                break;
            case 6:
                gl.glTranslatef(w, 3 * w + 2 * l, 0);
                gl.glRotatef(-90, 0, 0, 1);
                drawLine(gl);
                break;
            default:
            // IMPOSSIBRU!
        }
    }
    
    private static void drawLine(GL2 gl) {
        final float w = LINE_WIDTH, l = LINE_LENGTH;
        gl.glBegin(GL_QUADS);
        gl.glNormal3f(0, 0, 1);
        gl.glVertex3f(0, l, 0);
        gl.glVertex3f(w, l, 0);
        gl.glVertex3f(w, 0, 0);
        gl.glVertex3f(0, 0, 0);
        gl.glEnd();
        
        gl.glBegin(GL_TRIANGLES);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(w, 0, 0);
        gl.glVertex3f(w / 2, -w / 2, 0);
        
        gl.glVertex3f(w, l, 0);
        gl.glVertex3f(0, l, 0);
        gl.glVertex3f(w / 2, l + w / 2, 0);
        gl.glEnd();
    }
    
    public static float width() {
        return 2 * LINE_WIDTH + LINE_LENGTH;
    }
    
    public static float height() {
        return 3 * LINE_WIDTH + 2 * LINE_LENGTH;
    }
}
