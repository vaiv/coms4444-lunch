package lunch.g5;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import javafx.util.Pair;
import lunch.sim.Animal;
import lunch.sim.AnimalType;
import lunch.sim.Command;
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
        return this.status.executeStrategy(ps);

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
        ArrayList<Point> animalLocs = new ArrayList<Point>();
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

                // Log.log("Monkey count = " + monkeyCount + " " + src + " => " + this.status);
                // Log.log("Removing : " + this.status.toString());
                this.status.strategy.remove(0);
                // Log.log("Removed : " + this.status.toString());
            }
        }
        return true;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, ArrayList<Animal> prevanimals,
            PlayerState ps) {

        // if (this.status != null)
        // Log.log(this.status.toString());
        // else
        // Log.log("Null status " + this.validateStrategy());
        if (this.validateStrategy(animals, ps.get_location())) {
            Command command = this.executeStrategy(ps);
            return command;
        }

        Point playerPos = ps.get_location();

        // If Player has non-integer co-ordinates, move to integer location
        if (playerPos.x - Math.round(playerPos.x) > 1e-5 || playerPos.y - Math.round(playerPos.y) > 1e-5) {
            return Command.createMoveCommand(new Point(Math.round(playerPos.x), Math.round(playerPos.y)));
        }

        long time1 = System.nanoTime();

        // Get current player position in integers
        Integer x = (int) (Math.round(playerPos.x));
        Integer y = (int) (Math.round(playerPos.y));

        Integer bestWalkFound = 0;
        DistractionStatus bestStrategy = null;
        Integer maxWalkPossible = Math.max(Math.max(x, 40 - x), Math.max(y, 40 - y));

        // Get positions and directions of monkeys
        ArrayList<ArrayList<Point>> posEstimate = DistractionUtilities.predictNotNull(prevanimals, animals, playerPos,
                20);

        // Log.log("posEstimate size = " + posEstimate.size());
        // for (int t = 0; t < posEstimate.size(); t++) {
        // String tmp = "";
        // for (Point p : posEstimate.get(t))
        // tmp += String.format("(%.1f, %.1f), ", p.x, p.y);
        // Log.log(String.format("T = %d\n[%s]", t, tmp));
        // }

        long time2 = System.nanoTime();

        // Only consider a few starting positions to avoid timeout
        int consideredCount = 0;
        int maxConsideredCount = 50;

        class Temporary {
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
        }

        ArrayList<Temporary> startingPositions = new ArrayList<Temporary>();
        for (int t = 0; t < posEstimate.size() - 10; t++) {
            ArrayList<Point> monkeyPositions = posEstimate.get(t + 10);

            // TODO: Sort by eating time
            for (int i = -t; i <= t; i++) {
                if (x + i < 0 || x + i > 50)
                    continue;

                int jlim = t - Math.abs(i);
                for (int j = -jlim; j <= jlim; j++) {
                    if (y + j < 0 || y + j > 50)
                        continue;

                    Point startingPosition = new Point(x + i, y + j);
                    Integer eatSteps = DistractionUtilities.getThirdMonkeyLocation(monkeyPositions, startingPosition);
                    if (eatSteps < 0)
                        continue;

                    startingPositions.add(new Temporary(startingPosition, t, eatSteps));
                }
            }
        }

        startingPositions.sort((o1, o2) -> o1.buffer - o2.buffer);

        // Log.log("Starting Position Size : " + startingPositions.toString());

        for (Temporary positionPair : startingPositions) {
            consideredCount++;
            if (consideredCount >= maxConsideredCount)
                break;
            Point startingPosition = positionPair.startingPos;
            Integer t = positionPair.t;
            Integer eatSteps = positionPair.buffer;

            ArrayList<Point> monkeyPositions = posEstimate.get(t + 10);

            // Simulate a walk starting from startingPosition in UDLR order
            Pair<Integer, Point> maxWalk = DistractionUtilities.simulateWalk(monkeyPositions, startingPosition);

            Integer numWalkSteps = maxWalk.getKey();
            Point walkDestination = maxWalk.getValue();

            if (numWalkSteps + eatSteps > bestWalkFound) {
                bestWalkFound = numWalkSteps;
                bestStrategy = new DistractionStatus(startingPosition, t, eatSteps, walkDestination, numWalkSteps);
            }

            if (bestWalkFound >= maxWalkPossible - t)
                break;
        }

        long time3 = System.nanoTime();

        this.status = bestStrategy;

        // Log.log("DEBUG : Timed predictNotNull(" + (double) (time2 - time1) / 1e9 + ")
        // simulateWalk("
        // + (double) (time3 - time2) / 1e9 + ") consideredCount(" +
        // startingPositions.size() + ")");
        if (this.validateStrategy(animals, ps.get_location())) {
            // Log.log("Strategy discovered : " + this.status.toString());
            return this.executeStrategy(ps);
        }
        return new Command();
    }
}
