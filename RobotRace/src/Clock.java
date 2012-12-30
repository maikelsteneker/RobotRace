
import javax.media.opengl.GL2;

/**
 *
 * @author maikel
 */
public class Clock {
    public static void draw(GL2 gl, int... numbers) {
        gl.glPushMatrix();
        gl.glScalef(1,-1,1);
        for(int number : numbers) {
            Number.draw(number, gl);
            gl.glTranslatef(Number.width() + Number.LINE_WIDTH, 0, 0);
        }
        gl.glPopMatrix();
    }
}
