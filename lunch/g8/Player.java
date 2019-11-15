package lunch.g8;

import java.util.ArrayList;
import java.util.Random;
import lunch.sim.Animal;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class Player implements lunch.sim.Player {

    private Integer turn;
    private Random random;

    /**
     *
     * @param members family members
     * @param id id of the member represented by this player
     * @param f number of family members
     * @param animals list of animals in the simulation
     * @param m number of monkeys
     * @param g number of geese
     * @param t number of turns
     * @param s seed for random number generation purposes
     */
    @Override
    public void init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        random = new Random(s);
        turn = 0;
    }

    @Override
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        Double minDist = Double.MAX_VALUE;

        for (Integer i = 0; i < animals.size(); i++) {
            minDist = Math.min(minDist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }

        if (turn < 100) {
            boolean validMoveFound = false;
            Point nextMove = new Point(-1, -1);
            while (!validMoveFound) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                validMoveFound = Point.within_bounds(nextMove);
            }
            // System.out.println("move command issued");
            turn++;
            return Command.createMoveCommand(nextMove);
        }

        // abort taking out if animal is too close
        if (minDist < 3.0 && ps.is_player_searching() && ps.get_held_item_type() == null) {
            // System.out.println("abort command issued");
            // System.out.println(min_dist.toString());
            return new Command(CommandType.ABORT);
        } // keep food item back if animal is too close
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null && minDist < 2.0) {
            return new Command(CommandType.KEEP_BACK);
        } // move away from animal 
        else if (minDist < 3.0) {
            boolean validMoveFound = false;
            Point nextMove = new Point(-1, -1);
            while (!validMoveFound) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                validMoveFound = Point.within_bounds(nextMove);
            }
            return Command.createMoveCommand(nextMove);

        } // if no animal is near then take out food
        else if (!ps.is_player_searching() && minDist >= 5 && ps.get_held_item_type() == null) {
            for (FoodType food_type : FoodType.values()) {
                if (ps.check_availability_item(food_type)) {
                    Command c = new Command(CommandType.TAKE_OUT, food_type);
                    return c;
                }
            }
        } // if no animal in vicinity then take a bite
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
            return new Command(CommandType.EAT);
        }

        // System.out.println("player is searching");
        return new Command();
    }

}
