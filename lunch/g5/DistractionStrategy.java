package lunch.g5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javafx.util.Pair;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.Log;
import lunch.sim.PlayerState;
import lunch.sim.Point;

public class DistractionStrategy {
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;

    public static Integer minXd = 0, maxXd = 35, minYd = 0, maxYd = 35;

    private DistractionStatus status;

    public DistractionStrategy() {
        this.turn = 0;
        this.status = null;
    }

    public void init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g,
            double t, Integer s) {
        this.id = id;
        this.seed = s;
        this.status = null;
        this.random = new Random(this.seed);
    }

    public Command executeStrategy(PlayerState ps) {
        return (this.status == null) ? new Command() : this.status.executeStrategy(ps);
    }

    public boolean validateStrategy(AnimalPosition animalLocs, PlayerState ps) {
        if (this.status == null || this.status.getNumMoves() == 0) {
            this.status = null;
            return false;
        }

        // Improved strategy validator to avoid unforseen events
        DistractionStatus.StrategyType curMove = this.status.strategy.get(0);
        switch (curMove.mode) {
        case MOVE_NOFOOD:
            return true;
        case TAKE_FOOD:
            // Check if monkeys are around when food is taken out
            Integer eatSteps = DistractionUtilities.getEatBuffer(
                    DistractionUtilities.simulateTimestep(animalLocs, curMove.timestep), ps.get_location());
            if (eatSteps < 0) {
                // Log.log("validateStrategy : Aborting food removal");
                this.status.strategy.clear();
                this.status.addAbort();
            }
            return true;
        case EAT_FOOD:
            // Check if monkeys are around for 1 to x steps
            Integer available = DistractionUtilities.getEatBuffer(animalLocs, ps.get_location()) + 1;
            if (available < curMove.timestep && available < 2) {
                if (available < 0) {
                    // Log.log("validateStrategy : clearing Movement");
                    this.status.strategy.clear();
                    this.status.keepFoodIn();
                } else {
                    // Log.log("validateStrategy : Decreasing eating time " + curMove.timestep + "
                    // -> " + available);
                    curMove.timestep = available;
                }
            }
            // else if (available > curMove.timestep) {
            // TODO: Recalculate movement with food
            // curMove.timestep = available;
            // if (this.status.strategy.size() > 1
            // && this.status.strategy.get(1).mode ==
            // DistractionStatus.StrategyMode.MOVE_FOOD)
            // this.status.strategy.remove(1);
            // }
            return true;
        case MOVE_FOOD:
            // Check if the next step is still clear
            Point curLoc = ps.get_location();
            Point newLoc = PointUtilities.add(curLoc,
                    PointUtilities.normalizedSubtract(curLoc, curMove.destination, 1.));

            // Check if there are > 3 monkeys in the next time step

            Integer buffer = DistractionUtilities.getEatBuffer(animalLocs, newLoc) + 1;
            if (buffer < 0) {
                // Log.log("validateStrategy : Removing food movement " + curLoc.toString() +
                // newLoc.toString() + curMove.destination.toString() + " " + buffer);
                this.status.strategy.remove(0);
            }
            return true;
        case KEEP_FOOD:
            return true;
        default:
            return true;
        }
    }

    public DistractionStatus generateDistractionStrategy(AnimalPosition animalLocs, PlayerState ps, Boolean eatFood) {

        class Temporary implements Comparable<Temporary> {
            public Point startingPos;
            public Integer t;
            public Integer buffer;

            public Temporary(Point s, Integer a, Integer b) {
                this.startingPos = s;
                this.t = a;
                this.buffer = b;
            }

            public String toString() {
                return String.format("(%.2f, %.2f)=>[%d, %d]", startingPos.x, startingPos.y, t, buffer);
            }

            @Override
            public int compareTo(Temporary o) {
                return (o.buffer == buffer) ? t - o.t : o.buffer - buffer;
            }
        }

        // Get current player position in integers
        Point playerPos = ps.get_location();
        Integer x = (int) (Math.round(playerPos.x));
        Integer y = (int) (Math.round(playerPos.y));

        // Get positions and directions of monkeys
        AnimalPosition posEstimate = DistractionUtilities.simulateTimestep(animalLocs, 10);
        AnimalPosition[] posEstimates = DistractionUtilities.simulateTimesteps(posEstimate, 10);

        ArrayList<Temporary> startingPositions = new ArrayList<Temporary>();

        for (int t = 0; t < posEstimates.length; t++) {
            AnimalPosition monkeyPositions = posEstimates[t];

            for (int i = -t; i <= t; i++) {
                if (x + i < minXd || x + i > maxXd)
                    continue;

                int jlim = t - Math.abs(i);
                for (int j = -jlim; j <= jlim; j++) {
                    if (y + j < minYd || y + j > maxYd)
                        continue;

                    Point startingPosition = new Point(x + i, y + j);
                    Integer eatSteps = DistractionUtilities.getEatBuffer(monkeyPositions, startingPosition);
                    if (eatSteps < 0)
                        continue;

                    startingPositions.add(new Temporary(startingPosition, t, eatFood ? eatSteps : 0));
                }
            }
        }

        Collections.sort(startingPositions);
        // Log.log(startingPositions.toString() + ". Considering " +
        // startingPositions.size());

        Integer bestWalkFound = 0;
        DistractionStatus bestStrategy = null;
        Integer maxWalkPossible = Math.max(Math.max(x, maxXd - x), Math.max(y, maxYd - y));

        // Only consider a few starting positions to avoid timeout
        int consideredCount = 0;
        int maxConsideredCount = 200;

        for (Temporary positionPair : startingPositions) {
            consideredCount++;
            if (consideredCount >= maxConsideredCount)
                break;

            Point startingPosition = positionPair.startingPos;
            Integer t = positionPair.t;
            Integer eatSteps = positionPair.buffer;

            // Simulate a walk starting from startingPosition in UDLR order
            Pair<Integer, Point> maxWalk = DistractionUtilities.simulateWalking(posEstimates[t], startingPosition,
                    eatSteps);

            Integer numWalkSteps = maxWalk.getKey();
            Point walkDestination = maxWalk.getValue();

            // Log.log(startingPosition.toString() + " can walk to " +
            // walkDestination.toString() + " in " + numWalkSteps
            // + " steps and " + eatSteps + ". Has "
            // + DistractionUtilities.getEatBuffer(posEstimates[t], startingPosition) + "
            // monkeys.");
            if (numWalkSteps + eatSteps > bestWalkFound) {
                bestWalkFound = numWalkSteps;
                bestStrategy = new DistractionStatus(startingPosition, t, eatSteps, walkDestination, numWalkSteps);
            }

            if (bestWalkFound >= maxWalkPossible - t)
                break;
        }

        if (bestStrategy != null) {
            // // if (this.turn <= 6)
            // Log.log("(" + this.turn + ") " + bestStrategy.toString());
            // // if (this.turn == 6) {
            // Log.log("Initial loc = " + animalLocs.toString());
            // for (int t = 0; t < posEstimates.length; t++)
            // Log.log(String.format("t=%d. %s", t, posEstimates[t].toString()));
            // // }
            this.turn++;
        } else {
            // Log.log("Null strategy");
        }
        return bestStrategy;
    }

    /**
     * Reset pre-calculated distraction strategy
     */
    public void resetDistractionStrategy(PlayerState ps) {
        this.status = null;
        if (ps.get_held_item_type() != null) {
            this.status = new DistractionStatus();
            this.status.keepFoodIn();
        } else if (ps.is_player_searching()){
            this.status = new DistractionStatus();
            this.status.addAbort();
        }
    }

    /**
     * Redirect food to player
     * 
     * @param playerLoc
     * @param ps
     * @return number of steps required to redirect monkeys to the player
     */
    public Integer redirectToPlayer(Point playerLoc, ArrayList<Animal> animals, ArrayList<Animal> prevanimals,
            PlayerState ps) {

        return 0;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, ArrayList<Animal> prevanimals,
            PlayerState ps) {
        return this.getCommand(members, animals, prevanimals, ps, true);
    }

    /**
     * 
     * @param members
     * @param curAnimals
     * @param prvAnimals
     * @param ps
     * @param eatFood    Boolean on whether to eat or not
     * @return
     */
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> curAnimals, ArrayList<Animal> prvAnimals,
            PlayerState ps, Boolean eatFood) {

        // Reset to bottom right integer co-ordinate
        Point playerPos = ps.get_location();

        // If Player has non-integer co-ordinates, move to integer location
        final Double intX = (double) Math.round(playerPos.x);
        final Double intY = (double) Math.round(playerPos.y);
        if (playerPos.x - intX > 1e-5 || playerPos.y - intY > 1e-5)
            return Command.createMoveCommand(new Point(intX, intY));

        // If Player is not on lower-right, move to lower-right
        if ((playerPos.x < minXd || playerPos.y < minYd || playerPos.x > maxXd || playerPos.y > maxYd)
                && ps.get_held_item_type() != null)
            return new Command(CommandType.KEEP_BACK);
        if (playerPos.x < minXd)
            return Command.createMoveCommand(new Point(playerPos.x + 1, playerPos.y));
        if (playerPos.x > maxXd)
            return Command.createMoveCommand(new Point(playerPos.x - 1, playerPos.y));
        if (playerPos.y < minYd)
            return Command.createMoveCommand(new Point(playerPos.x, playerPos.y + 1));
        if (playerPos.y > maxYd)
            return Command.createMoveCommand(new Point(playerPos.x, playerPos.y - 1));

        AnimalPosition animalLocs = AnimalPosition.parse(curAnimals, prvAnimals, true);
        // if (this.turn == 6)
        // Log.log(animalLocs.toString());

        if (!this.validateStrategy(animalLocs, ps))
            this.status = this.generateDistractionStrategy(animalLocs, ps, eatFood);

        // if (this.turn == 7)
        // if (this.status != null)
        // Log.log("(" + this.turn + ") " + this.status.toString());
        // else
        // Log.log("Null strategy");
        return this.executeStrategy(ps);
    }
}
