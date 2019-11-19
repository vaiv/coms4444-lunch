package lunch.g5;

import java.util.ArrayList;
import javafx.util.Pair;

import lunch.sim.Animal;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class GreedyEater {
	// Configs for calculating future matrices
    private double monkeyDangerDistance = 5.0;// How far way from the monkeys we still consider them in calculation
    private double geeseDangerDistance = 6.0; // How far way from the geese we still consider them in calculation
    private int bufferLookahead = 0;          // When calculating a matrix, how much aditional steps to consider
	private MatrixPredictor matrixPredictor;

	private boolean moving = false;
    private int towardX;
    private int towardY;
    private int waitStep = 0;
	private FoodType searching;

    public GreedyEater() {
		super();
		matrixPredictor = new MatrixPredictor(monkeyDangerDistance, geeseDangerDistance, bufferLookahead);
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps, ArrayList<Animal> previousAnimals, int turn) {
        if (turn <= 1) {
            // Go to an integer position in the first turn
            Point p = new Point(Math.floor(ps.get_location().x), Math.floor(ps.get_location().y));
            turn++;
            previousAnimals = animals;
            return Command.createMoveCommand(p);
        }

        // move to a place which has no animal within this range
        int clean_range = 10;

        int x = (int) ps.get_location().x;
        int y = (int) ps.get_location().y;
        Command command = null;

        // not moving and not holding or searching anything, find the safe position
        if (!moving && !ps.is_player_searching() && ps.get_held_item_type() == null) {
            findSafePosition(animals, ps, clean_range, previousAnimals);
        }
        // danger
        // [surrounded by monkey] or [has close goose while holding or searching sandwich]
        if (Utilities.monkey_surround(animals, ps.get_location()) ||
                (Utilities.goose_close(animals, ps.get_location()) &&
                        (ps.get_held_item_type() == FoodType.SANDWICH ||
                                (ps.is_player_searching() && searching == FoodType.SANDWICH)))) {
            // abort if searching
            if (ps.is_player_searching() && ps.get_held_item_type() == null) {
                turn++;
                previousAnimals = animals;
                return new Command(CommandType.ABORT);
            }
            // put back if holding
            if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
                turn++;
                previousAnimals = animals;
                return new Command(CommandType.KEEP_BACK);
            }
        }
        //safe
        if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
            command = new Command(CommandType.EAT);
        } else if (waitStep > 0) {
            waitStep--;
            command = new Command(CommandType.WAIT);
        }
        // in the moving process
        else if (moving) {
            // arrived designate destination, start taking out
            if (towardX == x && towardY == y) {
                moving = false;
                for (FoodType food_type : FoodType.values()) {
                    if (ps.check_availability_item(food_type)) {
                        command = new Command(CommandType.TAKE_OUT, food_type);
                    }
                }
            }
            // haven't arrive, continue moving
            else if (towardX < x) {
                Point nextMove = new Point(x - 1, y);
                command = Command.createMoveCommand(nextMove);
            } else if (towardX > x) {
                Point nextMove = new Point(x + 1, y);
                command = Command.createMoveCommand(nextMove);
            } else if (towardY < y) {
                Point nextMove = new Point(x, y - 1);
                command = Command.createMoveCommand(nextMove);
            } else if (towardY > y) {
                Point nextMove = new Point(x, y + 1);
                command = Command.createMoveCommand(nextMove);
            }
        }

        // Increase the turn counter
        turn++;
        // Store animals for the next turn
        previousAnimals = animals;
        return command;
    }
    
    private void findSafePosition(ArrayList<Animal> animals, PlayerState ps, int range, ArrayList<Animal> previousAnimals) {
        int step = 0;
        int nTimesteps = 10;
        int x = (int) ps.get_location().x;
        int y = (int) ps.get_location().y;
        while (step <= 100) {
            nTimesteps = step + 10;
            Pair<ArrayList<Matrix>, ArrayList<Matrix>> matrices = matrixPredictor.predict(previousAnimals, animals, ps, nTimesteps);
            ArrayList<Matrix> monkey_matrices = matrices.getKey();
            Matrix m = monkey_matrices.get(nTimesteps - 1);
            for (int i = 0; i <= step; i++) {
                for (int j = 0; j <= step - i; j++) {
                    int[][] directions = new int[][]{{x + i, y + j}, {x - i, y + j}, {x + i, y - j}, {x - i, y + j}};
                    for (int[] dir : directions) {
                        if (isClean(range, dir[0], dir[1], m)) {
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
    }
    
    private boolean isClean(int range, int x, int y, Matrix m) {
        for (int i = Math.max(x - range, -50); i < x + range && i <= 50; i++) {
            for (int j = Math.max(y - range, -50); j < y + range && j <= 50; j++) {
                if (m.get(i, j) != 0)
                    return false;
            }
        }
        return true;
    }
}
