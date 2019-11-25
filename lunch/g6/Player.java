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
        prev_animals = new ArrayList<>(animals);
        return avatars;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        // Calculate the trajectories of animals
        trajectories = Helper.calculateTrajectories(animals, prev_animals);
        // Step 1: wait and try to eat in the middle, both distracting and 
        // getting a sense of layout
        if (++turn < 50)
            return tryToEat(animals, prev_animals, ps);
        // Step 2: find the sparsest location on a wall to eat
        if (turn == 50)
            corner = Helper.findSparseLoc(members, ps, random);
        // Step 3: go to corner and eat or go to center and distract depending on progress
        Point location = !Helper.shouldDistract(ps) ? corner : new Point(0, 0);
        if (!ps.get_location().equals(location)) {
            // Need to put food away before we can move
            if (ps.get_held_item_type() != null || ps.is_player_searching()) {
                prev_animals = new ArrayList<>(animals);
                return new Command(CommandType.KEEP_BACK);
            }
            prev_animals = new ArrayList<>(animals);
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), location));
        }
        return tryToEat(animals, prev_animals, ps);
    }

    /**
     * Function for main logic 
     * @param animals
     * @param ps
     * @return
     */
    private static Command tryToEat(ArrayList<Animal> animals, ArrayList<Animal> prev_animals, PlayerState ps) {
        // Find time until geese / monkeys can snatch food
        double geeseTime = Helper.getGeeseTime(animals, prev_animals, ps);
        double monkeyTime = Helper.getMonkeyTime(animals, ps);
        prev_animals = new ArrayList<>(animals);
        // No food in hand
        if (ps.get_held_item_type() == null) {
            double minTime = !ps.is_player_searching() ? 11.0 : (ps.time_to_finish_search() + 1.0);
            if ((!ps.check_availability_item(FoodType.EGG))) {
                // Due to ordering, this check implies eating a sandwich
                if ((geeseTime > minTime) && (monkeyTime > minTime)) {
                    return Helper.takeOutFood(ps);
                } else if (geeseTime < 0) {
                    // Panic mode
                    return new Command(CommandType.ABORT);
                } else {
                    // Deal with what we do in case where don't have enough time to eat
                    //return new Command(CommandType.ABORT);
                }
            } else if (monkeyTime > minTime) {
                return Helper.takeOutFood(ps);
            } else {
                // Deal with what we do in case where don't have enough time to eat
                //return new Command(CommandType.ABORT);
            }
        }
        
        // With food in hand
        else if (ps.get_held_item_type() != null) {
            boolean cond1 = (ps.get_held_item_type() == FoodType.SANDWICH && geeseTime <= 1.0);
            boolean cond2 = (monkeyTime <= 1.0);
            boolean cond3a = !Helper.shouldDistract(ps);
            boolean cond3b = (ps.get_held_item_type() == FoodType.FRUIT);
            boolean cond3c = (ps.get_time_for_item(FoodType.FRUIT2) <= 115);
            boolean cond3 = cond3a && cond3b && cond3c;
            if (cond1 || cond2 || cond3) {
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

}
