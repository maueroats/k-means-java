import java.util.Arrays;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class CentroidAnimation {

    private long window;
    private int width;
    private int height;
    private double[][] color_palette;

    private static final double CIRCLE_SIZE = 8.0;
    private static final int CIRCLE_SEGMENTS = 20;
    private static final int NUM_POINTS_MINUMUM = 100;
    private static final int NUM_POINTS_DELTA = 50;

    // AMO
    private double points[][]; // NPTS x 2
    private int ngroups;
    private int groups[]; // NPTS
    private double centroids[][]; // ngroups x 2

    private int groups(int k) {
        int g = groups[k];
        if (0 <= g && g < ngroups) {
            return g;
        } else {
            return 0;
        }
    }

    // Corrected Euclidean Distance
    private double distance (double p[], double q[]) {
        double dx = p[0] - q[0];
        double dy = p[1] - q[1];
        return Math.sqrt(dx*dx + dy*dy);
    }

    private int closest_index (double p[]) {
        int best = 0;
        for (int k=1; k < centroids.length; k++) {
            if (distance(p, centroids[k]) < distance(p, centroids[best])) {
                best = k;
            }
        }
        return best;
    }

    public int[] recalculate_groups() {
        int newgroups[] = new int[points.length];
        for (int n=0; n < points.length; n++) {
            newgroups[n] = closest_index(points[n]);
        }
        return newgroups;
    }

    private double[][] recalculate_centroids() {
        double totals[][] = new double[ngroups][2];
        int groupcount[] = new int[ngroups];

        // Loop over all points to find new average positions
        for(int n = 0; n < points.length; n++) {
            int g = groups(n);
            groupcount[g] += 1;
            totals[g][0] += points[n][0];
            totals[g][1] += points[n][1];
        }

        for (int n=0; n < ngroups; n++) {
            int c = groupcount[n];
            if (c != 0) {
                totals[n][0] /= c;
                totals[n][1] /= c;
            }
        }
        return totals;
    }

    public CentroidAnimation(int width, int height, int ngroups, double[][] color_palette) {
        this.width = width;
        this.height = height;
        this.ngroups = ngroups;
        this.color_palette = color_palette;

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        window = glfwCreateWindow(width, height, "Centroid Animation", NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Setup Keyboard Callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) {
                    this.groups = recalculate_groups();
                    System.out.println("Groups recalculated.");
                } else if (key == GLFW_KEY_2) {
                    this.centroids = recalculate_centroids();
                    System.out.println("Centroids recalculated.");
                } else if (key == GLFW_KEY_3) {
                    this.groups = recalculate_groups();
                    this.centroids = recalculate_centroids();
                } else if (key == GLFW_KEY_R) {
                    rerandomizeData();
                    System.out.println("Data re-randomized.");
                } else if (key == GLFW_KEY_EQUAL  && // PLUS key
                        ((mods & GLFW_MOD_SHIFT) != 0 || key == GLFW_KEY_KP_ADD)) {
                    add_group();
                    System.out.println("Group added, now ngroups = " + this.ngroups);
                } else if (key == GLFW_KEY_MINUS || key == GLFW_KEY_KP_SUBTRACT) {
                    remove_group();
                    System.out.println("Group removed, now ngroups = " + this.ngroups);
                } else if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, true);
                } else if (key == GLFW_KEY_D) {
                    addPoints(random_points(NUM_POINTS_DELTA));
                    System.out.println("Points: added "+NUM_POINTS_DELTA+", total is now " +this.points.length);
                } else if (key == GLFW_KEY_A) {
                    removePoints(NUM_POINTS_DELTA);
                    System.out.println("Points: removed "+NUM_POINTS_DELTA+", total is now " +this.points.length);
                }
            }});
        // Setup Mouse Callback
        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                // 1. Get current pixel coordinates of the mouse
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(windowHandle, xpos, ypos);

                // 2. Get the ACTUAL current window size from the OS
                int[] currentW = new int[1];
                int[] currentH = new int[1];
                glfwGetWindowSize(windowHandle, currentW, currentH);

                // 3. Map the mouse pixel to our fixed OpenGL coordinate system (0 to width)
                // This handles stretching/scaling automatically
                double mouseX = (xpos[0] / (double)currentW[0]) * this.width;
                double mouseY = (1.0 - (ypos[0] / (double)currentH[0])) * this.height;

                addCentroidAt(mouseX, mouseY);
                System.out.println("Centroid added at relative click: " + (int)mouseX + ", " + (int)mouseY);
            }
        });

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
    }

    // Setters for initial data
    public void setPoints(double[][] p) { this.points = p; this.groups = new int[p.length]; }
    public void addPoints(double[][] q) {
        double[][] new_points = new double[points.length + q.length][2];
        System.arraycopy(points, 0, new_points, 0, points.length);
        System.arraycopy(q, 0, new_points, points.length, q.length);
        this.points = new_points;
        this.groups = Arrays.copyOf(this.groups, new_points.length);
        this.groups = recalculate_groups();
    }
    public void removePoints(int how_many_to_remove) {
        int want = Math.max(NUM_POINTS_MINUMUM, points.length - how_many_to_remove);
        points = Arrays.copyOf(points, want);
        groups = Arrays.copyOf(groups, want);
    }
    public void setCentroids(double[][] c) { this.centroids = c; this.ngroups = c.length;}
    private void addCentroidAt(double x, double y) {
        add_group();

        // 2. Already expanded the Centroids array. Insert the new point.
        centroids[ngroups - 1] = new double[]{x, y};

        // Optional: Re-calculate groups immediately so points snap to the new centroid
        this.groups = recalculate_groups();
    }
    public void prepareFrame() {
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        glfwGetFramebufferSize(window, fbW, fbH);

        // 1. Update the class variables to the actual current size
        // Note: We cast to fields so the rest of your logic (like random_points)
        // uses the new boundaries immediately.
        this.width = fbW[0];
        this.height = fbH[0];

        // 2. Match the OpenGL viewport to the pixels
        glViewport(0, 0, fbW[0], fbH[0]);

        // 3. Re-set the projection so 1 unit = 1 pixel (No stretching!)
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, fbW[0], 0, fbH[0], -1, 1);
        glMatrixMode(GL_MODELVIEW);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
    }

    public void drawCurrentState() {
        // Draw the points colored by their group
        drawPoints(this.points, this.groups);
        // Draw centroids as slightly larger white circles to distinguish them
        for (int k=0; k<ngroups; k++) {
            double[] c = centroids[k];
            glColor3f(1.0f, 1.0f, 1.0f);
            drawCircle(c[0], c[1], CIRCLE_SIZE * 1.5);
        }
    }

    public void drawPoints(double[][] points, int[] color) {
        for (int k = 0; k < points.length; k++) {
            int v = color[k];
            if (0 <= v && v < ngroups /* color_palette.length */) {
                glColor3dv(color_palette[v]);
            } else {
                glColor3f(1.0f, 1.0f, 1.0f);
            }
            drawCircle(points[k][0], points[k][1], CIRCLE_SIZE);
        }
    }

    public void finalizeFrame() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    private void drawCircle(double x, double y, double radius) {
        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = i * 2.0 * Math.PI / CIRCLE_SEGMENTS;
            glVertex2d(x + (Math.cos(angle) * radius),
                    y + (Math.sin(angle) * radius));
        }
        glEnd();
    }

    public boolean shouldClose() { return glfwWindowShouldClose(window); }
    public void show() { glfwShowWindow(window); }
    public void cleanup() {
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public void loop() {
        while (!shouldClose()) {
            prepareFrame();
            drawCurrentState();
            finalizeFrame();
        }
    }


    public double[][] random_points(int how_many) {
        double [][]pts = new double[how_many][2];
        while(how_many > 0) {
            pts[how_many-1][0] = (int)(this.width * Math.random());
            pts[how_many-1][1] = (int)(this.height * Math.random());
            how_many -= 1;
        }
        return pts;
    }
    private void rerandomizeData() {
        // Generate new random points and centroids
        this.points = random_points(points.length);
        this.centroids = random_points(this.ngroups);

        // Initialize groups array to all zeros (or call recalculate_groups immediately)
        this.groups = new int[this.points.length];

    }

    private double[] random_color() {
        return new double[]{Math.random(), Math.random(), Math.random()};
    }

    private void add_group() {
        ngroups += 1;
        if (ngroups > color_palette.length) {
            double [][] new_colors = Arrays.copyOf(color_palette, ngroups);
            new_colors[ngroups-1] = random_color();
            color_palette = new_colors;
        }
        if (ngroups > centroids.length) {
            centroids = Arrays.copyOf(centroids, ngroups);
            double [][]pt = random_points(1);
            centroids[ngroups-1] = pt[0];
        }
    }

    private void remove_group() {
        if (this.ngroups > 1) {
            this.ngroups -= 1;
        }
    }

    public static void main(String[] args) {
        int ngroups = 3;

        double [][]rgb = {{1.0, 0.0, 0.0}, // Red
                {0.0, 1.0, 0.0}, // Green
                {0.0, 0.0, 1.0}, // Blue
                {1.0, 1.0, 0.0}, // Yellow
        };

        CentroidAnimation x = new CentroidAnimation(800,800,ngroups,rgb);

        x.setPoints(x.random_points(NUM_POINTS_MINUMUM));
        x.setCentroids(x.random_points(3));
        x.show();
        x.loop();
        x.cleanup();
    }
}