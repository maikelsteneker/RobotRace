
import java.awt.Color;
import javax.media.opengl.GL;
import static javax.media.opengl.GL2.*;
import robotrace.Base;
import robotrace.Vector;
import static java.lang.Math.*;
import javax.media.opengl.GL2;

/**
 * Handles all of the RobotRace graphics functionality, which should be extended
 * per the assignment.
 *
 * OpenGL functionality: - Basic commands are called via the gl object; -
 * Utility commands are called via the glu and glut objects;
 *
 * GlobalState: The gs object contains the GlobalState as described in the
 * assignment: - The camera viewpoint angles, phi and theta, are changed
 * interactively by holding the left mouse button and dragging; - The camera
 * view width, vWidth, is changed interactively by holding the right mouse
 * button and dragging upwards or downwards; - The center point can be moved up
 * and down by pressing the 'q' and 'z' keys, forwards and backwards with the
 * 'w' and 's' keys, and left and right with the 'a' and 'd' keys; - Other
 * settings are changed via the menus at the top of the screen.
 *
 * Textures: Place your "track.jpg", "brick.jpg", "head.jpg", and "torso.jpg"
 * files in the same folder as this file. These will then be loaded as the
 * texture objects track, bricks, head, and torso respectively. Be aware, these
 * objects are already defined and cannot be used for other purposes. The
 * texture objects can be used as follows:
 *
 * gl.glColor3f(1f, 1f, 1f); track.bind(gl); gl.glBegin(GL_QUADS);
 * gl.glTexCoord2d(0, 0); gl.glVertex3d(0, 0, 0); gl.glTexCoord2d(1, 0);
 * gl.glVertex3d(1, 0, 0); gl.glTexCoord2d(1, 1); gl.glVertex3d(1, 1, 0);
 * gl.glTexCoord2d(0, 1); gl.glVertex3d(0, 1, 0); gl.glEnd();
 *
 * Note that it is hard or impossible to texture objects drawn with GLUT. Either
 * define the primitives of the object yourself (as seen above) or add
 * additional textured primitives to the GLUT object.
 */
public class RobotRace9 extends Base {

    double fovy = -1;
    Robot[] robots;
    final private static int NUMROBOTS = 100;

    /**
     * Called upon the start of the application. Primarily used to configure
     * OpenGL.
     */
    @Override
    public void initialize() {
        // Enable blending.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Enable anti-aliasing.
        gl.glEnable(GL_LINE_SMOOTH);
        //gl.glEnable(GL_POLYGON_SMOOTH);
        gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        //gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        // Enable depth testing.
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);

        // Enable textures. 
        gl.glEnable(GL_TEXTURE_2D);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        // Enable lighting (2.1)
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_COLOR_MATERIAL);
        
        //initialize robots
        robots = new Robot[NUMROBOTS];
        for (int i = 0; i < NUMROBOTS; i++) {
            robots[i] = new Robot();
        }
    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        if (gs.persp) {
            if (fovy == -1) {
                fovy = atan2(gs.w, 0.1) / (gs.w / gs.h);
            }
            glu.gluPerspective(fovy, gs.w / gs.h, 0.1, 1000);
        } else {
            float height = gs.vWidth / (gs.w / gs.h);
            gl.glOrtho(-0.5 * gs.vWidth, 0.5 * gs.vWidth, -0.5 * height, 0.5 * height, 0.1, 1000);
        }


        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();


        Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                sin(gs.phi) * cos(gs.theta),
                sin(gs.theta));
        Vector eye = gs.cnt.subtract(dir.scale(gs.vDist));

        glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
                gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                0.0, 0.0, 1.0);   // up axis

    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        //save current position
        gl.glPushMatrix();

        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);
        /*
         * // Unit box around origin. glut.glutWireCube(1f);
         *
         * // Move in x-direction. gl.glTranslatef(2f, 0f, 0f);
         *
         * // Rotate 30 degrees, around z-axis. gl.glRotatef(30f, 0f, 0f, 1f);
         *
         * // Scale in z-direction. gl.glScalef(1f, 1f, 2f);
         *
         * // Translated, rotated, scaled box. glut.glutWireCube(1f);
         */
        //revert back to original position
        gl.glPopMatrix();

        //draw grid
        drawGrid();

        // Axis Frame
        drawAxisFrame();

        //draw robots
        gl.glPushMatrix();
        for (Robot r : robots) {
            gl.glTranslatef(1.0f, 0, 0);
            r.draw();
        }
        gl.glPopMatrix();
    }

    public void drawArrow() {
        gl.glPushMatrix();

        gl.glTranslatef(0f, 0, 0.5f);
        gl.glScalef(0.01f, 0.01f, 1f);
        glut.glutSolidCube(0.9f);

        gl.glPopMatrix();
        gl.glPushMatrix();

        gl.glTranslatef(0f, 0f, 1f);
        glut.glutSolidCone(0.05, 0.1, 15, 2);

        gl.glPopMatrix();
    }

    public void drawAxisFrame() {
        if (gs.showAxes) {
            gl.glColor3f(1.0f, 1.0f, 0);
            glut.glutSolidSphere(0.10f, 20, 20);

            gl.glPushMatrix();
            gl.glRotatef(90, 0, 1, 0);
            gl.glColor3f(1.0f, 0, 0);
            drawArrow();
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0);
            gl.glColor3f(0, 1.0f, 0);
            drawArrow();
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glColor3f(0, 0, 1.0f);
            drawArrow();
            gl.glPopMatrix();
        }
    }

    public void drawGrid() {
        if (gs.showAxes) {
            for (float i = -1000; i < 1000; i += 0.25) {
                gl.glBegin(GL_LINES);
                gl.glVertex3f(-1000, i, 0);
                gl.glVertex3f(1000, i, 0);
                gl.glVertex3f(i, -1000, 0);
                gl.glVertex3f(i, 1000, 0);
                gl.glEnd();
            }
        }
    }

    /*
     * public void drawAxisFrame() { //TODO: adjust lengths if (gs.showAxes) {
     * //yellow sphere on origin gl.glColor3f(1.0f, 1.0f, 0);
     * glut.glutSolidSphere(0.10f, 20, 20);
     *
     * //red X arrow gl.glPushMatrix(); gl.glColor3f(1.0f, 0, 0); for (int i =
     * 0; i < 10; i++) { gl.glTranslatef(0.025f, 0, 0);
     * glut.glutSolidCube(0.05f); } gl.glRotatef(90, 0, 1, 0);
     * glut.glutSolidCone(0.05f, 0.10f, 10, 10); gl.glPopMatrix();
     *
     * //green Y arrow gl.glPushMatrix(); gl.glColor3f(0, 1.0f, 0); for (int i
     * = 0; i < 10; i++) { gl.glTranslatef(0, 0.025f, 0);
     * glut.glutSolidCube(0.05f); } gl.glRotatef(-90, 1, 0, 0);
     * glut.glutSolidCone(0.05f, 0.10f, 10, 10); gl.glPopMatrix();
     *
     * //blue Z arrow gl.glPushMatrix(); gl.glColor3f(0, 0, 1.0f); for (int i =
     * 0; i < 10; i++) { gl.glTranslatef(0, 0, 0.025f);
     * glut.glutSolidCube(0.05f); } glut.glutSolidCone(0.05f, 0.10f, 10, 10);
     * gl.glPopMatrix(); } }
     */
    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    class Robot {

        float legAngle = 0;
        boolean legDirection = false;
        float yPos = 0;
        float speed = 8;
        final static private float MAXANGLE = 30;

        /**
         * Draws the robot
         */
        public void draw() {
            gl.glPushMatrix();
            
            //move in y position
            gl.glTranslatef(0, yPos, 0);

            //draw hat
            gl.glColor3f(1.0f, 0.4f, 0.7f);
            drawHat();

            //draw head
            gl.glColor3f(0.75f, 0.75f, 0.75f);
            drawHead();

            //draw torso
            drawTorso();

            //draw left arm
            drawArm(true);

            //draw right arm
            drawArm(false);

            //draw left leg
            drawLeg(true);

            //draw right leg
            drawLeg(false);

            handleMovement();
            
            gl.glPopMatrix();
        }

        private void drawLeg(boolean left) {
            int s = left ? 1 : -1;
            if (gs.showStick) {
            } else {
                gl.glPushMatrix();
                gl.glTranslated(0, 0, cos(toRadians(legAngle)) * 0.5f);
                gl.glRotatef(s * legAngle, 1, 0, 0);
                gl.glTranslatef(s * 0.20f, 0, -0.25f);
                gl.glScalef(1.0f, 1.0f, 5.0f);
                glut.glutSolidCube(0.1f);
                gl.glPopMatrix();
            }
        }

        private void drawArm(boolean left) {
            int s = left ? 1 : -1;
            gl.glPushMatrix();
            gl.glTranslated(0, 0, cos(toRadians(legAngle)) * 0.5f + 0.5f);
            gl.glRotatef(s * -legAngle, 1, 0, 0);
            gl.glTranslatef(s * 0.30f, 0, -0.25f);
            gl.glScalef(1.0f, 1.0f, 5.0f);
            glut.glutSolidCube(0.1f);
            gl.glPopMatrix();
        }

        private void drawTorso() {
            gl.glPushMatrix();
            gl.glTranslated(0, 0, 0.25 + cos(toRadians(legAngle)) * 0.5f);
            glut.glutSolidCube(0.5f);
            gl.glPopMatrix();
        }

        private void drawHead() {
            gl.glPushMatrix();
            gl.glTranslated(0, 0, 0.75 + cos(toRadians(legAngle)) * 0.5f);
            glut.glutSolidSphere(0.25f, 10, 10);
            gl.glPopMatrix();
        }

        private void drawHat() {
            gl.glPushMatrix();
            gl.glTranslated(0, 0, 0.9 + cos(toRadians(legAngle)) * 0.5f);
            glut.glutSolidCone(0.25f, 0.5f, 10, 10);
            gl.glPopMatrix();
        }

        private void handleMovement() {
            if (legAngle > MAXANGLE) {
                legDirection = false;
            } else if (legAngle < -MAXANGLE) {
                legDirection = true;
            }

            if (legDirection) {
                legAngle+= speed;
            } else {
                legAngle-= speed;
            }

            yPos += speed * 0.01;
        }
    }

    /**
     * Main program execution body, delegates to an instance of the RobotRace
     * implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
    }
}