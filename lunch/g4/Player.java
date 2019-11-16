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
	private Point targetCorner = new Point(-50, -50);

	public Player() {
		turn = 0;
	}

	public String init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id = id;
		avatars = "flintstone";
		random = new Random();
		return avatars;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
		if(turn < 300) {
			boolean found_valid_move = false;
			Point next_move = new Point(-1, -1);
			while(!found_valid_move) {
				Double bearing = random.nextDouble() * 2 * Math.PI;
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			turn++;
			return Command.createMoveCommand(next_move);
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
		
//		System.out.println("Player: " + id);
//		for(Animal animal : clonedAnimals) {
//			double distanceToAnimal = Point.dist(ps.get_location(), animal.get_location());
//			System.out.println("Distance to animal: " + distanceToAnimal);
//		}
//		System.out.println();
				
		// Assign monkeys and geese, each sorted by closest distance to player
		monkeys = new ArrayList<>();
		geese = new ArrayList<>();
		
		for(Animal animal : clonedAnimals) {
			if(animal.which_animal() == AnimalType.MONKEY)
				monkeys.add(animal);
			else if(animal.which_animal() == AnimalType.GOOSE)
				geese.add(animal);
		}
		
		boolean monkeysTooClose, gooseTooClose;
		
		if(monkeys.size() < 3)
			monkeysTooClose = false;
		else {
			double distanceToFirstMonkey = Point.dist(ps.get_location(), monkeys.get(0).get_location());			
			double distanceToSecondMonkey = Point.dist(ps.get_location(), monkeys.get(1).get_location());			
			double distanceToThirdMonkey = Point.dist(ps.get_location(), monkeys.get(2).get_location());			
			if(distanceToFirstMonkey < 6.0 && distanceToSecondMonkey < 6.0 && distanceToThirdMonkey < 6.0)
				monkeysTooClose = true;
			else
				monkeysTooClose = false;
		}
		
		if(geese.size() == 0 || Point.dist(ps.get_location(), geese.get(0).get_location()) >= 5.0)
			gooseTooClose = false;
		else
			gooseTooClose = true;
						
		System.out.println();
		if(gooseTooClose) {
			System.out.println("Monkeys too close: " + monkeysTooClose);
			System.out.println("Geese too close: " + gooseTooClose);
			System.out.println("Food type being held is sandwich 1: " + (ps.get_held_item_type() == FoodType.SANDWICH));
		}
		
		// Abort taking out the food item if the animal is too close
		if((monkeysTooClose || gooseTooClose) && ps.is_player_searching() && ps.get_held_item_type() == null) {
			System.out.println("Player " + id + " is aborting search.");
			return new Command(CommandType.ABORT);
		}

		// Keep the food item back if the animal is too close
		if(((monkeysTooClose || gooseTooClose) && (ps.get_held_item_type() == FoodType.SANDWICH)) ||
				(monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH))) {
			System.out.println("Player " + id + " is keeping back " + ps.get_held_item_type().name() + ".");
			return new Command(CommandType.KEEP_BACK);
		}
		
//		// Move away from the animal
//		if(min_dist < 3.0) {
//			boolean found_valid_move= false;
//			Point next_move = new Point(-1, -1);
//			while(!found_valid_move) {
//				Double bearing = random.nextDouble()*2*Math.PI;
//				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
//				found_valid_move = Point.within_bounds(next_move);
//			}
//			return Command.createMoveCommand(next_move);
//			
//		}
		
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
					return new Command(CommandType.TAKE_OUT, foodType);
				}
				
				Point currPoint = ps.get_location();
				if(currPoint.x == targetCorner.x && currPoint.y == targetCorner.y) {
					if(!gooseTooClose) {
						System.out.println("Player " + id + " is taking out " + foodType.name() + ".");
						return new Command(CommandType.TAKE_OUT, foodType);
					}
					else {
						System.out.println("Player " + id + " is not going to take out a sandwich because the goose is too close.");
						return new Command();
					}
				}
								
				double distanceFromCorner = Math.sqrt(Math.pow(targetCorner.y - currPoint.y, 2) + Math.pow(targetCorner.x - currPoint.x, 2));
				if(distanceFromCorner < 1.0)
					return Command.createMoveCommand(targetCorner);

				double slope = ((double) (targetCorner.y - currPoint.y)) / ((double) (targetCorner.x - currPoint.x));
				double deltaX = -1.0 / Math.sqrt(Math.pow(slope, 2) + 1);
				double deltaY = slope * deltaX;
				return Command.createMoveCommand(new Point(currPoint.x + deltaX, currPoint.y + deltaY));
			}
		}
		
		// Eat if no animal is too close
		if(!ps.is_player_searching()) {
			if((!monkeysTooClose && (ps.get_held_item_type() != FoodType.SANDWICH)) ||
				(!monkeysTooClose && !gooseTooClose && (ps.get_held_item_type() == FoodType.SANDWICH))) {
				System.out.println("Player " + id + " is going to eat " + ps.get_held_item_type().name() + ".");
				return new Command(CommandType.EAT);
			}
		}
		System.out.println("Player " + id + " is doing nothing after trying everything.");
		return new Command(CommandType.KEEP_BACK);
	}
}