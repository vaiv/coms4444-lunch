package lunch.g7;

import java.util.*;
import java.util.List;

import javafx.util.Pair;

import lunch.sim.*;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;
	private String avatars;
	private FoodType foodToTakeOut = null;

	public Player()
	{
		turn = 0;
	}

	public String init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		avatars = "flintstone";
		random = new Random(s);
		return avatars;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{

		Double min_dist = Double.MAX_VALUE;
		List<Animal> monkeys = new ArrayList<>();
		List<Animal> geese = new ArrayList<>();
		for(Animal animal : animals)
		{
			if (animal.which_animal() == AnimalType.MONKEY) {
				monkeys.add(animal);
			}
			else {
				geese.add(animal);
			}
		}

		if(turn<300)
		{
			boolean found_valid_move= false;
			Point next_move = new Point(-1,-1);
			while(!found_valid_move)
			{
				Double bearing = random.nextDouble()*2*Math.PI;
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			// System.out.println("move command issued");
			turn++;
			return Command.createMoveCommand(next_move);
		}

		Collections.sort(monkeys, (a, b) -> Double.compare(Point.dist(a.get_location(), ps.get_location())
				,Point.dist(b.get_location(), ps.get_location())));
		Collections.sort(geese, (a, b) -> Double.compare(Point.dist(a.get_location(), ps.get_location())
				,Point.dist(b.get_location(), ps.get_location())));

		// abort taking out if animal is too close
		if(ps.is_player_searching() && ps.get_held_item_type()==null && isDangerours(id, ps, monkeys, geese))
		{
			return new Command(CommandType.ABORT);
		}
		// keep food item back if animal is too close
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null && isDangerours(id, ps, monkeys, geese))
		{

			return new Command(CommandType.KEEP_BACK);
		}
		// move away from animal 
		else if(min_dist<3.0)
		{
			boolean found_valid_move= false;
			Point next_move = new Point(-1,-1);
			while(!found_valid_move)
			{
				Double bearing = random.nextDouble()*2*Math.PI;
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			return Command.createMoveCommand(next_move);
			
		}
		// if no animal is near then take out food
		else if (!ps.is_player_searching() &&  min_dist>=5 && ps.get_held_item_type()==null )
		{
			for(FoodType food_type: FoodType.values())
			{
				if(ps.check_availability_item(food_type))
				{
					Command c = new Command(CommandType.TAKE_OUT, food_type);
					foodToTakeOut = food_type;
					return c;
				}
			}
		}
		// if no animal in vicinity then take a bite
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null)
		{
			return new Command(CommandType.EAT);
		}

		// System.out.println("player is searching");
		return new Command();

	}

	private boolean isDangerours(int id, PlayerState ps, List<Animal> monkeys, List<Animal> geese) {
		if (foodToTakeOut == FoodType.SANDWICH1 || foodToTakeOut == FoodType.SANDWICH2)
			return detectGeese(ps, geese) || detectMonkeys(ps, monkeys);
		else {
			return detectMonkeys(ps, monkeys);
		}
	}

	// returns true if in the next time step there will be a goose that can grab your sandwich
	// Geese can fly at 3 meters per second, if a goose makes it to within 2m of your position, they swoop in and grab your sandwich.
	private boolean detectGeese(PlayerState ps, List<Animal> geese) {
		for (Animal g: geese) {
			Point playerLoc = ps.get_location();
			Point gooseLoc = g.get_location();
			double dist = Point.dist(gooseLoc, playerLoc);
			// TODO: consider busyEating
			if (dist <= 5.0) {
				return true;
			}
		}
		return false;
	}

	// returns true if in the next time step there will be one or more monkeys that can grab your sandwich
	private boolean detectMonkeys(PlayerState ps, List<Animal> monkeys) {
		int monkeyCount = 0;
		for (Animal m: monkeys) {
			Point playerLoc = ps.get_location();
			Point monkeyLoc = m.get_location();
			double dist = Point.dist(monkeyLoc, playerLoc);
			if (dist <= 5.0) {
				monkeyCount++;
			}
		}
		if (monkeyCount < 3) {
			return false;
		}
		for (Animal m: monkeys) {
			Point playerLoc = ps.get_location();
			Point monkeyLoc = m.get_location();
			double dist = Point.dist(monkeyLoc, playerLoc);
			if (dist <= 1.0) {
				return true;
			}
		}
		return false;
	}

}
