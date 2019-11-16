package lunch.g3;

import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import javafx.util.Pair;
import lunch.sim.*;

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
        // Is food in hand? Yes -> should we stop?; No -> should we pull food out?
        if (ps.get_held_item_type() != null) {
            // Should we stop? Yes -> put food away; No -> keep eating
            if (shouldStopEating(animals, ps)) {
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.EAT, ps.get_held_item_type());
            }
        } else {
            // Are we pulling food out? Yes -> should we continue?; No -> are we in the corner?
            if (ps.is_player_searching()) {
                // Should we continue pulling food out? Yes -> pull out; No -> put it back
                if (shouldFinishRemoving(animals, ps)) {
                    return new Command(CommandType.WAIT);
                } else {
                    return new Command(CommandType.KEEP_BACK);
                }
            } else {
                // Are we in a corner? Yes -> select food
                if (inCorner(ps)) {
                    return Command.createRetrieveCommand(selectFood(ps));
                } else {
                    return Command.createMoveCommand(getNextMoveToCorner(ps));
                }
            }
        }
    };

    public FoodType selectFood(PlayerState ps) {
        return FoodType.COOKIE;
    }

    public boolean shouldStopEating(ArrayList<Animal> animals, PlayerState ps) {
        int dangerMonkeys = 0;
        for (Animal animal : animals) {
            if(animal.which_animal() == AnimalType.GOOSE) {
                if(distToAnimal(animal, ps) <= 6) {
                    return true;
                }
            } else {
                //monkey
                if(distToAnimal(animal, ps) <= 6) {
                    if(dangerMonkeys == 2) {
                        return true;
                    }
                    dangerMonkeys++;
                }
            }
        }
        return false;
    }
    public boolean shouldFinishRemoving(ArrayList<Animal> animals, PlayerState ps) {
        return false;
    }

    public double distToAnimal(Animal animal, PlayerState ps) {
        return Math.sqrt(Math.pow(animal.get_location().x - ps.get_location().x, 2) + Math.pow(animal.get_location().y - ps.get_location().y, 2));
    }

    public Pair locateNearestCorner(){
        ArrayList<Pair> corners = new ArrayList<Pair>();
		Pair top_right = new Pair(0, 100);
		Pair top_left = new Pair(0, 0);
		corners.add(top_right);
		return top_right;
    }

    public Point getNextMoveToCorner(PlayerState ps){
        Double bearing = 1*2*Math.PI;
		Point move_to_corner = new Point (ps.get_location().x + .5, ps.get_location().y + .5);
		return move_to_corner;
    }

    public boolean inCorner(PlayerState ps) {
        return false;
    }

    /*
    public Double getBearingFromDestination(){
		Math.atan()
	}*/
}
