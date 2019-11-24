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
    private double radius = 10.0;

    public FamilyBehaviorPredictor(int f, int m) {
        this.nMonkeys = m;
        this.nFamily = f;
    }

    /**
     * Returns behaviour type for each family member.
     *
     * @param animals: An array of current elements on the board
     */
    public ArrayList<BehaviorType> predict(ArrayList<Family> members, ArrayList<Animal> animals) {
        ArrayList<BehaviorType> familyBehavior = new ArrayList<>();
        // Go through each family member and predict the command
        for (Family member : members) {
            int nAnimalsInRadius = 0;
            for (Animal animal : animals) {
                if (animal.which_animal() == AnimalType.MONKEY && Point.dist(animal.get_location(), member.get_location()) < radius) {
                    nAnimalsInRadius += 1;
                }
            }
            if(nAnimalsInRadius > 2 * nMonkeys / nFamily) {
                familyBehavior.add(BehaviorType.DISTRACTION);
            } else {
                familyBehavior.add(BehaviorType.AGGRESSIVE);
            }
        }
        return familyBehavior;
    }
}