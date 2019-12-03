package lunch.g8;

import java.util.List;
import java.util.Random;
import static lunch.g8.PositionUtils.*;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class HelpIfNeededStrategy extends Strategy {

    private final LureAtPositionStrategy lureStrategy;
    private final GreedyEatingStrategy greedyStrategy;
    private Strategy subStrategy;

    private FamilyMember familyBeingHelped;

    public HelpIfNeededStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        super(family, animals, state, random);
        lureStrategy = new LureAtPositionStrategy(family, animals, state, random);
        lureStrategy.setMonkeyMargin(10);
        lureStrategy.setCanFinishEating(false);
        greedyStrategy = new GreedyEatingStrategy(family, animals, state, random);
        subStrategy = greedyStrategy;
    }

    @Override
    public Command run() throws AbortStrategyException {
        //System.out.println("T: " + state.getTurn());
        if (!isHelpingOther()) {
            FamilyMember famToHelp = getFamilyToHelp();
            if (famToHelp != null) {
                familyBeingHelped = famToHelp;
                lureStrategy.setPosition(pickPositionToLure());
                return switchStrategy(lureStrategy);
            }
        } else {
            if (isDoneHelping()) {
                familyBeingHelped = null;
                return switchStrategy(greedyStrategy);
            } else {
                lureStrategy.setPosition(pickPositionToLure());
            }
        }
        return subStrategy.run();
    }

    protected Command switchStrategy(Strategy nextStrategy) throws AbortStrategyException {
        subStrategy = nextStrategy;
        if (state.isHoldingItem()) {
            return new Command(CommandType.KEEP_BACK);
        } else {
            return subStrategy.run();
        }
    }

    /**
     * If there is a family member that needs help finishing their food this
     * method will return the corresponding object.
     *
     * @return the family member to help or null if no one is too far behind
     */
    protected FamilyMember getFamilyToHelp() {
        final double percEaten = state.getPercentageOfFoodEaten();
        for (FamilyMember fm : family) {
            if ((!fm.isOneSelf() && percEaten - fm.getMaxPercentageOfFoodEaten() > 10) || 
                    (percEaten > 99 && fm.getMaxPercentageOfFoodEaten() < 110 && fm.isHoldingItem())) {
                return fm;
            }
        }
        return null;
    }

    protected boolean isDoneHelping() {
        if (familyBeingHelped == null) {
            return true;
        }
        final double percEaten = state.getPercentageOfFoodEaten();
        return (percEaten < 99 || !familyBeingHelped.isHoldingItem())
                && percEaten - familyBeingHelped.getMaxPercentageOfFoodEaten() < 3;
    }

    protected boolean isHelpingOther() {
        return familyBeingHelped != null;
    }

    protected Point pickPositionToLure() {
        Double direction = getDirection(familyBeingHelped.getLocation(), CENTER);
        if (direction == null) {
            direction = getDirection(familyBeingHelped.getLocation(), state.getLocation());
        }
        List<Animal> monkeys = this.getAnimalsWithIn(AnimalType.MONKEY, 40, familyBeingHelped.getLocation());
        final double distance = monkeys.size() > 2 ? 15 : 40;
        return moveInDirection(familyBeingHelped.getLocation(), direction, distance);
    }
}
