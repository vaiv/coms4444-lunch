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

    public boolean validateStrategy(ArrayList<Animal> animals, Point src) {
        if (this.status == null)
            return false;
        if (this.status.getNumMoves() == 0) {
            this.status = null;
            return false;
        }

        Integer topMode = this.status.strategy.get(0).mode;
        if (topMode == 0 || topMode == 1 || topMode == 2)
            return true;

        if (topMode == 4) {
            Point dst = this.status.strategy.get(0).destination;
            Point dp = PointUtilities.normalizedSubtract(src, dst, 1.0);

            src = PointUtilities.add(src, dp);
        }

        Integer monkeyCount = 0;
        for (Animal animal : animals) {
            if (animal.which_animal() != AnimalType.MONKEY)
                continue;

            Point p = animal.get_location();
            if (Math.hypot(src.x - p.x, src.y - p.y) <= 6.0)
                monkeyCount++;
        }

        if (monkeyCount > 2) {
            while (true) {
                topMode = this.status.strategy.get(0).mode;

                if (topMode == 0 || topMode == 1 || topMode == 2)
                    return true;

                return true;
                // Log.log("Monkey count = " + monkeyCount + " " + src + " => " + this.status);
                // Log.log("Removing : " + this.status.toString());
                // this.status.strategy.remove(0);
                // Log.log("Removed : " + this.status.toString());
            }
        }
        return true;
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
                if (x + i < 0 || x + i > 40)
                    continue;

                int jlim = t - Math.abs(i);
                for (int j = -jlim; j <= jlim; j++) {
                    if (y + j < 0 || y + j > 40)
                        continue;

                    Point startingPosition = new Point(x + i, y + j);
                    Integer eatSteps = DistractionUtilities.getEatBuffer(monkeyPositions, startingPosition);
                    if (eatSteps < 0)
                        continue;

                    startingPositions.add(new Temporary(startingPosition, t, eatSteps));
                }
            }
        }

        Collections.sort(startingPositions);
        // Log.log(startingPositions.toString() + ". Considering " +
        // startingPositions.size());

        Integer bestWalkFound = 0;
        DistractionStatus bestStrategy = null;
        Integer maxWalkPossible = Math.max(Math.max(x, 40 - x), Math.max(y, 40 - y));

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
                Log.log("(" + this.turn + ") " + bestStrategy.toString());
            // // if (this.turn == 6) {
            //     Log.log("Initial loc = " + animalLocs.toString());
            //     for (int t = 0; t < posEstimates.length; t++)
            //         Log.log(String.format("t=%d. %s", t, posEstimates[t].toString()));
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
    public void resetDistractionStrategy() {
        this.status = null;
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
        if ((playerPos.x < -1 || playerPos.y < -1) && ps.get_held_item_type() != null)
            return new Command(CommandType.KEEP_BACK);
        if (playerPos.x < -1)
            return Command.createMoveCommand(new Point(playerPos.x + 1, playerPos.y));
        if (playerPos.y < -1)
            return Command.createMoveCommand(new Point(playerPos.x, playerPos.y + 1));

        AnimalPosition animalLocs = AnimalPosition.parse(curAnimals, prvAnimals, true);
        // if (this.turn == 6)
        //     Log.log(animalLocs.toString());

        if (!this.validateStrategy(curAnimals, playerPos))
            this.status = this.generateDistractionStrategy(animalLocs, ps, eatFood);

        // if (this.turn == 7)
        //     if (this.status != null)
        //         Log.log("(" + this.turn + ") " + this.status.toString());
        //     else
        //         Log.log("Null strategy");
        return this.executeStrategy(ps);
    }
}
