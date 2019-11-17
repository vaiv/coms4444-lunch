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
import lunch.sim.AnimalType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

public class Helper {
    /**
     * Helper function: takes current position and movement in one second
     * returns point where boundary is reached
     * @param loc
     * @param dx
     * @param dy
     * @return
    */
    public static Point find_boundary(Point loc, double dx, double dy) {
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
    public static HashMap<Integer, Point> calculateTrajectories(ArrayList<Animal> animals, ArrayList<Animal> prev_animals) {
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

    public static ArrayList<Animal> findIncomingMonkeys (ArrayList<Animal> animals, ArrayList<Animal> prev_animals, PlayerState ps){
	    ArrayList<Animal> incomingMonkeys = new ArrayList<Animal>();
    	for(int i = 0; i<animals.size();i++){
	        if (animals.get(i).which_animal() == AnimalType.GOOSE){
		        continue;
	        }
	        Point curr_loc = animals.get(i).get_location();
            Point prev_loc = prev_animals.get(i).get_location();
            double delta_x = curr_loc.x - prev_loc.x;
            double delta_y = curr_loc.y - prev_loc.y;
            
            double animal_slope = delta_y/delta_x;

            Point my_loc = ps.get_location();
            
            double an_human_dx = my_loc.x - prev_loc.x;
            double an_human_dy = my_loc.y - prev_loc.y;
            
            double human_slope = an_human_dy / an_human_dx;
            
            if ((animal_slope >= human_slope - 1) && (animal_slope <= human_slope + 1)) {
                incomingMonkeys.add(animals.get(i));
            }
        }
        return incomingMonkeys;
    }

    public static ArrayList<Animal> findIncomingGeese (ArrayList<Animal> animals, ArrayList<Animal> prev_animals, PlayerState ps){
        ArrayList<Animal> incomingGeese = new ArrayList<Animal>();
        for(int i = 0; i < animals.size(); i++){
            if (animals.get(i).which_animal() == AnimalType.MONKEY){
                continue;
            }
            Point curr_loc = animals.get(i).get_location();
            Point prev_loc = prev_animals.get(i).get_location();
            double delta_x = curr_loc.x - prev_loc.x;
            double delta_y = curr_loc.y - prev_loc.y;

            double animal_slope = delta_y / delta_x;

            Point my_loc = ps.get_location();

            double an_human_dx = my_loc.x - prev_loc.x;
            double an_human_dy = my_loc.y - prev_loc.y;

            double human_slope = an_human_dy / an_human_dx;

            if ((animal_slope >= human_slope - 1) && (animal_slope <= human_slope + 1)) {
                incomingGeese.add(animals.get(i));
            }
        }
        return incomingGeese;
    }
    
    double getGeeseTime(ArrayList<Animal> animals, ArrayList<Animal> incomingGeese, PlayerState ps) {
        double minTime = Double.MAX_DOUBLE;
        double d, t;
        for (Animal goose: incomingGeese) {
            d = Point.dist(goose.get_location(), ps.get_location());
            t = (d - 2.0) / goose.get_max_speed();
            if (t < minTime) {
                minTime = t;
            }
        }
        return minTime;
    }
    
    /**
     * Helper function: move a player to a new target position
     * returns the next position in the next second
     * @param currPos
     * @param targetPos
     * @return
     */
    public static Point moveTo(Point currPos, Point targetPos) {
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
    public Point getAnimalDisplacement(Animal animal, Animal prevAnimal) {
        Point currPos = animal.get_location();  // current location
        Point prevPos = prevAnimal.get_location();  // previous location
        double delta_x = currPos.x - prevPos.x;  // movement on the x-direction
        double delta_y = currPos.y - prevPos.y;  // movement on the y-direction
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
    public HashMap<Integer, Point> calculateDisplacement(ArrayList<Animal> animals, ArrayList<Animal> prev_animals) {
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
    
    /**
     * Helper function: takes current position of animals and target location
     * returns time for the animal to move to the target location
     * @param animal
     * @param loc
     * @return
     */
    public double getAnimalMoveTime(Animal animal, Point loc) {
        double dist = Point.dist(animal.get_location(), loc);  // distance to the target location
        double time = dist / animal.get_max_speed();
        return time;
    }
    
    /**
     * Helper function: takes current position and movement of an animal and player state
     * returns time for this animal to arrive if player starts eating
     * @param animal
     * @param dx
     * @param dy
     * @param ps
     * @return
     */
    public double getRemainingTime(Animal animal, double dx, double dy, PlayerState ps) {
        // NOT YET FINISHED
        AnimalType type = animal.which_animal();  // type of the animal (MONKEY or GOOSE)
        Point locBoundary = find_boundary(animal.get_location(), dx, dy);  // the point on the boundary where the animal will hit
        double timeToBoundary = getAnimalMoveTime(animal, locBoundary);  // time for the animal to arrive at that point
        double new_x = animal.get_location().x + 10 * dx;
        double new_y = animal.get_location().y + 10 * dy;
        Point loc10Sec = new Point(new_x, new_y);  // location of the animal in 10 seconds, assuming it moves forward and no boundary
        double seeDist;  // distance to player at which animal can see the food
        double stealDist; // distance to player at which animal can steal the food
        switch (type) {
            case MONKEY:
                seeDist = 40.0;
                stealDist = 5.0;
                break;
            case GOOSE:
                seeDist = 20.0;
                stealDist = 2.0;
                break;
            default:
                seeDist = 0.0;
                stealDist = 0.0;
                break;
        }
        // TODO
        return 0.0;
    }
    
    public static Command takeOutFood(PlayerState ps){
        // Implement priority: cookie --> non sandwhich --> sandwhich 
        FoodType[] ordered = new FoodType[]{FoodType.COOKIE, FoodType.FRUIT1, FoodType.FRUIT2, FoodType.EGG, FoodType.SANDWICH1, FoodType.SANDWICH2};
        for (FoodType food_type: ordered) {
            if (ps.check_availability_item(food_type)) {
                Command c = new Command(CommandType.TAKE_OUT, food_type);
                return c;
            }
        }
        return new Command(); 
    }

    
}
