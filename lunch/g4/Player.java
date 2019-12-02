package lunch.g4;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

/**
 * @author adityasridhar
 * 
 * Represents a COMS 4444 player that is having lunch
 *
 */
public class Player implements lunch.sim.Player {
	private Random random;
	private Integer id;
	private Integer turn;
	private Double totalTime;
	private String avatars;
	private List<Animal> monkeys = new ArrayList<>();
	private List<Animal> geese = new ArrayList<>();
	private List<Trajectory> monkeyTrajectories = new ArrayList<>();
	private List<Trajectory> geeseTrajectories = new ArrayList<>();
	private List<Point> targetCorners = Arrays.asList(new Point[]{
			new Point(-50, -50), // Top-left corner
			new Point(1, -50),	 // Top center
			new Point(-50, 1),	 // Left center
			new Point(-50, 50),  // Bottom-left corner
			new Point(1, 50),	 // Bottom center
			new Point(50, 1),	 // Right center
			new Point(50, -50),  // Top-right corner
			new Point(50, 50)    // Bottom-right corner
			});
	private Map<Integer, Point> targetCornersChosen = new HashMap<>();
	private FoodType foodCurrentlySearchingFor = null;
	private static final double MONKEY_DISTANCE_THRESHOLD = 6.0 + 10e-6;
	private static final double MONKEY_VISION_THRESHOLD = 40.0 + 10e-6;
	private static final double GOOSE_DISTANCE_THRESHOLD = 5.0 + 10e-6;
	private static final double GOOSE_VISION_THRESHOLD = 20.0 + 10e-6;
	private static final double TURN_THRESHOLD = 50;
	private double bearing = -1;
	
	/**
	 * Player constructor
	 */
	public Player() {
		turn = 0;
	}

	/**
	 * Initializes the player
	 * 
	 * @param members: all family members
	 * @param id: player ID
	 * @param f: number of family members
	 * @param animals: all animals
	 * @param m: number of monkeys
	 * @param g: number of geese
	 * @param t: simulation time
	 * @param s: seed
	 * 
	 * @return the avatars the player will use
	 * 
	 */
	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id = id;
		this.totalTime = t;
		avatars = "flintstone";
		random = new Random();
		
		monkeyTrajectories = new ArrayList<>();
		geeseTrajectories = new ArrayList<>();

		for(Animal animal : animals) {
			if(animal.which_animal() == AnimalType.MONKEY) {
				monkeyTrajectories.add(new Trajectory(animal.get_location(), true));
			}
			else if(animal.which_animal() == AnimalType.GOOSE)
				geeseTrajectories.add(new Trajectory(animal.get_location(), false));
		}
		
		return avatars;
	}
	
	/**
	 *
	 * Determines the command the player submits based on
	 * information about family members, the available
	 * food items, and the animals
	 *
	 * @param members: all family members
	 * @param animals: all animals
	 * @param ps: this player's state
	 * 
	 * @return the command/action submitted by the player
	 *
	 */
	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {

		int monkeyIndex = 0;
		int gooseIndex = 0;
		int monkeyInRange = 0;
		int geeseInRange = 0;
		for(Animal animal : animals) {
			if(animal.which_animal() == AnimalType.MONKEY) {
				monkeyTrajectories.get(monkeyIndex).update(animal.get_location());
				if(!monkeyTrajectories.get(monkeyIndex).isFar(ps.get_location()))
					monkeyInRange++;
				monkeyIndex++;
			}
			else if(animal.which_animal() == AnimalType.GOOSE) {
				geeseTrajectories.get(gooseIndex).update(animal.get_location());
				if(!geeseTrajectories.get(gooseIndex).isFar(ps.get_location()))
					geeseInRange++;
				gooseIndex++;
			}
		}
		
		// Assign a corner for the player to move to based on family members' locations
		Point currPoint = ps.get_location();
		Point targetCorner = new Point(-1, -1);
		if(turn > TURN_THRESHOLD) {
			if(!targetCornersChosen.containsKey(id)) {
				double maxCornerDistFromFamilyMembers = Double.MIN_VALUE;
				for(Point corner : targetCorners) {
					double closestFamilyMemberDist = Double.MAX_VALUE;
					for(int i = 0; i < members.size(); i++) {
						if(i == id)
							continue;
						
						double familyMemberDist = Point.dist(members.get(i).get_location(), corner);
						if(familyMemberDist < closestFamilyMemberDist)
							closestFamilyMemberDist = familyMemberDist;
					}
					if(closestFamilyMemberDist > maxCornerDistFromFamilyMembers) {
						maxCornerDistFromFamilyMembers = closestFamilyMemberDist;
						targetCorner = corner;
					}
				}
				if(targetCorner.x != -1 && targetCorner.y != -1)
					targetCornersChosen.put(id, targetCorner);
				else
					targetCornersChosen.put(id, targetCorners.get(id % targetCorners.size()));
			}
			else
				targetCorner = targetCornersChosen.get(id);
		
			if(currPoint.x != targetCorner.x || currPoint.y != targetCorner.y) {
				turn++;
				if(ps.get_held_item_type() != null) {
					foodCurrentlySearchingFor = null;
					return new Command(CommandType.KEEP_BACK);
				}
				if(ps.is_player_searching()) {
					foodCurrentlySearchingFor = null;
					return new Command(CommandType.ABORT);
				}
	
				// The player reaches the corner if the distance is within (or exactly) 1 m
				double distanceFromCorner = Math.sqrt(Math.pow(targetCorner.y - currPoint.y, 2) + Math.pow(targetCorner.x - currPoint.x, 2));
				if(distanceFromCorner <= 1.0) {
					return Command.createMoveCommand(targetCorner);
				}
				
				// Move the player toward the corner
				double slope = ((double) (targetCorner.y - currPoint.y)) / ((double) (targetCorner.x - currPoint.x));
				double deltaX = (targetCorner.x > currPoint.x ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				double deltaY = Math.abs(slope) * (targetCorner.y > currPoint.y ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				return Command.createMoveCommand(new Point(currPoint.x + deltaX, currPoint.y + deltaY));
			}
		}
		turn++;
		
		// Determine animals sorted by closest distance to the player
		ArrayList<Animal> clonedAnimals = new ArrayList<>(animals);
		Collections.sort(clonedAnimals, new Comparator<Animal>() {
		    public int compare(Animal animal1, Animal animal2) {
				double distanceToAnimal1 = Point.dist(ps.get_location(), animal1.get_location());
				double distanceToAnimal2 = Point.dist(ps.get_location(), animal2.get_location());
		        return (int) Math.signum(distanceToAnimal1 - distanceToAnimal2);
		    }
		});
						
		// Assign monkeys and geese, each sorted by closest distance to the player
		monkeys = new ArrayList<>();
		geese = new ArrayList<>();
		
		for(Animal animal : clonedAnimals) {
			if(animal.which_animal() == AnimalType.MONKEY && !animal.busy_eating())
				monkeys.add(animal);
			else if(animal.which_animal() == AnimalType.GOOSE && !animal.busy_eating())
				geese.add(animal);
		}
		
		boolean monkeysTooClose, gooseTooClose;
		
		// Determine if at least 3 monkeys are too close
		if(monkeys.size() < 3)
			monkeysTooClose = false;
		else {
			double distanceToFirstMonkey = Point.dist(ps.get_location(), monkeys.get(0).get_location());			
			double distanceToSecondMonkey = Point.dist(ps.get_location(), monkeys.get(1).get_location());			
			double distanceToThirdMonkey = Point.dist(ps.get_location(), monkeys.get(2).get_location());			
			if(distanceToFirstMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToSecondMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToThirdMonkey <= MONKEY_DISTANCE_THRESHOLD) {
				monkeysTooClose = true;
			}
			else
				monkeysTooClose = false;
		}
		
		// Determine if any goose is too close
		if(geese.size() == 0 || Point.dist(ps.get_location(), geese.get(0).get_location()) > GOOSE_DISTANCE_THRESHOLD)
			gooseTooClose = false;
		else {
			gooseTooClose = true;
		}
		
		// Print the state of the player
		printAvailability(ps);

		// Abort taking out the food item if the animal is too close
		if(ps.is_player_searching() && ps.get_held_item_type() == null &&
				(monkeysTooClose || (gooseTooClose && foodCurrentlySearchingFor == FoodType.SANDWICH))) {
			foodCurrentlySearchingFor = null;
			return new Command(CommandType.ABORT);
		}

		// Keep the food item back if the animal is too close
		if(!ps.is_player_searching() && ps.get_held_item_type() != null && (((monkeysTooClose || gooseTooClose) && (ps.get_held_item_type() == FoodType.SANDWICH)) ||
				(monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)))) {
			foodCurrentlySearchingFor = null;
			return new Command(CommandType.KEEP_BACK);
		}
		
		FoodType foodType = ps.check_availability_item(FoodType.COOKIE) ? FoodType.COOKIE : 
			ps.check_availability_item(FoodType.FRUIT1) ? FoodType.FRUIT1 :
			ps.check_availability_item(FoodType.FRUIT2) ? FoodType.FRUIT2 : 
			ps.check_availability_item(FoodType.EGG) ? FoodType.EGG :
			ps.check_availability_item(FoodType.SANDWICH1) ? FoodType.SANDWICH1 :
			ps.check_availability_item(FoodType.SANDWICH2) ? FoodType.SANDWICH2	: 
			null;

		// Take out a food item if no animal is too close
		if (!ps.is_player_searching() && ps.get_held_item_type() == null && !monkeysTooClose) {
			foodCurrentlySearchingFor = null;						
			if(foodType != null) {
				
				boolean usePrediction = true;
				if(monkeyInRange > 2)
					usePrediction = false;
				
				double thirdClosestMonkeyDist = Double.MAX_VALUE;
				if(monkeys.size() > 0)
					thirdClosestMonkeyDist = Point.dist(ps.get_location(), monkeys.get((int) Math.min(monkeys.size() - 1, 2)).get_location());
				double closestGooseDist = Double.MAX_VALUE;
				if(geese.size() > 0)
					closestGooseDist = Point.dist(ps.get_location(), geese.get(0).get_location());
				double thirdClosestMonkeyThreshold = (turn < TURN_THRESHOLD) ? MONKEY_VISION_THRESHOLD / 2 : MONKEY_VISION_THRESHOLD;
				
				// Take out the food item if it is not a sandwich and increase consumption time
				if(foodType != FoodType.SANDWICH1 && foodType != FoodType.SANDWICH2) {
					if(totalTime - turn < 500 || usePrediction) {
						foodCurrentlySearchingFor = foodType;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
				}

				// Take out the food item if it is a sandwich
				if(!gooseTooClose) {
					
					// Take out the sandwich as a distraction
					if(totalTime - turn > 200 && foodType == FoodType.SANDWICH2 && ps.get_time_for_item(foodType) == 1) {
						foodCurrentlySearchingFor = FoodType.SANDWICH;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
					
					// Take out the sandwich for eating
					if(totalTime - turn < 500 || usePrediction) {
						foodCurrentlySearchingFor = FoodType.SANDWICH;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
				}

				return new Command();
			}
		}
		
		if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
			foodCurrentlySearchingFor = null;

			// If there is enough time and there is little left of the last sandwich, use it as a distraction
			if(!monkeysTooClose && !gooseTooClose && totalTime - turn > 200 && foodType == FoodType.SANDWICH2 && ps.get_time_for_item(foodType) == 1)
				return new Command(CommandType.WAIT);

			// Eat if no animal is too close
			if((!monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)) ||
				(!monkeysTooClose && !gooseTooClose && (ps.get_held_item_type() == FoodType.SANDWICH))) {
				return new Command(CommandType.EAT);
			}
		}
		
		// The player is waiting, as it did not submit any other actions
		return new Command(CommandType.WAIT);
	}
	
	/**
	 * Prints all of the available items that
	 * the player currently has left
	 * 
	 * @param ps: this player's state
	 */
	private void printAvailability(PlayerState ps) {
		System.out.println("Cookie is available: " + ps.check_availability_item(FoodType.COOKIE)); 
		System.out.println("Fruit 1 is available: " + ps.check_availability_item(FoodType.FRUIT1)); 
		System.out.println("Fruit 2 is available: " + ps.check_availability_item(FoodType.FRUIT2)); 
		System.out.println("Egg is available: " + ps.check_availability_item(FoodType.EGG)); 
		System.out.println("Sandwich 1 is available: " + ps.check_availability_item(FoodType.SANDWICH1)); 
		System.out.println("Sandwich 2 is available: " + ps.check_availability_item(FoodType.SANDWICH2)); 
	}
	
	public class Trajectory {
		private Point location;
		private Point v;
		private boolean isMonkey;

		public Trajectory(Point l, boolean isMonkey) {
			this.location = l;
			this.v = new Point(0.0, 0.0);
			this.isMonkey = isMonkey;
		}

		public void update(Point l) {
			Point temp = this.location;
			this.location = l;
			this.v.x = l.x - temp.x;
			this.v.y = l.y - temp.y;
		}

		public Point getNext10() {
			Point temp = new Point(0,0);
			temp.x = location.x + v.x * 4.5;
			temp.y = location.y + v.y * 4.5;

			return temp;
		}

		// Assume that the player is in the corner for animals bouncing off the corner
		public boolean isFar(Point l) {
			Point temp = this.getNext10();

			if(isMonkey && Point.dist(l, location) < 10.) {
				if(temp.x > 50. || temp.x < -50. || temp.y > 50. || temp.y < -50.) {
					return false;
				}
			}

			double distance = Point.dist(l, temp);
			if(this.isMonkey) {
				if(distance > 40.) return true;
				else return false;
			}
			else {
				if(distance > 20.) return true;
				else return false;
			}
		}
	}
}