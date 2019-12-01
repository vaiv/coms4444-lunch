package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
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
	
	// [surrounded by monkey] or [has close goose while holding or searching sandwich]
	public static boolean dangerous(PlayerState ps, ArrayList<Animal> animals, FoodType searching) {
		if(monkey_surround(animals, ps.get_location()) || (goose_close(animals, ps.get_location())
				&& (ps.get_held_item_type() == FoodType.SANDWICH || (ps.is_player_searching()
						&& (searching == FoodType.SANDWICH1 || searching == FoodType.SANDWICH2)))))
				return true;
		return false;
	}
	
	// how many geese in the corner that closer to the geese shield than the player being protected
	public static int countCornerGeese(ArrayList<Point> geese, Point corner, Point geeseShield) {
		int cnt = 0;
		for(Point goose: geese) {
			if(goose == null)
				continue;
			if (Point.dist(corner, goose) <= 20 && Point.dist(corner, goose) > Point.dist(geeseShield, goose)) {
				cnt++;
			}
		}
		
		return cnt;
	}
}
