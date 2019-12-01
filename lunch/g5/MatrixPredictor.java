package lunch.g5;

import javafx.util.Pair;

import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.PlayerState;


public class MatrixPredictor {

    // Configs for calculating future matrices
    private double monkeyDangerDistance = 5.0;// How far way from the monkeys we still consider them in calculation
    private double geeseDangerDistance = 6.0; // How far way from the geese we still consider them in calculation
    private int bufferLookahead = 0;          // When calculating a matrix, how much aditional steps to consider

    public MatrixPredictor() { }

    public MatrixPredictor(double monkeyDangerDistance, double geeseDangerDistance, int bufferLookahead) {
        this.monkeyDangerDistance = monkeyDangerDistance;
        this.geeseDangerDistance = geeseDangerDistance;
        this.bufferLookahead = bufferLookahead;
    }

    /**
     * A function that calculates board matrices in the future.
     *
     * IMPORTANT: The matrices it returns have a diferent indexing method. The indices correspond to coordinates on the
     * board. So m.get(0, 0) will always get the value at Point(0, 0) and not the point of the player.
     *
     * Example usage:
     *
     * ArrayList<Matrix> matrices = getFutureMatrices(animals);
     * matrices.get(9).show(); // Prints a matrix at timestep 9 to System.out
     * System.print(matrices.get(9).get(10, 10); // Prints what will happen at point (10, 10) at timestep 9
     *
     * @param animals: An array of current elements on the board
     * @param ps: Current player state object
     * @return An array of matrices, where each matrix under index j is predicted state of the board after
     *         j timesteps.
     */
    public Pair<ArrayList<Matrix>, ArrayList<Matrix>> predict(
            ArrayList<Animal> previousAnimals, ArrayList<Animal> animals, PlayerState ps, int nTimesteps
    ) {
        if (previousAnimals == null) {
            throw new RuntimeException("No previous animals saved => cannot calculate the animal directions");
        }
        ArrayList<Matrix> monkeyTimesteps = new ArrayList<>();
        ArrayList<Matrix> geeseTimesteps = new ArrayList<>();
        // Initiate empty matrices for each timestep
        Point ourLocation = ps.get_location();
        int ourLocationX = (int)Math.round(ourLocation.x);
        int ourLocationY = (int)Math.round(ourLocation.y);
        for (int i = 0; i < nTimesteps; i++) {
            int size = i + 3 + bufferLookahead;
            int sizeToLeft = Math.min(size, 50 + ourLocationX);
            int sizeToTop = Math.min(size, 50 + ourLocationY);
            int sizeToRight = Math.min(size, 50 - ourLocationX);
            int sizeToBottom = Math.min(size, 50 - ourLocationY);
            Matrix m = new Matrix(sizeToLeft + sizeToRight + 1, sizeToBottom + sizeToTop + 1);
            int originX = sizeToLeft - ourLocationX;
            int originY = sizeToTop - ourLocationY;
            m.setOrigin(originX, originY);
            monkeyTimesteps.add(m);
            geeseTimesteps.add(new Matrix(m));
        }
        // Go through each animal and 'draw' it's path on the matrices
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            Animal previousAnimal = previousAnimals.get(i);
            ArrayList<Matrix> timesteps;
            double dangerDistance;
            if (animal.which_animal() == AnimalType.MONKEY) {
                timesteps = monkeyTimesteps;
                dangerDistance = monkeyDangerDistance;
            } else if (animal.which_animal() == AnimalType.GOOSE) {
                timesteps = geeseTimesteps;
                dangerDistance = geeseDangerDistance;
            } else {
                continue;
            }
            Point currentLocation = animal.get_location();
            Point previousLocation = previousAnimal.get_location();
            // Get animal direction vector
            Point directionVector = PointUtilities.substract(currentLocation, previousLocation);
            // We start with timestep 0, which is the current timestep
            Point nextLocation = currentLocation;
            for (int j = 0; j < nTimesteps; j++) {
                Matrix matrix = timesteps.get(j);
                // Increase all counters in the radius of dangerDistance
                // We look in a square of dangetDistance around a monekey;
                int animalXFrom = (int)Math.floor(nextLocation.x - dangerDistance);
                int animalXTo = (int)Math.ceil(nextLocation.x + dangerDistance);
                int animalYFrom = (int)Math.floor(nextLocation.y - dangerDistance);
                int animalYTo = (int)Math.ceil(nextLocation.y + dangerDistance);
                for (int x = animalXFrom; x <= animalXTo; x++) {
                    for (int y = animalYFrom; y <= animalYTo; y++) {
                        // If our matrix does not have these coordinates we continue
                        if (!matrix.has(x, y)) {
                            continue;
                        }
                        // We check if the distance is actually less than danger distance
                        double distance = Point.dist(nextLocation, new Point(x, y));
                        if (distance > dangerDistance) {
                            continue;
                        }
                        matrix.increment(x, y);
                    }
                }
                // Make a step after each timestep
                nextLocation = PointUtilities.add(nextLocation, directionVector);
            }
        }
        return new Pair<ArrayList<Matrix>, ArrayList<Matrix>>(monkeyTimesteps, geeseTimesteps);
    }
}