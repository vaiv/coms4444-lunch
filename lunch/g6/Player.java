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
<<<<<<< HEAD
    private Point wall; 

=======

    private Point corner;
>>>>>>> 25b21ef6f05ae051ceb6a358776928c9cf8c642c

    public Player() {
        turn = 0;
    }
    
    public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        this.id = id;
        avatars = "flintstone";
        random = new Random(s);
        prev_animals = new ArrayList<>();
<<<<<<< HEAD
=======
        // Define ideal locations
        ArrayList<Point> corners = new ArrayList<>();
        corners.add(new Point(-34, -34));
        corners.add(new Point(-34, 34));
        corners.add(new Point(34, -34));
        corners.add(new Point(34, 34));
        int corner_ind = Math.abs(random.nextInt() % 4);
        corner = corners.get(corner_ind);
>>>>>>> 25b21ef6f05ae051ceb6a358776928c9cf8c642c
        return avatars;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        // Calculate the trajectories of animals
        trajectories = Helper.calculateTrajectories(animals, prev_animals);
       
<<<<<<< HEAD
	    // //determine which monkeys and birds are heading towards us.
=======
	    // Determine which monkeys and birds are heading towards us.
>>>>>>> 25b21ef6f05ae051ceb6a358776928c9cf8c642c
        // incomingMonkeys = Helper.findIncomingMonkeys(animals, prev_animals, ps);
        // incomingGeese = Helper.findIncomingGeese(animals, prev_animals, ps);
        
        // Update prev animals to be where animals were this time
        prev_animals = new ArrayList<>(animals);
<<<<<<< HEAD

        // Step 1: wait and try to eat in the middle, both distracting and 
        // getting a sense of layout 
        if (++turn < 50) return tryToEat(animals, ps);
        // Step 2: find the sparsest location on a wall to eat
        if (turn == 50) wall = Helper.findSparseLoc(members, ps);
        // Step 3: go to said location
        if(!ps.get_location().equals(wall)){
            // Need to put food away before we can move 
            if (ps.get_held_item_type() != null ){
                return new Command(CommandType.KEEP_BACK);
            }
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), wall));
        }
        // Step 4: continue trying to eat 
        return tryToEat(animals, ps);
    }


    /**
     * Function for main logic 
     * @param animals
     * @param ps
     * @return
     */
    private static Command tryToEat(ArrayList<Animal> animals, PlayerState ps){
        // Find time until geese / monkeys can snatch food 
        // TODO: Right now based only on surround animals, include incoming 
        double geeseTime = Helper.getGeeseTime(animals, animals, ps);
        double monkeyTime = Helper.getMonkeyTime(animals, animals, ps);

        // No food in hand 
        // Case 1: Not searching
        // Case 2: Currently searching
        double minTime = !ps.is_player_searching() ? 11.0 : ps.time_to_finish_search() +1;
        if (ps.get_held_item_type() == null){
            // Due to ordering, this check implies eating a sandwich 
            if ((!ps.check_availability_item(FoodType.EGG) && geeseTime > minTime) || (monkeyTime > minTime)) {
                // Means we would have one second to eat 
                return Helper.takeOutFood(ps);
            }
            else{
                // Deal with what we do in case where don't have enough time to eat 
            }
    
        }
        // With food in hand 
        // Case 3: Not searching 
        // Case 4: Currently searching 
        else if (ps.get_held_item_type() != null ){
            // TODO: Check to make sure this is generic sandwich 
            if ((ps.get_held_item_type()==FoodType.SANDWICH && geeseTime <= 5.0 )|| monkeyTime <= 5.0){
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

=======
        // Not currently using, from random, could be helpful 
        Double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < animals.size(); i++) {
            min_dist = Math.min(min_dist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }

        // Step 1: Move to one of four corner locations
        // If not at corner --> move towards it
        if (!ps.get_location().equals(corner)) {
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), corner));
        }

        // Step 2: Based on incoming monkeys / geese, do we have time to eat?
        // If yes: eat
        // If no: no?
        double geeseTime = Helper.getGeeseTime(animals, ps);
        double monkeyTime = Helper.getMonkeyTime(animals, ps);

        // No food in hand 
        if (ps.get_held_item_type() == null) {
            double minTime = !ps.is_player_searching() ? 11.0 : (ps.time_to_finish_search() + 1.0);
            if ((!ps.check_availability_item(FoodType.EGG))) {
                // Due to ordering, this check implies eating a sandwich
                if ((geeseTime > minTime) && (monkeyTime > minTime)) {
                    return Helper.takeOutFood(ps);
                } else {
                    // Deal with what we do in case where don't have enough time to eat
                }
            } else if (monkeyTime > minTime) {
                return Helper.takeOutFood(ps);
            } else {
                // Deal with what we do in case where don't have enough time to eat
            }
        }
        
        // With food in hand 
        else if (ps.get_held_item_type() != null) {
            // TODO: Check to make sure this is generic sandwich (checked: yes)
            if ((ps.get_held_item_type() == FoodType.SANDWICH && geeseTime <= 1.0) || monkeyTime <= 1.0) {
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.EAT);
            }
        }
        
        // Missed case 
        else {
            System.out.println("oops");
            return new Command();
        }
        return new Command();
    }
>>>>>>> 25b21ef6f05ae051ceb6a358776928c9cf8c642c
}
