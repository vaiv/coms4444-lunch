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
        
        
        
        // If safe: start eating (cookies first, then non sandwhich, then sandwhich)
        // if unsafe: put away 
        // alternate these 


        Double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < animals.size(); i++) {
            min_dist = Math.min(min_dist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }


        // Step 1: Move to one of four corner locations 
        // If not at corner --> move towards it 
        if(!ps.get_location().equals(corner)){
            return Command.createMoveCommand(Helper.moveTo(ps.get_location(), corner));
        }
        // Abort taking out if animal is too close
        if (min_dist < 3.0 && ps.is_player_searching() && ps.get_held_item_type() == null) {
            return new Command(CommandType.ABORT);
        }
        // Put food away if anim
        // Keep food item back if animal is too close
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null && min_dist < 2.0) {
            return new Command(CommandType.KEEP_BACK);
        }
        // If no animal is near then take out food
        else if (!ps.is_player_searching() &&  min_dist >= 5 && ps.get_held_item_type() == null) {
            // Implement priority: cookie --> non sandwhich --> sandwhich 
            FoodType[] ordered = new FoodType[]{FoodType.COOKIE, FoodType.FRUIT1, FoodType.FRUIT2, FoodType.EGG, FoodType.SANDWICH1, FoodType.SANDWICH2};
            for (FoodType food_type: ordered) {
                if (ps.check_availability_item(food_type)) {
                    Command c = new Command(CommandType.TAKE_OUT, food_type);
                    return c;
                }
            }
        }
        // If no animal in vicinity then take a bite
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
            return new Command(CommandType.EAT);
        }
        
        // Update prev animals to be where animals were this time
        prev_animals = new ArrayList<>(animals);
        return new Command();
    }

}
