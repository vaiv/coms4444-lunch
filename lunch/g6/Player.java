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
    private double t;

    private ArrayList<Animal> prev_animals;
    private HashMap<Integer, Point> trajectories;
    private ArrayList<Animal> incomingMonkeys;
    private ArrayList<Animal> incomingGeese;
    private Point corner; 
    private static boolean shouldDistract; 
    private Integer numMonkeys;
    private Integer numGeese;


    public Player() {
        turn = 0;
    }
    
    public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        this.id = id;
        this.t = t;
        avatars = "flintstone";
        random = new Random(s);
        prev_animals = new ArrayList<>(animals);
        shouldDistract = false;
        numMonkeys = m;
        numGeese = g;
        return avatars;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
 
        boolean noGeese = false;
        if (numGeese < 1) {
            noGeese = true;
        }
        shouldDistract = Helper.shouldDistract(members, ps, random, this.id, noGeese);
        // shouldDistract = false;
        // Never distract if the time is no more than 20 minutes or if there are fewer than 20 monkeys
        // The 20 monkeys condition can be changed
        if ((t <= 1200) || (numMonkeys <= 20)) {
            shouldDistract = false;
        }

        // if(!shouldDistract && turn > 100){
        //     int monkeys = Helper.countMonkeys(animals, new Point(50, 50));
        //     double ratio = (double) monkeys / (double) numMonkeys;
        //     if(ratio < 0.3)
        //         shouldDistract = true;
        //     //else shouldDistract = false;
        // }
        
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
        Point location = !shouldDistract ? corner : new Point(50, 50);
        if (!ps.get_location().equals(location)) {
            // Need to put food away before we can move
            // means we're not there yet
            // MAKE SURE THAT NOT DOING SANDWICH FOR MONKEY WALK (unless no geese)
            if (shouldDistract == true && (ps.check_availability_item(FoodType.EGG) || noGeese)) {
                return MonkeyWalk(animals, location, ps);
            }
            if (ps.get_held_item_type() != null || ps.is_player_searching()) {
                prev_animals = new ArrayList<>(animals);
                return new Command(CommandType.KEEP_BACK);
            }
            else{
                prev_animals = new ArrayList<>(animals);
                return Command.createMoveCommand(Helper.moveTo(ps.get_location(), location));
            }
            
        } else {
            if (shouldDistract == true && (ps.check_availability_item(FoodType.EGG) || noGeese)) {
                return MonkeyStop(animals, location, ps);
            }
            return tryToEat(animals, prev_animals, ps);
        }
    }

    public Command MonkeyWalk(ArrayList<Animal> animals, Point location, PlayerState ps) {
        double monkey_time = Helper.getMonkeyTime(animals, ps);
        prev_animals = new ArrayList<>(animals);
        // if food out and monkeys not in grabbing distance, just keep going
        if (ps.get_held_item_type() != null && monkey_time > 2) {
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), location));
        }
        // if food is being pulled out and time remaining is 1 second, but monkey time is less than 2, abort.
        //if (ps.time_to_finish_search() <= 2&& monkey_time <= 2) {
        //    return new Command(CommandType.ABORT);
        //}
        // if food not out and monkeys too far, pull food out
        if (ps.get_held_item_type() == null && monkey_time >= 3) {
            return Helper.takeOutFood(ps);
        }
        // if food out and monkeys too close, put away
        if (ps.get_held_item_type() != null && monkey_time <= 2) {
            return new Command(CommandType.KEEP_BACK);
        }
        // if food not out and monkeys too close, just move away
        //if (ps.get_held_item_type() == null && monkey_time < 2)
        else {
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), location));
        }
    }

    public Command MonkeyStop(ArrayList<Animal> animals, Point location, PlayerState ps) {
        double monkey_time = Helper.getMonkeyTime(animals, ps);
        //prev_animals = new ArrayList<>(animals);
        if (ps.get_held_item_type() != null) {
            if (monkey_time <= 2) {
                prev_animals = new ArrayList<>(animals);
                return new Command(CommandType.KEEP_BACK);
            } else {
                return tryToEat(animals, prev_animals, ps);
            }
        } else {
            // food is inside bag
            prev_animals = new ArrayList<>(animals);
            if (monkey_time >= 3) {
                return Helper.takeOutFood(ps);
            } else {
                return new Command(CommandType.ABORT);
            }
        }
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
        Double eatTime = 2.0; 
        if (ps.get_held_item_type() == null) {
            double minTime = !ps.is_player_searching() ? 10.0 + eatTime: (ps.time_to_finish_search() + eatTime);
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
            boolean cond1 = (ps.get_held_item_type() == FoodType.SANDWICH && geeseTime <= eatTime);
            boolean cond2 = (monkeyTime <= eatTime);
            if (cond1 || cond2) {
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.EAT);
            }
        }
        
        // Missed case
        else {
            //System.out.println("oops");
            return new Command();
        }
        
        return new Command();
    }

}
