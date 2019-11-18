package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Point;

public class Utilities {
	// more than 3 monkeys within 6 meters
	public static boolean monkey_surround(ArrayList<Animal> animals, Point p) {
		int near_monkey = 0;
		for(Animal animal: animals) {
			if(animal.which_animal() == AnimalType.MONKEY && Point.dist(animal.get_location(), p) < 6) {
				near_monkey++;
			}
		}
		return near_monkey >= 3;
	}
	
	// has goose within 5 meters
	public static boolean goose_close(ArrayList<Animal> animals, Point p) {
		for(Animal animal: animals) {
			if(animal.which_animal() == AnimalType.GOOSE && Point.dist(animal.get_location(), p) < 5) {
				return true;
			}
		}
		return false;
	}
}
