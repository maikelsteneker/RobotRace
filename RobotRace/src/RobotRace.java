
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.GLException;
import robotrace.*;

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
public class RobotRace extends Base {

    double fovy = -1; // vertical field of view angle
    Robot[] robots; // array to store drawable robots
    final private static int NUMROBOTS = 4; // size of robots array
    final private static int NUMBUMPS = 500; // number of bumps in terrain
    Vector eye; // current location of the camera
    Vector light = new Vector(0, 10, 10); // current location of the light source
    Matrix m_0 = null; // matrix to transfer from world to camera coordinates
    Track t; // the track the robots are moving on
    float phi_old, theta_old; // holds old values for phi and theta
    Texture landscape; // 1D texture for landscape
    Terrain terrain; // terrain that's being shown

    /**
     * Class containing static variables representing different materials.
     */
    public final static class Material {

        // Array contanining the parameters for a gold material. Contains similar
        // values for the diffues and specular reflection to give a metal look.
        public final static float[] GOLD = {
            0.24725f, 0.1995f, 0.0745f, 1.0f, //ambient
            0.75164f, 0.60648f, 0.22648f, 1.0f, //diffuse
            0.628281f, 0.555802f, 0.366065f, 1.0f, //specular
            51.2f //shininess
        };
        // Array containing the parameters for yet an other gold material.
        public final static float[] VERA_GOLD = {
            0.24725f, 0.1995f, 0.0745f, 1.0f, //ambient
            0.8f, 0.6f, 0.1f, 1.0f, //diffuse
            0.8f, 0.6f, 0.1f, 1.0f, //specular
            51.2f //shininess
        };
        // Array containing the parameteres for a silver material. Just like for
        // gold, the values for the diffuse and specular reflection are similar in order
        // to get a metal look.
        public final static float[] SILVER = {
            0.19225f, 0.19225f, 0.19225f, 1.0f, //ambient
            0.50754f, 0.50754f, 0.50754f, 1.0f, //diffuse
            0.508273f, 0.508273f, 0.508273f, 1.0f, //specular 
            51.2f //shininess
        };
        // Array containing parameters for a green plastic material. 
        public final static float[] GREEN_PLASTIC = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            0.1f, 0.35f, 0.1f, 1.0f, //diffuse
            0.45f, 0.55f, 0.45f, 1.0f, //specular
            32f //shininess
        };
        //Array containing parameteres for a yellow plastic materail.
        public final static float[] YELLOW_PLASTIC = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            0.5f, 0.5f, 0.0f, 1.0f, //diffuse
            0.60f, 0.60f, 0.50f, 1.0f, //specular
            32f //shininess
        };
        // Array containing parameteres for a red plastic material.
        public final static float[] RED_PLASTIC = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            1.0f, 0f, 0.0f, 1.0f, //diffuse
            0.60f, 0.60f, 0.50f, 1.0f, //specular
            32f //shininess
        };
        // Array containing parameters for a blue plastci material.
        public final static float[] BLUE_PLASTIC = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            0f, 0.5f, 1.0f, 1.0f, //diffuse
            0.60f, 0.60f, 0.50f, 1.0f, //specular
            32f //shininess
        };
        // Array containing parameters for an orange plastic material.
        public final static float[] ORANGE_PLASTIC = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            1f, 0.65f, 0.0f, 1.0f, //diffuse
            0.5f, 0.5f, 0.5f, 1.0f, //specular
            90f //shininess
        };
        // Array containing parameters  for a wood-like material.
        public final static float[] WOOD = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            0.36f, 0.2f, 0.01f, 1.0f, //diffuse
            0.36f, 0.2f, 0.01f, 1.0f, //specular
            0f //shininess
        };
        // Array containing parameters for a water surface-like material.
        public static float[] WATER_SURFACE = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            0.5f, 0.5f, 0.5f, 0.5f, //diffuse
            0.5f, 0.5f, 0.5f, 0.5f, //specular
            0f //shininess
        };
        // Array containing parameters for a white material (for textures).
        public static float[] WHITE = {
            0.0f, 0.0f, 0.0f, 1.0f, //ambient
            1f, 1f, 1f, 1.0f, //diffuse
            1f, 1f, 1f, 1.0f, //specular
            0f //shininess
        };
    }

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

        // Enable lighting.
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_NORMALIZE);

        // Initialize robots array.
        robots = new Robot[NUMROBOTS];
        for (int i = 0; i < NUMROBOTS; i++) {
            robots[i] = new Robot(i);
        }

        // Load a 1D texture for the landscape.
        try {
            landscape = TextureIO.newTexture(new File("src/landscape.jpg"), false);
        } catch (IOException ex) {
            Logger.getLogger(RobotRace.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GLException ex) {
            Logger.getLogger(RobotRace.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Randomly generate terrain.
        Bump[] bumps = new Bump[NUMBUMPS];
        Random generator = new Random(0);
        for (int i = 0; i < NUMBUMPS; i++) {
            double center_x = (generator.nextDouble() * 40) - 20;
            double center_y = (generator.nextDouble() * 40) - 20;
            double height = (generator.nextDouble() * 2) - 1;
            double radius = generator.nextDouble() * 3;
            bumps[i] = new Bump(center_x, center_y, height, radius);
        }
        terrain = new Terrain(bumps);
    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Calculate directional vector of the camera (view direction).
        Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                sin(gs.phi) * cos(gs.theta),
                sin(gs.theta));

        // Calculate position of the camera.
        eye = gs.cnt.add(dir.scale(gs.vDist));

        final float AR = gs.w / (float) gs.h; // aspect ratio
        float vHeight = gs.vWidth / AR; // height of scene to be shown

        // We only compute the field of view once to prevent strange effects.
        if (fovy == -1) { // If the field of view has not yet been computed:
            /*
             * Consider a triangle with base vHeight and height vDist. The
             * vertical field of view angle we are looking for is the top angle
             * fovy. By adding the altitude from this corner to the base of the
             * triangle, we obtain two right triangles, in which tan(a/2) =
             * (vHeight / 2) / 2. Then fovy = arctan((vHeight / 2) / gs.vDist)
             */
            fovy = 2 * atan2(vHeight / 2, gs.vDist);
        }

        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        if (gs.persp) {
            // Use perspective projection.
            glu.gluPerspective(toDegrees(fovy), AR, 0.1, 1000);
        } else {
            double left = -0.5 * gs.vWidth; // left clipping plane
            double right = 0.5 * gs.vWidth; // right clipping plane
            double bottom = -0.5 * vHeight; // bottom clipping plane
            double top = 0.5 * vHeight; // top clipping plane
            double near_val = 0.1; // near value
            double far_val = 1000; // far value

            // Use isometric projection.
            gl.glOrtho(left, right, bottom, top, near_val, far_val);
        }

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Choose the way that the camera is placed, according to the gs.camMode 
        // parameter.
        switch (gs.camMode) {
            case 0: //Set the view to overview when gs.camMode = 0.
                setOverviewCamMode();
                break;
            case 1: //Set the view to helicopter when gs.camMode = 1.
                setHelicopterCamMode();
                break;
            case 2: // Set the view to motorcycle when gs.camMode = 2.
                setMotorcycleCamMode();
                break;
            case 3: // Set the view to first person when gs.camMode = 3.
                setFirstPersonCamMode();
                break;
            case 4: // Switch every 3 seconds between the view modes when gs.camMode = 4.
                int val = (int) (gs.tAnim % 12);
                if (val < 3) {
                    setOverviewCamMode();
                } else if (val < 6) {
                    setHelicopterCamMode();
                } else if (val < 9) {
                    setMotorcycleCamMode();
                } else {
                    setFirstPersonCamMode();
                }
                break;
        }

        if (!gs.lightCamera) {
            // The light should remain static, so set old values to get an
            // identity matrix in the calculations.
            phi_old = gs.phi;
            theta_old = gs.theta;
        }

        // Matrices to switch from world to eye coordinates and back.
        /*
         * The eyeToWorld matrix is defined as a matrix that rotates over the
         * phi and theta angles in the correct way to change from world to eye
         * coordinates. The worldToEye matrix is defined as the inverse of
         * eyeToWorld, but using the old values for phi and theta. Since the
         * vectors in the matrices are perpendicular, we can use the transposed
         * for this.
         */
        Matrix worldToEye = eyeToWorldMatrix(phi_old, theta_old).transposed();
        Matrix eyeToWorld = eyeToWorldMatrix(gs.phi, gs.theta);

        // The new position of the light is the product of the worldToEye matrix
        // (switching the coordinates of light to the previous eye coordinates)
        // and the eyeToWorld matrix (switching the coordinates back to world
        // coordinates using the new angles) applied on the light vector.
        light = eyeToWorld.times(worldToEye.times(light));

        // Set the light source in the direction of the calculated light vector.
        float[] location = {(float) light.x(), (float) light.y(), (float) light.z(), 0};
        gl.glLightfv(GL_LIGHT0, GL_POSITION, location, 0); //set location of ls0

        // Update the old values of phi and theta.
        phi_old = gs.phi;
        theta_old = gs.theta;
    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);

        // Draw Axis Frame.
        drawAxisFrame();

        // Reset to default texture.
        track.disable(gl);

        // Make a track in the shape of a simple curve, with the wdith of the
        // number of robots plus 1. Let the height of the track be between -1
        // and 1. TODO: change comment

        Curve c;
        switch (gs.trackNr) {
            case 0:
                //letter O
                c = new BezierCurve2(new Vector(-10, 0, 1),
                        new Vector(-10, 10, 1),
                        new Vector(10, 10, 1),
                        new Vector(10, 0, 1),
                        new Vector(10, -10, 1),
                        new Vector(-10, -10, 1),
                        new Vector(-10, 0, 1));
                break;
            case 1:
                //letter D
                c = null;
                break;
            case 2:
                //letter L
                c = null;
                break;
            case 3:
                //custom track
                c = null;
                break;
            default:
                c = null;
        }
        t = new Track(c, NUMROBOTS + 1, -1, 1);

        //t = new Track(new SimpleCurve(), NUMROBOTS + 1, -1, 1);

        // Set the material of the track to plastic green.
        //setMaterial(Material.GREEN_PLASTIC); // TOOD comment
        setMaterial(Material.WHITE);
        t.draw(); // Draw track.

        // Draw robots to showcase materials.
        float[][] robot_materials = {Material.GOLD, Material.SILVER, Material.WOOD, Material.ORANGE_PLASTIC};
        gl.glPushMatrix();
        // Translate such that the robots will be in the middle of the field.
        gl.glTranslatef(-3, 0, 0);
        for (float[] material : robot_materials) { // Make a robot for each material.
            Robot robot = new Robot(); // Construct the robot.
            robot.speed = 0; // Make the robots stand still.
            setMaterial(material); // Set the material
            robot.draw(); // Draw the robot.
            //Translate two units to the right to make space for the new robot.
            gl.glTranslatef(2, 0, 0);
        }
        gl.glPopMatrix();

        // Draw robots participating in the race.
        for (int i = 0; i < robots.length; i++) {
            Robot robot = robots[i];
            gl.glPushMatrix();
            // Calculate the position of the robot. First get the point where the
            // robot is currently and add to it the normal of the curve normalized,
            // so that the robots will follow the shape of the track, and scaled
            // such that the robots will keep the same line on the track.   
            Vector pos = t.curve.getPoint(robot.position).add(
                    t.curve.getNormalVector(robot.position).normalized().scale(i + 1));
            // Translate the robot to the position.
            gl.glTranslated(pos.x(), pos.y(), pos.z());

            // Calculate the angle for which the robots need to be rotated such
            // that they will always seem to walk straight.

            // First get the track tangent at the robot position.
            Vector tangent = t.curve.getTangent(robot.position);
            // Calculate the dot product between the tangent and the Y axis.
            double dot = tangent.dot(Vector.Y);
            // Get the cos angle between the tangent and the Y axis.
            double cosangle = dot / (tangent.length() * Vector.Y.length());
            // If the robot is on the 2nd half of the track flip the sign of the
            // angle. We do this because the acos method always returns a value
            // between 0 and PI. TODO: change comment
            boolean h = (robot.position % 1 >= 0.5f); // reverse after half the track
            boolean ypos = (t.curve.getTangent(robot.position).x() >= 0); // reverse if tangent points in negative Y direction
            double angle = ypos ? -acos(cosangle) : acos(cosangle);
            gl.glRotated(toDegrees(angle), 0, 0, 1);
            setMaterial(Material.SILVER); // Set the material to silver.
            robot.draw(); // Draw the robot
            gl.glPopMatrix();
        }

        // Draw rotationally symmetric shape (for demonstration purposes).
        double[] x = {2, 1, 2, 0};
        double[] z = {3, 4, 5, 6};
        setMaterial(Material.YELLOW_PLASTIC);
        drawRotSymShape(x, z, true, 100, 0.05);

        // Draw clock (because we don't know where to put it).
        // TODO: move
        gl.glPushMatrix();
        setMaterial(Material.RED_PLASTIC);
        gl.glTranslated(0, -10, 0);
        Clock.draw(gl, gs.tAnim);
        gl.glPopMatrix();

        // Draw terrain.
        terrain.draw();
    }

    /**
     * Draws an arrow in the Z direction.
     *
     * @param length length of the arrow
     * @param line_radius width and depth of the line
     * @param arrowhead_radius radius of the arrowhead
     */
    private void drawArrow(float length, float line_radius,
            float arrowhead_radius) {
        gl.glPushMatrix();

        // Scale to correct length.
        gl.glScalef(1, 1, length);

        // Draw a box with length 0.9 and width and depth line_radius
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, 0.5f);
        gl.glScalef(line_radius, line_radius, 0.9f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        // Translate to the end of the line
        gl.glTranslatef(0f, 0f, 0.9f);
        // Draw an arrowhead with length 0.1 and radius arrowhead_radius
        glut.glutSolidCone(arrowhead_radius, 0.1f, 15, 2);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    /**
     * Draws an axis frame, consisting of a yellow sphere in the origin and
     * three arrows of length 1 in the positive directions of the X, Y and Z
     * axes, if gs.showAxes is true.
     */
    private void drawAxisFrame() {
        if (gs.showAxes) {
            gl.glPushAttrib(GL_CURRENT_BIT);

            // Draw yellow sphere in origin.
            gl.glColor3f(1.0f, 1.0f, 0); // set color to yellow
            setMaterial(Material.YELLOW_PLASTIC);
            glut.glutSolidSphere(0.10f, 20, 20); // draw sphere

            // Draw arrow for X axis.
            gl.glPushMatrix();
            gl.glRotatef(90, 0, 1, 0); // rotate 90 degrees around y axis
            gl.glColor3f(1.0f, 0, 0); // set color to red
            setMaterial(Material.RED_PLASTIC); // set material to red plastic
            drawArrow(1, 0.01f, 0.05f); // draw arrow
            gl.glPopMatrix();

            // Draw arrow for Y axis.
            gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0); // rotate -90 degrees around x axis
            gl.glColor3f(0, 1.0f, 0); // set color to green
            setMaterial(Material.GREEN_PLASTIC); // set material to green plastic
            drawArrow(1, 0.01f, 0.05f); // draw arrow
            gl.glPopMatrix();

            // Draw arrow for Z axis.
            gl.glPushMatrix();
            gl.glColor3f(0, 0, 1.0f); // set color to blue
            setMaterial(Material.BLUE_PLASTIC); // set material to blue plastic
            drawArrow(1, 0.01f, 0.05f); // draw arrow
            gl.glPopMatrix();
            gl.glPopAttrib();
        }
    }

    /**
     * Draws a grid on the XOY plane if gs.showAxes is true.
     */
    private void drawGrid() {
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

    /**
     * Draws the unit circle in a 3D world in the X and Z directions.
     *
     * @param radius the radius of the circle
     * @param slices the precision of the circle
     */
    private void drawCircle(float radius, int slices) {
        float angle; // angle of ray from origin to current point with x axis
        gl.glBegin(GL_LINE_LOOP);
        for (int i = 0; i < slices; i++) {
            angle = (float) (i * 2 * PI / slices); // compute angle
            // Define vertex by definition of the unit circle.
            gl.glVertex3d((cos(angle) * radius), 0, (sin(angle) * radius));
        }
        gl.glEnd();
    }

    private void drawCube(float h, Texture texture, int part) {
        //TODO: draw top and bottom

        // Enable texture.
        texture.enable(gl);
        texture.bind(gl);

        // Draw the front face of the cube.
        drawFace(h, part);

        // Disable texture.
        texture.disable(gl);

        gl.glPushMatrix();
        for (int i = 0; i < 3; i++) {
            gl.glRotatef(90, 0, 0, 1);
            drawFace(h, part);
        }
        gl.glPopMatrix();
    }

    private void drawFace(float h, int part) {
        float lowx, highx, lowy, highy;
        switch (part) {
            case 0:
                lowx = 0;
                highx = 1;
                lowy = 0;
                highy = 1;
                break;
            case 1:
                lowx = 0;
                highx = 0.5f;
                lowy = 0;
                highy = 0.5f;
                break;
            case 2:
                lowx = 0.5f;
                highx = 1f;
                lowy = 0f;
                highy = 0.5f;
                break;
            case 3:
                lowx = 0f;
                highx = 0.5f;
                lowy = 0.5f;
                highy = 1f;
                break;
            case 4:
                lowx = 0.5f;
                highx = 1f;
                lowy = 0.5f;
                highy = 1f;
                break;
            default:
                lowx = 0;
                highx = 0;
                lowy = 0;
                highy = 0;
        }
//TODO: fix for fourth robot
        gl.glBegin(GL_QUADS);
        gl.glNormal3f(0, 1, 0);
        gl.glTexCoord2f(highx, highy);
        gl.glVertex3f(-h / 2, h / 2, -h / 2);
        gl.glTexCoord2f(lowx, highy);
        gl.glVertex3f(h / 2, h / 2, -h / 2);
        gl.glTexCoord2f(lowx, lowy);
        gl.glVertex3f(h / 2, h / 2, h / 2);
        gl.glTexCoord2f(highx, lowy);
        gl.glVertex3f(-h / 2, h / 2, h / 2);
        gl.glEnd();
    }

    /**
     * Draws a rotationally symmetric shape. This is done by rotating a contour
     * around the Z axis and drawing the result in 3D.
     *
     * The contour is defined as multiple coordinates (x,z), which are connected
     * by lines. In this sense, the input is assumed to be sorted. The contour
     * is rotated in a few different directions (slices). The resulting lines
     * will be connected. If any two adjacent points in the contour are further
     * away than the minimum specified distance, subdivision along the line is
     * applied.
     *
     * @param x x coordinates of the points defining the contour
     * @param z z coordinates of the points defining the contour
     * @param sm if true, use smooth shading. if false, use flat shading.
     * @param slices the number of slices to use
     * @param dmin the minimum distance between to points in the contour
     */
    private void drawRotSymShape(double[] x, double[] z, boolean sm, int slices,
            double dmin) {
        final int N = x.length; // numbers of points in contour

        // Compute all necessary angles.
        double[] angle = new double[slices + 1]; // angle from first to ith slice
        for (int i = 0; i < slices; i++) {
            // The angle between two slices is a=2*PI/slices.
            // The angle between the first slice and slice i is i*a.
            angle[i] = i * (2 * PI / slices);
        }

        // Apply subdivision.
        List<Vector> list = new ArrayList<Vector>(); // list of points
        int n; // minimal number of line segments a line needs to be divided in
        for (int i = 0; i < N - 1; i++) { // for each line between two points
            Vector P = new Vector(x[i], 0, z[i]); // first point
            Vector Q = new Vector(x[i + 1], 0, z[i + 1]); // second point
            Vector V = Q.subtract(P); // vector from P to Q

            if (V.length() > dmin) {
                // |PQ| > dmin; divide PQ into equal parts
                n = (int) Math.ceil(V.length() / dmin); // calculate n            
                double l = V.length() / n; // length of each new line segment

                // Add P and new points on line PQ to the list.
                for (int j = 0; j < n; j++) {
                    // Add (P+V*(l*j)).
                    list.add(P.add(V.normalized().scale(l * j)));
                }
            } else {
                // |PQ| <= dmin; no need to apply subdivision.
                // Add P to the list.
                list.add(P);
            }
        }
        // Add last point of the contour to the list.
        list.add(new Vector(x[N - 1], 0, z[N - 1]));

        // Determine normals for all vertices of the 3D shape.
        // This step will be skipped if smooth shading is disabled.
        Vector[][] normals = new Vector[list.size()][slices]; // normal for each vertex
        /*
         * Our goal is to store the normal for each vertex in this array. The
         * normal of a vertex is defined to be the average of the normals of the
         * surrounding quads.
         *
         * Note that we normalize these normals later, so the length of the
         * normal has no meaning. Therefore, we can simply add the normals for
         * the quads together.
         */
        if (sm) {
            // Smooth shading enabled.
            // Initialize array to zero vector for all vertices.
            for (int i = 0; i < normals.length; i++) {
                for (int j = 0; j < normals[i].length; j++) {
                    normals[i][j] = Vector.O;
                }
            }

            for (int i = 0; i < list.size() - 1; i++) { // for each quad:
                for (int j = 0; j < slices; j++) {
                    // Compute the normal of the quad.
                    Vector bl, br, ul; // vertices of this quad
                    bl = new Vector(cos(angle[j]) * list.get(i).x(),
                            sin(angle[j]) * list.get(i).x(), list.get(i).z());
                    br = new Vector(cos(angle[j + 1]) * list.get(i).x(),
                            sin(angle[j + 1]) * list.get(i).x(), list.get(i).z());
                    ul = new Vector(cos(angle[j]) * list.get(i + 1).x(),
                            sin(angle[j]) * list.get(i + 1).x(),
                            list.get(i + 1).z());
                    Vector up = ul.subtract(bl);
                    Vector right = br.subtract(bl);
                    Vector normal = right.cross(up).normalized();

                    // Add the normal to the current normal for each vertex.
                    normals[i][j] = normals[i][j].add(normal);
                    normals[i][(j + 1) % (slices)] = normals[i][(j + 1) % (slices)].add(normal);
                    normals[(i + 1) % list.size()][j] = normals[(i + 1) % list.size()][j].add(normal);
                    normals[(i + 1) % list.size()][(j + 1) % (slices)] = normals[(i + 1) % list.size()][(j + 1) % (slices)].add(normal);
                }
            }
        }

        // Draw the polygons.
        gl.glBegin(GL_QUADS);
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < slices; j++) {
                Vector bl, // bottom left corner of the quad
                        br, // bottom right corner of the quad
                        ur, // upper right corner of the quad
                        ul; // upper left corner of the quad

                // Compute vertices for this quad.
                bl = new Vector(cos(angle[j]) * list.get(i).x(),
                        sin(angle[j]) * list.get(i).x(), list.get(i).z());
                br = new Vector(cos(angle[j + 1]) * list.get(i).x(),
                        sin(angle[j + 1]) * list.get(i).x(), list.get(i).z());
                ur = new Vector(cos(angle[j + 1]) * list.get(i + 1).x(),
                        sin(angle[j + 1]) * list.get(i + 1).x(), list.get(i + 1).z());
                ul = new Vector(cos(angle[j]) * list.get(i + 1).x(),
                        sin(angle[j]) * list.get(i + 1).x(), list.get(i + 1).z());

                if (sm) {
                    // Use smooth shading; use normal per vertex.
                    Vector bl_normal, // normal vector for bottom left vertex
                            br_normal, // normal vector for bottom right vertex
                            ur_normal, // normal vector for upper right vertex
                            ul_normal; // normal vector for upper left vertex

                    // Get normals from the array we constructed earlier.
                    bl_normal = normals[i][j].normalized();
                    br_normal = normals[i][(j + 1) % (slices)].normalized();
                    ur_normal = normals[(i + 1) % list.size()][(j + 1) % (slices)].normalized();
                    ul_normal = normals[(i + 1) % list.size()][j].normalized();

                    // Draw all vertices with their respective normals.
                    gl.glNormal3d(bl_normal.x(), bl_normal.y(), bl_normal.z());
                    gl.glVertex3d(bl.x(), bl.y(), bl.z());

                    gl.glNormal3d(br_normal.x(), br_normal.y(), br_normal.z());
                    gl.glVertex3d(br.x(), br.y(), br.z());

                    gl.glNormal3d(ur_normal.x(), ur_normal.y(), ur_normal.z());
                    gl.glVertex3d(ur.x(), ur.y(), ur.z());

                    gl.glNormal3d(ul_normal.x(), ul_normal.y(), ur_normal.z());
                    gl.glVertex3d(ul.x(), ul.y(), ur.z());

                } else {
                    // Use flat shading; calculate normal for this quad.
                    Vector up = ul.subtract(bl); // vector from bl to ul
                    Vector right = br.subtract(bl); // vector from bl to br
                    Vector normal = right.cross(up); // normal vector
                    normal = normal.normalized(); // normalize the vector

                    // Draw the vertices with the calculated normal.
                    gl.glNormal3d(normal.x(), normal.y(), normal.z());
                    gl.glVertex3d(bl.x(), bl.y(), bl.z());
                    gl.glVertex3d(br.x(), br.y(), br.z());
                    gl.glVertex3d(ur.x(), ur.y(), ur.z());
                    gl.glVertex3d(ul.x(), ul.y(), ur.z());
                }
            }
        }
        gl.glEnd();
    }

    /**
     * Sets the camera such that it will show an overview of the scene. The
     * camera is positioned at the eye point and is looking towards the center
     * point. Is using the Z axis as the up vector.
     */
    private void setOverviewCamMode() {
        glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
                gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                0, 0, 1); // up axis
    }

    /**
     * Sets the camera such that it will give a helicopter view of the scene.
     * The camera will be placed at the average position of the robots on the
     * track.
     */
    private void setHelicopterCamMode() {
        float total = 0;
        // Calculate the total position of the robots. 
        for (Robot robot : robots) {
            total += robot.position;
        }
        float avg = total / NUMROBOTS; // Compute the average position.
        // Set the center point to be at the average position of the robots on
        // the track plus the normal vector with a magnitude of half of the
        // number of robots. This places the center in the middle of the robots.
        Vector center = t.curve.getPoint(avg).add(t.curve.getNormalVector(avg).normalized().scale(NUMROBOTS / 2));
        // Set the camers position to be 10 units above the center point.
        Vector camPos = center.add(new Vector(0, 0, 10));
        // Get the track tangent at the current point. 
        Vector tangent = t.curve.getTangent(avg);
        // Set the camera. The up vector is set to be the tangent vector to make
        // it turn with the track.
        glu.gluLookAt(camPos.x(), camPos.y(), camPos.z(), //eye point
                center.x(), center.y(), center.z(), //center point
                tangent.x(), tangent.y(), tangent.z()); //up vector
    }

    /**
     * Sets the camera such that it will follow the robots from the side of
     * track like a motorcycle. The camera is following the fastest robot.
     */
    private void setMotorcycleCamMode() {
        float max = 0;
        // Get the position of the robot that is in front.
        for (Robot robot : robots) {
            max = max(max, robot.position);
        }
        // Set the center point to be one unit higher than the robot's position.
        Vector center = t.curve.getPoint(max).add(new Vector(0, 0, 1));
        // Get the camer aposition such that is next to the front robot at a height
        // of one unit.
        Vector camPos = t.curve.getPoint(max).add(t.curve.getNormalVector(max).normalized().scale(NUMROBOTS + 1)).add(new Vector(0, 0, 1));
        glu.gluLookAt(camPos.x(), camPos.y(), camPos.z(), //eye point
                center.x(), center.y(), center.z(), //center point
                0, 0, 1);//up vector
    }

    /**
     * Sets the camera such that the view will appear to be from the perspective
     * of a robot. We chose this robot to be the one closest to the center
     * because he sees the most other robots.
     */
    private void setFirstPersonCamMode() {
        Robot robot = robots[0];
        // Get the position of the first created robot.
        Vector pos = t.curve.getPoint(robot.position);
        Vector camPos;
        Vector center;
        if (gs.persp) {
            // For the perspective projection set the camera to be the position
            // of the robot plus the height of the robot and
            // add the tangent of the robot with the track normalized.
            camPos = pos.add(new Vector(0, 0, robot.hatPart.getHeight())).add(t.curve.getTangent(robot.position).normalized());
            // Set the camera to look from its position towards the tangent of the
            // robot with the track.
            center = camPos.add(t.curve.getTangent(robot.position).normalized().scale(5));
        } else {
            // For the isometric projection set the camerato be the position of the 
            // robot plus the height of the robot and
            // add the tangent of the robot with the track, normalized and
            // scaled two untis.
            camPos = pos.add(new Vector(0, 0, robot.hatPart.getHeight())).add(t.curve.getTangent(robot.position).normalized().scale(2));
            // Set the camera to look from its position along the tangent vector.
            center = pos.add(t.curve.getTangent(robot.position).normalized().scale(7));
            /*
             * NB: using first person mode in isometric projection gives some
             * strange effects. First of all, the track seems to up and down.
             * This is because objects that get farther away are not drawn
             * smaller in isometric projection. Therefore, when a larger part of
             * the track is visible, it seems like it is going up. Secondly, the
             * bottom of the track and the sides are partly visible. This is
             * because the width of the screen is user- configurable. Finally,
             * robots that appear close to the robot we are following partly get
             * clipped off. This also has to do with the width of the scene that
             * is being displayed in combination with isometric projection.
             */
        }
        glu.gluLookAt(camPos.x(), camPos.y(), camPos.z(), // eye point 
                center.x(), center.y(), center.z(), // center point
                0, 0, 1); // up vector
    }

    /**
     * Parses an array and sets the given parameters for the ambient, diffuse,
     * specular and shininess values of the material.
     *
     * @param material An array with 13 floats where the first 4 values
     * represents the ambient, the next 4 the diffuse factor, the next 4 the
     * specular factor, and the last the shininess value of the material.
     */
    private void setMaterial(float[] material) {
        gl.glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material, 0);
        gl.glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material, 4);
        gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material, 8);
        gl.glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, material, 12);
    }

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    class Robot {

        boolean legDirection = false; // specifies if the leg is moving forward
        float speed = 10f; // specifies speed at which legs.angle is increased
        final static private float MAXANGLE = 20; // upper bound for legs.angle
        HatPart hatPart; // object representing the hat of the robot
        HeadPart headPart; // object representing the head of the robot
        TorsoPart torsoPart; // object representing the torso of the robot
        ArmsPart arms; // object representing the arms of the robot
        LegsPart legs; // object representing the legs of the robot
        Set<RobotPart> parts; // set containing all components which are drawn
        Color color; // color of this robot
        float position = 0; // current position on the track
        float tAnim_old = 0; // value for tAnim when last updating position
        int number; // number of the robot

        /**
         * Constructs a Robot with some default dimensions.
         */
        public Robot() {
            this(
                    0.5f, // hatSize
                    0.5f, // headSize
                    0.5f, // torsoHeight
                    0.5f, // torsoWidth
                    0.5f, // torsoThickness
                    0.5f, // armsLength
                    0.1f, // armsWidth
                    0.1f, // armsThickness
                    0.5f, // legsLength
                    0.1f, // legsWidth
                    0.1f, // legsThickness
                    5 // number
                    );
        }

        /**
         * Constructs a Robot with some default dimensions.
         *
         * @param number number of the robot
         */
        public Robot(int number) {
            this(
                    0.5f, // hatSize
                    0.5f, // headSize
                    0.5f, // torsoHeight
                    0.5f, // torsoWidth
                    0.5f, // torsoThickness
                    0.5f, // armsLength
                    0.1f, // armsWidth
                    0.1f, // armsThickness
                    0.5f, // legsLength
                    0.1f, // legsWidth
                    0.1f, // legsThickness
                    number // number
                    );
        }

        /**
         * Constructs the robot with the desired parameters.
         *
         * @param hatSize size of the hat.
         * @param headSize diameter of the circle representing the head.
         * @param torsoHeight height of the torso.
         * @param torsoWidth width of the torso.
         * @param torsoThickness thickness of the torso.
         * @param armsLength length of the arms.
         * @param armsWidth width of the arms.
         * @param armsThickness thickness of the arms.
         * @param legsLength length of the legs
         * @param legsWidth width of the legs.
         * @param legsThickness thickness of the legs.
         * @param number number of the robot
         */
        public Robot(float hatSize, float headSize, float torsoHeight,
                float torsoWidth, float torsoThickness, float armsLength,
                float armsWidth, float armsThickness, float legsLength,
                float legsWidth, float legsThickness, int number) {
            parts = new HashSet<RobotPart>(); // initialize parts set

            // Construct all parts with the given parameters.
            hatPart = new HatPart(hatSize);
            headPart = new HeadPart(headSize);
            torsoPart = new TorsoPart(torsoHeight, torsoWidth, torsoThickness);
            arms = new ArmsPart(armsLength, armsWidth, armsThickness);
            legs = new LegsPart(legsLength, legsWidth, legsThickness);

            // Set the number of the robot.
            this.number = number;

            // Add all parts that need to be displayed to the parts set.
            parts.add(hatPart);
            parts.add(headPart);
            parts.add(torsoPart);
            parts.add(arms);
            parts.add(legs);
        }

        /**
         * Draws the robot.
         */
        public void draw() {
            this.handleMovement();

            gl.glPushMatrix();
            gl.glPushAttrib(GL_CURRENT_BIT);

            // Draw parts.
            for (RobotPart p : parts) { // for all parts that should be drawn:
                p.draw(); //draw the part
            }

            gl.glPopAttrib();
            gl.glPopMatrix();
        }

        /**
         * Moves the robot and turns the arms and legs by updating
         * {@code legDirection}, {@code legs.angle} and {@code position} based
         * on {@code legDirection}, {@code speed}, {@code MAXANGLE} and
         * {@code gs.tAnim}.
         */
        public void handleMovement() {
            if (abs(legs.angle) >= MAXANGLE) {
                // The maximum angle has been reached.
                // Reverse the direction.
                legDirection = !legDirection;
            }

            // When moving the leg forwards, increase the angle.
            // When moving the leg backwards, decrease the angle.
            legs.angle += (legDirection ? 1 : -1) * speed;

            // Update current position on the track.
            /*
             * The position should be increased by a value that is: - positive;
             * robots should not move backwards - random; the race should be
             * exciting - dependent on time; gs.tAnim decides how fast time
             * progresses. Rendering using higher FPS should not influence the
             * speeds of the robots. To accomplish this, we compute the time
             * that has passed since the last update of the position. We then
             * increase the position by the product of this difference, a random
             * variable and some scalar to prevent the robots from moving to
             * fast.
             */
            float tAnim = gs.tAnim; // the current time
            float dt = tAnim - tAnim_old; // change in time since last update
            float random =
                    new java.util.Random().nextFloat(); // random value in [0,1]
            position += dt * random / 8; // increase the position
            tAnim_old = tAnim; // store the current time for future reference
        }

        /**
         * Represents the legs of a robot. A leg includes a hip, an upper part,
         * a knee and a lower part.
         */
        public class LegsPart implements RobotPart {

            RobotPart leftLeg; //object for left leg
            RobotPart rightLeg; //object for right leg
            float angle = 0; //current angle the legs are rotated by
            float length; //length of the legs
            float width; //width of the legs
            float thickness; //thickness of the legs

            /**
             * Constructs a LegsPart object.
             *
             * @param length Length of the legs.
             * @param width Width of the legs.
             * @param thickness Thickness of the legs.
             */
            public LegsPart(float length, float width, float thickness) {
                this.leftLeg = new LegPart(true, this);
                this.rightLeg = new LegPart(false, this);
                this.length = length;
                this.width = width;
                this.thickness = thickness;
            }

            /**
             * Draws both legs.
             */
            @Override
            public void draw() {
                leftLeg.draw();
                rightLeg.draw();
            }

            /**
             * Computes the vertical distance between the bottom of the torso
             * and the XOY plane.
             *
             * @return the distance
             */
            @Override
            public float getHeight() {
                return (float) (cos(toRadians(angle)) * length);
            }
        }

        /**
         * Represents a leg of the robot.
         */
        public class LegPart implements RobotPart {

            boolean left; // specifies whether this is the left leg
            LegsPart parent; // reference to the LegsPart object
            int s; // sign of the angle to rotate over

            /**
             * Constructs a LegPart object.
             *
             * @param left specifies whether this is the left leg
             * @param parent reference to the LegsPart object
             */
            public LegPart(boolean left, LegsPart parent) {
                this.left = left;
                this.parent = parent;
                s = left ? -1 : 1;
            }

            /**
             * Draws a hip for the full robot in a shape of a cylinder.
             */
            private void drawHip() {
                if (!gs.showStick) {
                    gl.glPushMatrix();
                    // Translate the current position such that the x coordinate
                    // is at the side  of the torso, meaning moving half of the
                    // torso width to the left or to the right, depennding on
                    // the leg. In the Z direction, we translate to just under
                    // the height of the leg.
                    gl.glTranslatef(s * 0.5f * (torsoPart.width), 0,
                            parent.getHeight() - parent.thickness / 3);
                    // Rotate 90 degrees in a direction dependent on the leg.
                    gl.glRotatef(s * -90, 0, 1, 0);
                    // Scale to the leg's width for the x and z axes and on the
                    // leg's thickness for the y axis.
                    gl.glScalef(parent.thickness, parent.thickness, parent.width);
                    // Draw a cylinder with the radius of half of the leg's
                    // thickness, and as long as the leg's width. 
                    glut.glutSolidCylinder(0.5, 1, 20, 10);
                    gl.glPopMatrix();
                }

            }

            /**
             * Draws the upper part of a leg.
             */
            private void drawUpperLeg() {
                if (gs.showStick) {
                    // Draw a line from the end of the leg to the base of the
                    // torso.
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); // Base of the torso.
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length
                            - getHeight() * getHeight()), 0); // End of the leg.
                    gl.glEnd();
                } else {
                    // Draw a rectangular prism to represent the upper leg part.
                    gl.glPushMatrix();
                    // Translate to the height of the leg.
                    gl.glTranslated(0, 0, getHeight());
                    // Rotate the leg in the x direction, over the specified
                    // angle.
                    gl.glRotatef(s * parent.angle, 1, 0, 0);
                    // Translate to half of the height of the leg (vertically)
                    // and the edge of the torso (horizontally).
                    gl.glTranslatef(s * (0.5f * torsoPart.width
                            - 0.5f * parent.width), 0,
                            (-0.25f * getHeight()) - parent.thickness / 2f);
                    gl.glPushMatrix();
                    // Scale a unit cube according to the leg's dimensions.
                    // The length will be divided by two,since this is
                    // one half of the leg.
                    gl.glScalef(parent.width, parent.thickness,
                            0.5f * parent.length);
                    glut.glutSolidCube(1);
                    gl.glPopMatrix();
                }
            }

            /**
             * Draws a knee for the full robot in a shape of a cylinder.
             */
            private void drawKnee() {
                if (!gs.showStick) {
                    gl.glPushMatrix();
                    // Translate the current position such that the x coordinate
                    // is at the side of the leg, meaning moving half of the
                    // leg's width to the left or to the right, depending on the
                    // leg. And the Z coordinate under the upper leg, which is
                    // half of the upper leg's length (a quarter of the leg). 
                    gl.glTranslatef(s * 0.5f * parent.width, 0,
                            -0.25f * parent.length);
                    // Rotate 90 degrees clockwise for the right leg, and
                    // 90 degrees counterclockwise for the left leg.
                    gl.glRotatef(s * -90, 0, 1, 0);
                    // Scale the knee according to the leg's dimensions:
                    // the leg's width for the x and z axis the leg's thickness
                    // for the y axis.
                    gl.glScalef(parent.width, parent.thickness, parent.width);
                    // Draw a cylinder to represent the knee with a radius of
                    // half of the leg's thickness and of the length of
                    // the leg's width.
                    glut.glutSolidCylinder(0.5, 1, 20, 10);
                    gl.glPopMatrix();
                }
            }

            /**
             * Draws the lower part of a leg.
             */
            private void drawLowerLeg() {
                if (gs.showStick) {
                    // Draw a line from the end of the leg to the base of the
                    // torso to represent the leg for the stick robot.
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); // base of the torso
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length
                            - getHeight() * getHeight()), 0); // end of the leg
                    gl.glEnd();
                } else {
                    // Translate the current position to be at the same height
                    // as the knee.
                    gl.glTranslatef(0, 0, -0.25f * parent.length);
                    // Rotate 90 degrees counterclockwise for the left leg, and
                    // 90 degrees clockwise for the right leg around the X axis.
                    gl.glRotatef(s * -parent.angle, 1, 0, 0);
                    // Translate again in order for the height of the drawn tree
                    // to reach the knee.
                    gl.glTranslatef(0, 0, -0.25f * parent.length);
                    // Scale a unit cube accorind with the leg's dimensions,
                    // divide the length by two since this is one half of
                    // the leg.
                    gl.glScalef(parent.width, parent.thickness,
                            0.5f * parent.length);
                    glut.glutSolidCube(1);
                    gl.glPopMatrix();
                }
            }

            /**
             * Draws this Leg. Makes a call to all the drawing function for each
             * part of the leg: hip, upper leg, knee and lower leg.
             */
            @Override
            public void draw() {
                drawHip();
                drawUpperLeg();
                drawKnee();
                drawLowerLeg();
            }

            /**
             * Computes the distance between the top of the leg and the XOY
             * plane. This is not simply the length, since the leg can be
             * rotated.
             *
             * @return the distance
             */
            @Override
            public float getHeight() {
                return parent.getHeight();
            }
        }

        /**
         * Represents the arms of the robot.
         */
        public class ArmsPart implements RobotPart {

            ArmPart leftArm; // object for left arm.
            ArmPart rightArm; // object for right arm.
            float length; // length of the arms.
            float width; // width of the arms.
            float thickness; // thickness of the arms.

            /**
             * Constructs an ArmsPart object.
             *
             * @param length length of the arms
             * @param width width of the arms.
             * @param thickness thickness of the arms.
             */
            public ArmsPart(float length, float width, float thickness) {
                leftArm = new ArmPart(true, this);
                rightArm = new ArmPart(false, this);
                this.length = length;
                this.width = width;
                this.thickness = thickness;

            }

            /**
             * Draws the arms. Meaning it makes a call to the draw method of the
             * ArmPart for the left and the right arm.
             */
            @Override
            public void draw() {
                leftArm.draw();
                rightArm.draw();
            }

            /**
             * Computes the vertical distance between the top of the torso and
             * the XOY plane.
             *
             * @return the distance
             */
            @Override
            public float getHeight() {
                return torsoPart.getHeight();
            }
        }

        /**
         * Represents an arm of the robot.
         */
        public class ArmPart implements RobotPart {

            boolean left; // specifies whether this is the left arm
            ArmsPart parent; // reference to the ArmPart object
            int s; // sign of the angle to rotate over

            /**
             * Constructs a ArmPart object.
             *
             * @param left specifies whether this is the left arm
             * @param parent reference to the ArmPart object
             */
            public ArmPart(boolean left, ArmsPart parent) {
                this.left = left;
                this.parent = parent;
                s = left ? -1 : 1;
            }

            /**
             * Draws the shoulder of an arm in a shape of a cylinder for the
             * full robot.
             */
            private void drawShoulder() {
                if (!gs.showStick) {
                    gl.glPushMatrix();
                    // Draw the shoulder at the appropriate position. For the X
                    // coordinate that is to the edge of the torso plus the
                    // width of the arm. And for the Z coordinate it is the arm
                    // height minus the radius of the cylinder.
                    gl.glTranslatef(s * (0.5f * torsoPart.width + parent.width),
                            0, parent.getHeight() - 0.5f * parent.width);
                    // Rotate around the Y axis so that the cylinder will
                    // be drawn vertically.
                    gl.glRotatef(s * -90, 0, 1, 0);
                    // Scale the shoulder according to the arm's dimensions:
                    // the arm's width for the x and z axis,
                    // the arm's thickness for the y axis.        
                    gl.glScalef(parent.width, parent.thickness, parent.width);
                    // Draw a cylinder to represent the shoulder with a radius
                    // of half of the arm's thickness and with length of the
                    // arm's width.
                    glut.glutSolidCylinder(0.5, 1, 20, 10);
                    gl.glPopMatrix();
                }
            }

            /**
             * Draws an arm, by drawing its components the shoulder and the
             * actual arm.
             */
            @Override
            public void draw() {
                drawShoulder();
                drawArm();
            }

            /**
             * Draws an arm for the robot.
             */
            private void drawArm() {
                // For the stick robot draw a line from the end of the length of
                // the arm that rotates over legs.angle in negative direction.
                if (gs.showStick) {
                    gl.glPushMatrix();
                    // Translate to the height of the torso.
                    gl.glTranslated(0, 0, torsoPart.getHeight());
                    // Rotate around the x axis by an angle of legs.angle
                    // clockwise. 
                    gl.glRotatef(s * -legs.angle, 1, 0, 0);
                    // Translate the current position to be at the top and 
                    // middle of the arm.
                    gl.glTranslatef(0, 0, -0.5f * parent.length);
                    // Draw a line between the two ends of the arm. 
                    gl.glBegin(GL_LINES);
                    gl.glVertex3d(0, 0, -0.5 * parent.length);
                    gl.glVertex3d(0, 0, 0.5 * parent.length);
                    gl.glEnd();
                    gl.glPopMatrix();
                } else {
                    gl.glPushMatrix();
                    // Translate the current position to be at the same height
                    // as the shoulder.
                    gl.glTranslated(0, 0,
                            torsoPart.getHeight() - 0.5f * parent.width);
                    // Rotate around the x axis by an angle of legs.angle
                    // clockwise. 
                    gl.glRotatef(s * -legs.angle, 1, 0, 0);
                    // Translate such that the arm will be drawn to the side of
                    // the torso. That is, moving to the left or to the right
                    // (depending on s) half of the torso width plus half
                    // of the arm width. Also move downwards half of the arm's
                    // length, such that the prism will just reach the shoulder.
                    gl.glTranslatef(s * 0.5f * (torsoPart.width + parent.width),
                            0, -0.5f * parent.length);
                    // Scale a cube according to the arm's dimensions.
                    gl.glScalef(parent.width, parent.thickness, parent.length);
                    glut.glutSolidCube(1f);
                    gl.glPopMatrix();
                }
            }

            /**
             * Computes the vertical distance between the top of the torso and
             * the XOY plane.
             *
             * @return parent.getHeight()
             */
            @Override
            public float getHeight() {
                return parent.getHeight();
            }
        }

        /**
         * Represents the torso of a robot.
         */
        public class TorsoPart implements RobotPart {

            // Variables for the torso dimensions.
            float height;
            float width;
            float thickness;

            /**
             * Constructs a torso with the given dimensions.
             *
             * @param height height of the torso.
             * @param width width of the torso.
             * @param thickness thickness of the torso.
             */
            public TorsoPart(float height, float width, float thickness) {
                this.height = height;
                this.width = width;
                this.thickness = thickness;
            }

            /**
             * Draws a shape to represent the torso of a robot. For the stick
             * robot that is a line, and for the full robot a prism.
             */
            @Override
            public void draw() {
                if (gs.showStick) {
                    gl.glPushMatrix();
                    // Draw a line from the top of the legs
                    // to the top of the torso. 
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, legs.getHeight()); // Leg's height.
                    gl.glVertex3f(0, 0, this.getHeight()); // Torso's height.
                    gl.glEnd();
                    gl.glPopMatrix();
                } else {
                    gl.glPushMatrix();
                    // Move the current position at the center of the torso's
                    // height. That is, the height of the legs plus half of
                    // the torso height.
                    gl.glTranslated(0, 0, legs.getHeight() + (0.5f * height));
                    // Scale a cube according to the torso's dimensions.
                    gl.glScaled(width, thickness, height);
                    gl.glRotatef(180, 0, 0, 1);
                    drawCube(1, torso, number);
                    gl.glPopMatrix();
                }
            }

            /**
             * Computes the height at which the torso ends. Which is the height
             * of the legs plus the height of itself.
             *
             * @return The height at which the torso ends.
             */
            @Override
            public float getHeight() {
                return legs.getHeight() + height;
            }
        }

        /**
         * Represents the head of a robot.
         */
        public class HeadPart implements RobotPart {
            // Variable representing the dimension (diameter) of the head. 

            float height;

            /**
             * Constructs the head with given parameters.
             *
             * @param height The height or diameter of the head.
             */
            public HeadPart(float height) {
                this.height = height;
            }

            /**
             * Draws the head of a robot. For the stick robot that is a circle
             * and for the full robot a sphere.
             */
            @Override
            public void draw() {
                if (gs.showStick) {
                    gl.glPushMatrix();
                    // Translate to the middle of the head.
                    gl.glTranslatef(0, 0, (0.5f * height + torsoPart.getHeight()));
                    // Draw a unit circle with radius height/2.
                    drawCircle(height / 2, 100);
                    gl.glPopMatrix();
                } else {
                    gl.glPushMatrix();
                    // Translate the current position to be at the height of the
                    // head's center point. That is the height of the torso plus 
                    // half of the head's height.
                    gl.glTranslated(0, 0, 0.5f * height + torsoPart.getHeight());
                    // Draw a sphere to represent the head with a radius of half
                    // of the head's height.
                    drawCube(height, head, 0);
                    gl.glPopMatrix();
                }
            }

            /**
             * Computes the height at which the head ends. That is the height of
             * the torso plus its own height.
             *
             * @return The height at which the head ends.
             */
            @Override
            public float getHeight() {
                return torsoPart.getHeight() + height;
            }
        }

        /**
         * Represents the hat of the robot. For the stick robot that is a
         * triangle and a cone for the full robot
         */
        public class HatPart implements RobotPart {

            float height; // height of the hat

            /**
             * Constructs a hat with the specified parameters.
             *
             * @param height The height of the hat.
             */
            public HatPart(float height) {
                this.height = height;
            }

            @Override
            public void draw() {
                if (gs.showStick) {
                    // Draw a triangle.
                    gl.glBegin(GL_LINE_LOOP);
                    // Define the top of the hat.
                    gl.glVertex3f(0, 0, this.getHeight());
                    // Define the left corner of the hat.
                    gl.glVertex3f(-0.5f * height, 0, headPart.getHeight());
                    // Define the right corner of the hat.
                    gl.glVertex3f(0.5f * height, 0, headPart.getHeight());
                    gl.glEnd();
                } else {
                    gl.glPushMatrix();

                    // Move to just under the top of the head.
                    gl.glTranslated(0, 0, headPart.getHeight() - 0.1f);
                    // Draw a cone with the specified height and a base of
                    // height/2.
                    glut.glutSolidCone(0.5f * height, height, 10, 10);

                    gl.glPopMatrix();
                }
            }

            /**
             * Calculates the height of the complete robot.
             *
             * @return the height
             */
            @Override
            public float getHeight() {
                return headPart.getHeight() + height;
            }
        }
    }

    /**
     * Interface for a part of the robot.
     */
    public interface RobotPart {

        /**
         * Uses GL calls to draw this part.
         */
        public void draw();

        /**
         * Returns the height in the world which this part reaches.
         *
         * @return the height
         */
        public float getHeight();
    }

    /**
     * Represents the track on which the robots are running.
     */
    public class Track {

        private Curve curve; // determines the shape of the track
        private float width; // width of the track
        private float minHeight; // height at which the track starts
        private float maxHeight; // height at which the track ends
        final static private int N = 100; // number of polygons used to display

        /**
         * Constructs a truck with the given parameters.
         *
         * @param curve The shape that the track will have.
         * @param width The width of the track.
         * @param minHeight height at which the track starts
         * @param maxHeight height at which the track ends
         */
        public Track(Curve curve, float width, float minHeight, float maxHeight) {
            this.curve = curve;
            this.width = width;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        /**
         * Draws the track.
         */
        public void draw() {
            List<Vector> points = new ArrayList<Vector>(); // points defining inside of the track
            List<Vector> offset_points = new ArrayList<Vector>(); // points defining the outside of the track
            List<Vector> normals = new ArrayList<Vector>(); // vectors pointing pointing outwards

            // Compute all points and normals.
            for (int i = 0; i <= N; i++) {
                float t = (float) i / N;
                Vector point = curve.getPoint(t);
                points.add(point);

                Vector normal = curve.getNormalVector(t);
                normals.add(normal);
                Vector off = point.add(normal.normalized().scale(width));
                offset_points.add(off);
            }

            gl.glBegin(GL_LINE_STRIP);
            // Draw a line on the inside of the track.
            for (int i = 0; i <= N; i++) {
                Vector point = points.get(i);
                gl.glVertex3d(point.x(), point.y(), point.z());
            }
            // Draw a line on the outside of the track (and connect the two to
            // show a start/finish line).
            for (int i = 0; i <= N; i++) {
                Vector point = offset_points.get(i);
                gl.glVertex3d(point.x(), point.y(), point.z());
            }
            gl.glEnd();

            track.enable(gl);
            track.bind(gl);
            gl.glBegin(GL_QUADS);
            // Draw the top of the track.
            for (int i = 0; i < N; i++) {
                Vector point = points.get(i); // point on the inside
                Vector off = offset_points.get(i); // point on the outside
                Vector next_off = offset_points.get(i + 1); // next point (outside)
                Vector next_point = points.get(i + 1); // next point (inside)

                gl.glNormal3d(0, 0, 1); // upwards pointing normal
                gl.glTexCoord2f(0, 0);
                gl.glVertex3d(point.x(), point.y(), point.z());
                gl.glTexCoord2f(1, 0);
                gl.glVertex3d(off.x(), off.y(), off.z());
                gl.glTexCoord2f(1, 1);
                gl.glVertex3d(next_off.x(), next_off.y(), next_off.z());
                gl.glTexCoord2f(0, 1);
                gl.glVertex3d(next_point.x(), next_point.y(), next_point.z());
            }
            gl.glEnd();
            track.disable(gl);

            brick.enable(gl);
            brick.bind(gl);
            // Draw the sides of the track.
            gl.glBegin(GL_QUADS);
            for (int i = 0; i < N; i++) {
                // TODO: vertically split up quads
                // Draw inside of the track.
                Vector normal = normals.get(i).scale(-1); // use reverse normal
                gl.glNormal3d(normal.x(), normal.y(), normal.z());

                // Draw quad spanning between two points between minHeight, maxHeight.
                Vector point = points.get(i); // point on inside of the track
                Vector next_point = points.get(i + 1);
                gl.glTexCoord2f(0, 0);
                gl.glVertex3d(point.x(), point.y(), maxHeight);
                gl.glTexCoord2f(1, 0);
                gl.glVertex3d(next_point.x(), next_point.y(), maxHeight);
                gl.glTexCoord2f(1, 1);
                gl.glVertex3d(next_point.x(), next_point.y(), minHeight);
                gl.glTexCoord2f(0, 1);
                gl.glVertex3d(point.x(), point.y(), minHeight);

                // Draw outside of the track.
                normal = normals.get(i);
                gl.glNormal3d(normal.x(), normal.y(), normal.z());
                // Draw quad spanning between two points between minHeight, maxHeight.
                point = offset_points.get(i); // point on outside of the track
                next_point = offset_points.get(i + 1);
                gl.glTexCoord2f(0, 0);
                gl.glVertex3d(point.x(), point.y(), maxHeight);
                gl.glTexCoord2f(1, 0);
                gl.glVertex3d(next_point.x(), next_point.y(), maxHeight);
                gl.glTexCoord2f(1, 1);
                gl.glVertex3d(next_point.x(), next_point.y(), minHeight);
                gl.glTexCoord2f(0, 1);
                gl.glVertex3d(point.x(), point.y(), minHeight);
            }

            gl.glEnd();
            brick.disable(gl);
        }
    }

    /**
     * Interface that represents a curve.
     */
    public interface Curve {

        /**
         * Converts a given parameter into a point on the curve.
         *
         * @param t A parameter in the range 0 to 1.
         * @return A vector representing the point resulting from the
         * conversion.
         */
        public Vector getPoint(double t);

        /**
         * Returns the tangent of a given parameter with the curve.
         *
         * @param t A parameter in the range 0 to 1.
         * @return A vector representing the tangent at getPoint(t).
         */
        public Vector getTangent(double t);

        /**
         * Returns the normal of a given parameter with the curve.
         *
         * @param t A parameter in the range 0 to 1.
         * @return A vector representing the normal between t and the curve.
         */
        public Vector getNormalVector(double t);
    }

    /**
     * Simple implementation of Curve specifying an ellipse.
     */
    public static class SimpleCurve implements Curve {

        @Override
        public Vector getPoint(double t) {
            double x, y, z; // x,y,z coordinates as defined in the assignment
            x = 10 * cos(2 * PI * t);
            y = 14 * sin(2 * PI * t);
            z = 1;
            return new Vector(x, y, z);
        }

        @Override
        public Vector getTangent(double t) {
            double x, y, z; // x,y,z coordinates as defined in the assignment
            x = -20 * PI * sin(2 * PI * t);
            y = 28 * PI * cos(2 * PI * t);
            z = 0;
            return new Vector(x, y, z);
        }

        @Override
        public Vector getNormalVector(double t) {
            Vector tangent = this.getTangent(t);
            // Rotate 90 degrees in negative direction (outward) in XOY plane.
            return new Vector(tangent.y(), -tangent.x(), 0);
        }
    }

    /**
     * Implementation of Curve that models a Bezier curve.
     */
    public static class BezierCurve implements Curve {

        final private Vector P0, P1, P2, P3;

        public BezierCurve(Vector P0, Vector P1, Vector P2, Vector P3) {
            this.P0 = P0;
            this.P1 = P1;
            this.P2 = P2;
            this.P3 = P3;
        }

        @Override
        public Vector getPoint(double t) {
            return getCubicBezierPnt(t, P0, P1, P2, P3);
        }

        @Override
        public Vector getTangent(double t) {
            return getCubicBezierTng(t, P0, P1, P2, P3);
        }

        public static Vector getCubicBezierPnt(double t, Vector P0, Vector P1,
                Vector P2, Vector P3) {
            return P0.scale(pow(1 - t, 3)).add(
                    P1.scale(3 * t * pow(1 - t, 2))).add(
                    P2.scale(3 * pow(t, 2) * (1 - t))).add(
                    P3.scale(pow(t, 3)));

        }

        public static Vector getCubicBezierTng(double t, Vector P0, Vector P1,
                Vector P2, Vector P3) {
            return P1.subtract(P0).scale(pow(1 - t, 2)).add(
                    P2.subtract(P1).scale(2 * t * (1 - t))).add(
                    P3.subtract(P2).scale(pow(t, 2))).scale(3);
            /*
             Vector s1 = P0.scale((t-1)*(t-1));
             Vector s2 = P1.scale(-3 * t * t + 4 * t - 1);
             Vector s3 = P2.scale(3 * t).subtract(P2.scale(-2)).subtract(P3.scale(t)).scale(t);
             Vector s = s1.add(s2).add(s3);
             return s.scale(-3);*/
        }

        @Override
        public Vector getNormalVector(double t) {
            Vector tangent = this.getTangent(t);
            // Rotate 90 degrees in negative direction (outward) in XOY plane.
            // TODO: check if minus sign is in the correct place
            return new Vector(-tangent.y(), tangent.x(), 0);
        }
    }

    /**
     * Implementation of Curve that models a Bezier curve.
     */
    public static class BezierCurve2 implements Curve {

        final private Vector[] P;
        double nsegments;

        public BezierCurve2(Vector... points) {
            this.P = points;
            nsegments = (points.length - 1) / 3;
        }

        @Override
        public Vector getPoint(double t) {
            t = t % 1;
            double s = (t * nsegments) % 1;
            int i = 3 * (int) (t / (double) (1 / nsegments));
            return BezierCurve.getCubicBezierPnt(s, P[i], P[i + 1], P[i + 2], P[i + 3]);
        }

        @Override
        public Vector getTangent(double t) {
            t = t % 1;
            double s = (t * nsegments) % 1;
            int i = 3 * (int) (t / (1 / (double) nsegments));
            return BezierCurve.getCubicBezierTng(s, P[i], P[i + 1], P[i + 2], P[i + 3]);
        }

        @Override
        public Vector getNormalVector(double t) {
            Vector tangent = this.getTangent(t);
            // Rotate 90 degrees in negative direction (outward) in XOY plane.
            // TODO: check if minus sign is in the correct place
            return new Vector(-tangent.y(), tangent.x(), 0);
        }
    }

    /**
     * Class representing a matrix.
     */
    public static class Matrix {

        double[][] numbers; // stores the numbers in the matrix

        /**
         * Construct a matrix.
         *
         * @param numbers numbers in the matrix
         */
        public Matrix(double[][] numbers) {
            this.numbers = numbers;
        }

        /**
         * Returns the result of the matrix-vector multiplication of
         * {@code this} and {@code v}.
         *
         * @param v the vector
         * @return the result of the multiplication
         */
        public Vector times(Vector v) {
            Vector h1 = new Vector(numbers[0][0], numbers[0][1], numbers[0][2]);
            double x = v.dot(h1); // dot product of first row and vector v

            Vector h2 = new Vector(numbers[1][0], numbers[1][1], numbers[1][2]);
            double y = v.dot(h2); // dot product of second row and vector v

            Vector h3 = new Vector(numbers[2][0], numbers[2][1], numbers[2][2]);
            double z = v.dot(h3); // dot product of third row and vector v

            return new Vector(x, y, z);
        }

        /**
         * Calculates the transposed matrix, that is, a matrix with the indices
         * reversed.
         *
         * @return the transposed matrix
         */
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

    /**
     * Calculates a transformation matrix that rotates over an angle {@code phi}
     * around the Z axis and then over an angle {@code theta} around the y axis.
     *
     * @param phi phi angle mentioned above
     * @param theta theta angle mentioned above
     * @return the transformation matrix
     */
    public static Matrix eyeToWorldMatrix(float phi, float theta) {
        double[][] worldToEyeMatrix = {
            {cos(phi) * cos(theta), -sin(phi), -cos(phi) * sin(theta)},
            {sin(phi) * cos(theta), cos(phi), -sin(phi) * sin(theta)},
            {sin(theta), 0, cos(theta)}
        };
        return new Matrix(worldToEyeMatrix);
    }

    public class Terrain {

        private Set<Bump> bumps;
        private Vector[][] points = new Vector[M][N];
        final static private float MIN = -20;
        final static private float MAX = 20;
        final static private int M = 200; // number of lines in x direction
        final static private int N = 200; // number of lines in y direction

        public Terrain(Bump... bumps) {
            this.bumps = new HashSet(Arrays.asList(bumps));
        }

        public double z(double x, double y) {
            float sum = 0;
            for (Bump b : bumps) {
                sum += b.summand(x, y);
            }
            return sum;
        }

        private void precalculate() {
            float l = MAX - MIN;
            float w = l / (float) M;
            float h = l / (float) N;

            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    double x = MIN + i * w;
                    double y = MIN + j * h;
                    double z = z(x, y);
                    points[i][j] = new Vector(x, y, z);
                }
            }
        }

        public void draw() {
            if (points[0][0] == null) {
                precalculate();
            }

            setMaterial(Material.WHITE);
            landscape.enable(gl);
            landscape.bind(gl);

            gl.glPushMatrix();
            gl.glBegin(GL_TRIANGLES);
            for (int i = 0; i < M - 1; i++) {
                for (int j = 0; j < N - 1; j++) {
                    Vector bl = points[i][j];
                    Vector br = points[i + 1][j];
                    Vector ur = points[i + 1][j + 1];
                    Vector ul = points[i][j + 1];

                    Vector diag = br.subtract(ul);
                    Vector down = bl.subtract(ul);
                    Vector right = ur.subtract(ul);

                    Vector normal1 = down.cross(diag);
                    Vector normal2 = diag.cross(right);


                    gl.glNormal3d(normal1.x(), normal1.y(), normal1.z());
                    gl.glTexCoord1d(textureCoord(bl.z()));
                    glVertex(bl);
                    gl.glTexCoord1d(textureCoord(br.z()));
                    glVertex(br);
                    gl.glTexCoord1d(textureCoord(ul.z()));
                    glVertex(ul);

                    gl.glNormal3d(normal2.x(), normal2.y(), normal2.z());
                    gl.glTexCoord1d(textureCoord(br.z()));
                    glVertex(br);
                    gl.glTexCoord1d(textureCoord(ur.z()));
                    glVertex(ur);
                    gl.glTexCoord1d(textureCoord(ul.z()));
                    glVertex(ul);
                }
            }
            gl.glEnd();
            gl.glPopMatrix();

            landscape.disable(gl);

            drawWater();
        }

        private void drawWater() {
            setMaterial(Material.WATER_SURFACE);
            gl.glBegin(GL_QUADS);
            gl.glVertex3d(-20, -20, -0.01);
            gl.glVertex3d(-20, 20, -0.01);
            gl.glVertex3d(20, 20, -0.01);
            gl.glVertex3d(20, -20, -0.01);
            gl.glEnd();
        }

        private double textureCoord(double z) {
            return (z + 1) / 2;
        }
    }

    public static class Bump {

        private double center_x;
        private double center_y;
        private double height;
        private double radius;

        public Bump(double center_x, double center_y, double height, double radius) {
            this.center_x = center_x;
            this.center_y = center_y;
            this.height = height;
            this.radius = radius;
        }

        double summand(double x, double y) {
            double r = (sqrt((x - center_x) * (x - center_x) + (y - center_y) * (y - center_y)) / radius);
            return height * bump(r);
        }

        double bump(double r) {
            return ((r < 1) ? pow(cos(0.5 * PI * r), 2) : 0);
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
