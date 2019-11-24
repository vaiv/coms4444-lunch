package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Point;

public class Utilities {
	public static int count_close_animal(ArrayList<Animal> animals, AnimalType at, Point p, int range) {
		int cnt = 0;
		for(Animal animal: animals) {
			if(animal.which_animal() == at && Point.dist(animal.get_location(), p) < range) {
				cnt++;
			}
		}
		return cnt;
	}

	// more than 3 monkeys within 6 meters
	public static boolean monkey_surround(ArrayList<Animal> animals, Point p) {
		int near_monkeys = count_close_animal(animals, AnimalType.MONKEY, p, 6);
		return near_monkeys >= 3;
	}
	
	// has goose within 5 meters
	public static boolean goose_close(ArrayList<Animal> animals, Point p) {
		int near_geese = count_close_animal(animals, AnimalType.GOOSE, p, 5);
		return near_geese > 0;
	}
}
