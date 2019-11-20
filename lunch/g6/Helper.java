package lunch.g6;

import java.util.Arrays;
import java.util.List;
import java.lang.Double;
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

    // Get the list of incoming monkeys
    //public static ArrayList<Animal> findIncomingMonkeys(ArrayList<Animal> animals, ArrayList<Animal> prev_animals, PlayerState ps) {
    //    ArrayList<Animal> incomingMonkeys = new ArrayList<Animal>();
    //    for (int i = 0; i < animals.size(); i++) {
    //        if (animals.get(i).which_animal() == AnimalType.GOOSE) {
    //            continue;
    //        }
    //        Point curr_loc = animals.get(i).get_location();
    //        Point prev_loc = prev_animals.get(i).get_location();
    //        double delta_x = curr_loc.x - prev_loc.x;
    //        double delta_y = curr_loc.y - prev_loc.y;
	//        double animal_slope = delta_y / delta_x;
    //        Point my_loc = ps.get_location();
    //        double an_human_dx = my_loc.x - prev_loc.x;
    //        double an_human_dy = my_loc.y - prev_loc.y;
    //        double human_slope = an_human_dy / an_human_dx;
    //        if ((animal_slope >= human_slope - 1) && (animal_slope <= human_slope + 1)) {
    //            incomingMonkeys.add(animals.get(i));
    //        }
    //    }
    //    return incomingMonkeys;
    //}

    // Get the list of incoming geese
    //public static ArrayList<Animal> findIncomingGeese(ArrayList<Animal> animals, ArrayList<Animal> prev_animals, PlayerState ps) {
    //    ArrayList<Animal> incomingGeese = new ArrayList<Animal>();
    //    for(int i = 0; i < animals.size(); i++){
    //        if (animals.get(i).which_animal() == AnimalType.MONKEY){
    //            continue;
    //        }
    //        Point curr_loc = animals.get(i).get_location();
    //        Point prev_loc = prev_animals.get(i).get_location();
    //        double delta_x = curr_loc.x - prev_loc.x;
    //        double delta_y = curr_loc.y - prev_loc.y;
    //        double animal_slope = delta_y / delta_x;
    //        Point my_loc = ps.get_location();
    //        double an_human_dx = my_loc.x - prev_loc.x;
    //        double an_human_dy = my_loc.y - prev_loc.y;
    //        double human_slope = an_human_dy / an_human_dx;
    //        if ((animal_slope >= human_slope - 1) && (animal_slope <= human_slope + 1)) {
    //            incomingGeese.add(animals.get(i));
    //        }
    //    }
    //    return incomingGeese;
    //}
    
    // Get the dot product of two vectors (given as Points)
    public static double dot(Point p1, Point p2) {
        double d = p1.x * p2.x + p1.y * p2.y;
        return d;
    }
        
    // Get the new location of an animal after t seconds
    public static Point newLocation(Animal animal, Animal prevAnimal, double t) {
        Point traj = getAnimalDisplacement(animal, prevAnimal);
        double new_x = animal.get_location().x + t * traj.x;
        double new_y = animal.get_location().y + t * traj.y;
        Point newLoc = new Point(new_x, new_y);
        return newLoc;
    }
    
    // Get the time remaining before one particular goose can steal the food
    public static double getOneGooseTime(Animal goose, Animal prevGoose, PlayerState ps) {
        double curr_d = Point.dist(goose.get_location(), ps.get_location());
        double t;
        Point traj = getAnimalDisplacement(goose, prevGoose);
        Point hitLoc = find_boundary(goose.get_location(), traj.x, traj.y);
        double timeToWall = getAnimalMoveTime(goose, hitLoc);
        double timeToPlayer = getMoveTime(hitLoc, ps.get_location(), goose.get_max_speed());
        // Case 1: The goose is outside the boundary (in the nest)
        if (!Point.within_bounds(goose.get_location())) {
            return Double.MAX_VALUE;
        }
        // Case 2: The goose is within 20m from the player, and the player is holding a sandwich
        if (curr_d <= 20.0 && ps.get_held_item_type() == FoodType.SANDWICH) {
            t = (curr_d - 2.0) / goose.get_max_speed();
            return t;
        }
        // Case 3: The goose is not within 20m from the player, and the player is holding a sandwich
        if (curr_d > 20.0 && ps.get_held_item_type() == FoodType.SANDWICH) {
            Point toPlayer = new Point(ps.get_location().x - goose.get_location().x, ps.get_location().y - goose.get_location().y);
            double dotProduct = dot(traj, toPlayer);
            if (dotProduct > 0) {
                // Assume the goose is coming towards the player
                t = (curr_d - 2.0) / goose.get_max_speed();
                return t;
            } else {
                // The goose is not coming towards the player
                t = timeToWall + timeToPlayer;
                return t;
            }
        }
        // Case 4: The player is not holding a sandwich
        if (ps.get_held_item_type() != FoodType.SANDWICH) {
            double timeToStart = ps.is_player_searching() ? ps.time_to_finish_search() : 10.0;
            Point newLoc = newLocation(goose, prevGoose, timeToStart);
            if (timeToWall <= timeToStart) {
                // The goose hits the wall and then comes towards the player
                t = timeToWall + timeToPlayer;
                return t;
            } else {
                // The goose has not yet hit the wall
                Point toPlayer = new Point(ps.get_location().x - newLoc.x, ps.get_location().y - newLoc.y);
                double dotProduct = dot(traj, toPlayer);
                if (dotProduct > 0) {
                    double new_d = Point.dist(ps.get_location(), newLoc);
                    t = (new_d - 2.0) / goose.get_max_speed();
                    return t;
                } else {
                    t = timeToWall + timeToPlayer;
                    return t;
                }
            }
        }
        // This scenario should never apply, just in case
        return Double.MAX_VALUE;
    }
    
    // Get the time remaining before a goose can steal the food
    public static double getGeeseTime(ArrayList<Animal> animals, ArrayList<Animal> prevAnimals, PlayerState ps) {
        double minTime = Double.MAX_VALUE;
        double t;
        for (int i = 0; i < animals.size(); i++) {
            Animal goose = animals.get(i);
            Animal prevGoose = prevAnimals.get(i);
            if (goose.which_animal() == AnimalType.MONKEY)
                continue;
            t = getOneGooseTime(goose, prevGoose, ps);
            if (t < minTime) {
                minTime = t;
            }
        }
        return minTime;
    }

    // Get the time remaining before monkeys can steal the food
    public static double getMonkeyTime(ArrayList<Animal> animals, PlayerState ps) {
        // Problem with this code: only considers monkeys that are headed in your direction. Must fix later.
        // 1. Find three closest Monkeys. How much time will it take 3rd closest monkey to get to you?
        double close_1 = Double.MAX_VALUE;
        double close_2 = Double.MAX_VALUE;
        double close_3 = Double.MAX_VALUE;
        double dist_between = 0;
        for (int i = 0; i < animals.size(); i++) {
            Animal cur_Monkey = animals.get(i);
            if (cur_Monkey.which_animal() == AnimalType.GOOSE)
                continue;
            Point monkey_loc = cur_Monkey.get_location();
            Point my_loc = ps.get_location();
            dist_between = Point.dist(my_loc, monkey_loc);
            if (dist_between < close_1) {
                close_3 = close_2;
                close_2 = close_1;
                close_1 = dist_between;
            } else if (dist_between < close_2) {
                close_3 = close_2;
                close_2 = dist_between;
            } else if (dist_between < close_3) {
                close_3 = dist_between;
            }
        }
        double time_to_reach = close_3 - 5;
        return time_to_reach;
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
    public static Point getAnimalDisplacement(Animal animal, Animal prevAnimal) {
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
    public static HashMap<Integer, Point> calculateDisplacement(ArrayList<Animal> animals, ArrayList<Animal> prev_animals) {
        HashMap<Integer, Point> result = new HashMap<>();
        // This assumes animal's position in array does not change
        for (int i = 0; i < prev_animals.size(); i++) {
            Animal animal = animals.get(i);
            Animal prevAnimal = prev_animals.get(i);
            Point displacement = getAnimalDisplacement(animal, prevAnimal);
            result.put(i, displacement);
        }
        return result;
    }
    
    // Get the move time from location A to location B
    public static double getMoveTime(Point locA, Point locB, double speed) {
        double dist = Point.dist(locA, locB);
        double time = dist / speed;
        return time;
    }
    
    /**
     * Helper function: takes current position of animals and target location
     * returns time for the animal to move to the target location
     * @param animal
     * @param loc
     * @return
     */
    public static double getAnimalMoveTime(Animal animal, Point loc) {
        double time = getMoveTime(animal.get_location(), loc, animal.get_max_speed());
        return time;
    }
    
    public static Command takeOutFood(PlayerState ps) {
        // Implement priority: cookie --> non sandwich --> sandwich 
        FoodType[] ordered = new FoodType[]{FoodType.COOKIE, FoodType.FRUIT1, FoodType.FRUIT2, FoodType.EGG, FoodType.SANDWICH1, FoodType.SANDWICH2};
        for (FoodType food_type: ordered) {
            if (ps.check_availability_item(food_type)) {
                Command c = new Command(CommandType.TAKE_OUT, food_type);
                return c;
            }
        }
        return new Command(); 
    }

    /**
     * 
     * @param members
     * @param ps
     * @return
     */
    public static Point findSparseLoc(ArrayList<Family> members, PlayerState ps) {
        ArrayList<Point> walls = new ArrayList<Point>(Arrays.asList(new Point(-50, 0), new Point(50, 0), new Point(0, 50), new Point(0, -50)));
        HashMap<Point, ArrayList<Family>> wallMemberMap = new HashMap<>();
        for (Family f: members){
            Point curLoc = f.get_location();
            Point closestWall = walls.get(0);
            for (Point wall: walls){
                if (Point.dist(curLoc, wall) < Point.dist(curLoc, closestWall)) {
                    closestWall = wall; 
                }
                if (!wallMemberMap.containsKey(wall)) {
                    wallMemberMap.put(wall, new ArrayList<Family>());
                }
            }
            wallMemberMap.get(closestWall).add(f);
        }
        Point sparsestWall = null; 
        int nearbyFamily = Integer.MAX_VALUE; 
        for (Point wall: wallMemberMap.keySet()) {
            if (wallMemberMap.get(wall).size() < nearbyFamily || sparsestWall == null) {
                nearbyFamily = wallMemberMap.get(wall).size(); 
                sparsestWall = wall; 
            }
        }
        return sparsestWall; 
    }
    
}
