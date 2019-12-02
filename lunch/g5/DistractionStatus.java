package lunch.g5;

import java.util.ArrayList;

import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.FoodType;
import lunch.sim.Log;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class DistractionStatus {
    class StrategyType {
        public Integer mode; // 0 for move, 1 for take food out
        public Integer timestep;
        public Point destination;

        public StrategyType(Integer mode, Integer timestep) {
            this.mode = mode;
            this.timestep = timestep;
        }

        public StrategyType(Integer mode, Integer timestep, Point dest) {
            this.mode = mode;
            this.timestep = timestep;
            this.destination = dest;
        }

        public String toString() {
            switch (mode) {
            case 0:
            case 4:
                return String.format("MOVE[%d, (%.0f, %.0f)]", timestep, destination.x, destination.y);
            case 1:
                return String.format("FOOD[%d]", timestep);
            case 2:
                return String.format("KEEP[%d]", timestep);
            case 3:
                return String.format("EAT[%d]", timestep);
            default:
                Log.log("Cannot decipher wtf this is");
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
        this.addMove(starting, t1, 0); // Mode 0
        this.addFoodOut(); // Mode 1
        this.eatFood(eatTime); // Mode 3
        this.addMove(ending, t2, 4); // Mode 4
        this.keepFoodIn(); // Mode 2
    }

    public void addMove(Point dest, Integer steps, Integer mode) {
        if (steps <= 0)
            return;
        strategy.add(new StrategyType(mode, steps, dest));
    }

    public void addFoodOut() {
        strategy.add(new StrategyType(1, 10));
    }

    public void eatFood(Integer timestep) {
        if (timestep > 0)
            strategy.add(new StrategyType(3, timestep));
    }

    public void keepFoodIn() {
        strategy.add(new StrategyType(2, 10));
    }

    public Integer getNumMoves() {
        Integer numMoves = 0;
        for (StrategyType move : strategy)
            numMoves += move.timestep;
        return numMoves;
    }

    public Command executeStrategy(PlayerState ps) {
        StrategyType move = strategy.get(0);

        if (move.mode == 0 || move.mode == 4) {
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

        } else if (move.mode == 1) {
            if (move.timestep-- == 10) {
                FoodType[] foodlist = { FoodType.COOKIE, FoodType.FRUIT1, FoodType.FRUIT2, FoodType.EGG };

                for (FoodType food : foodlist) {
                    if (ps.check_availability_item(food))
                        return Command.createRetrieveCommand(food);
                }
            }

            if (move.timestep <= 0)
                this.strategy.remove(0);

            return new Command();
        } else if (move.mode == 2) {
            if (--move.timestep <= 0)
                this.strategy.remove(0);
            return new Command(CommandType.KEEP_BACK);
        } else if (move.mode == 3) {
            if (--move.timestep <= 0)
                this.strategy.remove(0);
            return new Command(CommandType.EAT);
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
