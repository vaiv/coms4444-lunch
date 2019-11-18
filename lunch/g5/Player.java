package lunch.g5;

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

public class Player implements lunch.sim.Player {

    // Configs for calculating future matrices
    private int nTimesteps = 10;           // How much timesteps in the future to calculate
    private final double dangerDistance = 5.0;   // How far way from the monkeys we still consider them in calculation
    private final int bufferLookahead = 0;       // When calculating a matrix, how much aditional steps to consider

    // An array to store the animals in previous turn (Mainly to know their positions, so we know where they are going)
    private ArrayList<Animal> previousAnimals;
    
    private boolean moving = false;
    private int towardX;
    private int towardY;
    private int waitStep = 0;
    private FoodType searching;

    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;
    private String avatars;

    public Player() {
        turn = 0;
    }

    public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        this.id = id;
        avatars = "flintstone";
        random = new Random(s);
        return avatars;
    }


    /**
     * A function that calculates board matrices in the future.
     *
     * IMPORTANT: The matrices it returns have a diferent indexing method. The indices correspond to coordinates on the
     * board. So m.get(0, 0) will always get the value at Point(0, 0) and not the point of the player.
     *
     * Example usage:
     *
     * ArrayList<Matrix> matrices = getFutureMatrices(animals);
     * matrices.get(9).show(); // Prints a matrix at timestep 9 to System.out
     * System.print(matrices.get(9).get(10, 10); // Prints what will happen at point (10, 10) at timestep 9
     *
     * @param animals: An array of current elements on the board
     * @param ps: Current player state object
     * @return An array of matrices, where each matrix under index j is predicted state of the board after
     *         j timesteps.
     */
    private ArrayList<Matrix> getFutureMatrices(ArrayList<Animal> animals, PlayerState ps) {
        if (turn < 1) {
            throw new RuntimeException("Cannot calculate future matrices on 0th turn");
        }
        if (previousAnimals == null) {
            throw new RuntimeException("No previous animals saved => cannot calculate the animal directions");
        }
        ArrayList<Matrix> timesteps = new ArrayList<>();
        // Initiate empty matrices for each timestep
        Point ourLocation = ps.get_location();
        int ourLocationX = (int)Math.round(ourLocation.x);
        int ourLocationY = (int)Math.round(ourLocation.y);
        for (int i = 0; i < nTimesteps; i++) {
            int size = i + 3 + bufferLookahead;
            int sizeToLeft = Math.min(size, 50 + ourLocationX);
            int sizeToTop = Math.min(size, 50 + ourLocationY);
            int sizeToRight = Math.min(size, 50 - ourLocationX);
            int sizeToBottom = Math.min(size, 50 - ourLocationY);
            Matrix m = new Matrix(sizeToLeft + sizeToRight + 1, sizeToBottom + sizeToTop + 1);
            int originX = sizeToLeft - ourLocationX;
            int originY = sizeToTop - ourLocationY;
            m.setOrigin(originX, originY);
            timesteps.add(m);
        }
        // Go through each animal and 'draw' it's path on the matrices
        for (int i = 0; i < animals.size(); i++) {
            Animal animal = animals.get(i);
            Animal previousAnimal = previousAnimals.get(i);
            // Only consider monkeys for now
            if (animal.which_animal() == AnimalType.MONKEY) {
                Point currentLocation = animal.get_location();
                Point previousLocation = previousAnimal.get_location();
                // Get animal direction vector
                Point directionVector = PointUtilities.substract(currentLocation, previousLocation);
                // We start with timestep 0, which is the current timestep
                Point nextLocation = currentLocation;
                for (int j = 0; j < nTimesteps; j++) {
                    Matrix m = timesteps.get(j);
                    // Increase all counters in the radius of dangerDistance
                    // We look in a square of dangetDistance around a monekey;
                    int monkeyXFrom = (int)Math.floor(nextLocation.x - dangerDistance);
                    int monkeyXTo = (int)Math.ceil(nextLocation.x + dangerDistance);
                    int monkeyYFrom = (int)Math.floor(nextLocation.y - dangerDistance);
                    int monkeyYTo = (int)Math.ceil(nextLocation.y + dangerDistance);
                    for (int x = monkeyXFrom; x <= monkeyXTo; x++) {
                        for (int y = monkeyYFrom; y <= monkeyYTo; y++) {
                            // If our matrix does not have these coordinates we continue
                            if (!m.has(x, y)) {
                                continue;
                            }
                            // We check if the distance is actually less than danger distance
                            double distance = Point.dist(nextLocation, new Point(x, y));
                            if (distance > dangerDistance) {
                                continue;
                            }
                            m.increment(x, y);
                        }
                    }
                    // Make a step after each timestep
                    nextLocation = PointUtilities.add(nextLocation, directionVector);
                }
            }
        }
        return timesteps;
    }
    
    private boolean isClean(int range, int x, int y, Matrix m) {
    		for(int i = Math.max(x-range, -50); i < x+range && i <= 50; i++) {
    			for(int j = Math.max(y-range, -50); j < y+range && j <= 50; j++) {
    				if(m.get(i, j) != 0)
    					return false;
    			}
    		}
    		return true;
    }

    private void findSafePosition(ArrayList<Animal> animals, PlayerState ps, int range) {
	    	int step = 0;
	    	int x = (int) ps.get_location().x;
	    int y = (int) ps.get_location().y;
	    	while(step <= 100) {
	    		nTimesteps = step + 10;
	    		ArrayList<Matrix> matrices = getFutureMatrices(animals, ps);
	    		Matrix m = matrices.get(nTimesteps-1);
	    		for(int i = 0; i <= step; i++) {
	    			for(int j = 0; j <= step - i; j++) {
	    				int[][] directions = new int[][]{{x+i, y+j}, {x-i, y+j}, {x+i, y-j}, {x-i, y+j}};
	    				for(int[] dir: directions) {
	    					if(isClean(range, dir[0], dir[1], m)) {
	        					towardX = dir[0];
	        					towardY = dir[1];
	        					waitStep = step - i - j;
	        					moving = true;
	        					return;
	        				}
	    				}
	    			}
	    		}
	    		step++;
	    }
    }
    
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        if (turn <= 1) {
        		// Go to an interger position in the first turn
        		Point p = new Point(Math.floor(ps.get_location().x), Math.floor(ps.get_location().y));
        		turn++;
        		previousAnimals = animals;
        		return Command.createMoveCommand(p);
        }
        
        // move to a place which has no animal within this range
        int clean_range = 10;
        
        int x = (int) ps.get_location().x;
        int y = (int) ps.get_location().y;
        Command command = null;
        
        // not moving and not holding or searching anything, find the safe position
        if(!moving && !ps.is_player_searching() && ps.get_held_item_type() == null) {
        		findSafePosition(animals, ps, clean_range);
	    }
        // danger
		// [surrounded by monkey] or [has close goose while holding or searching sandwich]
		if(Utilities.monkey_surround(animals, ps.get_location()) ||
				(Utilities.goose_close(animals, ps.get_location()) && 
						(ps.get_held_item_type() == FoodType.SANDWICH || 
						(ps.is_player_searching() && searching == FoodType.SANDWICH)))) {
			// abort if searching
			if(ps.is_player_searching() && ps.get_held_item_type() == null) {
		        turn++;
		        previousAnimals = animals;
				return new Command(CommandType.ABORT);
			}
			// put back if holding
			if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
		        turn++;
		        previousAnimals = animals;
				return new Command(CommandType.KEEP_BACK);
			}
		}
		//safe
        if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
        		command = new Command(CommandType.EAT);
        }
        else if(waitStep > 0) {
        		waitStep--;
        		command = new Command(CommandType.WAIT);
        	}
        // in the moving process
        else if(moving) {
        		// arrived designate destination, start taking out
        		if(towardX == x && towardY == y) {
        			moving = false;
        			for(FoodType food_type: FoodType.values()) {
        				if(ps.check_availability_item(food_type)) {
        					command = new Command(CommandType.TAKE_OUT, food_type);
        				}
        			}
        		}
        		// haven't arrive, continue moving
        		else if(towardX < x) {
        			Point nextMove = new Point(x-1, y);
        			command = Command.createMoveCommand(nextMove);
        		}
        		else if(towardX > x) {
        			Point nextMove = new Point(x+1, y);
        			command = Command.createMoveCommand(nextMove);
        		}
        		else if(towardY < y) {
        			Point nextMove = new Point(x, y-1);
        			command = Command.createMoveCommand(nextMove);
        		}
        		else if(towardY > y) {
        			Point nextMove = new Point(x, y+1);
        			command = Command.createMoveCommand(nextMove);
        		}
        }
        
        // Increase the turn counter
        turn++;
        // Store animals for the next turn
        previousAnimals = animals;
        return command;
    }
}

