package lunch.g5;

import java.util.ArrayList;
import java.util.PriorityQueue;

import javafx.util.Pair;
import lunch.sim.Animal;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class DistractionUtilities {
    public static Matrix calculateMatrix(ArrayList<Point> monkeyPositions, PlayerState ps, Double dangerDistance) {
        // Initiate empty matrices for each timestep
        Point ourLocation = ps.get_location();
        // int ourLocationX = (int) Math.round(ourLocation.x);
        // int ourLocationY = (int) Math.round(ourLocation.y);

        Matrix matrix = new Matrix(101, 101);
        matrix.setOrigin(50, 50);

        // Go through each animal and 'draw' it's path on the matrices
        for (int i = 0; i < monkeyPositions.size(); i++) {
            Point curLocation = monkeyPositions.get(i);

            // Increase all counters in the radius of dangerDistance
            // We look in a square of dangetDistance around a monekey;
            int animalXFrom = (int) Math.floor(curLocation.x - dangerDistance);
            int animalXTo = (int) Math.ceil(curLocation.x + dangerDistance);
            int animalYFrom = (int) Math.floor(curLocation.y - dangerDistance);
            int animalYTo = (int) Math.ceil(curLocation.y + dangerDistance);

            for (int x = animalXFrom; x <= animalXTo; x++) {
                for (int y = animalYFrom; y <= animalYTo; y++) {
                    // If our matrix does not have these coordinates we continue
                    if (!matrix.has(x, y))
                        continue;

                    // We check if the distance is actually less than danger distance
                    double distance = Point.dist(curLocation, new Point(x, y));
                    if (distance > dangerDistance)
                        continue;
                    matrix.increment(x, y);
                }
            }
        }
        return matrix;
    }

    public static Integer countMonkeys(ArrayList<Point> monkeyPositions, Point ourLocation) {
        Integer numMonkeys = 0;
        for (Point p : monkeyPositions)
            if (Math.hypot(ourLocation.x - p.x, ourLocation.y - p.y) <= 6.0)
                numMonkeys++;
        return numMonkeys;
    }

    public static Integer getThirdMonkeyLocation(ArrayList<Point> monkeyPositions, Point ourLocation) {
        if (monkeyPositions.size() < 3)
            return 100;

        PriorityQueue<Double> dist = new PriorityQueue<Double>();
        for (Point pos : monkeyPositions)
            dist.offer(Math.hypot(ourLocation.x - pos.x, ourLocation.y - pos.y));

        dist.poll();
        dist.poll();

        return (int) Math.floor(dist.poll()) - 6;
    }

    public static Pair<Integer, Point> simulateWalk(ArrayList<Point> monkeyPositions, Point ourLocation) {
        Integer minX = 0, maxX = 40, minY = 0, maxY = 40;

        // Initiate empty matrices for each timestep
        Integer x = (int) Math.round(ourLocation.x);
        Integer y = (int) Math.round(ourLocation.y);

        Integer[] dxs = { 0, 0, -1, +1 };
        Integer[] dys = { -1, 1, 0, 0 };
        // Integer[] numSteps = { x + 30, 30 - x, y + 30, 30 - y };
        boolean[] valid = { x >= minX, x <= maxX, y >= minY, y <= maxY };

        Integer bestNumSteps = -1;
        Point bestDestination = null;

        for (int dir = 0; dir < 4; dir++) {
            if (!valid[dir])
                continue;

            // TODO: Simplify to simply select after steps numSteps
            Integer curSteps = 0;
            Integer numSteps = 0;

            if (dir == 0)
                numSteps = (y < minY) ? 0 : y - minY;
            else if (dir == 1)
                numSteps = (y > maxY) ? 0 : maxY - y;
            else if (dir == 2)
                numSteps = (x < minX) ? 0 : x - minX;
            else if (dir == 3)
                numSteps = (x > maxX) ? 0 : maxX - x;

            ArrayList<Point> newMonkeyPos = monkeyPositions;
            for (int steps = 0; steps < numSteps; steps++) {
                Integer curX = x + steps * dxs[dir];
                Integer curY = y + steps * dys[dir];

                Integer numMonkeys = DistractionUtilities.countMonkeys(newMonkeyPos, new Point(curX, curY));
                // Log.log(newMonkeyPos.toString());

                if (numMonkeys > 2)
                    break;

                newMonkeyPos = DistractionUtilities.predictFood(newMonkeyPos, new Point(curX, curY));
                curSteps = steps;
            }

            // Log.log(String.format("Direction = %d. Steps = [%d / %d]", dir, curSteps,
            // numSteps));

            if (curSteps > bestNumSteps) {
                bestNumSteps = curSteps;
                bestDestination = new Point(x + curSteps * dxs[dir], y + curSteps * dys[dir]);
            }
        }

        return new Pair<Integer, Point>(bestNumSteps, bestDestination);
    }

    /**
     * A function that predicts the future positions of animals. If the future
     * position is out of bounds of the field than instead of the Point the field
     * will have null.
     *
     * Example usage:
     *
     * if(turn > 1) { ArrayList<ArrayList<Point>> futurePositions =
     * DistractionUtilities.predict(previousAnimals, animals, 10);
     * System.out.println(futurePositions.get(9)); }
     *
     * @param previousAnimals: An array of previous animals in the field
     * @param animals:         An array of current animals in the field
     * @param nTimesteps:      Number of timesteps to predict
     * @return An array of arrays, where each inner array under index j is predicted
     *         animal positions in the field after j timesteps.
     */
    public static ArrayList<ArrayList<Point>> predictNotNull(ArrayList<Animal> previousAnimals,
            ArrayList<Animal> animals, Point playerPos, int nTimesteps) {

        if (previousAnimals == null)
            throw new RuntimeException("No previous animals saved => cannot calculate the animal directions");

        ArrayList<ArrayList<Point>> animalTimesteps = new ArrayList<ArrayList<Point>>();
        // Initiate empty arrays for each timestep
        for (int i = 0; i < nTimesteps; i++) {
            animalTimesteps.add(new ArrayList<Point>());
        }

        // Go through each animal and 'draw' it's path
        for (int i = 0; i < animals.size(); i++) {
            Point currentLocation = animals.get(i).get_location();
            Point previousLocation = previousAnimals.get(i).get_location();

            // Get animal direction vector
            Boolean outOfBounds = false;
            Point directionVector = PointUtilities.normalizedSubtract(previousLocation, currentLocation, 1.0);

            // Log.log("Debugging normalizedSubtract "+previousLocation + " -> " +
            // currentLocation + " = "+ directionVector);
            // Initialize with timestep 0
            animalTimesteps.get(0).add(currentLocation);

            // We start with timestep 0, which is the current timestep
            for (int j = 1; j < nTimesteps; j++) {
                // Log.log("Debugging timestep " + currentLocation + " = "+ directionVector + "
                // [" + outOfBounds + "]");

                // Make a step after each timestep
                Point nextLocation = PointUtilities.add(currentLocation, directionVector);

                if (!Point.within_bounds(nextLocation) || outOfBounds) {
                    outOfBounds = true;
                    directionVector = PointUtilities.normalizedSubtract(currentLocation, playerPos, 1.0);
                    nextLocation = PointUtilities.add(currentLocation, directionVector);
                }

                animalTimesteps.get(j).add(nextLocation);
                currentLocation = nextLocation;
            }
        }
        return animalTimesteps;
    }

    public static ArrayList<Point> predictFood(ArrayList<Point> monkeyLocations, Point playerPos) {

        ArrayList<Point> newMonkeyLocations = new ArrayList<Point>();

        for (Point monkeyLocation : monkeyLocations) {
            Point directionVector = PointUtilities.normalizedSubtract(monkeyLocation, playerPos, 1.0);

            newMonkeyLocations.add(PointUtilities.add(monkeyLocation, directionVector));
        }

        return newMonkeyLocations;
    }

}