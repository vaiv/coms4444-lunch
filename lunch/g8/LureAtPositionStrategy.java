package lunch.g8;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static lunch.g8.PositionUtils.*;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class LureAtPositionStrategy extends EatAtPositionStrategy {

    public boolean reCentering = true;

    public LureAtPositionStrategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        super(CENTER, family, animals, state, random);
        monkeyMargin = 6;
    }

    @Override
    public Command run() throws AbortStrategyException {
        Command command = super.run();
        if (command.get_type() != null || state.isSearching()) {
            return command;
        }
        List<Animal> nearMonkeys = getAnimalsWithIn(AnimalType.MONKEY, 20);
        Point mCenter = centerOfGravity(nearMonkeys.stream().map((a) -> a.predictNextLocation()).collect(Collectors.toList()));
        double direction = getDirection(mCenter, state.getLocation());
        return Command.createMoveCommand(moveInDirection(state.getLocation(), direction));
    }

    @Override
    protected boolean isFarFromPosition() {
        final double distToCenter = distance(state.getLocation(), getPosition());
        if (distToCenter < 1) {
            reCentering = false;
        }
        if (reCentering || distToCenter > 10) {
            reCentering = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Point pickAPosition() {
        return CENTER;
    }

}
