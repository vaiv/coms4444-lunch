package lunch.g2;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair; 
import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;
	private String avatars;

	private boolean inPosition;  // indicates whether we are happy with our location

	public Player()
	{
		turn = 0;
		inPosition = false;
	}

	public String init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		avatars = "flintstone";
		random = new Random(s);
		return avatars;
	}

	// get sorted distances for all monkeys and geese
	private HashMap<AnimalType, ArrayList<Double>> getDistances(ArrayList<Animal> animals, PlayerState ps) {
		ArrayList<Double> monkey_dists = new ArrayList<Double>();
		ArrayList<Double> goose_dists = new ArrayList<Double>();

		for(Integer i=0; i<animals.size(); i++)
		{
			Animal animal = animals.get(i);
			Double dist = Point.dist(ps.get_location(), animal.get_location());
			
			if (animal.which_animal() == AnimalType.MONKEY) {
				monkey_dists.add(dist);
			}
			
			else if (animal.which_animal() == AnimalType.GOOSE) {
				goose_dists.add(dist);
			}
			
			else {
				System.out.println("Error: animal type unknown");
			}
		}

		Collections.sort(monkey_dists);
		Collections.sort(goose_dists);

		HashMap<AnimalType, ArrayList<Double>> distances = new HashMap<>(); 
        distances.put(AnimalType.MONKEY, monkey_dists); 
        distances.put(AnimalType.GOOSE, goose_dists);  

        return distances;
	}

	// indicates whether monkeys may be about to steal food
	private boolean monkeyDanger(ArrayList<Double> monkey_dists, Double threshold) {
		// check if 3 closest monkeys are within a distance of threshold suggested to be 7
		int num_close = 0;
		for (int i=0; i<3; i++) {
			if (monkey_dists.get(i) <= threshold) {num_close += 1;}
		}
		return num_close == 3;
	}

	// indicates whether geese are about to steal food
	private boolean gooseDanger(ArrayList<Double> goose_dists, Double threshold) {
		// check if closest goose is within a distance of threshold suggested to be 6
		return goose_dists.get(0) <= threshold;
	}


	// issues a response command if in danger and null otherwise
	private Command respondToDanger(ArrayList<Double> monkey_dists, ArrayList<Double> goose_dists, PlayerState ps) {
		Double monkey_threshold = 7.0;
		Double goose_threshold = 6.0;
		boolean animalsTooClose = gooseDanger(goose_dists, goose_threshold) || monkeyDanger(monkey_dists, monkey_threshold);

		if (animalsTooClose && holdingFood(ps)) {
			// danger sensed, start putting food away
			return new Command(CommandType.KEEP_BACK);
		}

		if (animalsTooClose && ps.is_player_searching()) {
			// danger sensed, start putting food away
			return new Command(CommandType.ABORT);
		}

		return null;
	}

	private boolean holdingFood(PlayerState ps) {
		return ps.get_held_item_type() != null;
	}

	// to be completed
	private Command walkToPosition(PlayerState ps, ArrayList<Family> members) {
		inPosition = true;
		return null;
	}

	// to be completed
	private Command makeEatingProgress(PlayerState ps) {
		return null;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		
		HashMap<AnimalType, ArrayList<Double>> distances = getDistances(animals, ps);
		ArrayList<Double> monkey_dists = distances.get(AnimalType.MONKEY);
		ArrayList<Double> goose_dists = distances.get(AnimalType.GOOSE);

		// scaffolding for monday's submission ////////////////////////
		///////////////////////////////////////////////////////////////

		// to be completed
		if (!inPosition) {
			return walkToPosition(ps, members);
		}

		Command hideFood = respondToDanger(monkey_dists, goose_dists, ps);
		if (hideFood != null) {
			return hideFood;
		}

		// to be completed
		Command progress = makeEatingProgress(ps);
		if (progress != null) {
			return progress;
		}

		//////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////


		




		// TA code
		Double monkey_min = monkey_dists.get(0);
		if(turn<10)
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

		// abort taking out if animal is too close
		if(monkey_min<3.0 && ps.is_player_searching() && ps.get_held_item_type()==null)
		{
			// System.out.println("abort command issued");
			// System.out.println(monkey_min.toString());
			return new Command(CommandType.ABORT);
		}

		// move away from animal 
		else if(monkey_min<3.0)
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
		else if (!ps.is_player_searching() &&  monkey_min>=5 && ps.get_held_item_type()==null )
		{
			for(FoodType food_type: FoodType.values())
			{
				if(ps.check_availability_item(food_type))
				{
					Command c = new Command(CommandType.TAKE_OUT, food_type);
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


}
