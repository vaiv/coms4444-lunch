package lunch.g5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import javafx.util.Pair;
import lunch.sim.Animal;
import lunch.sim.Log;
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

    public static Integer getEatBuffer(AnimalPosition monkeyPositions, Point playerLoc) {
        return DistractionUtilities.getEatBuffer(monkeyPositions, playerLoc, 0);
    }

    public static Integer getEatBuffer(AnimalPosition monkeyPositions, Point playerLoc, Integer hungrySteps) {
        if (monkeyPositions.size() < 3)
            return 100;

        Double[] distances = { 100., 100., 100. };
        for (PositionStruct pos : monkeyPositions) {
            Double distance = Math.hypot(playerLoc.x - pos.location.x, playerLoc.y - pos.location.y);

            if (hungrySteps > 0 || pos.movement == null || pos.numSteps > 0)
                distance = distance - pos.numSteps - hungrySteps;

            if (distance < distances[0]) {
                distances[2] = distances[1];
                distances[1] = distances[0];
                distances[0] = distance;
            } else if (distance < distances[1]) {
                distances[2] = distances[1];
                distances[1] = distance;
            } else if (distance < distances[2]) {
                distances[2] = distance;
            }
        }

        return distances[2].intValue() - 6;
    }

    public static Pair<Integer, Point> simulateWalking(AnimalPosition monkeyPositions, Point ourLocation,
            Integer eatSteps) {
        
        Integer minX = DistractionStrategy.minXd;
        Integer maxX = DistractionStrategy.maxXd;
        Integer minY = DistractionStrategy.minYd;
        Integer maxY = DistractionStrategy.maxYd;

        // Initiate empty matrices for each timestep
        Integer x = (int) Math.round(ourLocation.x);
        Integer y = (int) Math.round(ourLocation.y);

        Integer[] dxs = { 0, 0, -1, +1 };
        Integer[] dys = { -1, 1, 0, 0 };

        boolean[] valid = { y >= minY, y <= maxY, x >= minX, x <= maxX };
        Integer[] numSteps = { y - minY, maxY - y, x - minX, maxX - x };

        Integer bestNumSteps = -1;
        Point bestDestination = null;

        // Simulate eating
        // monkeyPositions = DistractionUtilities.simulateTimestep(monkeyPositions, );
        for (int dir = 0; dir < 4; dir++) {
            if (!valid[dir])
                continue;

            // TODO: Simplify to simply select after steps numSteps
            Integer curSteps = 0;
            Integer maxSteps = numSteps[dir];

            for (int steps = 0; steps < maxSteps; steps++) {
                Integer curX = x + steps * dxs[dir];
                Integer curY = y + steps * dys[dir];

                Integer eatBuffer = DistractionUtilities.getEatBuffer(monkeyPositions, new Point(curX, curY),
                        steps + eatSteps);

                if (eatBuffer < 0)
                    break;
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

    // public static Pair<Integer, Point> simulateWalk(ArrayList<Point>
    // monkeyPositions, Point ourLocation) {
    // Integer minX = 0, maxX = 35, minY = 0, maxY = 35;

    // // Initiate empty matrices for each timestep
    // Integer x = (int) Math.round(ourLocation.x);
    // Integer y = (int) Math.round(ourLocation.y);

    // Integer[] dxs = { 0, 0, -1, +1 };
    // Integer[] dys = { -1, 1, 0, 0 };
    // // Integer[] numSteps = { x + 30, 30 - x, y + 30, 30 - y };
    // boolean[] valid = { x >= minX, x <= maxX, y >= minY, y <= maxY };

    // Integer bestNumSteps = -1;
    // Point bestDestination = null;

    // for (int dir = 0; dir < 4; dir++) {
    // if (!valid[dir])
    // continue;

    // // TODO: Simplify to simply select after steps numSteps
    // Integer curSteps = 0;
    // Integer numSteps = 0;

    // if (dir == 0)
    // numSteps = (y < minY) ? 0 : y - minY;
    // else if (dir == 1)
    // numSteps = (y > maxY) ? 0 : maxY - y;
    // else if (dir == 2)
    // numSteps = (x < minX) ? 0 : x - minX;
    // else if (dir == 3)
    // numSteps = (x > maxX) ? 0 : maxX - x;

    // ArrayList<Point> newMonkeyPos = monkeyPositions;
    // for (int steps = 0; steps < numSteps; steps++) {
    // Integer curX = x + steps * dxs[dir];
    // Integer curY = y + steps * dys[dir];

    // Integer numMonkeys = DistractionUtilities.countMonkeys(newMonkeyPos, new
    // Point(curX, curY));
    // // Log.log(newMonkeyPos.toString());

    // if (numMonkeys > 2)
    // break;

    // newMonkeyPos = DistractionUtilities.predictFood(newMonkeyPos, new Point(curX,
    // curY));
    // curSteps = steps;
    // }

    // // Log.log(String.format("Direction = %d. Steps = [%d / %d]", dir, curSteps,
    // // numSteps));

    // if (curSteps > bestNumSteps) {
    // bestNumSteps = curSteps;
    // bestDestination = new Point(x + curSteps * dxs[dir], y + curSteps *
    // dys[dir]);
    // }
    // }

    // return new Pair<Integer, Point>(bestNumSteps, bestDestination);
    // }

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
     * @param steps:           Number of timesteps to predict
     * @return An array of arrays, where each inner array under index j is predicted
     *         animal positions in the field after j timesteps.
     */

    public static AnimalPosition simulateTimestep(AnimalPosition animalLocs, int steps) {
        AnimalPosition newLocs = new AnimalPosition();

        for (PositionStruct position : animalLocs) {
            Point location = position.location;
            Point movement = position.movement;
            Integer numSteps = position.numSteps;

            if (movement == null)
                numSteps = numSteps + steps;
            else {
                Double newX = location.x + movement.x * steps;
                Double newY = location.y + movement.y * steps;

                if (newX < -50 || newX > 50 || newY < -50 || newY > 50) {
                    Integer excessSteps = -10;
                    if (newX < -50 || newX > 50)
                        excessSteps = Math.max(excessSteps,
                                (int) Math.ceil((Math.abs(newX) - 50) / Math.abs(movement.x)));
                    if (newY < -50 || newY > 50)
                        excessSteps = Math.max(excessSteps,
                                (int) Math.ceil((Math.abs(newY) - 50) / Math.abs(movement.y)));

                    Integer stepsTaken = steps - excessSteps;
                    newX = location.x + movement.x * stepsTaken;
                    newY = location.y + movement.y * stepsTaken;

                    location = new Point(newX, newY);
                    movement = null;
                    numSteps = steps - stepsTaken;
                } else {
                    location = new Point(newX, newY);
                }
            }

            newLocs.add(new PositionStruct(location, movement, numSteps));
        }

        return newLocs;
    }

    public static AnimalPosition[] simulateTimesteps(AnimalPosition animalLocs, int steps) {
        // TODO: Figure out this array shenanigans
        ArrayList<AnimalPosition> animalLocations = new ArrayList<AnimalPosition>();

        animalLocations.add(animalLocs);
        for (int i = 1; i < steps; i++) {
            AnimalPosition tmp = DistractionUtilities.simulateTimestep(animalLocations.get(i - 1), 1);
            animalLocations.add(tmp);
        }

        AnimalPosition[] animalLocat = new AnimalPosition[steps];
        for (int i = 0; i < steps; i++)
            animalLocat[i] = animalLocations.get(i);
        return animalLocat;
    }

    // public static ArrayList<ArrayList<Point>> predictNotNull(AnimalPosition
    // animalLocs, Point playerPos,
    // int nTimesteps) {

    // ArrayList<ArrayList<Point>> animalTimesteps = new
    // ArrayList<ArrayList<Point>>();
    // // Initiate empty arrays for each timestep
    // for (int i = 0; i < nTimesteps; i++) {
    // animalTimesteps.add(new ArrayList<Point>());
    // }

    // // Go through each animal and 'draw' it's path
    // for (int i = 0; i < animalLocs.size(); i++) {
    // Point position = animalLocs.get(i).location;
    // Point movement = animalLocs.get(i).movement;
    // Integer numSteps = animalLocs.get(i).numSteps;

    // // Get animal direction vector
    // Boolean outOfBounds = false;

    // // Log.log("Debugging normalizedSubtract "+previousLocation + " -> " +
    // // currentLocation + " = "+ directionVector);
    // // Initialize with timestep 0
    // animalTimesteps.get(0).add(currentLocation);

    // // We start with timestep 0, which is the current timestep
    // for (int j = 1; j < nTimesteps; j++) {
    // // Log.log("Debugging timestep " + currentLocation + " = "+ directionVector +
    // "
    // // [" + outOfBounds + "]");

    // // Make a step after each timestep
    // Point nextLocation = PointUtilities.add(currentLocation, directionVector);

    // if (!Point.within_bounds(nextLocation) || outOfBounds) {
    // outOfBounds = true;
    // directionVector = PointUtilities.normalizedSubtract(currentLocation,
    // playerPos, 1.0);
    // nextLocation = PointUtilities.add(currentLocation, directionVector);
    // }

    // animalTimesteps.get(j).add(nextLocation);
    // currentLocation = nextLocation;
    // }
    // }
    // return animalTimesteps;
    // }

    public static AnimalPosition simulateTimestepWithFood(AnimalPosition animalLocs, Point playerLoc) {
        AnimalPosition newLocs = new AnimalPosition();

        for (PositionStruct position : animalLocs) {
            Point location = position.location;
            Point movement = position.movement;
            Integer numSteps = position.numSteps;

            // Assume player attracts all monkeys. Improve by considering other players
            if (Point.dist(location, playerLoc) < 40)
                movement = PointUtilities.normalizedSubtract(location, playerLoc, 1.0);

            if (movement == null)
                numSteps++;
            else {
                Double newX = location.x + movement.x;
                Double newY = location.y + movement.y;

                if (newX < -50 || newX > 50 || newY < -50 || newY > 50) {
                    movement = null;
                    numSteps = 1;
                } else {
                    location = new Point(newX, newY);
                }
            }

            newLocs.add(new PositionStruct(location, movement, numSteps));
        }

        return newLocs;
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