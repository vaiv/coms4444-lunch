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

    private final MonkeyLureStrategy lureStrategy;
    private final GreedyEatingStrategy greedyStrategy;
    private Strategy subStrategy;
    private boolean waitingToFinish = false;
    private int consecutiveNoDistractor = 0;
    private int consecutiveMoreThanOneDistractors = 0;
    private int dontSwitchUntil;

    public DistractIfNeededStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        super(family, animals, state, random);
        lureStrategy = new MonkeyLureStrategy(family, animals, state, random);
        //lureStrategy.setMonkeyMargin(10);
        //lureStrategy.setCanFinishEating(false);
        greedyStrategy = new GreedyEatingStrategy(family, animals, state, random);
        subStrategy = greedyStrategy;
    }

    @Override
    public Command run() throws AbortStrategyException {
        if (state.getTimeToFinish() == 0)
            return new Command();
        //System.out.println("T: " + state.getTurn());
        //if (waitingToFinish && (getAveragePercFoodEatenByFamily() > 110 || state.getRemainingTime() < 5)) {
        //    lureStrategy.setCanFinishEating(true);
        //}
        if (state.getTurn() % 5 == 0) {
            if (!isDistracting() && shouldStartDistracting()) {
                return switchStrategy(lureStrategy);
            } else if (isDistracting() && isDoneDistracting()) {
                return switchStrategy(greedyStrategy);
            }
        }
        return subStrategy.run();
    }

    protected Command switchStrategy(Strategy nextStrategy) throws AbortStrategyException {
        if (subStrategy != nextStrategy) {
            dontSwitchUntil = state.getTurn() + 100 + random.nextInt(200);
        }
        subStrategy = nextStrategy;
        if (state.isHoldingItem()) {
            return new Command(CommandType.KEEP_BACK);
        } else {
            return subStrategy.run();
        }
    }

    protected boolean shouldStartDistracting() {
        if (dontSwitchUntil > state.getTurn()) {
            return false;
        }
        // if we are alone or someone else seems to be distracting
        if (family.size() == 1) {
            return false;
        }
        boolean isThereRandomPlayer = family.stream().anyMatch(fm -> fm.isRandomPlayer());
        if (state.getTurn() > (state.getId() + 1) * 100) {
            int distractors = countDistractors(isThereRandomPlayer ? 0.3 : 0.5);
            if (distractors == 0) {
                consecutiveNoDistractor++;
            } else {
                consecutiveNoDistractor = 0;
            }
            if (consecutiveNoDistractor > 4) {
                return true;
            }
        }
        return false;
//        final double percEaten = state.getPercentageOfFoodEaten();
//        List<FamilyMember> familyDistracting = getOtherFamilyWithIn(10, RB_CORNER);
//        if (familyDistracting.size() > 1) {
//            return false;
//        } else if (!familyDistracting.isEmpty() && percEaten - familyDistracting.get(0).getMaxPercentageOfFoodEaten() > 7) {
//            return true;
//        }
//        final double avgPercEaten = getAveragePercFoodEatenByFamily();
//        return percEaten - avgPercEaten > 10 || (percEaten > 99 && avgPercEaten < 110);
    }

    protected boolean isDoneDistracting() {
        if (dontSwitchUntil > state.getTurn()) {
            return false;
        }
        int distractors = countDistractors(0.5);
        if (distractors > 1) {
            consecutiveMoreThanOneDistractors++;
        } else {
            consecutiveMoreThanOneDistractors = 0;
        }
        if (consecutiveMoreThanOneDistractors > 50) {
            return true;
        }
        return false;
//        final double percEaten = state.getPercentageOfFoodEaten();
//        final double avgPercEaten = getAveragePercFoodEatenByFamily();
//        waitingToFinish = percEaten > 99;
//        boolean eatenDiffOk = (percEaten < 99 || !isSomeoneHoldingFood())
//                && percEaten - avgPercEaten < 3;
//        if (!eatenDiffOk) {
//            // check if someone else is nearby and have eaten well enough
//            // we could assume they are here to distract
//            List<FamilyMember> familyNearby = this.getOtherFamilyWithIn(10);
//            for (FamilyMember fm : familyNearby) {
//                if (fm.getMaxPercentageOfFoodEaten() - percEaten > -5) {
//                    return true;
//                }
//            }
//        }
//        return eatenDiffOk;
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
