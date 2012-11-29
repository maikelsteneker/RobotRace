
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

    double fovy = -1;
    Robot[] robots;
    final private static int NUMROBOTS = 500;
    Vector old_eye = new Vector(0, 0, 0);
    Vector eye;// = new Vector(0, 0, 0);

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
        final float AR = gs.w / (float) gs.h;
        if (gs.persp) {
            if (fovy == -1) {
                fovy = 2 * atan2((gs.vWidth / AR) / 2, gs.vDist);
            }
            glu.gluPerspective(toDegrees(fovy), AR, 0.1, 1000);
        } else {
            //TODO: center point
            float height = gs.vWidth / AR;
            gl.glOrtho(-0.5 * gs.vWidth, 0.5 * gs.vWidth, -0.5 * height, 0.5 * height, 0.1, 1000);
        }


        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();


        Vector dir = new Vector(cos(gs.phi) * cos(gs.theta),
                sin(gs.phi) * cos(gs.theta),
                sin(gs.theta));

        eye = gs.cnt.add(dir.scale(gs.vDist));
        if (gs.lightCamera) {
            old_eye = new Vector(eye.x(), eye.y(), eye.z());
        }

        glu.gluLookAt(eye.x(), eye.y(), eye.z(), // eye point
                gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), // center point
                0.0, 0.0, 1.0);   // up axis

        // Enable lighting (2.1)
       // gl.glEnable(GL_LIGHTING); //enable lighting (lighting influences color)
        //gl.glEnable(GL_LIGHT0); //enable light source 0
        //gl.glLoadIdentity();
        //Vector camera = eye.subtract(eye).add(old_eye);
        //float[] location = {(float) camera.x(), (float) camera.y(), (float) camera.z()};
        //gl.glLightfv(GL_LIGHT0, GL_POSITION, location, 0); //set location of ls0
        //gl.glEnable(GL_COLOR_MATERIAL); //enable materials (material influences color)
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
        //drawGrid();

        // Axis Frame
        drawAxisFrame();

        //draw robots
        /*
         * gl.glPushMatrix(); gl.glTranslatef(-NUMROBOTS / 2, 0, 0); for (Robot
         * r : robots) { gl.glTranslatef(1.0f, 0, 0); r.draw(); }
         * gl.glPopMatrix();
         */

        gl.glPushMatrix();
        /*
         * //gold float[] ambient = {0.24725f, 0.1995f, 0.0745f, 1.0f}; float[]
         * diffuse = {0.75164f, 0.60648f, 0.22648f, 1.0f}; float[] specular =
         * {0.628281f, 0.555802f, 0.366065f, 1.0f}; //color float[] shininess =
         * {51.2f}; gl.glMaterialfv(GL_FRONT, GL_AMBIENT, ambient, 0);
         * gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse, 0);
         * gl.glMaterialfv(GL_FRONT, GL_SPECULAR, specular, 0);
         * gl.glMaterialfv(GL_FRONT, GL_SHININESS, shininess, 0);
         */
        gl.glScaled(2, 2, 2);
        robots[0].draw();
        gl.glPopMatrix();

        /*
         * gl.glPushMatrix(); gl.glTranslatef(1.0f, 0, 0); //plastic green
         * float[] ambient2 = {0.0f, 0.0f, 0.0f, 1.0f}; float[] diffuse2 =
         * {0.1f, 0.35f, 0.1f, 1.0f}; float[] specular2 = {0.45f, 0.55f, 0.45f,
         * 1.0f}; float[] shininess2 = {1};
         *
         * gl.glMaterialfv(GL_FRONT, GL_AMBIENT, ambient2, 0);
         * gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, diffuse2, 0);
         * gl.glMaterialfv(GL_FRONT, GL_SPECULAR, specular2, 0);
         * gl.glMaterialfv(GL_FRONT, GL_SHININESS, shininess2, 0);
         * robots[1].draw(); gl.glPopMatrix();
         */

        gl.glColor3f(1.0f, 0, 1.0f);
        /*
         * Vector horizontal =
         * gs.cnt.subtract(eye).cross(Vector.Z).normalized().scale(gs.vWidth /
         * 2); Vector p1 = gs.cnt.subtract(horizontal); Vector p2 =
         * gs.cnt.add(horizontal); gl.glBegin(GL_LINES); gl.glVertex3d(p1.x(),
         * p1.y(), p1.z()); gl.glVertex3d(p2.x(), p2.y(), p2.z()); gl.glEnd();
         */
    }

    public void drawArrow() {
        gl.glPushMatrix();

        gl.glTranslatef(0f, 0, 0.5f);
        gl.glScalef(0.01f, 0.01f, 1f);
        glut.glutSolidCube(0.9f);

        gl.glPopMatrix();
        gl.glPushMatrix();

        gl.glTranslatef(0f, 0f, 0.9f);
        glut.glutSolidCone(0.05, 0.1, 15, 2);

        gl.glPopMatrix();
    }

    public void drawAxisFrame() {
        //gl.glPushAttrib(GL_LIGHTING_BIT);
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

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    class Robot {
        //TODO: specify torso width (and possible arms/legs as well)

        boolean legDirection = false; //specifies if the leg is moving forward
        float yPos = 0; //specifies the y position of the robot
        float speed = 0.5f; //specifies the speed at which yPos is increased
        final static private float MAXANGLE = 20; //specifies the maximum angle
        //the arms and legs can turn
        HatPart hatPart; //object representing the hat of the robot
        HeadPart headPart; //object representing the head of the robot
        TorsoPart torsoPart; //objedbmsct representing the torso of the robot
        ArmsPart arms; //object representing the arms of the robot
        LegsPart legs; //object representing the legs of the robot
        Set<RobotPart> parts; //set containing all components which are drawn

        /**
         * Constructs a Robot with some default dimensions.
         */
        public Robot() {
            this(0.5f, 0.5f, 0.75f, 0.5f, 0.5f, 0.85f, 0.75f, 0.1f, 0.1f);
        }

        public Robot(float hatSize, float headSize, float torsoHeight, float torsoWidth, float torsoThickness, float armsLength, float legsLength, float legsWidth, float legsThickness) {
            parts = new HashSet<RobotPart>();

            //construct all parts with the given parameters
            hatPart = new HatPart(hatSize);
            headPart = new HeadPart(headSize);
            torsoPart = new TorsoPart(torsoHeight, torsoWidth, torsoThickness);
            arms = new ArmsPart(armsLength);
            legs = new LegsPart(legsLength, legsWidth, legsThickness);

            //add all parts that need to be displayed to the parts set
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
            gl.glPushMatrix(); //push the current matrix onto the stack

            //move in y position
            gl.glTranslatef(0, yPos, 0);

            //draw parts
             gl.glColor3f(218f / 255f, 165f / 255f, 32f / 255f);
            for (RobotPart p : parts) { //for all parts that should be drawn
                p.draw(); //draw the part
            }

            handleMovement();

            gl.glPopMatrix(); //pop the matrix from the stack
        }

        /**
         * Moves the robot and turns the arms and legs by updating
         * {@code legDirection}, {@code legs.angle} and {@code yPos} based on
         * {@code legDirection}, {@code speed} and  {@code MAXANGLE}.
         */
        private void handleMovement() {
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
         * Represents the legs of a robot.
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
             * @param length length of the legs
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

            boolean left; //specifies whether this is the left leg
            LegsPart parent; //reference to the LegsPart object this leg is part of
            int s; //sign of the angle to rotate over
            
            /**
             * Constructs a LegPart object.
             *
             * @param left specifies whether this is the left leg
             * @param parent reference to the LegsPart object
             */
            public LegPart(boolean left, LegsPart parent) {
                this.left = left;
                this.parent = parent;
                s = left ? 1 : -1;
            }
            private void drawHip() {
                 if (!gs.showStick) {
                                
                gl.glPushMatrix();
                    gl.glTranslatef(s * 0.5f* (torsoPart.width), 0, parent.getHeight()-parent.width/3);
                    gl.glRotatef(s * -90, 0, 1, 0);
                    gl.glScalef(parent.width, parent.thickness, parent.width);
                    glut.glutSolidCylinder(0.5, 1, 20, 10);
                    gl.glPopMatrix();}
            
            }
            
            private void drawUpperLeg() {
                if (gs.showStick) {
                    //draw a line from the end of the leg to the base of the torso
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); //base of the torso
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length - getHeight() * getHeight()), 0); //end of the leg
                    gl.glEnd();
                } else {
                    //draw a rectangular prism
                    gl.glPushMatrix();
                    //translate to the height of the leg
                    gl.glTranslated(0, 0, getHeight());
                    gl.glRotatef(s * parent.angle, 1, 0, 0); //rotate around x axis over specified angle
                    //translate to half of the height of the leg (vertically)
                    //  and the edge of the torso (horizontally)
                    gl.glTranslatef(s * (0.5f * torsoPart.width - 0.5f * parent.width), 0, (-0.25f * getHeight()) - parent.width/2f);
                    gl.glPushMatrix();
                    //scale a unit cube to the correct dimensions
                    gl.glScalef(parent.width, parent.thickness, 0.5f * parent.length);
                    glut.glutSolidCube(1);
                    gl.glPopMatrix();
                }
            }
            
            private void drawKnee() {
                                if (!gs.showStick) {
                                
                gl.glPushMatrix();
                    gl.glTranslatef(s * 0.5f * parent.width, 0, -0.25f * parent.length);
                    gl.glRotatef(s * -90, 0, 1, 0);
                    gl.glScalef(parent.width, parent.thickness, parent.width);
                    glut.glutSolidCylinder(0.5, 1, 20, 10);
                    gl.glPopMatrix();}
            }
            
            private void drawLowerLeg() {
                                if (gs.showStick) {
                                    //draw a line from the end of the leg to the base of the torso
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); //base of the torso
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length - getHeight() * getHeight()), 0); //end of the leg
                    gl.glEnd();
                                } else {
                gl.glTranslatef(0, 0, -0.25f * parent.length);
                    gl.glRotatef(s * -parent.angle, 1, 0, 0);
                    gl.glTranslatef(0, 0, -0.25f * parent.length);
                    gl.glScalef(parent.width, parent.thickness, 0.5f * parent.length);
                    glut.glutSolidCube(1);
                    //glut.glutSolidCube(1);
                    /*
                     * gl.glTranslatef(s * 0.5f * (torsoPart.width), 0, 0.5f *
                     * getHeight()); gl.glRotatef(-s * 90, 0, 1, 0);
                     * gl.glScalef(parent.width, parent.thickness, parent.width
                     * / 2); glut.glutSolidCylinder(0.5, 1, 20, 10);
                     * //glut.glutSolidCube(0.5f);
                     */

                    gl.glPopMatrix();}
            }

            /**
             * Draws this Leg.
             */
            @Override
            public void draw() {
                int s = left ? 1 : -1; //sign of the angle to rotate over
                //and the x-distance to translate over
                drawHip();
                drawUpperLeg();
                drawKnee();
                drawLowerLeg();
                /*
                if (gs.showStick) {
                    //draw a line from the end of the leg to the base of the torso
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); //base of the torso
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length - getHeight() * getHeight()), 0); //end of the leg
                    gl.glEnd();
                } else {
                    //draw a rectangular prism
                    gl.glPushMatrix();
                    //translate to the height of the leg
                    gl.glTranslated(0, 0, getHeight());
                    gl.glRotatef(s * parent.angle, 1, 0, 0); //rotate around x axis over specified angle
                    //translate to half of the height of the leg (vertically)
                    //  and the edge of the torso (horizontally)
                    gl.glTranslatef(s * 0.5f * (torsoPart.width - 0.5f * parent.width), 0, -0.25f * getHeight());
                    gl.glPushMatrix();
                    //scale a unit cube to the correct dimensions
                    gl.glScalef(parent.width, parent.thickness, 0.5f * parent.length);
                    glut.glutSolidCube(1);
                    //gl.glPopMatrix();

                    //gl.glPushMatrix();

                    //gl.glTranslatef(s * 0.5f, 0, 3f);

                    //translate down the leg
                    gl.glPopMatrix();
                    gl.glPushMatrix();
                    gl.glTranslatef(s * 0.5f * parent.width, 0, -0.25f * parent.length);
                    gl.glRotatef(s * -90, 0, 1, 0);
                    gl.glScalef(parent.width, parent.thickness, 0.5f * parent.width);
                    glut.glutSolidCylinder(0.5, 2, 20, 10);
                    gl.glPopMatrix();
                    
                    gl.glTranslatef(0, 0, -0.25f * parent.length);
                    gl.glRotatef(s * -parent.angle, 1, 0, 0);
                    gl.glTranslatef(0, 0, -0.25f * parent.length);
                    gl.glScalef(parent.width, parent.thickness, 0.5f * parent.length);
                    glut.glutSolidCube(1);
                    //glut.glutSolidCube(1);
                    /*
                     * gl.glTranslatef(s * 0.5f * (torsoPart.width), 0, 0.5f *
                     * getHeight()); gl.glRotatef(-s * 90, 0, 1, 0);
                     * gl.glScalef(parent.width, parent.thickness, parent.width
                     * / 2); glut.glutSolidCylinder(0.5, 1, 20, 10);
                     * //glut.glutSolidCube(0.5f);
                     *

                    gl.glPopMatrix();

                }*/
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
         * Represents a leg of the robot.
         */
        public class SimpleLegPart implements RobotPart {

            boolean left; //specifies whether this is the left leg
            LegsPart parent; //reference to the LegsPart object this leg is part of

            /**
             * Constructs a LegPart object.
             *
             * @param left specifies whether this is the left leg
             * @param parent reference to the LegsPart object
             */
            public SimpleLegPart(boolean left, LegsPart parent) {
                this.left = left;
                this.parent = parent;
            }

            /**
             * Draws this Leg.
             */
            @Override
            public void draw() {
                int s = left ? 1 : -1; //sign of the angle to rotate over
                //and the x-distance to translate over
                if (gs.showStick) {
                    //draw a line from the end of the leg to the base of the torso
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, getHeight()); //base of the torso
                    gl.glVertex3d(0, s * sqrt(parent.length * parent.length - getHeight() * getHeight()), 0); //end of the leg
                    gl.glEnd();
                } else {
                    //draw a rectangular prism
                    gl.glPushMatrix();
                    //translate to the height of the leg
                    gl.glTranslated(0, 0, getHeight());
                    gl.glRotatef(s * parent.angle, 1, 0, 0); //rotate around x axis over specified angle
                    //translate to half of the height of the leg (vertically)
                    //  and the edge of the torso (horizontally)
                    gl.glTranslatef(s * 0.5f * (torsoPart.width - 0.5f * parent.width), 0, -0.5f * getHeight());
                    //scale a unit cube to the correct dimensions
                    gl.glScalef(parent.width, parent.thickness, parent.length);
                    glut.glutSolidCube(1);
                    gl.glPopMatrix();
                }
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

            ArmPart leftArm; //object for left arm
            ArmPart rightArm; //object for right arm
            float length; //length of the arms

            /**
             * Constructs an ArmsPart object.
             *
             * @param length length of the arms
             */
            public ArmsPart(float length) {
                leftArm = new ArmPart(true, this);
                rightArm = new ArmPart(false, this);
                this.length = length;
            }

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

        public class ArmPart implements RobotPart {

            boolean left;
            ArmsPart parent;

            public ArmPart(boolean left, ArmsPart parent) {
                this.left = left;
                this.parent = parent;
            }

            @Override
            public void draw() {
                int s = left ? 1 : -1;
                if (gs.showStick) {
                    gl.glPushMatrix();
                    gl.glTranslated(0, 0, torsoPart.getHeight());
                    gl.glRotatef(s * -legs.angle, 1, 0, 0);
                    gl.glTranslatef(0, 0, -0.5f * parent.length);
                    gl.glBegin(GL_LINES);
                    gl.glVertex3d(0, 0, -0.5 * parent.length);
                    gl.glVertex3d(0, 0, 0.5 * parent.length);
                    gl.glEnd();
                    gl.glPopMatrix();
                } else {
                    gl.glPushMatrix();
                    gl.glTranslated(0, 0, torsoPart.getHeight());
                    gl.glRotatef(s * -legs.angle, 1, 0, 0);
                    gl.glTranslatef(s * 0.30f, 0, -0.5f * parent.length);
                    gl.glScalef(1.0f, 1.0f, parent.length * 10);
                    glut.glutSolidCube(0.1f);
                    gl.glPopMatrix();
                }
            }

            @Override
            public float getHeight() {
                return parent.getHeight();
            }
        }

        public class TorsoPart implements RobotPart {

            float height;
            float width;
            float thickness;

            public TorsoPart(float height, float width, float thickness) {
                this.height = height;
                this.width = width;
                this.thickness = thickness;
            }

            @Override
            public void draw() {
                if (gs.showStick) {
                    gl.glPushMatrix();
                    gl.glBegin(GL_LINES);
                    gl.glVertex3f(0, 0, legs.getHeight());
                    gl.glVertex3f(0, 0, legs.getHeight() + height);
                    gl.glEnd();
                    gl.glPopMatrix();
                } else {
                    gl.glPushMatrix();
                    gl.glTranslated(0, 0, 0.5f * height + legs.getHeight());
                    gl.glScaled(width, thickness, height);
                    glut.glutSolidCube(1f);
                    gl.glPopMatrix();
                }
            }

            @Override
            public float getHeight() {
                return legs.getHeight() + height;
            }
        }

        public class HeadPart implements RobotPart {

            float height;

            public HeadPart(float height) {
                this.height = height;
            }

            @Override
            public void draw() {
                if (gs.showStick) {
                    float angle;
                    float x = 0;
                    float y = (0.5f * height + torsoPart.getHeight());
                    float radius = 0.5f * height;
                    gl.glBegin(GL_LINE_LOOP);
                    for (int i = 0; i < 100; i++) {
                        angle = (float) (i * 2 * PI / 100);
                        gl.glVertex3d(x + (cos(angle) * radius), 0, y + (sin(angle) * radius));
                    }
                    gl.glEnd();
                } else {
                    gl.glPushMatrix();
                    gl.glTranslated(0, 0, 0.5f * height + torsoPart.getHeight());
                    glut.glutSolidSphere(0.5f * height, 10, 10);
                    gl.glPopMatrix();
                }
            }

            @Override
            public float getHeight() {
                return torsoPart.getHeight() + height;
            }
        }

        public class HatPart implements RobotPart {

            float height;

            public HatPart(float height) {
                this.height = height;
            }

            @Override
            public void draw() {
                if (gs.showStick) {
                    gl.glBegin(GL_LINE_LOOP);
                    gl.glVertex3f(0, 0, this.getHeight());
                    gl.glVertex3f(-0.5f * height, 0, headPart.getHeight());
                    gl.glVertex3f(0.5f * height, 0, headPart.getHeight());
                    gl.glEnd();
                } else {
                    gl.glPushMatrix();
                    gl.glTranslated(0, 0, headPart.getHeight() - 0.1f);
                    glut.glutSolidCone(0.5f * height, height, 10, 10);
                    gl.glPopMatrix();
                }
            }

            @Override
            public float getHeight() {
                return headPart.getHeight() + height;
            }
        }
    }

    /**
     *
     */
    public interface RobotPart {
        //TODO: add color variable

        public void draw();

        public float getHeight();
    }

    /**
     * Main program execution body, delegates to an instance of the RobotRace
     * implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
    }
}