package lunch.g2;

import java.awt.geom.Line2D;
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
	private ArrayList<Point> prevAnimalLocs;
	private Point walkingTarget;
	private boolean inPosition;  // indicates whether we are happy with our location
	private String descriptiveState;
	private String playerRole; // "distract" or "eat"
	private boolean fanningOut; // when distracting are we currently going toward dense region or bringing
	// animals back to center
	private Double total_time;
	private ArrayList<Point> eatLocations;



	public Player()
	{
		turn = 0;
		inPosition = false;
		descriptiveState = "initial";
		playerRole = "distract";
		fanningOut = true;
		prevAnimalLocs = null;
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
		this.total_time = t;

		this.eatLocations = new ArrayList<Point>();
		this.eatLocations.add(new Point(50, -50));
		this.eatLocations.add(new Point(0, -50));
		this.eatLocations.add(new Point(-50, -50));
		this.eatLocations.add(new Point(-50, 0));
		this.eatLocations.add(new Point(-50, 50));

		prevAnimalLocs = new ArrayList<>();
		for(int i = 0; i < animals.size(); i++) {
			prevAnimalLocs.add(new Point(0.0, 0.0));
		}

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
		Double monkey_threshold = 6.0;  // should be lowest we can go
		Double goose_threshold = 5.0;   // ditto
		boolean gooseTooClose = gooseIsNear(goose_dists, goose_threshold);
		boolean monkeysTooClose = monkeysAreNear(monkey_dists, monkey_threshold);
		if ((monkeysTooClose && holdingFood(ps)) || (gooseTooClose && ps.get_held_item_type() == FoodType.SANDWICH)) {
			return new Command(CommandType.KEEP_BACK);
		}

		// We only want to abort if we're close to getting the food out
		if ((gooseTooClose || monkeysTooClose) && ps.is_player_searching() && ps.time_to_finish_search() < 2) {
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

	private FoodType pickEatingFood(PlayerState ps, ArrayList<Double> goose_dists) {
		FoodType f;
		Double minGooseDist = Collections.min(goose_dists)

		if(ps.check_availability_item(FoodType.COOKIE))
		{
			f = FoodType.COOKIE;
		}
		else if (ps.check_availability_item(FoodType.SANDWICH1) && minGooseDist > 25.0) 
		{
			f = SANDWICH1;
		}
		else if (ps.check_availability_item(FoodType.SANDWICH2) && minGooseDist > 25.0) 
		{
			f = SANDWICH2;
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

		return f
	}

	private Command makeEatingProgress(PlayerState ps, boolean waitToRestart, ArrayList<Double> goose_dists) {
		//if holding something, eat it
		if(holdingFood(ps))
		{
			System.out.println("Asking player to eat");
			return new Command(CommandType.EAT);
		}

		//else pull out in order if not already pulling out
		else if(!ps.is_player_searching())
		{
			f = pickEatingFood(ps, goose_dists)

			if(waitToRestart) {
				System.out.println("Asking to wait to restart eating...");
				return new Command(CommandType.WAIT);
			} else {
				Command c = new Command(CommandType.TAKE_OUT, f);
				System.out.println("Sending food to take out");
				return c;
			}
		}

		return null;
	}

	// regions are 1-4 corresponding to the 4 quadrants of the map.  We can go more fine grain later.
	private Integer getRegion(Double x, Double y) {
		if (x <= 0 && y >= 0) {
			return 1;
		}

		if (x >= 0 && y >= 0) {
			return 2;
		}

		if (x <= 0 && y <= 0) {
			return 3;
		}

		if (x >= 0 && y <= 0) {
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

	// sum of animals in each region over past window time steps
	private HashMap<Integer, Double> runningSumDensities(int window) {
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
		return sum_densities;
	}

	// find the region of the map that has had the least number of animals
	// averaged over the last window of seconds
	private Integer getMinRegion(int window) {
		HashMap<Integer, Double> sum_densities = runningSumDensities(window);

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

	private Double getMinRegionDensity(HashMap<Integer, Double> sum_densities) {
		Integer minRegion = 0;
		Double minDensity = Double.POSITIVE_INFINITY;
		for (Integer i=1; i<=4; i++) {
			if (sum_densities.get(i) < minDensity) {
				minDensity = sum_densities.get(i);
				minRegion = i;

			}
		}

		return sum_densities.get(minRegion);
	}

	private Integer getMaxRegion(int window) {
		HashMap<Integer, Double> sum_densities = runningSumDensities(window);

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

	private Point getWalkingTargetToDenseRegion(int window, PlayerState ps) {
		return this.getWalkingTarget(window, false, ps);
	}

	private Point getWalkingTargetToEmptyRegion(int window, PlayerState ps) {
		return this.getWalkingTarget(window, true, ps);
	}

	private boolean areRegionsDiagonal(Integer region1, Integer region2) {
		if (region1 == 1 && region2 == 4) {
			return true;
		}
		if (region1 == 2 && region2 == 3) {
			return true;
		}
		if (region1 == 3 && region2 == 2) {
			return true;
		}
		if (region1 == 4 && region2 == 1) {
			return true;
		}
		return false;
	}
	private boolean isMinRegionMuchBetter(Integer currentRegion, Integer minRegion, int window) {
		
		// if we have to walk diagonal, then no
		if (areRegionsDiagonal(currentRegion, minRegion)) {
			return false;
		}

		double threshold = 3.0;
		HashMap<Integer, Double> sum_densities = runningSumDensities(window);
		Double minRegionDensity = getMinRegionDensity(sum_densities);
		Double currentRegionDensity = sum_densities.get(currentRegion);
		if (currentRegionDensity - minRegionDensity > threshold) {
			return true;
		}
		return false;
	}


	// specifies where to walk once we know the region of least density
	private Point getWalkingTarget(int window, boolean useMinRegion, PlayerState ps) {
		Integer desiredRegion = useMinRegion ? getMinRegion(window) : getMaxRegion(window);
		int distFromOrigin = useMinRegion ? 35: 5;

		// don't walk unless it's really worth the time
		Integer currentRegion = getRegion(ps.get_location().x, ps.get_location().y);
		if (useMinRegion && !isMinRegionMuchBetter(currentRegion, desiredRegion, window)) {
			return null;
		}

		else if (desiredRegion == 1) {
			return new Point(-distFromOrigin, distFromOrigin);
		} 

		else if (desiredRegion == 2) {
			return new Point(distFromOrigin, distFromOrigin);
		}

		else if (desiredRegion == 3) {
			return new Point(-distFromOrigin, -distFromOrigin);
		}

		else if (desiredRegion == 4) {
			return new Point(distFromOrigin, -distFromOrigin);
		}

		return null;
	}


	private Integer getCurrentEatingLocation(PlayerState ps) {
		for (int i=0; i < eatLocations.size(); i++) {
			if (Point.dist(eatLocations.get(i), ps.get_location()) < 8) {
				return i;
			}
		}
		return null;
	}

	private Point getNewEatingLocation(ArrayList<Family> members, PlayerState ps) {
		// go to least occupied eating spot by other family members
		ArrayList<Integer> occupancies = new ArrayList<Integer>(Collections.nCopies(5, 0));
		for (Family member : members) {
			for (int i=0; i <=4; i++) {
				if (Point.dist(member.get_location(), this.eatLocations.get(i)) < 8) {
					occupancies.set(i, occupancies.get(i) + 1);
				}
			}
			
		}

		int minIdx = occupancies.indexOf(Collections.min(occupancies));
		// don't move if new location is equally as good as where we are at
		Integer current = getCurrentEatingLocation(ps);
		if (current != null && occupancies.get(minIdx) == occupancies.get(current) - 1) {
			return null;
		}

		return this.eatLocations.get(minIdx);
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
				targetDest = this.getWalkingTargetToDenseRegion(5, ps);
			} else {
				targetDest = new Point(0, 0);
			}

			Double delta_x = targetDest.x - ps.get_location().x;
			Double delta_y = targetDest.y - ps.get_location().y;
			double distance = Math.sqrt(Math.pow(delta_x, 2) + Math.pow(delta_y, 2));
			if(distance < 1) {
				this.fanningOut = ! this.fanningOut;
				if(this.fanningOut) {
					targetDest = this.getWalkingTargetToDenseRegion(5, ps);
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

	private void updatePlayerRole(PlayerState ps, ArrayList<Family> members) {
		if (this.playerRole.equals("eat") && startDistract(ps, members)) {
			this.playerRole = "distract";
		}
		else if (this.playerRole.equals("distract") && stopDistract(ps, members)){
			this.playerRole = "eat";
		}
	}

	public double dist(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p2.x - p1.x, 2.0) + Math.pow(p2.y - p1.y, 2.0));
	}

	public ArrayList<Integer> predictDirections(Point pp, ArrayList<Point> prevAnimalLocs, ArrayList<Animal> animals) {
		ArrayList<Integer> stepsToOrbit = new ArrayList<>();

		for(int i = 0; i < animals.size(); i++) {
			Point currLocation = animals.get(i).get_location();
			Point prevLocation = prevAnimalLocs.get(i);
			double deltaY = prevLocation.y - currLocation.y;
			double deltaX = currLocation.x - prevLocation.x;

			double theta = Math.atan2(deltaY, deltaX);
			double angle = 180 * theta / Math.PI;

			boolean initiallyWithin = this.dist(pp, new Point(currLocation.x, currLocation.y)) <= 40.0;
			int timeStepsAway = 100;
			for(int d = 1; d < (int) (Math.sqrt(2) * 100.0); d += 5) {
				double newX = currLocation.x + Math.cos(theta) * d;
				double newY = currLocation.y - Math.sin(theta) * d;
				double dist = this.dist(pp, new Point(newX, newY));

				if(dist <= 40.0 && !initiallyWithin) {
					timeStepsAway = d;
					break;
				}

				if(dist > 40.0 && initiallyWithin) {
					timeStepsAway = - d;
					break;
				}

				if(newX < -50 || newX > 50 || newY < -50 || newY > 50) {
					if(initiallyWithin) {
						timeStepsAway = -(d + 40);
					} else {
						timeStepsAway = d + 100;
					}
					break;
				}
			}
			stepsToOrbit.add(timeStepsAway);
		}

		return stepsToOrbit;
	}


	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		this.currentTime += 1;
		this.densityRecord.add(getDensities(animals));  // keep a running record of animal densities
		this.updateState(ps); // descriptive state - one of "taking_out", "initial", "food_out", "putting_back"
		updatePlayerRole(ps, members);

		ArrayList<Integer> stepsToOrbit = predictDirections(ps.get_location(), prevAnimalLocs, animals);
		double minSteps = 99999;
		int numWithin = 0;
		int numToEnter = 0;
		for(int steps : stepsToOrbit) {
			minSteps = Math.min(steps, minSteps);
			if(steps < 0) {
				numWithin += 1;
			}
		}
		minSteps *= -1.0;
		for(int steps : stepsToOrbit) {
			if(steps > 0 && steps <= minSteps) {
				numToEnter += 1;
			}
		}

		double flowRatio = numWithin == 0 ? 0.0 : (double) numWithin / (double) Math.max(1.0, numToEnter);
		boolean waitToEat = flowRatio >= 5.0;

		for(int i = 0; i < animals.size(); i++) {
			prevAnimalLocs.set(i, animals.get(i).get_location());
		}

		// get animal distances, useful for many core functions
		HashMap<AnimalType, ArrayList<Double>> distances = getDistances(animals, ps);
		ArrayList<Double> monkey_dists = distances.get(AnimalType.MONKEY);
		ArrayList<Double> goose_dists = distances.get(AnimalType.GOOSE);

		this.playerRole = "eat";

		if(this.playerRole.equals("distract")) {
			return distract(monkey_dists, goose_dists, ps);
		}

		Command hideFood = respondIfDanger(monkey_dists, goose_dists, ps);
		if (hideFood != null) {
			return hideFood;
		}

		// determines when we walk.  Will want to modify this.  Just something for now to see the behavior.
		if (this.currentTime == 30 || this.currentTime % 100 == 0) {
			this.walkingTarget = getNewEatingLocation(members, ps);  // also checks if walking is worth the time
		}

		if (this.walkingTarget != null) {
			if (ps.is_player_searching()) {
				return new Command(CommandType.ABORT);
			}
			return walkToPosition(ps);  // we have a target, make progress walking to it.  Null target when we arrive.
		}

		Command progress = makeEatingProgress(ps, waitToEat);
		if (progress != null) {
			return progress;
		}

		// just run TA code in case we didn't do anything
		print("Returning default command");
		return getDefaultCommand(members, animals, ps, monkey_dists, goose_dists);

		//////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////

	}

	//boolean jobFree to check if anyone is currently distracting.

	void print(String str){
		System.out.println(str);
	}

	boolean jobAvailable(ArrayList<Family> members)
	{
		Point center = new Point(0,0);
		Double radiusOfDistraction = 5.0;
		for(Family member: members)
		{
			if (Point.dist(member.get_location(), center) > radiusOfDistraction) 
			{
				return false;
			}
		}

		return true;

	}


	boolean startDistract(PlayerState ps, ArrayList<Family> members)
	{
		
		//if there's quite a bit of time left and we're almost done, start distracting
		if( this.total_time - this.currentTime > 100 && ps.get_time_for_item(FoodType.SANDWICH2)<3)
		{
			return true; 
		}

		else if(jobAvailable(members)) //if no one is doing it, take one for the team.
			return true;

		else
			return false;


	}


	boolean stopDistract(PlayerState ps, ArrayList<Family> members)
	{
		

		//if someone else came into fill in your spot, time to move on
		if(!jobAvailable(members))
			{
				return true;

			}

		// you still need to finish your sandwich so time to move on
		if (this.total_time - this.currentTime < 50) 
		{
			return true;
		}

		return false; //only stop distracting when you have good reason to.

	}

	public Command getDefaultCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps, ArrayList<Double> monkey_dists, ArrayList<Double> goose_dists) {
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
