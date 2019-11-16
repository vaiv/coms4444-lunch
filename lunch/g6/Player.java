package lunch.g6;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair; 
import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player {
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;
    private String avatars;

    private ArrayList<Animal> prev_animals;
    private HashMap<Integer, Point> trajectories;

    public Player() {
        turn = 0;
    }
    
    public void init(ArrayList<Family> members, int id, int f, ArrayList<Animal> animals, int m, int g, double t, int s) {
        this.id = id;
        avatars = "flintstone";
        random = new Random(s);
        prev_animals = new ArrayList<>();
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        // Calculate the trajectories of animals
        trajectories = calculateTrajectories(animals, prev_animals);
        
        Double min_dist = Double.MAX_VALUE;
        for (int i = 0; i < animals.size(); i++) {
            min_dist = Math.min(min_dist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }
        
        if (turn < 100) {
            boolean found_valid_move = false;
            Point next_move = new Point(-1, -1);
            while (!found_valid_move) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                found_valid_move = Point.within_bounds(next_move);
            }
            // System.out.println("move command issued");
            turn++;
            return Command.createMoveCommand(next_move);
        }

        // Abort taking out if animal is too close
        if (min_dist < 3.0 && ps.is_player_searching() && ps.get_held_item_type() == null) {
            // System.out.println("abort command issued");
            // System.out.println(min_dist.toString());
            return new Command(CommandType.ABORT);
        }
        // Keep food item back if animal is too close
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null && min_dist < 2.0) {
            return new Command(CommandType.KEEP_BACK);
        }
        // Move away from animal
        else if (min_dist < 3.0) {
            boolean found_valid_move = false;
            Point next_move = new Point(-1, -1);
            while (!found_valid_move) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                found_valid_move = Point.within_bounds(next_move);
            }
            return Command.createMoveCommand(next_move);
        }
        // If no animal is near then take out food
        else if (!ps.is_player_searching() &&  min_dist >= 5 && ps.get_held_item_type() == null) {
            for (FoodType food_type: FoodType.values()) {
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
    
    /**
     * Helper function: takes current position and movement in one second
     * returns point where boundary is reached
     * @param loc
     * @param dx
     * @param dy
     * @return
    */
    private Point find_boundary(Point loc, double dx, double dy) {
        double curr_x = loc.x;
        double curr_y = loc.y;
        double t1, t2, new_x, new_y;
        // Possible new location: (curr_x + t * dx, curr_y + t * dy)
        if (dx > 0) {
            if (dy > 0) {
                // Case 1: dx > 0, dy > 0. Intersect at lower or right boundaries
                t1 = (50.0 - curr_x) / dx;
                t2 = (50.0 - curr_y) / dy;
                if (t1 > t2) {
                    // Intersect at lower boundary
                    new_x = curr_x + t2 * dx;
                    new_y = 50.0;
                } else {
                    // Intersect at right boundary
                    new_x = 50.0;
                    new_y = curr_y + t1 * dy;
                }
            } else if (dy < 0) {
                // Case 2: dx > 0, dy < 0. Intersect at upper or right boundaries
                t1 = (50.0 - curr_x) / dx;
                t2 = (-50.0 - curr_y) / dy;
                if (t1 > t2) {
                    // Intersect at upper boundary
                    new_x = curr_x + t2 * dx;
                    new_y = -50.0;
                } else {
                    // Intersect at right boundary
                    new_x = 50.0;
                    new_y = curr_y + t1 * dy;
                }
            } else {
                // Case 3: dx > 0, dy = 0. Intersect at right boundary
                new_x = 50.0;
                new_y = curr_y;
            }
        } else if (dx < 0) {
            if (dy > 0) {
                // Case 4: dx < 0, dy > 0. Intersect at lower or left boundaries
                t1 = (-50.0 - curr_x) / dx;
                t2 = (50.0 - curr_y) / dy;
                if (t1 > t2) {
                    // Intersect at lower boundary
                    new_x = curr_x + t2 * dx;
                    new_y = 50.0;
                } else {
                    // Intersect at left boundary
                    new_x = -50.0;
                    new_y = curr_y + t1 * dy;
                }
            } else if (dy < 0) {
                // Case 5: dx < 0, dy < 0. Intersect at upper or left boundaries
                t1 = (-50.0 - curr_x) / dx;
                t2 = (-50.0 - curr_y) / dy;
                if (t1 > t2) {
                    // Intersect at upper boundary
                    new_x = curr_x + t2 * dx;
                    new_y = -50.0;
                } else {
                    // Intersect at left boundary
                    new_x = -50.0;
                    new_y = curr_y + t1 * dy;
                }
            } else {
                // Case 3: dx < 0, dy = 0. Intersect at left boundary
                new_x = -50.0;
                new_y = curr_y;
            }
        } else {
            if (dy > 0) {
                // Case 7: dx = 0, dy > 0. Intersect at lower boundary
                new_x = curr_x;
                new_y = 50.0;
            } else if (dy < 0) {
                // Case 8: dx = 0, dy < 0. Intersect at upper boundary
                new_x = curr_x;
                new_y = -50.0;
            } else {
                // Case 9: dx = 0, dy = 0. No intersection (should not happen)
                new_x = 55.0;
                new_y = 55.0;
            }
        }
        Point new_loc = new Point(new_x, new_y);
        return new_loc;
    }

    /**
     * Helper function: takes current position of animals and previous positions of animals
     * returns hashmap of mapping: location in animal_arr --> point where intersect boundary
     * the point returned would not be valid if the animal senses food on its route
     * @param animals
     * @param prev_animals
     * @return
     */
    private HashMap<Integer, Point> calculateTrajectories(ArrayList<Animal> animals, ArrayList<Animal> prev_animals) {
        HashMap<Integer, Point> result = new HashMap<>();
        // This assumes animal's position in array does not change
        for (int i = 0; i < prev_animals.size(); i++){
            Point curr_loc = animals.get(i).get_location();
            Point prev_loc = prev_animals.get(i).get_location();
            double delta_x = curr_loc.x - prev_loc.x;
            double delta_y = curr_loc.y - prev_loc.y;
            Point end_loc = find_boundary(curr_loc, delta_x, delta_y);
            result.put(i, end_loc);
            // double slope = delta_y/delta_x;
            // System.out.println("-----------------------");
            // System.out.println(curr_loc + " vs " + prev_loc  + " slope " + slope);
            // System.out.println(find_boundary(curr_loc, delta_x, delta_y));
            // Double dist = Math.sqrt(Math.pow(curr_loc.x - prev_loc.x, 2) + Math.pow(curr_loc.y-prev_loc.y,2));
            // System.out.println("dist: "+ dist);
        }
        return result;
    }
    
    /**
     * Helper function: move a player to a new target position
     * returns the next position in the next second
     * @param currPos
     * @param targetPos
     * @return
     */
    private Point moveTo(Point currPos, Point targetPos) {
        if (Point.dist(currPos, targetPos) <= 1.0) {
            // Distance to target position is no larget than 1
            return targetPos;
        }
        double dist = Point.dist(currPos, targetPos);  // distance to target position
        double disp_x = (targetPos.x - currPos.x) * 1.0 / dist;  // movement on the x-direction
        double disp_y = (targetPos.y - currPos.y) * 1.0 / dist;  // movement on the y-direction
        double next_x = currPos.x + disp_x;  // next position (x-coord)
        double next_y = currPos.y + disp_y;  // next position (y-coord)
        Point nextPos = new Point(next_x, next_y);
        return nextPos;
    }
    
    /**
     * Helper function: takes current position of one animal and previous position of this animal
     * returns movement (displacement) of this animal in one second
     * @param animal
     * @param prevAnimal
     * @return
     */
    private Point getAnimalDisplacement(Animal animal, Animal prevAnimal) {
        Point currPos = animal.get_location();
        Point prevPos = prevAnimal.get_location();
        double delta_x = curr_loc.x - prev_loc.x;
        double delta_y = curr_loc.y - prev_loc.y;
        Point displacement = new Point(delta_x, delta_y);
        return displacement;
    }
    
    /**
     * Helper function: takes current position of animals and previous positions of animals
     * returns hashmap of mapping: location in animal_arr --> movement (displacement) in one second
     * @param animals
     * @param prev_animals
     * @return
     */
    private HashMap<Integer, Point> calculateDisplacement(ArrayList<Animal> animals, ArrayList<Animal> prev_animals) {
        HashMap<Integer, Point> result = new HashMap<>();
        // This assumes animal's position in array does not change
        for (int i = 0; i < prev_animals.size(); i++){
            Animal animal = animals.get(i);
            Animal prevAnimal = prev_animals.get(i);
            Point displacement = getAnimalDisplacement(animal, prevAnimal);
            result.put(i, displacement);
        }
        return result;
    }
    
}
