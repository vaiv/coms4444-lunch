package lunch.g5;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

import lunch.sim.Point;
import lunch.sim.Family;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;


public class FamilyBehaviorPredictor {

    private int nMonkeys;
    private int nFamily;
    private double thresholdIn;
    private double thresholdOut;
    private double radius = 40.0;

    public FamilyBehaviorPredictor(int f, int m) {
        this.nMonkeys = m;
        this.nFamily = f;
        this.thresholdIn = Math.min(2.0 * m / f, m * 0.2);
        this.thresholdOut = Math.min(4.0 * m / f, m * 0.8);
    }

    public ArrayList<Integer> getFollowingNumbers(ArrayList<Family> members, ArrayList<Animal> animals, ArrayList<Animal> previousAnimals) {
        // An array to hold animals position and it's vector
        ArrayList<Pair<Point, Point>> animalLocations = new ArrayList<>();
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            if(animal.which_animal() == AnimalType.MONKEY) {
                Animal previousAnimal = previousAnimals.get(i);
                Point currentLocation = animal.get_location();
                Point previousLocation = previousAnimal.get_location();
                animalLocations.add(new Pair<Point, Point>(currentLocation, previousLocation));
            }
        }

        ArrayList<Integer> followingNumbers = new ArrayList<>();
        // Go through each family member and calculate the number of animals following them
        for (Family member : members) {
            int nAnimalsFollowing = 0;
            Point p0 = member.get_location();
            for(Pair<Point, Point> animal: animalLocations) {
                Point p1 = animal.getKey();
                Point p2 = animal.getValue();
                if((Point.dist(p1, p0) < 5) || (Point.dist(p1, p0) < radius && PointUtilities.isInLine(p1, p0, p2))) {
                    nAnimalsFollowing += 1;
                }
            }
            followingNumbers.add(nAnimalsFollowing);
        }
        return followingNumbers;
    }

    private boolean inDistractorZone(Point p) {
        if(p.x > 0 || p.y > 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns behaviour type for each family member.
     *
     * @param members: An array of current famile members in the field
     * @param animals: An array of current animals in the field
     */
    public ArrayList<BehaviorType> predict(ArrayList<Family> members, ArrayList<Animal> animals, ArrayList<Animal> previousAnimals) {
        ArrayList<BehaviorType> familyBehavior = new ArrayList<>();
        // Go through each family member and predict their behavior
        ArrayList<Integer> followingNumbers = getFollowingNumbers(members, animals, previousAnimals);
        for (int i = 0; i < members.size(); i++) {
            Family member = members.get(i);
            Point memberLocation = member.get_location();
            int nAnimalsInRadius = followingNumbers.get(i);
            if(inDistractorZone(memberLocation) && nAnimalsInRadius > thresholdIn) {
                familyBehavior.add(BehaviorType.DISTRACTION);
            } else if(nAnimalsInRadius > thresholdOut) {
                familyBehavior.add(BehaviorType.DISTRACTION);
            } else {
                familyBehavior.add(BehaviorType.AGGRESSIVE);
            }
        }
        return familyBehavior;
    }
}