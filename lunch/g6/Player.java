package lunch.g6;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import javafx.scene.shape.MoveTo;
import javafx.util.Pair;
import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

import lunch.g6.Helper.*;

public class Player implements lunch.sim.Player {
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;
    private String avatars;

    private ArrayList<Animal> prev_animals;
    private HashMap<Integer, Point> trajectories;
    private ArrayList<Animal> incomingMonkeys;
    private ArrayList<Animal> incomingGeese;

    private Point corner;

    public Player() {
        turn = 0;
    }
    
    public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        this.id = id;
        avatars = "flintstone";
        random = new Random(s);
        prev_animals = new ArrayList<>();
        // Define ideal locations
        ArrayList<Point> corners = new ArrayList<>();
        corners.add(new Point(-34, -34));
        corners.add(new Point(-34, 34));
        corners.add(new Point(34, -34));
        corners.add(new Point(34, 34));
        int corner_ind = Math.abs(random.nextInt()%4);
        corner = corners.get(corner_ind);
        return avatars;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        // Calculate the trajectories of animals
        trajectories = Helper.calculateTrajectories(animals, prev_animals);

        // Update prev animals to be where animals were this time
        prev_animals = new ArrayList<>(animals);

	    //determine which monkeys and birds are heading towards us.
        incomingMonkeys = Helper.findIncomingMonkeys(animals, prev_animals, ps);
	    incomingGeese = Helper.findIncomingGeese(animals, prev_animals, ps);

        // Not currently using, from random, could be helpful 
        Double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < animals.size(); i++) {
            min_dist = Math.min(min_dist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }

        // Step 1: Move to one of four corner locations
        // If not at corner --> move towards it
        if(!ps.get_location().equals(corner)){
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), corner));
        }

        // based on incoming monkey / geese -- do we have time to eat?
        // if yes: eat
        // if no: no?
        double geeseTime = Helper.getGeeseTime(animals, incomingGeese, ps);
        double monkeyTime = Helper.getMonkeyTime(animals, incomingMonkeys, ps);

        // No food in hand 
        // Case 1: Not searching
        // Case 2: Currently searching
        double minTime = !ps.is_player_searching() ? 11.0 : ps.time_to_finish_search() +1;
        System.out.println("times: " + geeseTime + " " + monkeyTime);
        if (ps.get_held_item_type() == null){
            // Due to ordering, this check implies eating a sandwich 
            if ((!ps.check_availability_item(FoodType.EGG) && geeseTime > minTime) || (monkeyTime > minTime)) {
                // Means we would have one second to eat 
                return Helper.takeOutFood(ps);
            }
    
        }
        // With food in hand 
        // Case 3: Not searching 
        // Case 4: Currently searching 
        else if (ps.get_held_item_type() != null ){
            // TODO: Check to make sure this is generic sandwich 
            if ((ps.get_held_item_type()==FoodType.SANDWICH && geeseTime <= 1.0 )|| monkeyTime <= 1.0){
                return new Command(CommandType.KEEP_BACK);
            }
            else{
                return new Command(CommandType.EAT);
            }

        }
        // Missed case 
        else{
            System.out.println("oops");
            return new Command();
        }

        return new Command();
    }

}
