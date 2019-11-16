package lunch.g4;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair; 
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

public class Player implements lunch.sim.Player {
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;
	private String avatars;
	private List<Animal> monkeys = new ArrayList<>();
	private List<Animal> geese = new ArrayList<>();
	private List<Point> targetCorners = Arrays.asList(new Point[]{
			new Point(-50, -50), 
			new Point(-50, 50), 
			new Point(50, -50)
			});
	private Map<Integer, Point> targetCornersChosen = new HashMap<>();
	private FoodType foodCurrentlySearchingFor = null;
	private static final double MONKEY_DISTANCE_THRESHOLD = 6.0 + 10e-6;
	private static final double GOOSE_DISTANCE_THRESHOLD = 5.0 + 10e-6;

	public Player() {
		turn = 0;
	}

	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id = id;
		avatars = "flintstone";
		random = new Random();
		return avatars;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
		if(turn < 100) {
			boolean foundValidMove = false;
			Point nextMove = new Point(-1, -1);
			while(!foundValidMove) {
				Double bearing = random.nextDouble() * 2 * Math.PI;
				nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				foundValidMove = Point.within_bounds(nextMove);
			}
			turn++;
			return Command.createMoveCommand(nextMove);
		}

		// Determine animals sorted by closest distance to player
		ArrayList<Animal> clonedAnimals = new ArrayList<>(animals);
		Collections.sort(clonedAnimals, new Comparator<Animal>() {
		    public int compare(Animal animal1, Animal animal2) {
				double distanceToAnimal1 = Point.dist(ps.get_location(), animal1.get_location());
				double distanceToAnimal2 = Point.dist(ps.get_location(), animal2.get_location());
		        return (int) Math.signum(distanceToAnimal1 - distanceToAnimal2);
		    }
		});
						
		// Assign monkeys and geese, each sorted by closest distance to player
		monkeys = new ArrayList<>();
		geese = new ArrayList<>();
		
		for(Animal animal : clonedAnimals) {
			if(animal.which_animal() == AnimalType.MONKEY && !animal.busy_eating())
				monkeys.add(animal);
			else if(animal.which_animal() == AnimalType.GOOSE && !animal.busy_eating())
				geese.add(animal);
		}
		
		boolean monkeysTooClose, gooseTooClose;
		
		System.out.println();
		if(monkeys.size() < 3)
			monkeysTooClose = false;
		else {
			double distanceToFirstMonkey = Point.dist(ps.get_location(), monkeys.get(0).get_location());			
			double distanceToSecondMonkey = Point.dist(ps.get_location(), monkeys.get(1).get_location());			
			double distanceToThirdMonkey = Point.dist(ps.get_location(), monkeys.get(2).get_location());			
			if(distanceToFirstMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToSecondMonkey <= MONKEY_DISTANCE_THRESHOLD && distanceToThirdMonkey <= MONKEY_DISTANCE_THRESHOLD) {
				monkeysTooClose = true;
				System.out.println("Monkey distances: [" + distanceToFirstMonkey + ", " + distanceToSecondMonkey + ", " + distanceToThirdMonkey + "]");
			}
			else
				monkeysTooClose = false;
		}
		
		if(geese.size() == 0 || Point.dist(ps.get_location(), geese.get(0).get_location()) > GOOSE_DISTANCE_THRESHOLD)
			gooseTooClose = false;
		else {
			System.out.println("Goose distance: " + Point.dist(ps.get_location(), geese.get(0).get_location()));
			gooseTooClose = true;
		}
						
		System.out.println("Player is still holding item: " + (ps.get_held_item_type() != null));
		System.out.println("Player is still searching: " + (ps.is_player_searching()));
		printAvailability(ps);

		// Abort taking out the food item if the animal is too close
		if(ps.is_player_searching() && ps.get_held_item_type() == null &&
				(monkeysTooClose || (gooseTooClose && foodCurrentlySearchingFor == FoodType.SANDWICH))) {
			System.out.println("Player " + id + " is aborting search.");
			return new Command(CommandType.ABORT);
		}

		// Keep the food item back if the animal is too close
		if(!ps.is_player_searching() && ps.get_held_item_type() != null && (((monkeysTooClose || gooseTooClose) && (ps.get_held_item_type() == FoodType.SANDWICH)) ||
				(monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)))) {
			System.out.println("Player " + id + " is keeping back " + ps.get_held_item_type().name() + ".");
			return new Command(CommandType.KEEP_BACK);
		}
		
		// Take out a food item (or move player) if no animal is too close
		if (!ps.is_player_searching() && !monkeysTooClose && ps.get_held_item_type() == null) {
			FoodType foodType = ps.check_availability_item(FoodType.COOKIE) ? FoodType.COOKIE : 
								ps.check_availability_item(FoodType.FRUIT1) ? FoodType.FRUIT1 :
								ps.check_availability_item(FoodType.FRUIT2) ? FoodType.FRUIT2 : 
								ps.check_availability_item(FoodType.EGG) ? FoodType.EGG :
								ps.check_availability_item(FoodType.SANDWICH1) ? FoodType.SANDWICH1 :
								ps.check_availability_item(FoodType.SANDWICH2) ? FoodType.SANDWICH2	: 
								null;
			
			if(foodType != null) {
				if(foodType != FoodType.SANDWICH1 && foodType != FoodType.SANDWICH2) {
					System.out.println("Player " + id + " is taking out " + foodType.name() + ".");
					foodCurrentlySearchingFor = foodType;
					return new Command(CommandType.TAKE_OUT, foodType);
				}
				
				Point currPoint = ps.get_location();
				Point targetCorner = new Point(-1, -1);
				if(!targetCornersChosen.containsKey(id)) {
					targetCornersChosen.put(id, targetCorners.get(random.nextInt(targetCorners.size())));
					System.out.println("Player " + id + " will go to corner " + targetCornersChosen.get(id));
				}
				targetCorner = targetCornersChosen.get(id);

				if(currPoint.x == targetCorner.x && currPoint.y == targetCorner.y) {
					if(!gooseTooClose) {
						System.out.println("Player " + id + " is taking out a sandwich.");
						foodCurrentlySearchingFor = FoodType.SANDWICH;
						return new Command(CommandType.TAKE_OUT, foodType);
					}
					else {
						System.out.println("Player " + id + " is not going to take out a sandwich because the goose is too close.");
						return new Command();
					}
				}
				
				double distanceFromCorner = Math.sqrt(Math.pow(targetCorner.y - currPoint.y, 2) + Math.pow(targetCorner.x - currPoint.x, 2));
				if(distanceFromCorner < 1.0) {
					System.out.println("Player " + id + " is making its final move to the corner.");
					return Command.createMoveCommand(targetCorner);
				}

				double slope = ((double) (targetCorner.y - currPoint.y)) / ((double) (targetCorner.x - currPoint.x));
				double deltaX = (targetCorner.x > 0 ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				double deltaY = Math.abs(slope) * (targetCorner.y > 0 ? 1.0 : -1.0) / Math.sqrt(Math.pow(slope, 2) + 1);
				System.out.println("Player " + id + " is moving to the corner.");
				return Command.createMoveCommand(new Point(currPoint.x + deltaX, currPoint.y + deltaY));
			}
		}
		
		// Eat if no animal is too close
		if(!ps.is_player_searching() && ps.get_held_item_type() != null) {
			if((!monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)) ||
				(!monkeysTooClose && !gooseTooClose && (ps.get_held_item_type() == FoodType.SANDWICH))) {
				System.out.println("Player " + id + " is going to eat " + ps.get_held_item_type().name() + ".");
				return new Command(CommandType.EAT);
			}
		}
		System.out.println("Player " + id + " is going to wait.");
		return new Command(CommandType.WAIT);
	}
	
	private void printAvailability(PlayerState ps) {
		System.out.println("Cookie is available: " + ps.check_availability_item(FoodType.COOKIE)); 
		System.out.println("Fruit 1 is available: " + ps.check_availability_item(FoodType.FRUIT1)); 
		System.out.println("Fruit 2 is available: " + ps.check_availability_item(FoodType.FRUIT2)); 
		System.out.println("Egg is available: " + ps.check_availability_item(FoodType.EGG)); 
		System.out.println("Sandwich 1 is available: " + ps.check_availability_item(FoodType.SANDWICH1)); 
		System.out.println("Sandwich 2 is available: " + ps.check_availability_item(FoodType.SANDWICH2)); 
	}
}