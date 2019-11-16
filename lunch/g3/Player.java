package lunch.g3;

import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player {
    // Initialization function.
    // members: other family members collborating with your player.
    // members_count: Number of family members.
    // t: Time limit for simulation.
    public void init(ArrayList<Family> members,Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        return;
    };

    // Gets the moves from the player. Number of moves is specified by first parameter.
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        return null;
    };

    public boolean inDanger(ArrayList<Animal> animals, PlayerState ps) {
        if(ps.held_food_item == null) {
            return false;
        }
        int dangerMonkeys = 0;
        for (Animal animal : animals) {
            if(animal.animal == GOOSE) {
                if(distToAnimal(animal, ps) > 6) {
                    continue;
                }
            } else {
                //monkey
                if(distToAnimal(animal, ps))
            }
        }
        return false;
    }

    public double distToAnimal(Animal animal, PlayerState ps) {
        return Math.sqrt(Math.pow(animal.location.x - player.location.x, 2) + Math.pow(animal.location.y - player.location.y, 2));
    }
}
