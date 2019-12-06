package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.FoodType;
import lunch.sim.Log;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class DistractionStatus {
    enum StrategyMode {
        MOVE_NOFOOD, TAKE_FOOD, EAT_FOOD, MOVE_FOOD, KEEP_FOOD, ABORT
    }

    class StrategyType {
        public StrategyMode mode; // 0 for move, 1 for take food out
        public Integer timestep;
        public Point destination;

        public StrategyType(StrategyMode mode, Integer timestep) {
            this.mode = mode;
            this.timestep = timestep;
        }

        public StrategyType(StrategyMode mode, Integer timestep, Point dest) {
            this.mode = mode;
            this.timestep = timestep;
            this.destination = dest;
        }

        public String toString() {
            switch (mode) {
            case MOVE_NOFOOD:
            case MOVE_FOOD:
                return String.format("MOVE[%d, (%.0f, %.0f)]", timestep, destination.x, destination.y);
            case TAKE_FOOD:
                return String.format("FOOD[%d]", timestep);
            case EAT_FOOD:
                return String.format("EAT[%d]", timestep);
            case KEEP_FOOD:
                return String.format("KEEP[%d]", timestep);
            case ABORT:
                return String.format("ABORT");
            default:
                // Log.log("Cannot decipher wtf this is");
            }
            return null;
        }
    }

    public ArrayList<StrategyType> strategy;

    public DistractionStatus() {
        this.strategy = new ArrayList<StrategyType>();
    }

    public DistractionStatus(Point starting, Integer t1, Integer eatTime, Point ending, Integer t2) {
        this.strategy = new ArrayList<StrategyType>();
        this.addMove(starting, t1, StrategyMode.MOVE_NOFOOD);
        this.addFoodOut();
        this.eatFood(eatTime);
        this.addMove(ending, t2, StrategyMode.MOVE_FOOD);
        this.keepFoodIn();
    }

    public void addMove(Point dest, Integer steps, StrategyMode mode) {
        if (steps <= 0)
            return;
        strategy.add(new StrategyType(mode, steps, dest));
    }

    public void addFoodOut() {
        strategy.add(new StrategyType(StrategyMode.TAKE_FOOD, 10));
    }

    public void eatFood(Integer timestep) {
        if (timestep > 0)
            strategy.add(new StrategyType(StrategyMode.EAT_FOOD, timestep));
    }

    public void keepFoodIn() {
        strategy.add(new StrategyType(StrategyMode.KEEP_FOOD, 10));
    }

    public void addAbort() {
        strategy.add(new StrategyType(StrategyMode.ABORT, 1));
    }

    public Integer getNumMoves() {
        Integer numMoves = 0;
        for (StrategyType move : strategy)
            numMoves += move.timestep;
        return numMoves;
    }

    public Command executeStrategy(PlayerState ps) {
        StrategyType move = strategy.get(0);

        if(move.mode == StrategyMode.MOVE_FOOD && ps.get_held_item_type() == null )
            this.strategy.clear();

        if (move.mode == StrategyMode.MOVE_NOFOOD || move.mode == StrategyMode.MOVE_FOOD) {
            Point source = ps.get_location();

            Point newLoc = null;
            if (source.x < move.destination.x)
                newLoc = new Point(source.x + 1, source.y);
            else if (source.x > move.destination.x)
                newLoc = new Point(source.x - 1, source.y);
            else if (source.y < move.destination.y)
                newLoc = new Point(source.x, source.y + 1);
            else if (source.y > move.destination.y)
                newLoc = new Point(source.x, source.y - 1);

            if (--move.timestep <= 0)
                this.strategy.remove(0);

            if (newLoc == null)
                return new Command();
            // Log.log("Moving from " + source.toString() + " to " + newLoc.toString());
            return Command.createMoveCommand(newLoc);

        } else if (move.mode == StrategyMode.TAKE_FOOD) {
            if (move.timestep-- == 10) {
                FoodType[] foodlist = { FoodType.COOKIE, FoodType.EGG, FoodType.FRUIT1, FoodType.FRUIT2 };

                for (FoodType food : foodlist) {
                    if (ps.check_availability_item(food))
                        return Command.createRetrieveCommand(food);
                }
            }

            if (move.timestep <= 0)
                this.strategy.remove(0);

            return new Command();
        } else if (move.mode == StrategyMode.KEEP_FOOD) {
            if (--move.timestep <= 0)
                this.strategy.remove(0);
            return new Command(CommandType.KEEP_BACK);
        } else if (move.mode == StrategyMode.EAT_FOOD) {
            if (--move.timestep <= 0)
                this.strategy.remove(0);

            if (ps.time_to_eat_remaining() < move.timestep) {
                this.strategy.clear();
                this.eatFood(ps.time_to_eat_remaining());
            }
            return new Command(CommandType.EAT);
        } else if (move.mode == StrategyMode.ABORT) {
            this.strategy.remove(0);
            return new Command(CommandType.ABORT);
        }
        return new Command();
    }

    public String toString() {
        String tmp = "";
        int i;
        for (int j = 0; j < this.strategy.size(); j++) {
            i = this.strategy.size() - j - 1;
            tmp = this.strategy.get(i).toString() + " " + tmp;
        }
        return tmp;
    }
}
