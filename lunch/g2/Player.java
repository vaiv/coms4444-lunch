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
	private ArrayList<HashMap<Integer, Double>> densityRecord;
	private int currentTime;
	private Point walkingTarget;
	private boolean inPosition;  // indicates whether we are happy with our location
	private String descriptiveState;
	private String playerRole; // "distract" or "eat"
	private boolean fanningOut; // when distracting are we currently going toward dense region or bringing
	// animals back to center

	public Player()
	{
		turn = 0;
		inPosition = false;
		descriptiveState = "initial";
		playerRole = "distract";
		fanningOut = true;
	}

	public void updateState(PlayerState ps) {
		String newState = "";
		if (! ps.is_player_searching() && ps.time_to_eat_remaining() == -1) {
			 newState = "initial";
		} else if (ps.is_player_searching() && ps.time_to_eat_remaining() == -1) {
			newState =  "taking_out";
		} else if (!ps.is_player_searching() && ps.time_to_eat_remaining() > -1) {
			newState = "food_out";
		} else {
			newState = "putting_back";
		}

		this.descriptiveState = newState;
	}

	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		avatars = "flintstone";
		random = new Random(s);
		this.densityRecord = new ArrayList<HashMap<Integer, Double>>();
		this.currentTime = 0;
		this.descriptiveState = "initial";
		this.playerRole = "distract";
		fanningOut = true;
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
	private boolean monkeysAreNear(ArrayList<Double> monkey_dists, Double threshold) {
		// check if 3 closest monkeys are within a distance of threshold suggested to be 7
		int num_close = 0;
		for (int i=0; i<3; i++) {
			if (monkey_dists.get(i) <= threshold) {num_close += 1;}
		}
		return num_close == 3;
	}

	// indicates whether geese are about to steal food
	private boolean gooseIsNear(ArrayList<Double> goose_dists, Double threshold) {
		// check if closest goose is within a distance of threshold suggested to be 6
		return goose_dists.get(0) <= threshold;
	}


	// issues a response command if in danger
	private Command respondIfDanger(ArrayList<Double> monkey_dists, ArrayList<Double> goose_dists, PlayerState ps) {
		Double monkey_threshold = 7.0;  // conservative
		Double goose_threshold = 6.0;   // conservative
		boolean gooseTooClose = gooseIsNear(goose_dists, goose_threshold);
		boolean monkeysTooClose = monkeysAreNear(monkey_dists, monkey_threshold);
		if ((monkeysTooClose && holdingFood(ps)) || (gooseTooClose && ps.get_held_item_type() == FoodType.SANDWICH)) {
			// danger sensed, start putting food away
			return new Command(CommandType.KEEP_BACK);
		}

		// actually we only want to abort if we're close to getting the food out
		if ((gooseTooClose || monkeysTooClose) && ps.is_player_searching() && ps.time_to_finish_search() < 2) {
			// danger sensed, start putting food away
			return new Command(CommandType.ABORT);
		}

		return null;
	}

	private boolean holdingFood(PlayerState ps) {
		return ps.get_held_item_type() != null;
	}

	private Command walkToPosition(PlayerState ps) {
		Double delta_x = this.walkingTarget.x - ps.get_location().x;
		Double delta_y = this.walkingTarget.y - ps.get_location().y;

		Double bearing = Math.atan(delta_y / delta_x);
		int fac = (delta_x < 0) ? -1: 1;
		Point move = new Point(ps.get_location().x + fac * Math.cos(bearing), ps.get_location().y + fac * Math.sin(bearing));

		if (Point.dist(ps.get_location(), this.walkingTarget) < 5) {
			this.walkingTarget = null;
		}
		
		boolean found_valid_move = Point.within_bounds(move);
		return Command.createMoveCommand(move);
	}

	// to be completed
	private Command makeEatingProgress(PlayerState ps) {
		//if holding something, eat it
		if(holdingFood(ps))
		{
			System.out.println("Asking player to eat");
			return new Command(CommandType.EAT);
		}

		//else pull out in order if not already pulling out
		else if(!ps.is_player_searching())
		{
			FoodType f;
			if(ps.check_availability_item(FoodType.COOKIE))
			{
				f = FoodType.COOKIE;
			}

			else if(ps.check_availability_item(FoodType.EGG))
			{
				f = FoodType.EGG;
			}

			else if(ps.check_availability_item(FoodType.FRUIT1))
			{
				f = FoodType.FRUIT1;
			}
			else if(ps.check_availability_item(FoodType.FRUIT2))
			{
				f = FoodType.FRUIT2;
			}
			else if(ps.check_availability_item(FoodType.SANDWICH1))
			{
				f = FoodType.SANDWICH1;
			}
			else 
			{
				f = FoodType.SANDWICH2;
			}
			
			Command c = new Command(CommandType.TAKE_OUT, f);
			System.out.println("Sending food to take out");
			return c;


		}

		return null;
	}

	// regions are 1-4 corresponding to the 4 quadrants of the map.  We can go more fine grain later.
	private Integer getRegion(Double x, Double y) {
		if (x < 0 && y >= 0) {
			return 1;
		}

		if (x >= 0 && y >= 0) {
			return 2;
		}

		if (x < 0 && y < 0) {
			return 3;
		}

		if (x >= 0 && y < 0) {
			return 4;
		}

		return null;
	}


	// count the number of animals in each region of the map
	private HashMap<Integer, Double> getDensities(ArrayList<Animal> animals) {
		HashMap<Integer, Double> densities = new HashMap<Integer, Double>();
		
		for (Integer i=1; i<=4; i++) {
			densities.put(i, 0.0);
		}

		Double x;
		Double y;
		Integer region;
		Double current;
		for (int i=0; i<animals.size(); i++) {
			x = animals.get(i).get_location().x;
			y = animals.get(i).get_location().y;
			region = getRegion(x,y);
			current = densities.get(region);
			densities.put(region, current + 1);
		}

	return densities;
	}

	public FoodType pickDistractorFood(PlayerState ps) {
		FoodType f;
		if(ps.check_availability_item(FoodType.SANDWICH1))
		{
			f = FoodType.SANDWICH1;
		}

		else if(ps.check_availability_item(FoodType.SANDWICH2))
		{
			f = FoodType.SANDWICH2;
		}

		else if(ps.check_availability_item(FoodType.FRUIT1))
		{
			f = FoodType.FRUIT1;
		}
		else if(ps.check_availability_item(FoodType.FRUIT2))
		{
			f = FoodType.FRUIT2;
		}
		else if(ps.check_availability_item(FoodType.EGG))
		{
			f = FoodType.EGG;
		}
		else
		{
			f = FoodType.COOKIE;
		}

		return f;
	}


	// find the region of the map that has had the least number of animals
	// averaged over the last window of seconds
	private Integer getMinRegion(int window) {
		// initialize sum over past densities within the window
		HashMap<Integer, Double> sum_densities = new HashMap<Integer, Double>();
		for (Integer i=1; i<=4; i++) {
			sum_densities.put(i, 0.0);
		}
 
 		// sum over past densities
 		Double density;
 		Double current;
 		HashMap<Integer, Double> densities;
		for (int i=1; i<window + 1; i++) {
			densities = this.densityRecord.get(this.densityRecord.size() - i);
			for (Integer region=1; region<=4; region++) {
				density = densities.get(region);
				current = sum_densities.get(region);
				sum_densities.put(region, current + density);
			}
		}

		// find min region
		Integer minRegion = 0;
		Double minDensity = Double.POSITIVE_INFINITY;
		for (Integer i=1; i<=4; i++) {
			if (sum_densities.get(i) < minDensity) {
				minDensity = sum_densities.get(i);
				minRegion = i;

			}
		}

		return minRegion;
	}

	private Integer getMaxRegion(int window) {
		// initialize sum over past densities within the window
		HashMap<Integer, Double> sum_densities = new HashMap<Integer, Double>();
		for (Integer i=1; i<=4; i++) {
			sum_densities.put(i, 0.0);
		}

		// sum over past densities
		Double density;
		Double current;
		HashMap<Integer, Double> densities;
		for (int i=1; i<window + 1; i++) {
			densities = this.densityRecord.get(this.densityRecord.size() - i);
			for (Integer region=1; region<=4; region++) {
				density = densities.get(region);
				current = sum_densities.get(region);
				sum_densities.put(region, current + density);
			}
		}

		// find max region
		Integer maxRegion = 0;
		Double maxDensity = Double.NEGATIVE_INFINITY;
		for (Integer i=1; i<=4; i++) {
			if (sum_densities.get(i) >= maxDensity) {
				maxDensity = sum_densities.get(i);
				maxRegion = i;
			}
		}

		return maxRegion;
	}

	private Point getWalkingTargetToDenseRegion(int window) {
		return this.getWalkingTarget(window, false);
	}

	private Point getWalkingTargetToEmptyRegion(int window) {
		return this.getWalkingTarget(window, true);
	}

	// specifies where to walk once we know the region of least density
	private Point getWalkingTarget(int window, boolean useMinRegion) {
		Integer minRegion = useMinRegion ? getMinRegion(window) : getMaxRegion(window);
		int distFromOrigin = 20;

		if (minRegion == 1) {
			return new Point(-distFromOrigin, distFromOrigin);
		} 

		if (minRegion == 2) {
			return new Point(distFromOrigin, distFromOrigin);
		}

		if (minRegion == 3) {
			return new Point(-distFromOrigin, -distFromOrigin);
		}

		if (minRegion == 4) {
			return new Point(distFromOrigin, -distFromOrigin);
		}

		return null;
	}

	public Command distract(ArrayList<Double> monkey_dists, ArrayList<Double> goose_dists, PlayerState ps) {
		// don't let food get taken
		Command hideFood = respondIfDanger(monkey_dists, goose_dists, ps);
		if (hideFood != null) {
			return hideFood;
		}

		if(this.descriptiveState.equals("food_out")) {
			Point targetDest;
			if(this.fanningOut) {
				targetDest = this.getWalkingTargetToDenseRegion(5);
			} else {
				targetDest = new Point(0, 0);
			}

			Double delta_x = targetDest.x - ps.get_location().x;
			Double delta_y = targetDest.y - ps.get_location().y;
			double distance = Math.sqrt(Math.pow(delta_x, 2) + Math.pow(delta_y, 2));
			if(distance < 1) {
				this.fanningOut = ! this.fanningOut;
				if(this.fanningOut) {
					targetDest = this.getWalkingTargetToDenseRegion(5);
				} else {
					targetDest = new Point(0, 0);
				}
			}
			this.walkingTarget = targetDest;
			return this.walkToPosition(ps);
		} else if(this.descriptiveState.equals("initial")) {
			FoodType f = this.pickDistractorFood(ps);
			return new Command(CommandType.TAKE_OUT, f);
		}

		return new Command(CommandType.WAIT);
	};

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		this.currentTime += 1;
		this.densityRecord.add(getDensities(animals));  // keep a running record of animal densities
		this.updateState(ps); // descriptive state - one of "taking_out", "initial", "food_out", "putting_back"
		System.out.println(this.descriptiveState);

		HashMap<AnimalType, ArrayList<Double>> distances = getDistances(animals, ps);
		ArrayList<Double> monkey_dists = distances.get(AnimalType.MONKEY);
		ArrayList<Double> goose_dists = distances.get(AnimalType.GOOSE);

		if(this.playerRole.equals("distract")) {
			return this.distract(monkey_dists, goose_dists, ps);
		}

		// basic scaffolding ////////////////////////
		///////////////////////////////////////////////////////////////

		// determines when we walk.  Will want to modify this.  Just something for now to see the behavior.
		if (this.currentTime == 50 || this.currentTime % 300 == 0) {
			this.walkingTarget = getWalkingTargetToEmptyRegion(3);
		}

		if (this.walkingTarget != null) {
			if (ps.is_player_searching()) {
				return new Command(CommandType.ABORT);
			}
			return walkToPosition(ps);  // we have a target, make progress walking to it.  Null target when we arrive.
		}

		Command hideFood = respondIfDanger(monkey_dists, goose_dists, ps);
		if (hideFood != null) {
			return hideFood;
		}

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
/////////////////
