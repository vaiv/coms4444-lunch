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

public class GeeseShield {
	private Point corner; // corner to protect
	private Point pos; // position of geese shield
	private boolean arrived; // arrive the shield position
	
	//x y is the corner to protect
	public GeeseShield(int x, int y) {
		this.corner = new Point(x*50, y*50);
		double posX = x * (50 - 10 * Math.sqrt(2));
		double posY = y * (50 - 10 * Math.sqrt(2));
		this.pos = new Point(posX, posY);
		this.arrived = false;
	}
	
	public Command getCommandGeeseShield(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps, ArrayList<Animal> previousAnimals, int totalTurn) {
		// go to the shield position
		if(!arrived) {
			if(ps.get_held_item_type() != null)
				return new Command(CommandType.KEEP_BACK);
			double x = ps.get_location().x;
			double y = ps.get_location().y;
			double dist = Point.dist(ps.get_location(), pos);
			if(dist <= 1.0) {
				arrived = true;
				return Command.createMoveCommand(pos);
			}
			double cos = (pos.x - x)/dist;
			double sin = (pos.y - y)/dist;
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
		// safe start shielding
		if (!ps.is_player_searching() && ps.get_held_item_type() == null) {
			// only consider geese
			ArrayList<Animal> geese = new ArrayList<Animal>();
			ArrayList<Animal> previousGeese = new ArrayList<Animal>();
			for(Animal animal: animals) {
				if(animal.which_animal() == AnimalType.GOOSE) {
					geese.add(animal);
				}
			}
			for(Animal previousAnimal: previousAnimals) {
				if(previousAnimal.which_animal() == AnimalType.GOOSE) {
					previousGeese.add(previousAnimal);
				}
			}
			// predict geese positions after 10 time steps
			ArrayList<ArrayList<Point>> futurePositions = PositionPredictor.predict(previousAnimals, animals, 10);	
			ArrayList<Point> fp = futurePositions.get(9);
			// can attract geese toward the shield
			if(Utilities.countCornerGeese(fp, corner, ps.get_location()) > 0) {
				if (ps.check_availability_item(FoodType.SANDWICH2)) {
					return new Command(CommandType.TAKE_OUT, FoodType.SANDWICH2);
				}
			}
		}
		return new Command();
	}
}
