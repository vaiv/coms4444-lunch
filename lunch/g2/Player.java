package lunch.g2;

import java.util.Collections;

import java.util.Random;
import java.util.HashMap;
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
	private Random random;
	private Integer id;
	private String avatars;
	private ArrayList<HashMap<Integer, Double>> densityRecord;
	private Double currentTime;
	private ArrayList<Point> prevAnimalLocs;
	private Point walkingTarget;
	private boolean inPosition;  // indicates whether we are happy with our location
	private String descriptiveState;
	private String playerRole; // "distract" or "eat"
	// animals back to center
	private Double total_time;
	private ArrayList<Point> eatLocations;
	private double flowThresh;
	private int timeEating;
	private int timeDistracting;
	private boolean foodAlmostGone;
	private Double totalTimeDistracting;
	private int timeSinceLastHarassed;
	private ArrayList<Double> keepBackHistory;

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

	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals,
					   Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		avatars = "flintstone";
		this.random = new Random(s);
		this.densityRecord = new ArrayList<HashMap<Integer, Double>>();
		this.currentTime = 0.0;
		this.descriptiveState = "initial";
		this.playerRole = "eat";
		this.total_time = t;
		this.flowThresh = 2.5;
		this.timeEating = 0;
		this.timeDistracting = 0;
		this.foodAlmostGone = false;
		this.totalTimeDistracting = 0.0;
		this.timeSinceLastHarassed = 0;
		this.keepBackHistory = new ArrayList<Double>();

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
	private HashMap<AnimalType, ArrayList<Double>> getDistances(ArrayList<Animal> animals, Point p) {
		ArrayList<Double> monkey_dists = new ArrayList<Double>();
		ArrayList<Double> goose_dists = new ArrayList<Double>();

		for(Integer i=0; i<animals.size(); i++)
		{
			Animal animal = animals.get(i);
			Double dist = Point.dist(p, animal.get_location());
			
			if (animal.which_animal() == AnimalType.MONKEY) {
				monkey_dists.add(dist);
			}
			
			else if (animal.which_animal() == AnimalType.GOOSE) {
				goose_dists.add(dist);
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
		if (monkey_dists.size() < 3) {
			return false;
		}

		// check if 3 closest monkeys are within a distance of threshold suggested to be 6
		int num_close = 0;
		for (int i=0; i<3; i++) {
			if (monkey_dists.get(i) <= threshold) {num_close += 1;}
		}
		return num_close >= 3;
	}

	// indicates whether geese are about to steal food
	private boolean gooseIsNear(ArrayList<Double> goose_dists, Double threshold) {
		// check if closest goose is within a distance of threshold suggested to be 5
		if(goose_dists.size() == 0) {
			return false;
		}
		return goose_dists.get(0) <= threshold;
	}


	// issues a response command if in danger
	private Command respondIfDanger(ArrayList<Double> monkey_dists, ArrayList<Double> goose_dists, PlayerState ps) {
		Double monkey_threshold = 6.5;  // should be lowest we can go
		Double goose_threshold = 5.5;   // ditto
		boolean gooseTooClose = gooseIsNear(goose_dists, goose_threshold);
		boolean monkeysTooClose = monkeysAreNear(monkey_dists, monkey_threshold);
		if ((monkeysTooClose && holdingFood(ps)) || (gooseTooClose && ps.get_held_item_type() == FoodType.SANDWICH)) {
			this.timeSinceLastHarassed = 0;
			this.keepBackHistory.add(this.currentTime);
			return new Command(CommandType.KEEP_BACK);
		}

		// We only want to abort if we're close to getting the food out
		if ((gooseTooClose || monkeysTooClose) && ps.is_player_searching() && ps.time_to_finish_search() < 2) {
			this.timeSinceLastHarassed = 0;
			return new Command(CommandType.ABORT);
		}

		return null;
	}

	private boolean holdingFood(PlayerState ps) {
		return ps.get_held_item_type() != null;
	}

	private Command walkToPosition(ArrayList<Family> members, PlayerState ps) {
		Double delta_x = this.walkingTarget.x - ps.get_location().x;
		Double delta_y = this.walkingTarget.y - ps.get_location().y;

		Double bearing = Math.atan(delta_y / delta_x);
		int fac = (delta_x < 0) ? -1: 1;
		Point move = new Point(ps.get_location().x + fac * Math.cos(bearing), ps.get_location().y + fac * Math.sin(bearing));

		if (Point.dist(ps.get_location(), this.walkingTarget) < 1) {
			this.walkingTarget = null;
		}
		
		boolean found_valid_move = Point.within_bounds(move);
		return Command.createMoveCommand(move);
	}

	private FoodType pickEatingFood(PlayerState ps, ArrayList<Double> goose_dists) {
        FoodType f;
        Double minGooseDist = goose_dists.size() == 0 ? Double.POSITIVE_INFINITY : Collections.min(goose_dists);
        if(ps.check_availability_item(FoodType.COOKIE))
        {
            f = FoodType.COOKIE;
        }
        else if (ps.check_availability_item(FoodType.SANDWICH1) && minGooseDist > 25.0 && goose_dists.size()>20) 
        {
            f = FoodType.SANDWICH1;
        }
        else if (ps.check_availability_item(FoodType.SANDWICH2) && minGooseDist > 25.0 && goose_dists.size()>20) 
        {
            f = FoodType.SANDWICH2;
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
        return f;
    }

	private Command makeEatingProgress(PlayerState ps, boolean waitToEat, ArrayList<Double> goose_dists) {
		//if holding something, eat it
		if(holdingFood(ps)) {
			return new Command(CommandType.EAT);
		}

		//else pull out in order if not already pulling out
		else if(!ps.is_player_searching())
		{
			FoodType f = pickEatingFood(ps, goose_dists);

			if(waitToEat) {
				return new Command(CommandType.WAIT);
			} else {
				Command c = new Command(CommandType.TAKE_OUT, f);
				return c;
			}
		}

		return null;
	}

	// regions are 1-4 corresponding to the 4 quadrants of the map.  We can go more fine grain later.
	private Integer getRegion(Double x, Double y) {
		int biasCenter = -25;

		if (x <= biasCenter && y <= biasCenter) {
			return 1;
		}

		if (x >= biasCenter && y <= biasCenter) {
			return 2;
		}

		if (x < biasCenter && y >= biasCenter) {
			return 3;
		}

		if (x >= biasCenter && y >= biasCenter) {
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
		
		Integer region;
		Double current;
		for (int i=0; i<animals.size(); i++) {
			Animal animal = animals.get(i);
			// only care about monkeys
			if (animal.which_animal() == AnimalType.MONKEY) {
				region = getRegion(animal.get_location().x, animal.get_location().y);
				current = densities.get(region);
				densities.put(region, current + 1);
			}
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

	private boolean isDistractorEffective() {
		if (this.currentTime < 25) {
			return true;
		}

		HashMap<Integer, Double> sum_densities = runningSumDensities(20);
		Double total = 0.0;
		for (int region=1; region<=4; region++) {
			total += sum_densities.get(region);
		}
		Double percentDistracted = total != 0.0 ? sum_densities.get(4) / total : 1.0;
		if (percentDistracted > .65) {
			return true;
		} 
		return false;
	}

	// find the region of the map that has had the least number of animals
	// averaged over the last window of seconds
	// don't consider bottom right because we're looking for somewhere to eat
	private Integer getMinRegion(int window) {
		HashMap<Integer, Double> sum_densities = runningSumDensities(window);

		// find min region
		Integer minRegion = 0;
		Double minDensity = Double.POSITIVE_INFINITY;
		for (Integer i=1; i<=3; i++) {
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

	private boolean areMonkeysEverywhere() {
		HashMap<Integer, Double> sum_densities = runningSumDensities(75);
		double total = 0.0;
		for (Integer i=1; i<=4; i++) {
			total += sum_densities.get(i);
		}
		for (Integer i=1; i<=4; i++) {
			if (sum_densities.get(i) / total < 0.1) {
				return false;
			}
		}
		return true;
	}

	private Integer getCurrentEatingLocation(PlayerState ps) {
		for (int i=0; i < eatLocations.size(); i++) {
			if (Point.dist(eatLocations.get(i), ps.get_location()) < 10) {
				return i;
			}
		}
		return null;
	}

	private Integer freeEatingCornerIdx(ArrayList<Integer> occupancies) {
		for (int i=0; i<=4; i+=2) {  // corners are at the even indices
			if (occupancies.get(i) == 0) {
				return i;
			}
		}
		return null;
	}

	private int convertRegionToEatingCorner(int region) {
		if (region == 1) {
			return 2;
		}
		else if (region == 2) {
			return 0;
		}
		else {
			return 4;
		}
	}

	private Point getNewEatingLocation(ArrayList<Family> members, PlayerState ps, boolean mustMove) {
		// don't move if we have not been harassed recently
		if (!mustMove && this.timeSinceLastHarassed > 20) {
			return null;
		}

		if (this.currentTime < 50.0) {
			return this.eatLocations.get(3);
		}

		// go to least occupied eating spot by other family members
		ArrayList<Integer> occupancies = new ArrayList<Integer>(Collections.nCopies(5, 0));
		for (Family member : members) {
			for (int i=0; i <=4; i++) {
				if (Point.dist(member.get_location(), this.eatLocations.get(i)) < 10) {
					occupancies.set(i, occupancies.get(i) + 1);
				}
			}
			
		}

		int minIdx = occupancies.indexOf(Collections.min(occupancies));
		
		// don't move if we already have an isolated position
		Integer current = getCurrentEatingLocation(ps);
		if (current != null && occupancies.get(current) == 1) {
			return null;
		}

		// if there is an empty eating spot, check if a corner is open
		// otherwise move to the empty spot 
		if (occupancies.get(minIdx) == 0) {
			Integer freeCorner = freeEatingCornerIdx(occupancies);
			return freeCorner != null ? this.eatLocations.get(freeCorner) : this.eatLocations.get(minIdx);
		}

		// if there is no empty spot, move to the corner of historically low monkey counts
		// we define historically as the last 100 seconds
		int minRegion = getMinRegion(200);
		int minCorner = convertRegionToEatingCorner(minRegion);
		return this.eatLocations.get(minCorner);
	}

	private boolean isPointSafe(Point p, ArrayList<Animal> animals) {
		HashMap<AnimalType, ArrayList<Double>> distances = getDistances(animals, p);
		ArrayList<Double> monkey_dists = distances.get(AnimalType.MONKEY);
		ArrayList<Double> goose_dists = distances.get(AnimalType.GOOSE);
		return !monkeysAreNear(monkey_dists, 6.0) && !gooseIsNear(goose_dists, 5.0);
	}

	private Point findSafeStep(PlayerState ps, ArrayList<Animal> animals) {
		// just try random steps until one is safe, or return false after 50 tries
		boolean found_valid_move = false;
		Point proposed = new Point(-1,-1);
		int trial = 0;
		while(!found_valid_move && trial <= 50)
		{
			trial += 1;
			Double bearing = random.nextDouble()*2*Math.PI;
			proposed = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
			boolean inDistractZone = Point.dist(proposed, new Point(25,25)) < 40;
			found_valid_move = Point.within_bounds(proposed) && inDistractZone && isPointSafe(proposed, animals);
		}
		if (found_valid_move) {
			return proposed;
		}
		return null;
	}

	public Command distract(PlayerState ps, ArrayList<Animal> animals) {
		if(this.descriptiveState.equals("initial")) {
			FoodType f = this.pickDistractorFood(ps);
			return new Command(CommandType.TAKE_OUT, f);
		}

		// if food is out, random walk to freespace
		if (this.descriptiveState.equals("food_out")) {
			// find freespace
			Point safeStep = findSafeStep(ps, animals);
			if (safeStep != null) {
				// make the step
				return Command.createMoveCommand(safeStep);
			}
		}

		// otherwise just wait
		return new Command(CommandType.WAIT);
	};

	// moves us slightly away from the wall
	private Point moveInwardTarget(PlayerState ps) {
		double x = ps.get_location().x / 1.5;
		double y = ps.get_location().y / 1.5;
		return new Point(x,y);
	}

	// gives how many times our fair shair of distracting we're ever willing to do
	private double getShareMultiple(int familySize) {
		if (familySize == 2) {
			return 1.0;
		}
		else if (familySize == 3) {
			return 1.7;
		}
		else if (familySize == 4 || familySize == 5) {
			return 2.0;
		}
		else if (familySize == 6) {
			return 2.5;
		}
		else {
			return 3.0;
		}
	}

	private boolean distractingTooOften(int familySize) {
		double shareMultiple = getShareMultiple(familySize);
		// limit to shareMultiple our fair share of distracting, we need to start eating too
		double threshold = 1 / (double)familySize * shareMultiple; 
		if (this.totalTimeDistracting / this.total_time > threshold) {
			return true;
		}
		return false;
	}

	private void updatePlayerRole(PlayerState ps, ArrayList<Family> members, ArrayList<Double> monkey_dists) {
		int timeInCurrentRole;

		// update time in current role
		if (this.playerRole.equals("eat")) {
			this.timeEating += 1; // time since started role
			timeInCurrentRole = this.timeEating;
		}

		else {
			this.timeDistracting += 1;  // time since started role
			timeInCurrentRole = this.timeDistracting;
			this.totalTimeDistracting += 1;  // time spent distracting over the entire game
		}

		// cases for which we should never distract again
		if(members.size() == 1 || timeRemaining() < 400 || distractingTooOften(members.size()) || monkey_dists.size() < 3) {
			this.playerRole = "eat";
		}

		else if (this.playerRole.equals("eat") && startDistract(ps, members) && timeInCurrentRole > (int)(this.total_time / 10)) {
			this.playerRole = "distract";
			this.timeDistracting = 0;
			this.walkingTarget = this.foodAlmostGone ? moveInwardTarget(ps) : new Point(25,25);
		}
		else if (this.playerRole.equals("distract") && stopDistract(ps, members) && timeInCurrentRole > (int)(this.total_time / 10)) {
			this.playerRole = "eat";
			this.timeEating = 0;
			this.walkingTarget = getNewEatingLocation(members, ps, true);
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

	private double getFlowRatio(PlayerState ps, ArrayList<Animal> animals) {
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
		return flowRatio;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		try {
			this.currentTime += 1;
			this.timeSinceLastHarassed += 1;
			this.densityRecord.add(getDensities(animals));  // keep a running record of animal densities
			
			// get animal distances, useful for many core functions
			HashMap<AnimalType, ArrayList<Double>> distances = getDistances(animals, ps.get_location());
			ArrayList<Double> monkey_dists = distances.get(AnimalType.MONKEY);
			ArrayList<Double> goose_dists = distances.get(AnimalType.GOOSE);

			this.updateState(ps); // descriptive state - one of "taking_out", "initial", "food_out", "putting_back"
			
			if (this.currentTime > 100) {
				updatePlayerRole(ps, members, monkey_dists);
			}

			double flowRatio = getFlowRatio(ps, animals);
			double lowThreshold = 2.5;
			double highThreshold = 4.0;
			double flowThreshold;
			if (this.currentTime > 80) {
				flowThreshold = this.areMonkeysEverywhere() ? highThreshold : lowThreshold;
			}
			else {
				flowThreshold = lowThreshold;
			}

			// if monkeys are uniform across the map, we should be less patient
			// if the monkeys seem to be congregating in one region, we should be more patient to let them go there
			boolean waitToEat = flowRatio >= flowThreshold;

			for(int i = 0; i < animals.size(); i++) {
				prevAnimalLocs.set(i, animals.get(i).get_location());
			}

			Command hideFood = respondIfDanger(monkey_dists, goose_dists, ps);
			if (hideFood != null) {
				return hideFood;
			}

			// determines when we check to see if we should update our position
			if ((this.currentTime == 5.0 || this.currentTime % 150 == 0) && this.playerRole.equals("eat")) {
				this.walkingTarget = getNewEatingLocation(members, ps, false);  // also checks if walking is worth the time
			}

			if (this.walkingTarget != null) {
				if (ps.is_player_searching()) {
					return new Command(CommandType.ABORT);
				}
				return walkToPosition(members, ps);  // we have a target, make progress walking to it.  Null target when we arrive.
			}

			if(this.playerRole.equals("distract")) {
				return distract(ps, animals);
			}

			Command progress = makeEatingProgress(ps, waitToEat, goose_dists);
			if (progress != null) {
				return progress;
			}

			// just run TA code in case we didn't do anything
			return new Command(CommandType.WAIT);
		
		} catch(Exception e) {
			return new Command(CommandType.WAIT);
		}
	}

	//boolean jobFree to check if anyone is currently distracting.
	boolean jobAvailable(ArrayList<Family> members)
	{
		Point distractionSpot = new Point(50,50);
		Double radiusOfDistraction = 70.0;
		int numberDistractors = 0;
		for(Family member: members)
		{
			boolean isSelf = member.get_id() == this.id;
			if (Point.dist(member.get_location(), distractionSpot) < radiusOfDistraction && !isSelf)
			{
				numberDistractors += 1;
			}
		}

		if (numberDistractors >= 1) {
			return false;
		}

		return true;

	}

	int time_needed(PlayerState ps) // get the time needed to finish eating everything 
	{

		return ps.get_time_for_item(FoodType.SANDWICH1) + ps.get_time_for_item(FoodType.SANDWICH2) +ps.get_time_for_item(FoodType.FRUIT1) + ps.get_time_for_item(FoodType.FRUIT2) + ps.get_time_for_item(FoodType.EGG)+ ps.get_time_for_item(FoodType.COOKIE);

	}


	Double timeRemaining() {
		return this.total_time - this.currentTime;
	}

	boolean startDistract(PlayerState ps, ArrayList<Family> members)
	{
		

		//if there's quite a bit of time left and we're almost done, start distracting
		if( timeRemaining() > 100 && time_needed(ps)<3)
		{
			foodAlmostGone = true;
			return true; 
		}

		// if no one is doing it, or doing it well, take one for the team.
		else if((jobAvailable(members) || !isDistractorEffective()) && time_needed(ps)/timeRemaining() < 0.5 )
			return true;
		
		else
			return false;
	}

	boolean stopDistract(PlayerState ps, ArrayList<Family> members) {
		// we MUST not stop if we would only start again next time step!
		return !startDistract(ps, members);
	}

}

