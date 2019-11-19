package lunch.g3;

import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import javafx.util.Pair;
import lunch.sim.*;
import javafx.util.Pair;


public class Player implements lunch.sim.Player {
    // Initialization function.
    // members: other family members collborating with your player.
    // members_count: Number of family members.
    // t: Time limit for simulation.

    public String init(ArrayList<Family> members,Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        return "";
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
        if (ps.check_availability_item(FoodType.COOKIE)) return FoodType.COOKIE;
        else if (ps.check_availability_item(FoodType.EGG)) return FoodType.EGG;
        else if (ps.check_availability_item(FoodType.FRUIT2)) return FoodType.FRUIT2;
        else if (ps.check_availability_item(FoodType.FRUIT1)) return FoodType.FRUIT1;
        else if (ps.check_availability_item(FoodType.SANDWICH2)) return FoodType.SANDWICH2;
        else if (ps.check_availability_item(FoodType.SANDWICH1)) return FoodType.SANDWICH1;
        else return null;
    }

    public boolean shouldStopEating(ArrayList<Animal> animals, PlayerState ps) {
        int dangerMonkeys = 0;
        for (Animal animal : animals) {
            if(animal.which_animal() == AnimalType.GOOSE) {
                if(ps.get_held_item_type() == FoodType.SANDWICH && distToAnimal(animal, ps) <= 6) {
                    return true;
                }
            } else {
                //monkey
                if(distToAnimal(animal, ps) <= 6) {
                    dangerMonkeys++;
                    if(dangerMonkeys == 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean shouldFinishRemoving(ArrayList<Animal> animals, PlayerState ps) {
        int secondsRemaining = ps.time_to_finish_search(); //Will be used later
        int dangerMonkeys = 0;
        for (Animal animal : animals) {
            if(animal.which_animal() == AnimalType.GOOSE) {
                //TODO: check if we're pulling out a sandwich
                if(distToAnimal(animal, ps) <= 6) {
                    return false;
                }
            } else {
                //monkey
                if(distToAnimal(animal, ps) <= 6) {
                    dangerMonkeys++;
                    if(dangerMonkeys == 3) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public double distToAnimal(Animal animal, PlayerState ps) {
        return Math.sqrt(Math.pow(animal.get_location().x - ps.get_location().x, 2) + Math.pow(animal.get_location().y - ps.get_location().y, 2));
    }

    public Point locateNearestCorner(PlayerState ps){
        ArrayList<Point> corners = new ArrayList<Point>();
		Point top_right = new Point(50, -50);
		Point top_left = new Point(-50, -50);
        Point bottom_left = new Point(-50, 50);
        Point bottom_right = new Point(50, 50);
		corners.add(top_right);
        corners.add(top_left);
        corners.add(bottom_right);
        corners.add(bottom_left);
        Point chosenCorner = top_right;
        double minDist = Integer.MAX_VALUE;
        for (int i = 0; i < corners.size(); i++){
            double dist = distanceBetweenPoints(corners.get(i), ps.get_location());
            if (dist <= minDist){
                minDist = dist;
                chosenCorner = corners.get(i);
            }
        }
		return chosenCorner;
    }

    public Point getNextMoveToCorner(PlayerState ps){
        Point nearestCorner = locateNearestCorner(ps);
        Double bearing = getBearingFromDestination(ps.get_location(), nearestCorner);
		Point move_to_corner = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
        return move_to_corner;
    }


    public Double getBearingFromDestination(Point playerLocation, Point destination){
        double deltax = destination.x - playerLocation.x;
        double deltay = destination.y - playerLocation.y;
		return Math.atan2(deltay, deltax);
	}

    public double distanceBetweenPoints(Point pt1, Point pt2){
		double distance = Math.sqrt((pt1.x - pt2.x) * (pt1.x - pt2.x) + (pt1.y - pt2.y) * (pt1.y - pt2.y));
        return distance;
	}

    public boolean inCorner(PlayerState ps){
        ArrayList<Point> corners = new ArrayList<Point>();
		Point top_right = new Point(50, -50);
		Point top_left = new Point(-50, -50);
        Point bottom_left = new Point(-50, 50);
        Point bottom_right = new Point(50, 50);
		corners.add(top_right);
        corners.add(top_left);
        corners.add(bottom_right);
        corners.add(bottom_left);
        boolean corner = false;
        for (int i = 0; i < corners.size(); i++){
            if (distanceBetweenPoints(corners.get(i), ps.get_location()) < 3){
                return true;
            }
        }
        return corner;
    }
}
