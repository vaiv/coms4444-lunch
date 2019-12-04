package lunch.g8;

import java.util.List;
import java.util.Random;
import static lunch.g8.PositionUtils.*;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class GreedyEatingStrategy extends EatAtPositionStrategy {

    public GreedyEatingStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        super(family, animals, state, random);
    }

    @Override
    protected Point pickAPosition() {
        int mDensity = getMonkeyDensity();
        boolean highDens = mDensity > 3;

        // if there is only one player and Monkey density is low, stay in the center
        if (family.size() == 1) {
            if (!highDens) {
                return CENTER;
            }
        }
        // otherwise move near the border of the map to eat
        int id = state.getId();
        switch (id % 3) {
            case 0:
                return new Point(MIN_X, MIN_Y);
            case 1:
                return new Point(MAX_X, MIN_Y);
            case 2:
                return new Point(MIN_X, MAX_Y);
            default:
                throw new IllegalArgumentException("A family member ID of " + id + " is not supported");
        }
    }
}
