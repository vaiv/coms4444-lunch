package lunch.g8;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static lunch.g8.PositionUtils.*;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class MonkeyLureStrategy extends Strategy {

    private final static int[] DELTAS = {0, -1, 1, 2, -2, 3, -3, 4, -4, 5, -5, 6};
    private Point targetPosition;
    //private int distractors = 0;

    public MonkeyLureStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        super(family, animals, state, random);
    }

    @Override
    public Command run() throws AbortStrategyException {

        //int nDistractors = countDistractors();
        //if (distractors != nDistractors) {
        //    System.out.println("T" + state.getTurn() + "Distractor " + nDistractors);
        //    distractors = nDistractors;
        //}
        //System.out.println("Monkey Concentration: " + getMonkeyConcentration());
        if (state.isSearching()) {
            if (dangerAnimal()) {
                targetPosition = getGoToPoint();
                return new Command(CommandType.ABORT);
            }
        } else if (targetPosition != null) {
            if (dangerAnimal()) {
                targetPosition = getGoToPoint();
                return new Command(CommandType.KEEP_BACK);
            }
            if (distance(targetPosition, state.getLocation()) > 0) {
                return Command.createMoveCommand(moveTowards(state.getLocation(), targetPosition));
            } else {
                targetPosition = null;
            }
        } else if (state.isHoldingItem()) {
            if (dangerAnimal()) {
                targetPosition = getGoToPoint();
                return new Command(CommandType.KEEP_BACK);
            }
            if (shouldScape()) {
                Point next = getScapeRoute();
                if (next != null) {
                    return Command.createMoveCommand(moveTowards(state.getLocation(), next));
                } else {
                    targetPosition = getGoToPoint();
                    return new Command(CommandType.KEEP_BACK);
                }
            } else if (state.getTimeToFinish() > 1 || state.getRemainingTime() < 50) {
                return new Command(CommandType.EAT);
            } else {
                return new Command();
            }
        } else {
            if (shouldTakeFoodOut()) {
                List<FoodType> food = state.getAvailableFood();
                return Command.createRetrieveCommand(orderFoodSandwichLast(food).get(0));
            }
        }
        return new Command();

//        if (command.get_type() != null || state.isSearching()) {
//            return command;
//        }
//        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 20);
//        Point mCenter = centerOfGravity(nearMonkeys.stream().map((a) -> a.predictNextLocation()).collect(Collectors.toList()));
//        double direction = getDirection(mCenter, state.getLocation());
//        return Command.createMoveCommand(moveInDirection(state.getLocation(), direction));
    }

    protected boolean shouldTakeFoodOut() {
//        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 20);
//        return nearMonkeys.stream()
//                .map(m -> m.predictLocation(10))
//                .filter(p -> distance(state.getLocation(), p) < 5.0 + 1e-2)
//                .count() < 3;
        return countAnimalsWithIn(AnimalType.MONKEY, 6) < 3;
    }

    protected boolean shouldScape() {
        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 20);
        List<Point> nextML = nearMonkeys.stream().map(m -> m.predictLocation(4)).collect(Collectors.toList());
        return countWithInRadius(nextML, state.getLocation(), 5.0 + 1e-7) >= 3;
    }

    protected Point getScapeRoute() {
        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 12);
        List<Point> nextML = nearMonkeys.stream().map(m -> m.predictNextLocation()).collect(Collectors.toList());
        final Point location = state.getLocation();
        Double dToCenter = PositionUtils.getDirection(location, centerOfGravity(nextML));
        if (dToCenter == null) {
            dToCenter = 0.0;
        }
        dToCenter = -dToCenter + Math.PI / 2;
//        final int[] deltas = {0, -1, 1, 2, -2, 3, -3, 4, -4, 5, -5, 6};
//        final double inc = 2 * Math.PI / 12;
        final int nDeltas = 8;
        final double inc = 2 * Math.PI / nDeltas;
        for (int i = 0; i < nDeltas; i++) {
            Point dest = moveInDirection(location, dToCenter + DELTAS[i] * inc);
            if (countWithInRadius(nextML, dest, 5.0 + 1e-7) < 3
                    //&& distance(dest, CENTER) < 10 + 20 * (1-getMonkeyConcentration())
                    && ((Math.abs(dest.x) < 15 && Math.abs(dest.y) < 15) || (dest.x > 0 && dest.x < 30 && dest.y > 0 && dest.y < 30))
                    && Point.within_bounds(dest)) {
                return dest;
            }
        }
        return null;
    }

    protected Point getGoToPoint() {
        int movingTime = 12;
        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 100);
        List<Point> nextML = nearMonkeys.stream().map(m -> m.predictLocation(20 + movingTime)).collect(Collectors.toList());
        final Point location = state.getLocation();
        Double dToCenter = getDirection(location, CENTER);
        if (dToCenter == null) {
            dToCenter = 0.0;
        }
        final int nDeltas = getMonkeyConcentration() > 0.5 ? 4 : 8;
        final double inc = 2 * Math.PI / nDeltas;
        for (int i = 0; i < nDeltas; i++) {
            Point dest = moveInDirection(location, dToCenter + DELTAS[i] * inc, movingTime);
            if (countWithInRadius(nextML, dest, 8.0 + 1e-7) < 3 && Point.within_bounds(dest)) {
                return dest;
            }
        }
        final double[] cardinal = {0, Math.PI / 2, Math.PI, 3 * Math.PI / 2};
        double cDist = Double.MAX_VALUE;
        Point finalDest = null;
        for (int i = 0; i < cardinal.length; i++) {
            final Point dest = moveInDirection(location, 0, movingTime);
            final double pDist = distance(dest, CENTER);
            if (pDist < cDist && Point.within_bounds(dest)) {
                finalDest = dest;
                cDist = pDist;
            }
        }
        return finalDest;
    }

    protected int countWithInRadius(List<Point> points, Point ref, double radius) {
        int count = 0;
        for (Point point : points) {
            if (distance(ref, point) <= radius) {
                count++;
            }
        }
        return count;
    }

}
