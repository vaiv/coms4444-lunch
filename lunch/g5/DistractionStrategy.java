package lunch.g5;

import java.util.ArrayList;
import java.util.Random;

import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.Log;
import lunch.sim.PlayerState;
import lunch.sim.Point;

enum DistractionStatus {
    DISTRACTION_INITIALIZING, // Initial status. Headed to start point
    DISTRACTION_WALKING_LEFT, // Walking left
    DISTRACTION_WALKING_RIGHT, // Walking right
    DISTRACTION_AT_LEFT, // At left location
    DISTRACTION_AT_RIGHT, // At right location
    DISTRACTION_COMPLETE // Completed distraction protocol
}

public class DistractionStrategy {
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;

    private DistractionStatus status;

    public DistractionStrategy() {
        turn = 0;
        this.status = DistractionStatus.DISTRACTION_INITIALIZING;
    }

    public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g,
            double t, Integer s) {
        this.id = id;
        this.seed = s;

        this.random = new Random(this.seed);
        return "guardians";
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {

        FoodType[] distractionFoods = { FoodType.FRUIT1, FoodType.FRUIT2, FoodType.EGG, FoodType.COOKIE };
        Family me = members.get(id);
        Point myLoc = me.get_location();
        Point left_loc = new Point(-30, -30);
        Point right_loc = new Point(30, -30);

        switch (this.status) {
        case DISTRACTION_INITIALIZING:
            if (myLoc.equals(left_loc))
                this.status = DistractionStatus.DISTRACTION_AT_LEFT;
            else
                return new Command(CommandType.MOVE_TO, this.getNextPoint(myLoc, left_loc, 1));
            break;

        case DISTRACTION_AT_LEFT:
        case DISTRACTION_AT_RIGHT:
            if (ps.is_player_searching()) {
                if (ps.time_to_finish_search() == 1) {
                    // Check for monkeys
                    if (getNumMonkeys(myLoc, animals) > 2)
                        // Abort if necessary
                        return new Command(CommandType.ABORT);
                }
                return new Command();
            } else if (me.get_held_item_type() == null) {
                // Holding nothing. Take food out
                for (FoodType item : distractionFoods) {
                    if (ps.check_availability_item(item))
                        return new Command(CommandType.TAKE_OUT, item);
                }

                this.status = DistractionStatus.DISTRACTION_COMPLETE;

            } else {
                if (this.status == DistractionStatus.DISTRACTION_AT_LEFT)
                    this.status = DistractionStatus.DISTRACTION_WALKING_RIGHT;
                else
                    this.status = DistractionStatus.DISTRACTION_WALKING_LEFT;
            }
            break;

        case DISTRACTION_WALKING_RIGHT:
            if (myLoc.equals(right_loc) || getNumMonkeys(myLoc, animals) > 2) {
                this.status = DistractionStatus.DISTRACTION_AT_RIGHT;
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.MOVE_TO, this.getNextPoint(myLoc, right_loc, 1));
            }

        case DISTRACTION_WALKING_LEFT:
            if (myLoc.equals(left_loc) || getNumMonkeys(myLoc, animals) > 2) {
                this.status = DistractionStatus.DISTRACTION_AT_LEFT;
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.MOVE_TO, this.getNextPoint(myLoc, left_loc, 1));
            }

        case DISTRACTION_COMPLETE:
            return new Command();
        }

        // Get my location
        return new Command();
    }

    private Integer getNumMonkeys(Point myLoc, ArrayList<Animal> animals) {
        Integer monkeyCount = 0;
        for (Animal animal : animals) {
            if (animal.which_animal() == AnimalType.GOOSE)
                continue;

            Point monkeyLoc = animal.get_location();
            if (Math.hypot(monkeyLoc.x - myLoc.x, monkeyLoc.y - myLoc.y) < 7)
                monkeyCount++;
        }
        return monkeyCount;
    }

    private Point getNextPoint(Point src, Point dst, Integer units) {
        if (Math.hypot(src.x - dst.x, src.y - dst.y) < 1e-5)
            return null;

        Double dx = dst.x - src.x;
        Double dy = dst.y - src.y;
        Double magnitude = Math.hypot(dx, dy);

        if (magnitude < units)
            return dst;

        return new Point(src.x + dx * units / magnitude, src.y + dy * units / magnitude);
    }
}
