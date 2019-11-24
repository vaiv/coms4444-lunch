package lunch.g8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public abstract class EatAtPositionStrategy extends Strategy {

    private Point position;
    protected double monkeyMargin;

    public EatAtPositionStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state) {
        super(family, animals, state);
        monkeyMargin = 40.0;
    }

    @Override
    public Command run() throws AbortStrategyException {
        if (state.getAvailableFood().isEmpty()) {
            throw new AbortStrategyException();
        }
        if (position == null) {
            position = pickAPosition();
        }
        if (state.isSearching()) {
            if (dangerAnimal()) {
                return new Command(CommandType.ABORT);
            }
        } else if (isFarFromPosition()) {
            if (!state.isSearching()) {
                return Command.createMoveCommand(moveTowards(state.getLocation(), position));
            }
        } else if (state.isHoldingItem()) {
            if (dangerAnimal()) {
                return new Command(CommandType.KEEP_BACK);
            } else {
                return new Command(CommandType.EAT);
            }
        } else {
            if (shouldTakeFoodOut()) {
                List<FoodType> food = state.getAvailableFood();
                return Command.createRetrieveCommand(orderFood(food).get(0));
            }
        }
        return new Command();
    }

    /**
     * Sorts the food by prioritizing the points obtained by eating it
     *
     * @param unordered the list of food without any order
     * @return the ordered list of food
     */
    protected List<FoodType> orderFood(List<FoodType> unordered) {
        ArrayList<FoodType> ordered = new ArrayList<>();
        ArrayList<Double> ord = new ArrayList<>();
        for (FoodType f : unordered) {
            if (f == FoodType.SANDWICH1) {
                ord.add(3.1);
            } else if (f == FoodType.SANDWICH2) {
                ord.add(3.2);
            } else if (f == FoodType.COOKIE) {
                ord.add(4.0);
            } else if (f == FoodType.FRUIT1) {
                ord.add(2.1);
            } else if (f == FoodType.FRUIT2) {
                ord.add(2.2);
            } else if (f == FoodType.EGG) {
                ord.add(2.0);
            } else {
                ord.add(1.0); //should never happen
            }
        }
        Collections.sort(ord, Collections.reverseOrder());
        //System.out.println("ordered ");
        for (Double d : ord) {
            if (d == 3.1) {
                ordered.add(FoodType.SANDWICH1);
            } else if (d == 3.2) {
                ordered.add(FoodType.SANDWICH2);
            } else if (d == 4.0) {
                ordered.add(FoodType.COOKIE);
            } else if (d == 2.1) {
                ordered.add(FoodType.FRUIT1);
            } else if (d == 2.2) {
                ordered.add(FoodType.FRUIT2);
            } else if (d == 2.0) {
                ordered.add(FoodType.EGG);
            } else {
                System.out.println("There is an error - this food type is invalid");
            }
        }
        return ordered;
    }

    /**
     * helper function: whether put food away if hold food and is in danger: put
     * food away; else do nothing.
     *
     * @return
     */
    protected Command putFoodBack() {
        if (dangerAnimal() && state.isHoldingItem()) {
            return new Command(CommandType.KEEP_BACK);
        }
        return null;
    }

    protected boolean shouldTakeFoodOut() {
        return countAnimalsWithIn(AnimalType.MONKEY, monkeyMargin) < 3;
    }

    /**
     * Determines if the agent is too far from the intended position and
     * henceforth should move towards it
     *
     * @return true if the agent is too far
     */
    protected boolean isFarFromPosition() {
        return distance(state.getLocation(), position) > 1;
    }

    /**
     * Chooses the position to eat
     *
     * @return the chosen position
     */
    protected abstract Point pickAPosition();

}
