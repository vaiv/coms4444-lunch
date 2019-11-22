package lunch.g8;

import java.util.List;
import static lunch.g8.PositionUtils.CENTER;
import static lunch.g8.PositionUtils.distance;
import lunch.sim.Command;
import lunch.sim.CommandType;

/**
 *
 * @author group8
 */
public class LureRotationStrategy extends Strategy {

    private final CenterLureStrategy centerStrategy;
    private final EatAtCornerStrategy cornerStrategy;
    private final double switchPercentage;
    private Strategy subStrategy;
    private boolean hasTakenTurn;
    private int switchOnlyAfter;

    public LureRotationStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state) {
        super(family, animals, state);
        switchPercentage = 100.0 * state.getId() / family.size();
        centerStrategy = new CenterLureStrategy(family, animals, state);
        cornerStrategy = new EatAtCornerStrategy(family, animals, state);
        if (state.getId() == 0) {
            subStrategy = centerStrategy;
            hasTakenTurn = true;
            switchOnlyAfter = 100;
        } else {
            subStrategy = cornerStrategy;
            hasTakenTurn = false;
        }
    }

    @Override
    public Command run() throws AbortStrategyException {
        //System.out.println("T: " + state.getTurn());
        if (!hasTakenTurn && state.getPercentageOfFoodEaten() >= switchPercentage) {
            subStrategy = centerStrategy;
            hasTakenTurn = true;
            switchOnlyAfter = state.getTurn() + 100;
            //System.out.println("Player " + state.getId() + " go to center, back only after " + switchOnlyAfter);
            // if the player is holding an item make sure to put it back
            // before moving
            if (state.isHoldingItem()) {
                return new Command(CommandType.KEEP_BACK);
            }
        } else if (hasTakenTurn && subStrategy == centerStrategy && state.getTurn() > switchOnlyAfter) {
            //List<FamilyMember> familyNearCenter = getOtherFamilyWithIn(10, CENTER);
            FamilyMember nextFm = family.get((state.getId() + 1) % family.size());
            //if (!familyNearCenter.isEmpty()) {
            if (distance(nextFm.getLocation(), CENTER) < 10) {
                subStrategy = cornerStrategy;
                //System.out.println("Player " + state.getId() + " going to corner at turn " + state.getTurn());
                // again if holding item put it back first
                if (state.isHoldingItem()) {
                    return new Command(CommandType.KEEP_BACK);
                }
            }
        }
        return subStrategy.run();
    }

}
