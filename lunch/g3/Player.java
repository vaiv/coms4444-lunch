package lunch.g3;
import lunch.g5.*;

import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import javafx.util.Pair;
import lunch.sim.*;
import javafx.util.Pair;

// g5 dependencies
import java.util.Random;

public class Player implements lunch.sim.Player {
    // Initialization function.
    // members: other family members collborating with your player.
    // members_count: Number of family members.
    // t: Time limit for simulation.
    private FoodType foodToPull;
    private double THRESHOLD = 6.5;
    private boolean firstMove;
    private double foodTimer;

    // Imported from g5's distractor
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;
    private String avatars;
    MatrixPredictor matrixPredictor;

    // An array to store the animals in previous turn (Mainly to know their positions, so we know where they are going)
    private ArrayList<Animal> previousAnimals;
    private GreedyEater greedyEater;
    private DistractionStrategy mDistraction;

    public Player() {
        turn = 0;
        matrixPredictor = new MatrixPredictor(5.0, 6.0, 0);
    }

    public String init(ArrayList<Family> members,Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        firstMove = true;

        // Imported from g5's distractor
        this.id = id;
        random = new Random(s);
        greedyEater = new GreedyEater();
        mDistraction = new DistractionStrategy();
        mDistraction.init(members, id, f, animals, m, g, t, s);

        return new String("");
    };

    // Gets the moves from the player. Number of moves is specified by first parameter.
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        if (distractorExists(ps, members, 3)) {
          if(firstMove) {
              firstMove = false;
              return Command.createMoveCommand(randomMove(ps));
          }

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
                      return new Command(CommandType.ABORT);
                  }
              } else {
                  // Are we in a corner? Yes -> select food
                  if (inCorner(ps) && startPullOut(animals, ps)) {
                      this.foodToPull = selectFood(ps);
                      foodTimer = 0;
                      return Command.createRetrieveCommand(this.foodToPull);
                  } else {
                      if(inCorner((ps))) {
                          foodTimer++;
                      }
                      return Command.createMoveCommand(getNextMoveToCorner(ps));
                  }
              }
          }
        } else {
            Command command = mDistraction.getCommand(members, animals, previousAnimals, ps, true);
            previousAnimals = animals;
            turn++;
            return command;
        }
    };

    public Point randomMove(PlayerState ps) {
        double theta = Math.random() * 2 * Math.PI;
        return new Point(ps.get_location().x + Math.cos(theta), ps.get_location().y + Math.sin(theta));
    }

    public FoodType selectFood(PlayerState ps) {
        if (ps.check_availability_item(FoodType.COOKIE)) return FoodType.COOKIE;
        else if (ps.check_availability_item(FoodType.EGG)) return FoodType.EGG;
        else if (ps.check_availability_item(FoodType.FRUIT2)) return FoodType.FRUIT2;
        else if (ps.check_availability_item(FoodType.FRUIT1)) return FoodType.FRUIT1;
        else if (ps.check_availability_item(FoodType.SANDWICH2)) return FoodType.SANDWICH2;
        else if (ps.check_availability_item(FoodType.SANDWICH1)) return FoodType.SANDWICH1;
        else return null;
    }
    public boolean startPullOut(ArrayList<Animal> animals, PlayerState ps) {
        int monkeysNear = 0;
        double thresholdDist = 35;
        if(foodTimer > 15) {
            thresholdDist = Math.max(thresholdDist - foodTimer/5, 20);
        }
        for(Animal animal : animals) {
            if(animal.which_animal() == AnimalType.MONKEY) {
                if(dist(animal.get_location(), ps.get_location()) < thresholdDist) {
                    monkeysNear++;
                    if(monkeysNear > 2) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public boolean shouldStopEating(ArrayList<Animal> animals, PlayerState ps) {
        int dangerMonkeys = 0;
        for (Animal animal : animals) {
            if(animal.which_animal() == AnimalType.GOOSE) {
                if(ps.get_held_item_type() == FoodType.SANDWICH && dist(animal.get_location(), ps.get_location()) <= THRESHOLD) {
                    return true;
                }
            } else {
                //monkey
                if(dist(animal.get_location(), ps.get_location()) <= THRESHOLD) {
                    dangerMonkeys++;
                    if(dangerMonkeys >= 3) {
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
            if(animal.which_animal() == AnimalType.GOOSE && this.foodToPull == FoodType.SANDWICH) {
                //TODO: check if we're pulling out a sandwich
                if(dist(animal.get_location(), ps.get_location()) <= THRESHOLD) {
                    return false;
                }
            } else {
                //monkey
                if(dist(animal.get_location(), ps.get_location()) <= THRESHOLD) {
                    dangerMonkeys++;
                    if(dangerMonkeys >= 3) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static double dist(Point a, Point b)
    {
        return Math.sqrt(Math.pow(a.x-b.x,2)+Math.pow(a.y-b.y,2));
    }

    public double distToAnimal(Animal animal, PlayerState ps) {
        return Math.sqrt(Math.pow(animal.get_location().x - ps.get_location().x, 2) + Math.pow(animal.get_location().y - ps.get_location().y, 2));
    }

    public Point locateNearestCorner(PlayerState ps){
        ArrayList<Point> corners = new ArrayList<Point>();
		Point top_right = new Point(50, -50);
		Point top_left = new Point(-50, -50);
        Point bottom_left = new Point(-50, 50);
        //Point bottom_right = new Point(50, 50);
		corners.add(top_right);
        corners.add(top_left);
        //corners.add(bottom_right);
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

    private boolean distractorExists(PlayerState ps, ArrayList<Family> members, int partition) {
        double x_threshold = -50+100/partition; double y_threshold = -50+100/partition;
        for (int i = 0; i < members.size(); ++i) {
            Point loc = members.get(i).get_location();
            if (this.id != i && x_threshold <= loc.x && y_threshold <= loc.y) {
                return true;
            }
        }
        return false;
    }
}
