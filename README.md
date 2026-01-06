# K-Means Animation

This is an animation showing how the K-means algorithm works. You can
place new "means" by clicking.

Hitting "3" adjusts each point to be associated to the nearest "mean",
and then moves the "means" to the center of its group. "D" increases
the number of points visible on the screen, which shows the regions
pretty well.

## Controls

* `1`: Recalculate the group for each point. (Recolors the points.)
* `2`: Recalculate the centroids for each group. (Moves the centers.)
* `3`: Combines both 1 and 2, for easy slow stepping.
* `R`: Re-randomizes all of the data.
* `+`: Adds another group with a random center. (Does not recalculate.)
* `-`: Removes a group. (Does not recalculate; points in that group turn white.)
* `D`: Add 50 more random points.
* `A`: Remove the last 50 points placed.
   The window is 800x800 by default, because that looks good on my screen.
* `Esc`: Quit.
* Clicking the mouse places the center for another group and recalculates all groups.

## Running the Code

If you are lucky, `gradlew run` will download and install everything
you need.

## Example output

Four regions and 250 points.

![Image of k-means with four regions with 250 points](screenshots/pic-6.png)

## Javascript Translation

One shot Gemini translation.

<div id="controls">
 1: Recalculate Groups | 2: Recalculate Centroids | 3: Both<br>
 R: Re-randomize | +: Add Group | -: Remove Group<br>
 D: Add Points | A: Remove Points | Click: Place Centroid
</div>
<canvas id="canvas"></canvas>

<script>
class CentroidAnimation {
    constructor(canvasId, ngroups, colorPalette) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');

        this.width = window.innerWidth;
        this.height = window.innerHeight;
        this.colorPalette = colorPalette;
        this.ngroups = ngroups;

        this.CIRCLE_SIZE = 6;
        this.NUM_POINTS_MINIMUM = 100;
        this.NUM_POINTS_DELTA = 50;

        this.points = [];
        this.groups = [];
        this.centroids = [];

        this.init();
        this.setupListeners();
        this.loop();
    }

    init() {
        this.resize();
        this.points = this.generateRandomPoints(this.NUM_POINTS_MINIMUM);
        this.centroids = this.generateRandomPoints(this.ngroups);
        this.groups = new Array(this.points.length).fill(0);
    }

    resize() {
        this.width = window.innerWidth;
        this.height = window.innerHeight;
        this.canvas.width = this.width;
        this.canvas.height = this.height;
    }

    // --- MATH LOGIC ---

    distance(p, q) {
        return Math.sqrt((p[0] - q[0]) ** 2 + (p[1] - q[1]) ** 2);
    }

    closestIndex(p) {
        let best = 0;
        for (let k = 1; k < this.centroids.length; k++) {
            if (this.distance(p, this.centroids[k]) < this.distance(p, this.centroids[best])) {
                best = k;
            }
        }
        return best;
    }

    recalculateGroups() {
        this.groups = this.points.map(p => this.closestIndex(p));
    }

    recalculateCentroids() {
        let totals = Array.from({ length: this.ngroups }, () => [0, 0]);
        let counts = new Array(this.ngroups).fill(0);

        for (let i = 0; i < this.points.length; i++) {
            let g = this.groups[i];
            counts[g]++;
            totals[g][0] += this.points[i][0];
            totals[g][1] += this.points[i][1];
        }

        this.centroids = totals.map((t, i) => {
            return counts[i] > 0 ? [t[0] / counts[i], t[1] / counts[i]] : this.centroids[i];
        });
    }

    // --- INTERACTION ---

    addGroup(x, y) {
        this.ngroups++;
        if (this.ngroups > this.colorPalette.length) {
            this.colorPalette.push(`rgb(${Math.random()*255},${Math.random()*255},${Math.random()*255})`);
        }
        const newPos = (x !== undefined) ? [x, y] : this.generateRandomPoints(1)[0];
        this.centroids.push(newPos);
        this.recalculateGroups();
    }

    removeGroup() {
        if (this.ngroups > 1) {
            this.ngroups--;
            this.centroids.pop();
            this.recalculateGroups();
        }
    }

    setupListeners() {
        window.addEventListener('resize', () => this.resize());

        window.addEventListener('keydown', (e) => {
            switch(e.key) {
                case '1': this.recalculateGroups(); break;
                case '2': this.recalculateCentroids(); break;
                case '3': this.recalculateGroups(); this.recalculateCentroids(); break;
                case 'r': case 'R': this.init(); break;
                case '+': case '=': this.addGroup(); break;
                case '-': case '_': this.removeGroup(); break;
                case 'd': case 'D':
                    this.points.push(...this.generateRandomPoints(this.NUM_POINTS_DELTA));
                    this.recalculateGroups();
                    break;
                case 'a': case 'A':
                    this.points = this.points.slice(0, Math.max(this.NUM_POINTS_MINIMUM, this.points.length - this.NUM_POINTS_DELTA));
                    this.recalculateGroups();
                    break;
            }
        });

        this.canvas.addEventListener('mousedown', (e) => {
            this.addGroup(e.clientX, e.clientY);
        });
    }

    // --- RENDERING ---

    generateRandomPoints(count) {
        return Array.from({ length: count }, () => [
            Math.random() * this.width,
            Math.random() * this.height
        ]);
    }

    drawCircle(x, y, radius, color) {
        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.arc(x, y, radius, 0, Math.PI * 2);
        this.ctx.fill();
    }

    loop() {
        this.ctx.clearRect(0, 0, this.width, this.height);

        // Draw Points
        for (let i = 0; i < this.points.length; i++) {
            const color = this.colorPalette[this.groups[i]] || 'white';
            this.drawCircle(this.points[i][0], this.points[i][1], this.CIRCLE_SIZE, color);
        }

        // Draw Centroids
        for (let i = 0; i < this.ngroups; i++) {
            this.drawCircle(this.centroids[i][0], this.centroids[i][1], this.CIRCLE_SIZE * 1.5, 'white');
            // Border for visibility
            this.ctx.strokeStyle = 'black';
            this.ctx.stroke();
        }

        requestAnimationFrame(() => this.loop());
    }
}

// Start the app
const initialColors = ['#ff0000', '#00ff00', '#0000ff', '#ffff00'];
new CentroidAnimation('canvas', 3, initialColors);
</script>
