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
     * A function that predicts the future positions of animals.
     *
     * Example usage:
     *
     * if(turn > 1) {
     *     ArrayList<ArrayList<Point>> futurePositions = PositionPredictor.predict(previousAnimals, animals, 10);
     *     System.out.println(futurePositions.get(9));
     * }
     *
     * @param previousAnimals: An array of previous animals in the field
     * @param animals: An array of current animals in the field
     * @param nTimesteps: Number of timesteps to predict
     * @return An array of arrays, where each inner array under index j is predicted animal positions in the field after
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