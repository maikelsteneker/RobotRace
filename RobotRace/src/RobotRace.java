
import java.awt.Color;
import javax.media.opengl.GL;
import static javax.media.opengl.GL2.*;
import robotrace.Base;
import robotrace.Vector;
import static java.lang.Math.*;
import java.util.HashSet;
import java.util.Set;
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
public class RobotRace extends Base {

    double fovy = -1; // vertical field of view angle
    Robot[] robots; // array to store drawable robots
    final private static int NUMROBOTS = 1; // size of robots array
    Vector eye; // current location of the camera

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
        gl.glEnable(GL_COLOR_MATERIAL);

        // Initialize robots array.
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
        // Calculate directional vector of the camera (view direction).
        Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                sin(gs.phi) * cos(gs.theta),
                sin(gs.theta));

        // Calculate position of the camera.
        eye = gs.cnt.add(dir.scale(gs.vDist));

        final float AR = gs.w / (float) gs.h; // aspect ratio
        float vHeight = gs.vWidth / AR; // height of scene to be shown
        Vector up = Vector.Z; // up vector

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
        glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
                gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                up.x(), up.y(), up.z()); // up axis
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

        // Draw robot.
        gl.glPushMatrix();
        gl.glScaled(2, 2, 2); // Scale, otherwise lighting is ugly
        robots[0].draw();
        gl.glPopMatrix();
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
            glut.glutSolidSphere(0.10f, 20, 20); // draw sphere

            // Draw arrow for X axis.
            gl.glPushMatrix();
            gl.glRotatef(90, 0, 1, 0); // rotate 90 degrees around y axis
            gl.glColor3f(1.0f, 0, 0); // set color to red
            drawArrow(1, 0.01f, 0.05f); // draw arrow
            gl.glPopMatrix();

            // Draw arrow for Y axis.
            gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0); // rotate -90 degrees around x axis
            gl.glColor3f(0, 1.0f, 0); // set color to green
            drawArrow(1, 0.01f, 0.05f); // draw arrow
            gl.glPopMatrix();

            // Draw arrow for Z axis.
            gl.glPushMatrix();
            gl.glColor3f(0, 0, 1.0f); // set color to blue
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

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    class Robot {

        boolean legDirection = false; // specifies if the leg is moving forward
        float yPos = 0; // specifies the y position of the robot
        float speed = 0.5f; // specifies the speed at which yPos is increased
        final static private float MAXANGLE = 20; // upper bound for legs.angle
        HatPart hatPart; // object representing the hat of the robot
        HeadPart headPart; // object representing the head of the robot
        TorsoPart torsoPart; // object representing the torso of the robot
        ArmsPart arms; // object representing the arms of the robot
        LegsPart legs; // object representing the legs of the robot
        Set<RobotPart> parts; // set containing all components which are drawn
        Color color; // color of this robot

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
                    0.1f // legsThickness
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
         */
        public Robot(float hatSize, float headSize, float torsoHeight,
                float torsoWidth, float torsoThickness, float armsLength,
                float armsWidth, float armsThickness, float legsLength,
                float legsWidth, float legsThickness) {
            this(hatSize, headSize, torsoHeight, torsoWidth, torsoThickness,
                    armsLength, armsWidth, armsThickness, legsLength,
                    legsWidth, legsThickness, new Color(191, 165, 32));
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
         * @param color color of this robot
         */
        public Robot(float hatSize, float headSize, float torsoHeight,
                float torsoWidth, float torsoThickness, float armsLength,
                float armsWidth, float armsThickness, float legsLength,
                float legsWidth, float legsThickness, Color color) {
            parts = new HashSet<RobotPart>(); // initialize parts set
            this.color = color;

            // Construct all parts with the given parameters.
            hatPart = new HatPart(hatSize);
            headPart = new HeadPart(headSize);
            torsoPart = new TorsoPart(torsoHeight, torsoWidth, torsoThickness);
            arms = new ArmsPart(armsLength, armsWidth, armsThickness);
            legs = new LegsPart(legsLength, legsWidth, legsThickness);

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
            gl.glPushMatrix();
            gl.glPushAttrib(GL_CURRENT_BIT);

            // Move in y position.
            gl.glTranslatef(0, yPos, 0);
            // Draw parts.
            gl.glColor3fv(color.getRGBComponents(null), 0); // set color
            for (RobotPart p : parts) { // for all parts that should be drawn:
                p.draw(); //draw the part
            }

            handleMovement();
            gl.glPopAttrib();
            gl.glPopMatrix();
        }

        /**
         * Moves the robot and turns the arms and legs by updating
         * {@code legDirection}, {@code legs.angle} and {@code yPos} based on
         * {@code legDirection}, {@code speed} and {@code MAXANGLE}.
         */
        private void handleMovement() {
            /*
             * We do not document this method in detail, since it is not
             * required for this assignment and will likely be replaced by a
             * more complex method later on.
             */
            if (legs.angle > MAXANGLE) {
                legDirection = false;
            } else if (legs.angle < -MAXANGLE) {
                legDirection = true;
            }

            if (legDirection) {
                legs.angle += speed;
            } else {
                legs.angle -= speed;
            }

            yPos += speed * 0.01;
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
                    glut.glutSolidCube(1f);
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
                    glut.glutSolidSphere(0.5f * height, 10, 10);
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

    public class Track {

        Curve curve;

        public Track(Curve curve) {
            this.curve = curve;
        }

        public void draw() {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    public interface Curve {

        public Vector getPoint(double t);

        public Vector getTangent(double t);
    }

    public static class SimpleTrackCurve implements Curve {

        @Override
        public Vector getPoint(double t) {
            double x, y, z;
            x = 10 * cos(2 * PI * t);
            y = 14 * sin(2 * PI * t);
            z = 1;
            return new Vector(x, y, z);
        }

        @Override
        public Vector getTangent(double t) {
            double x, y, z;
            x = -20 * PI * sin(2 * PI * t);
            y = 28 * PI * cos(2 * PI * t);
            z = 0;
            return new Vector(x, y, z);
        }
    }

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
            throw new UnsupportedOperationException("Not yet implemented");

        }

        public static Vector getCubicBezierTng(double t, Vector P0, Vector P1,
                Vector P2, Vector P3) {
            throw new UnsupportedOperationException("Not yet implemented");
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
