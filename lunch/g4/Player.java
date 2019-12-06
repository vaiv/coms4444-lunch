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
			new Point(-50, 50),  // Bottom-left corner
			new Point(50, -50)  // Top-right corner
			});
	private List<List<Boolean>> eatingEfficiencyWindows = new ArrayList<>();
	private List<Boolean> gameEatingPattern = new ArrayList<>();
	private List<EatingTimeArchive> gameEatingTimeArchives = new ArrayList<>();
	private Map<Integer, Point> targetCornersChosen = new HashMap<>();
	private FoodType foodCurrentlySearchingFor = null;
	private static final double MONKEY_DISTANCE_THRESHOLD = 6.0 + 10e-6;
	private static final double MONKEY_VISION_THRESHOLD = 40.0 + 10e-6;
	private static final double GOOSE_DISTANCE_THRESHOLD = 5.0 + 10e-6;
	private static final double GOOSE_VISION_THRESHOLD = 20.0 + 10e-6;
	private static final double TURN_THRESHOLD = 20;
	private static final double TIME_LEFT_TO_BE_GREEDY_WITH_NONSANDWICH = 10;
	private static final double TIME_LEFT_TO_BE_GREEDY_WITH_SANDWICH = 30;
	private static final double ANIMAL_COMPLEXITY_THRESHOLD = 150;
	private static final int EFFICIENCY_WINDOW_SIZE = 50;
	private static final int EFFICIENCY_WINDOW_CHECK_INTERVAL = 25;
	private static final double ADEQUATE_EFFICIENCY_RATE_THRESHOLD = 0.8;
	private static final int CONSECUTIVE_WINDOWS_CHECKED = 3;
	private static final double WAIT_TIME_FOR_VISION_THRESHOLD_CHANGE = 50;
	private double timeElapsedForVisionThresholdChange = 0;
	private double chosenVisionThreshold = VisionThresholdManager.visionThresholds.get(VisionThresholdManager.visionThresholds.size() - 1);
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
			if(animal.which_animal() == AnimalType.MONKEY)
				monkeyTrajectories.add(new Trajectory(animal.get_location(), true));
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
			
		timeElapsedForVisionThresholdChange++;
		
		computePlayerEatingPatternStep(ps);
		if(turn % EFFICIENCY_WINDOW_CHECK_INTERVAL == 0 && turn > 2 * TURN_THRESHOLD)
			generateNewEfficiencyWindow();
		if(timeElapsedForVisionThresholdChange >= WAIT_TIME_FOR_VISION_THRESHOLD_CHANGE)
			adjustVisionThreshold();
		
		int monkeyIndex = 0;
		int gooseIndex = 0;
		int monkeyInRangeInFuture = 0;
		int monkeyInRange = 0;
		int geeseInRange = 0;
		int geeseInRangeInFuture = 0;
		for(Animal animal : animals) {
			if(animal.which_animal() == AnimalType.MONKEY) {
				monkeyTrajectories.get(monkeyIndex).update(animal.get_location());
				if(!monkeyTrajectories.get(monkeyIndex).isFar(ps.get_location()))
					monkeyInRangeInFuture++;
				if(Point.dist(ps.get_location(), animal.get_location()) < 50.0)
					monkeyInRange++;
				monkeyIndex++;
			}
			else if(animal.which_animal() == AnimalType.GOOSE) {
				geeseTrajectories.get(gooseIndex).update(animal.get_location());
				if(!geeseTrajectories.get(gooseIndex).isFar(ps.get_location()))
					geeseInRangeInFuture++;
				if(Point.dist(ps.get_location(), animal.get_location()) < 50.0)
					geeseInRange++;
				gooseIndex++;
			}
		}
		
		// Assign a corner for the player to move to based on family members' locations
		Point currPoint = ps.get_location();
		Point targetCorner = new Point(-1, -1);
		
		if(turn <= TURN_THRESHOLD && (totalTime > 900 && (monkeyIndex > 30 || gooseIndex > 5))) {
			boolean found_valid_move= false;
			Point next_move = new Point(-1,-1);
			while(!found_valid_move)
			{
				Double bearing = (double) (id / 16.0);
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			// System.out.println("move command issued");
			turn++;
			return Command.createMoveCommand(next_move);
		}
		
		if(turn > TURN_THRESHOLD && (totalTime > 900 && (monkeyIndex > 30 || gooseIndex > 5))) {
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
				if(distanceFromCorner <= 1.0)
					return Command.createMoveCommand(targetCorner);
				
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
			if(distanceToFirstMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToSecondMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToThirdMonkey <= MONKEY_DISTANCE_THRESHOLD)
				monkeysTooClose = true;
			else
				monkeysTooClose = false;
		}
		
		// Determine if any goose is too close
		if(geese.size() == 0 || Point.dist(ps.get_location(), geese.get(0).get_location()) > GOOSE_DISTANCE_THRESHOLD)
			gooseTooClose = false;
		else
			gooseTooClose = true;
		
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
				
				boolean usePrediction = false;
				if(monkeyInRangeInFuture <= 2 && (chooseNormalApproach(monkeyIndex, gooseIndex) || (!chooseNormalApproach(monkeyIndex, gooseIndex) && (chosenVisionThreshold == Double.MAX_VALUE))))
					usePrediction = true;
				
				double thirdClosestMonkeyDist = Double.MAX_VALUE;
				if(monkeys.size() > 0)
					thirdClosestMonkeyDist = Point.dist(ps.get_location(), monkeys.get((int) Math.min(monkeys.size() - 1, 2)).get_location());
				
				// Take out the food item if it is not a sandwich and increase consumption time
				if(foodType != FoodType.SANDWICH1 && foodType != FoodType.SANDWICH2) {
					if(totalTime - turn < 500 || ps.get_time_for_item(foodType) < TIME_LEFT_TO_BE_GREEDY_WITH_NONSANDWICH || 
							usePrediction || (!usePrediction && !chooseNormalApproach(monkeyIndex, gooseIndex) && thirdClosestMonkeyDist > chosenVisionThreshold)) {
						foodCurrentlySearchingFor = foodType;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
				}

				// Take out the food item if it is a sandwich
				if(!gooseTooClose) {					
					
					// Take out the sandwich for eating
					if(totalTime - turn < 500 || ps.get_time_for_item(foodType) < TIME_LEFT_TO_BE_GREEDY_WITH_SANDWICH ||
							usePrediction || (!usePrediction && !chooseNormalApproach(monkeyIndex, gooseIndex) && thirdClosestMonkeyDist > chosenVisionThreshold)) {
						foodCurrentlySearchingFor = FoodType.SANDWICH;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
				}

				return new Command();
			}
		}
		
		if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
			foodCurrentlySearchingFor = null;

			// Eat if no animal is too close
			if((!monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)) ||
				(!monkeysTooClose && !gooseTooClose && (ps.get_held_item_type() == FoodType.SANDWICH)))
				return new Command(CommandType.EAT);
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
	
	private void generateNewEfficiencyWindow() {
		List<Boolean> newEfficiencyWindow = new ArrayList<>();
		for(int i = 0; i < EFFICIENCY_WINDOW_SIZE; i++)
			newEfficiencyWindow.add(gameEatingPattern.get(gameEatingPattern.size() + i - EFFICIENCY_WINDOW_SIZE));
		eatingEfficiencyWindows.add(newEfficiencyWindow);
	}
	
	private void computePlayerEatingPatternStep(PlayerState ps) {
		EatingTimeArchive newETA = new EatingTimeArchive();
		newETA.timeLeftForSandwich1 = ps.get_time_for_item(FoodType.SANDWICH1);
		newETA.timeLeftForSandwich2 = ps.get_time_for_item(FoodType.SANDWICH2);
		newETA.timeLeftForFruit1 = ps.get_time_for_item(FoodType.FRUIT1);
		newETA.timeLeftForFruit2 = ps.get_time_for_item(FoodType.FRUIT2);
		newETA.timeLeftForEgg = ps.get_time_for_item(FoodType.EGG);
		newETA.timeLeftForCookie = ps.get_time_for_item(FoodType.COOKIE);
		
		boolean finishedUpdateOfEatingPattern = true;
		try {
			gameEatingTimeArchives.add(newETA);
			if(gameEatingTimeArchives.size() == 1)
				gameEatingPattern.add(false);
			else {
				EatingTimeArchive prevETA = gameEatingTimeArchives.get(gameEatingTimeArchives.size() - 2);
				if(EatingTimeArchive.archivesDiffer(prevETA, newETA))
					gameEatingPattern.add(true);
				else
					gameEatingPattern.add(false);
			}
			finishedUpdateOfEatingPattern = true;
		} catch(Exception e) {
			if(!finishedUpdateOfEatingPattern)
				gameEatingPattern.add(false);
		}
	}
	
	private void adjustVisionThreshold() {
		if(eatingEfficiencyWindows.size() <= (CONSECUTIVE_WINDOWS_CHECKED - 1))			
			return;
		
		List<List<Boolean>> windowsWithInadequateEfficiencyRate = new ArrayList<>();
		List<List<Boolean>> windowsWithAdequateEfficiencyRate = new ArrayList<>();
		for(int i = 0; i < CONSECUTIVE_WINDOWS_CHECKED; i++) {
			List<Boolean> newWindow = eatingEfficiencyWindows.get(eatingEfficiencyWindows.size() + i - CONSECUTIVE_WINDOWS_CHECKED);
			double eatingEfficiencyRate = computeEatingEfficiencyRate(newWindow);
			if(eatingEfficiencyRate < ADEQUATE_EFFICIENCY_RATE_THRESHOLD)
				windowsWithInadequateEfficiencyRate.add(newWindow);
			else
				windowsWithAdequateEfficiencyRate.add(newWindow);
		}
		
		if(windowsWithInadequateEfficiencyRate.size() == CONSECUTIVE_WINDOWS_CHECKED) {
			chosenVisionThreshold = VisionThresholdManager.decrementVisionThreshold(chosenVisionThreshold);
			timeElapsedForVisionThresholdChange = 0;
		}
		if(windowsWithAdequateEfficiencyRate.size() == CONSECUTIVE_WINDOWS_CHECKED) {
			chosenVisionThreshold = VisionThresholdManager.incrementVisionThreshold(chosenVisionThreshold);
			timeElapsedForVisionThresholdChange = 0;
			System.out.println(" to " + chosenVisionThreshold);
		}
	}
	
	private double computeEatingEfficiencyRate(List<Boolean> efficiencyWindow) {
		int numStepsPlayerIsEating = 0;
		for(int i = 0; i < efficiencyWindow.size(); i++)
			if(efficiencyWindow.get(i))
				numStepsPlayerIsEating++;
		return (numStepsPlayerIsEating * 1.0 / efficiencyWindow.size());
	}
	
	private boolean chooseNormalApproach(int numMonkeys, int numGeese) {
		return (numMonkeys + 3 * numGeese) <= ANIMAL_COMPLEXITY_THRESHOLD;
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

			if(isMonkey && Point.dist(l, location) < 10.0)
				if(temp.x > 50.0 || temp.x < -50.0 || temp.y > 50.0 || temp.y < -50.0)
					return false;

			double distance = Point.dist(l, temp);
			if(this.isMonkey) {
				if(distance > MONKEY_VISION_THRESHOLD)
					return true;
				else
					return false;
			}
			else {
				if(distance > GOOSE_VISION_THRESHOLD)
					return true;
				else
					return false;
			}
		}
	}
	
	public static class VisionThresholdManager {
		public static List<Double> visionThresholds = Arrays.asList(new Double[] {
				MONKEY_DISTANCE_THRESHOLD,
				(1.0 / 4.0) * MONKEY_VISION_THRESHOLD,
				(1.0 / 2.0) * MONKEY_VISION_THRESHOLD,
				(3.0 / 4.0) * MONKEY_VISION_THRESHOLD,
				MONKEY_VISION_THRESHOLD,
				Double.MAX_VALUE
		});
		
		public static double decrementVisionThreshold(double currVisionThreshold) {
			if(!visionThresholds.contains(currVisionThreshold) || currVisionThreshold == visionThresholds.get(0))
				return visionThresholds.get(0);
			return visionThresholds.get(visionThresholds.indexOf(currVisionThreshold) - 1);
		}

		public static double incrementVisionThreshold(double currVisionThreshold) {
			if(!visionThresholds.contains(currVisionThreshold) || currVisionThreshold == visionThresholds.get(visionThresholds.size() - 1))
				return visionThresholds.get(visionThresholds.size() - 1);
			return visionThresholds.get(visionThresholds.indexOf(currVisionThreshold) + 1);
		}		
	}
	
	public static class EatingTimeArchive {
		public double timeLeftForSandwich1;
		public double timeLeftForSandwich2;
		public double timeLeftForFruit1;
		public double timeLeftForFruit2;
		public double timeLeftForEgg;
		public double timeLeftForCookie;
		
		public static boolean archivesDiffer(EatingTimeArchive archive1, EatingTimeArchive archive2) {			
			if(archive1.timeLeftForSandwich1 != archive2.timeLeftForSandwich1)
				return true;
			if(archive1.timeLeftForSandwich2 != archive2.timeLeftForSandwich2)
				return true;
			if(archive1.timeLeftForFruit1 != archive2.timeLeftForFruit1)
				return true;
			if(archive1.timeLeftForFruit2 != archive2.timeLeftForFruit2)
				return true;
			if(archive1.timeLeftForEgg != archive2.timeLeftForEgg)
				return true;
			if(archive1.timeLeftForCookie != archive2.timeLeftForCookie)
				return true;
			return false;
		}
	}
}
