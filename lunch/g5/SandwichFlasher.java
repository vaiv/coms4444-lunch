package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class SandwichFlasher {
	public Command getCommandSandwichFlasher(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
		// go to the center
		if(ps.get_location().x != 0 || ps.get_location().y != 0) {
			if(ps.get_held_item_type() != null)
				return new Command(CommandType.KEEP_BACK);
			double x = ps.get_location().x;
			double y = ps.get_location().y;
			double dist = Point.dist(ps.get_location(), new Point(0, 0));
			if(dist <= 1.0) {
				return Command.createMoveCommand(new Point(0, 0));
			}
			double cos = (0.0 - x)/dist;
			double sin = (0.0 - y)/dist;
			return Command.createMoveCommand(new Point(cos+x, sin+y));
		}
		// dangerous, keep back
		if(Utilities.dangerous(ps, animals, FoodType.SANDWICH2)) {
			// System.out.println("dangerous!");
			if (ps.is_player_searching() && ps.get_held_item_type() == null) {
				return new Command(CommandType.ABORT);
			}
			// put back if holding
			if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
				return new Command(CommandType.KEEP_BACK);
			}
			return new Command();
		}
		// safe start taking out
		if (!ps.is_player_searching() && ps.get_held_item_type() == null) {
			if (ps.check_availability_item(FoodType.SANDWICH2)) {
				return new Command(CommandType.TAKE_OUT, FoodType.SANDWICH2);
			}
		}
		return new Command();
	}
}
