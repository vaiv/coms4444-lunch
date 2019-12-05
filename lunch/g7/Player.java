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
	private int time;
	private int timeLimit;
	private FoodType foodToTakeOut = null;
	private final double eps = 10e-6;
	private final double monkeyRange = 6.0;
	private final double gooseRange = 3.0;
	private int size;
	private double currentRatio = 0;
	private double ratioToGo;
	private int roundToGo= 1;
	private final int totalFoodTime = (3*2 + 2*3 + 1)*60;
	private Point desToDistract = null;

	private boolean inPosition = false;
	private boolean isDistractor = false;
	private boolean isArrive = false;
	private boolean enoughTime = true;
	private boolean inDistractPosition = false;
	private boolean backToCorner = false;

	public Player()
	{
		turn = 0;
	}

	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		avatars = "jetson";
		random = new Random(s);
		size = members.size();
		timeLimit = (int)t;
		ratioToGo = 1.0 / size;
		return avatars;
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{
		++time;

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

		// initial move, go to corresponding corners
		if (!inPosition) {
			Point dest = new Point(0, 0);
			switch ((this.id + 1) % 3) {
				case 0:
					dest = new Point(50, -50);
					break;
				case 1:
					dest = new Point(-50, 50);
					break;
				case 2:
					dest = new Point(-50, -50);
					break;
			}
//			}
			Point start = new Point(ps.get_location());
			Command res = getMove(start, dest, ps);
			if (res == null) {
				inPosition = true;
			}
			else {
				return res;
			}
		}

		// calculate how long the distractor should distract
		double ratioForTime = 0.65;
		if (monkeys.size() <= 50)
			ratioForTime = 0.50;
		else if (monkeys.size() <= 100)
			ratioForTime = 0.60;
		else
			ratioForTime = 0.70;
		ratioForTime += geese.size() * 0.10;
		ratioForTime = Math.min(ratioForTime, 1.0);

		// if there is no distractor, we need to distract
		if (time == 250 + 75 * ps.get_id() && !isThereDistractor(members, monkeys)) {
			if (monkeys.size() <= 15 || timeLimit <= 600) {
				isDistractor = false;
			}
			else {
				isDistractor = true;
			}
		}

		// move distractor to position
		if (isDistractor && !inDistractPosition && enoughTime) {
			Point dest = new Point(0, 0);
			Command res = getMove(ps.get_location(), dest, ps);
			if (res != null) {
				return res;
			}
			inDistractPosition = true;
		}

//		// if someone else decided to be the distractor, stop being the distractor and move away
//		if (time == 460 + 50 * ps.get_id() && isThereDistractor(members, monkeys)) {
//			isDistractor = false;
//		}
//
//		if (!isDistractor && !backToCorner) {
//			Point dest = new Point(0, 0);
//			switch ((this.id + 1) % 3) {
//				case 0:
//					dest = new Point(50, -50);
//					break;
//				case 1:
//					dest = new Point(-50, 50);
//					break;
//				case 2:
//					dest = new Point(-50, -50);
//					break;
//			}
//			Command res = getMove(ps.get_location(), dest, ps);
//			if (res != null) {
//				return res;
//			}
//			backToCorner = true;
//		}

		// if there is not enough time for distractor to finish food, go to corner
		if (isDistractor && currentRatio <= ratioForTime - 0.1 && time >= ratioForTime * timeLimit) {
			Point dest = new Point(50, -50);
			Command res = getMove(ps.get_location(), dest, ps);
			if (res != null) {
				return res;
			}
			enoughTime = false;
			isDistractor = false;
		}

		// if the player almost finished food, and there is sufficient time to distract
		if (currentRatio >= 0.99871 && timeLimit - time >= 700 + 1.5 * geese.size()) {
			if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
				return new Command(CommandType.KEEP_BACK);
			}
			isDistractor = true;
			Point dest = new Point(0, 0);
			if (this.id == 0) {
				Point nxt = pointToHelpDistract(ps.get_location(), getDistractorLoctaion(members, monkeys), members);
				if (!isArrive) {
					dest = nxt;
					desToDistract = nxt;
				}
				else {
					dest = desToDistract;
				}
				if (dest == null) {
					dest = new Point(0, 0);
				}
			}
			else {
				Point nxt = pointToHelpDistract(ps.get_location(), getDistractorLoctaion(members, monkeys), members);
				if (!isArrive) {
					dest = nxt;
					desToDistract = nxt;
				}
				else {
					dest = desToDistract;
				}
				if (dest == null) {
					switch ((this.id + 1) % 3) {
						case 0:
							dest = new Point(20, -20);
							break;
						case 1:
							dest = new Point(-20, 20);
							break;
						case 2:
							dest = new Point(-20, -20);
							break;
					}
				}
			}
			Command res = getMove(ps.get_location(), dest, ps);
			if (res != null) {
				isArrive = true;
				return res;
			}
		}

		if (Double.compare(currentRatio, 0.9) == 0) {
			//TODO: Put back and move the current player to the middle
		}
		else if (areMembersMoving(members) && ps.get_location().equals(new Point(0, 0))) {
			//TODO: Put back and move to that player's location
		}


//		if (Double.compare(currentRatio, ratioToGo*roundToGo) == 0) {
//			if (toGo) {
//				//TODO: Move the current player to the middle
//			}
//			else {
//				//TODO: Move a small step to show the person has finished and then put back and go.
//			}
//			roundToGo++;
//		}
//		else if (areMembersMove(members)) {
//			roundToGo++;
//			if (ps.get_location().equals(new Point(0, 0))) {
//				//TODO: Move the current middle player to that player's location.
//			}
//		}

		monkeys.sort(Comparator.comparingDouble(a -> Point.dist(a.get_location(), ps.get_location())));
		geese.sort(Comparator.comparingDouble(a -> Point.dist(a.get_location(), ps.get_location())));

		// abort taking out if animal is too close
		if(ps.is_player_searching() && ps.get_held_item_type() == null && isDangerours(ps, monkeys, geese))
		{
			return new Command(CommandType.ABORT);
		}
		// keep food item back if animal is too close
		else if(!ps.is_player_searching() && ps.get_held_item_type() != null && isDangerours(ps, monkeys, geese))
		{
			foodToTakeOut = null;
			return new Command(CommandType.KEEP_BACK);
		}
		// move away from animal
//		else if(isDangerours(ps, monkeys, geese))
//		{
////			List<Double> directions = new ArrayList<>();
////			for (Animal monkey : monkeys) {
////				if (Point.dist(monkey.get_location(), ps.get_location()) <= 5) {
////					directions.add(Math.atan(
////							(monkey.get_location().y - ps.get_location().y) / (monkey.get_location().x - ps.get_location().x)));
////				}
////			}
////
////			for (Animal goose : geese) {
////
////			}
//
//			boolean found_valid_move= false;
//			Point next_move = new Point(-1,-1);
//			while(!found_valid_move)
//			{
//				Double bearing = random.nextDouble()*2*Math.PI;
//				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
//				found_valid_move = Point.within_bounds(next_move);
//			}
//			return Command.createMoveCommand(next_move);
//		}
		// if no animal is near then take out food
		else if (!ps.is_player_searching() && ps.get_held_item_type()==null && shouldPullFood(ps, monkeys, geese))
		{

			List<FoodType> allFood = getAllFood();
			for(FoodType food_type: allFood) // FoodType.values()
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
		else if(!ps.is_player_searching() && ps.get_held_item_type() != null)
		{
			// if almost finished food, flash it to distract until the last seconds
			if (currentRatio >= 0.99871 && timeLimit - time > 220 + 2 * geese.size()) {
				return new Command();
			}
			currentRatio += 1.0 / totalFoodTime;
			return new Command(CommandType.EAT);
		}
		// System.out.println("player is searching");
		return new Command();

	}

	private boolean areMembersMoving(ArrayList<Family> members) {
		Point point1 = new Point(0, 0);
		Point point2 = new Point(50, -50);
		Point point3 = new Point(-50, 50);
		Point point4 = new Point(50, 50);
		Point point5 = new Point(-50, -50);
		for (Family member : members) {
			Point cur = member.get_location();
			if (!cur.equals(point1) && !cur.equals(point2) && !cur.equals(point3) && !cur.equals(point4) &&
					!cur.equals(point5)) {
				return true;
			}
		}
		return false;
	}

	// get all food type, sorted by scores, from high to low
	private ArrayList<FoodType> getAllFood() {
		ArrayList<FoodType> result = new ArrayList<>(Arrays.asList(
				FoodType.COOKIE, FoodType.FRUIT, FoodType.EGG, FoodType.FRUIT1, FoodType.FRUIT2,
				FoodType.SANDWICH, FoodType.SANDWICH1, FoodType.SANDWICH2
		));
		return result;
	}

	private ArrayList<FoodType> getUnfinishedFood(PlayerState ps) {
		ArrayList<FoodType> allFood = getAllFood();
		ArrayList<FoodType> result = new ArrayList<>();
		for(FoodType food_type: allFood) { // FoodType.values()
			if(ps.check_availability_item(food_type)) {
				result.add(food_type);
			}
		}
		return result;
	}

	private boolean isDangerours(PlayerState ps, List<Animal> monkeys, List<Animal> geese) {
		if (foodToTakeOut != null && (foodToTakeOut == FoodType.SANDWICH1 || foodToTakeOut == FoodType.SANDWICH2))
			return detectGeese(ps, geese) || detectMonkeys(ps, monkeys);
		else if (foodToTakeOut != null) {
			return detectMonkeys(ps, monkeys);
		}
		else {
			return false;
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
			if (dist <= gooseRange + eps) {
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
			if (dist <= monkeyRange + eps) {
				monkeyCount++;
			}
		}
		return monkeyCount >= 3;
	}

	// returns a valid move command from start to dest, if already in dest, return null
	public Command getMove(Point start, Point dest, PlayerState ps) {
		if (Math.abs(Point.dist(start, dest)) <= 0.00001) {
			return null;
		}
		if (ps.is_player_searching()) {
			return new Command(CommandType.ABORT);
		}
		if (ps.get_held_item_type() != null) {
			return new Command(CommandType.KEEP_BACK);
		}
		double dist = Math.sqrt(Math.pow(dest.x - start.x, 2) + Math.pow(dest.y - start.y, 2));
		if (dist <= 1.0)
			return Command.createMoveCommand(new Point(dest));

		double ratio = 1 / dist;
		double xVector = start.x + (dest.x - start.x) * ratio * 0.999999;
		double yVector = start.y + (dest.y - start.y) * ratio * 0.999999;
		return Command.createMoveCommand(new Point(xVector, yVector));
	}

	private boolean shouldPullFood(PlayerState ps, List<Animal> monkeys, List<Animal> geese) {
		List<FoodType> foodType = getUnfinishedFood(ps);
		if (foodType.isEmpty()) {
			return false;
		}
		FoodType cur = foodType.get(0);
		double distMonkey = Integer.MAX_VALUE;
		double distGeese = Integer.MAX_VALUE;
		int rangeGeese = isDistractor ? 0 : (geese.size() >= 50 ? 7 : 12);
		int rangeMonkeys = isDistractor ? 0 : 30;
		if (monkeys.size() >= 3) {
			distMonkey = Point.dist(monkeys.get(2).get_location(), ps.get_location());
		}
		if (!geese.isEmpty()) {
			distGeese = Point.dist(geese.get(0).get_location(), ps.get_location());
		}
		if (geese.size() >= 50 && (cur == FoodType.SANDWICH1 || cur == FoodType.SANDWICH2)) {
			return true;
		}
		return ((cur != FoodType.SANDWICH1 && cur != FoodType.SANDWICH2) || distGeese >= rangeGeese) && distMonkey >= rangeMonkeys;
	}

	// if there is distractor on board
	private boolean isThereDistractor(List<Family> members, List<Animal> monkeys) {
		int totalMonkeys = monkeys.size();
		for (Family member : members) {
			if (member.get_id().equals(id))
				continue;
			Point location = member.get_location();
			if (Double.compare(location.x, 0) == 0 && Double.compare(location.y, 0) == 0) {
				return true;
			}
			int numMonkeys = 0;
			for (Animal monkey : monkeys) {
				if (Point.dist(member.get_location(), monkey.get_location()) <= 35) {
					numMonkeys++;
				}
			}
			if (numMonkeys >= 0.75 * totalMonkeys){
				return true;
			}
		}
		return false;
	}

	private Point getDistractorLoctaion(List<Family> members, List<Animal> monkeys) {
		int maxMonkeys = 0;
		Point loc = null;
		for (Family member : members) {
			int numberOfMonkeys = 0;
			for (Animal monkey : monkeys) {
				if (Point.dist(member.get_location(), monkey.get_location()) <= 40) {
					numberOfMonkeys++;
				}
			}
			if (maxMonkeys < numberOfMonkeys) {
				maxMonkeys = numberOfMonkeys;
				loc = member.get_location();
			}
		}
		return loc;
	}

	private Point pointToHelpDistract(Point cur, Point distractor, List<Family> members) {
		if (distractor == null) {
			return cur;
		}
		double dist = 28;
		boolean isMe = true;
		Point des = null;
		for (Family member : members) {
			if (Point.dist(distractor, member.get_location()) <= eps && !member.get_id().equals(id)) {
				isMe = false;
			}
		}
		if (isMe) {
			return des;
		}
		else if (Point.dist(cur, distractor) <= dist) {
			boolean found_valid_move = false;
			while(!found_valid_move)
			{
				Double bearing = random.nextDouble()*2*Math.PI;
				des = new Point(distractor.x + dist*Math.cos(bearing), distractor.y + dist*Math.sin(bearing));
				found_valid_move = Point.within_bounds(des);
			}
		}
		else {
			double len = Point.dist(cur, distractor);
			double xLen = Math.abs(cur.x - distractor.x) * dist / len;
			double x = cur.x >= distractor.x ? distractor.x + xLen : distractor.x - xLen;
			double yLen = Math.abs(cur.y - distractor.y) * dist / len;
			double y = cur.y >= distractor.y ? distractor.y + yLen : distractor.y - yLen;
			des = new Point(x, y);
		}
		return des;
	}


}
