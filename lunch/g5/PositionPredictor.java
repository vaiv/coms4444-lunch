package lunch.g5;

import javafx.util.Pair;

import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.PlayerState;


public class PositionPredictor {

    public PositionPredictor() { }

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
    public static ArrayList<ArrayList<Point>> predict(
            ArrayList<Animal> previousAnimals, ArrayList<Animal> animals, int nTimesteps
    ) {
        if (previousAnimals == null) {
            throw new RuntimeException("No previous animals saved => cannot calculate the animal directions");
        }
        ArrayList<ArrayList<Point>> animalTimesteps = new ArrayList<>();
        // Initiate empty arrays for each timestep
        for (int i = 0; i < nTimesteps; i++) {
            animalTimesteps.add(new ArrayList<Point>());
        }
        // Go through each animal and 'draw' it's path
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            Animal previousAnimal = previousAnimals.get(i);
            Point currentLocation = animal.get_location();
            Point previousLocation = previousAnimal.get_location();
            // Get animal direction vector
            Point directionVector = PointUtilities.substract(currentLocation, previousLocation);
            // We start with timestep 0, which is the current timestep
            Point nextLocation = currentLocation;
            for (int j = 0; j < nTimesteps; j++) {
                ArrayList<Point> timestep = animalTimesteps.get(j);
                timestep.add(new Point(nextLocation));
                // Make a step after each timestep
                nextLocation = PointUtilities.add(nextLocation, directionVector);
            }
        }
        return animalTimesteps;
    }
}