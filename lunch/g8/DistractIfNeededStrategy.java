package lunch.g8;

import java.util.List;
import java.util.Random;
import static lunch.g8.PositionUtils.RB_CORNER;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class DistractIfNeededStrategy extends Strategy {

    private final LureAtPositionStrategy lureStrategy;
    private final GreedyEatingStrategy greedyStrategy;
    private Strategy subStrategy;
    private boolean waitingToFinish = false;

    public DistractIfNeededStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
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
        if (waitingToFinish && (getAveragePercFoodEatenByFamily() > 110 || state.getRemainingTime() < 5)) {
            lureStrategy.setCanFinishEating(true);
        }
        if (state.getTurn() % 10 == 0) {
            if (!isDistracting() && shouldStartDistracting()) {
                return switchStrategy(lureStrategy);
            } else if (isDistracting() && isDoneDistracting()) {
                return switchStrategy(greedyStrategy);
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

    protected boolean shouldStartDistracting() {
        // if we are alone or someone else seems to be distracting
        if (family.size() == 1) {
            return false;
        }
        final double percEaten = state.getPercentageOfFoodEaten();
        List<FamilyMember> familyDistracting = getOtherFamilyWithIn(10, RB_CORNER);
        if (familyDistracting.size() > 1) {
            return false;
        } else if (!familyDistracting.isEmpty() && percEaten - familyDistracting.get(0).getMaxPercentageOfFoodEaten() > 7) {
            return true;
        }
        final double avgPercEaten = getAveragePercFoodEatenByFamily();
        return percEaten - avgPercEaten > 10 || (percEaten > 99 && avgPercEaten < 110);
    }

    protected boolean isDoneDistracting() {
        final double percEaten = state.getPercentageOfFoodEaten();
        final double avgPercEaten = getAveragePercFoodEatenByFamily();
        waitingToFinish = percEaten > 99;
        boolean eatenDiffOk = (percEaten < 99 || !isSomeoneHoldingFood())
                && percEaten - avgPercEaten < 3;
        if (!eatenDiffOk) {
            // check if someone else is nearby and have eaten well enough
            // we could assume they are here to distract
            List<FamilyMember> familyNearby = this.getOtherFamilyWithIn(10);
            for (FamilyMember fm : familyNearby) {
                if (fm.getMaxPercentageOfFoodEaten() - percEaten > -5) {
                    return true;
                }
            }
        }
        return eatenDiffOk;
    }

    protected double getAveragePercFoodEatenByFamily() {
        double addPercEaten = 0;
        for (FamilyMember fm : family) {
            if (!fm.isOneSelf()) {
                addPercEaten += fm.getMaxPercentageOfFoodEaten();
            }
        }
        return addPercEaten / (family.size() - 1);
    }

    protected boolean isDistracting() {
        return subStrategy == lureStrategy;
    }

    protected Point pickPositionToLure() {
        return RB_CORNER;
    }

    private boolean isSomeoneHoldingFood() {
        for (FamilyMember fm : family) {
            if (!fm.isOneSelf() && fm.isHoldingItem()) {
                return true;
            }
        }
        return false;
    }
}
