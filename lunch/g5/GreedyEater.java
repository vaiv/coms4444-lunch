package lunch.g5;

import java.util.ArrayList;
import java.util.PriorityQueue;

import javafx.util.Pair;

import lunch.sim.Animal;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
import lunch.sim.Point;
import java.util.Comparator;

public class GreedyEater {
	// Configs for calculating future matrices
	private double monkeyDangerDistance = 5.0;// How far way from the monkeys we still consider them in calculation
	private double geeseDangerDistance = 6.0; // How far way from the geese we still consider them in calculation
	private int bufferLookahead = 0; // When calculating a matrix, how much additional steps to consider
	private MatrixPredictor matrixPredictor;

	private int clean_range = 10; // move to a place which has few animal within this range
	private int max_monkey = 0; // max animal number when finding a safe place
	private int max_goose = 0;
	private Point corner;

	private boolean moving; // is in the moving process
	private int towardX; // point to move to
	private int towardY;
	private int waitStep;
	private FoodType searching; // which food is searching
	private FoodType[] eatingOrder;
	private int coolingTime; // cold time after in danger
	private boolean movingToCorner;
	
	private int turn;

	public GreedyEater() {
		super();
		// choose a corner randomly
		double x, y;
		do {
			x = Math.random() > 0.5 ? 50 : -50;
			y = Math.random() > 0.5 ? 50 : -50;
		} while(x == 50 && y == 50);
		corner = new Point(x, y);
		moving = false;
		this.matrixPredictor = new MatrixPredictor(monkeyDangerDistance, geeseDangerDistance, bufferLookahead);
		this.eatingOrder = new FoodType[] { FoodType.COOKIE, FoodType.EGG, FoodType.FRUIT1, FoodType.FRUIT2,
				FoodType.SANDWICH1, FoodType.SANDWICH2 };
		this.coolingTime = 0;
		this.turn = 0;
		this.movingToCorner = true;
	}

	// eating exactly at corner with a cold time
	public Command getCommandCornerEating(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps,
			ArrayList<Animal> previousAnimals, int totalTurn) {
		turn++;
		// move to the corner
		double x = ps.get_location().x;
		double y = ps.get_location().y;
		if(corner.x != x || corner.y != y) {
			movingToCorner = true;
	        corner = findNearestCorner (ps);
	        double dist = Point.dist(ps.get_location(), corner);
			if(dist <= 1.0) {
				movingToCorner = false;
				return Command.createMoveCommand(corner);
			}
			double cos = (corner.x - x)/dist;
			double sin = (corner.y - y)/dist;
			return Command.createMoveCommand(new Point(cos+x, sin+y));
		}
		if(coolingTime > 0) {
			coolingTime -= totalTurn - turn + 2;
			coolingTime = Math.max(coolingTime, 0);
			return new Command();
		}
		// danger
		// [surrounded by monkey] or [has close goose while holding or searching
		// sandwich]
		if (Utilities.dangerous(ps, animals, searching)) {
			if(Utilities.monkey_surround(animals, ps.get_location()))
				coolingTime = 35;
			// abort if searching
			if (ps.is_player_searching() && ps.get_held_item_type() == null) {
//				System.out.println("in danger, stop taking out");
				return new Command(CommandType.ABORT);
			}
			// put back if holding
			if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
//				System.out.println("in danger, put back");
				return new Command(CommandType.KEEP_BACK);
			}
		}
		// safe
		// holding something, eat it
		if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
//			System.out.println("safe, eating");
			return new Command(CommandType.EAT);
		}
		// not holding anything, take out
		if (!ps.is_player_searching() && ps.get_held_item_type() == null) {
			for (FoodType food_type : eatingOrder) {
				if (ps.check_availability_item(food_type)) {
					searching = food_type;
					coolingTime = 0;
					return new Command(CommandType.TAKE_OUT, food_type);
				}
			}
		}
		return new Command();
	}
/*
	public Command getCommandUseMatrix(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps,
			ArrayList<Animal> previousAnimals, int totalTurn) {
		turn++;
		// move to the corner
		if (turn <= 70) {
			System.out.println("moving to corner");
			Point next_move = moveToCorner(ps);
			return Command.createMoveCommand(next_move);
		}
		int x = (int) ps.get_location().x;
		int y = (int) ps.get_location().y;
		if (turn == 71) {
			// Go to an integer position
			Point p = new Point(x, y);
			return Command.createMoveCommand(p);
		}

		// not moving and not holding or searching anything, find the safe position
		if (!moving && !ps.is_player_searching() && ps.get_held_item_type() == null) {
			System.out.println("finding position to move");
			findSafePosition(animals, ps, clean_range, previousAnimals);
		}

		if (waitStep > 0) {
			waitStep--;
			return new Command(CommandType.WAIT);
		}
		// in the moving process
		if (moving) {
			// arrived designate destination, start taking out
			if (towardX == x && towardY == y) {
				System.out.println("arrived, take out");
				moving = false;
				for (FoodType food_type : eatingOrder) {
					if (ps.check_availability_item(food_type)) {
						searching = food_type;
						return new Command(CommandType.TAKE_OUT, food_type);
					}
				}
			}
			// haven't arrive, continue moving
			System.out.println("moving");
			Point nextMove;
			if (towardX < x) {
				nextMove = new Point(x - 1, y);
			} else if (towardX > x) {
				nextMove = new Point(x + 1, y);
			} else if (towardY < y) {
				nextMove = new Point(x, y - 1);
			} else {
				nextMove = new Point(x, y + 1);
			}
			return Command.createMoveCommand(nextMove);
		}
		// danger
		// [surrounded by monkey] or [has close goose while holding or searching
		// sandwich]
		if (Utilities.monkey_surround(animals, ps.get_location()) || (Utilities.goose_close(animals, ps.get_location())
				&& (ps.get_held_item_type() == FoodType.SANDWICH || (ps.is_player_searching()
						&& (searching == FoodType.SANDWICH1 || searching == FoodType.SANDWICH2))))) {
			// abort if searching
			if (ps.is_player_searching() && ps.get_held_item_type() == null) {
				System.out.println("in danger, stop taking out");
				return new Command(CommandType.ABORT);
			}
			// put back if holding
			if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
				System.out.println("in danger, put back");
				return new Command(CommandType.KEEP_BACK);
			}
		}
		// safe
		if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
			System.out.println("safe, eating");
			return new Command(CommandType.EAT);
		}

		return new Command();
	}

	private Point moveToCorner(PlayerState ps) {
		return new Point(ps.get_location().x + cornerX * Math.cos(Math.PI / 4),
				ps.get_location().y + cornerY * Math.cos(Math.PI / 4));
	}
*/
	
	private static Point findNearestCorner (PlayerState ps) {
		PriorityQueue<Point> pq = new PriorityQueue<Point>(4, new Comparator<Point>() {
	    		@Override
	        public int compare(Point p1, Point p2) {                         
	            double dist1 = Point.dist(ps.get_location(), p1);
	            double dist2 = Point.dist(ps.get_location(), p2);
	            if(dist1 > dist2) return 1;
	            if(dist1 < dist2) return -1;
	            return 0;
	        }      
	    }); 
	    pq.add(new Point(-50, 50));
	    pq.add(new Point(-50, -50));
	    pq.add(new Point(50, -50));
	    return pq.poll();
	}
	
	private boolean isSafePoint(Matrix m, Matrix g, int x, int y, int range) {
		for (int i = Math.max(x - range, -50); i < x + range && i <= 50; i++) {
			for (int j = Math.max(y - range, -50); j < y + range && j <= 50; j++) {
				if (m.get(i, j) > max_monkey || g.get(i, j) > max_goose)
					return false;
			}
		}
		return true;
	}

	private void findSafePosition(ArrayList<Animal> animals, PlayerState ps, int range,
			ArrayList<Animal> previousAnimals) {
		// max_monkey = 0;
		// max_goose = 0;
		int nTimesteps = 10;
		int x = (int) ps.get_location().x;
		int y = (int) ps.get_location().y;
		int step = 0;
		while (step <= 100) {
			nTimesteps = step + 10;
			Pair<ArrayList<Matrix>, ArrayList<Matrix>> matrices = matrixPredictor.predict(previousAnimals, animals, ps,
					nTimesteps);
			ArrayList<Matrix> monkey_matrices = matrices.getKey();
			ArrayList<Matrix> goose_matrices = matrices.getValue();
			Matrix m = monkey_matrices.get(nTimesteps - 1);
			Matrix g = goose_matrices.get(nTimesteps - 1);
			for (int i = 0; i <= step; i++) {
				for (int j = 0; j <= step - i; j++) {
					int[][] directions = new int[][] { { x + i, y + j }, { x - i, y + j }, { x + i, y - j },
							{ x - i, y - j } };
					for (int[] dir : directions) {
						if (Math.abs(dir[0]) > 50 || Math.abs(dir[1]) > 50) {
							continue;
						}
						if (isSafePoint(m, g, dir[0], dir[1], range)) {
							towardX = dir[0];
							towardY = dir[1];
							waitStep = step - i - j;
							moving = true;
							return;
						}
					}
				}
			}
			step++;
		}
		moving = false;
	}

	private int minAnimal(int range, int x, int y, Matrix m) {
		int min = Integer.MAX_VALUE;
		for (int i = Math.max(x - range, -50); i < x + range && i <= 50; i++) {
			for (int j = Math.max(y - range, -50); j < y + range && j <= 50; j++) {
				if (m.get(i, j) < min)
					min = m.get(i, j);
			}
		}
		return min;
	}
}
