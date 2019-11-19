package lunch.g8;

import static lunch.g8.PositionUtils.*;
import lunch.sim.AnimalType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class Animal extends Agent<lunch.sim.Animal> {

    public Animal(lunch.sim.Animal original) {
        super(original);
    }

    public AnimalType getType() {
        return lastest.which_animal();
    }

    public boolean isEating() {
        return lastest.busy_eating();
    }

    public double getMaxSpeed() {
        return lastest.get_max_speed();
    }

    @Override
    public Point getLocation() {
        return lastest.get_location();
    }

    public Point predictNextLocation() {
        return predictLocation(1);
    }

    public Point predictLocation(int time) {
        if (this.history.size() > 2) {
            Double dir = getDirection(history.get(1).get_location(), getLocation());
            if (dir == null) {
                return getLocation();
            }
            return moveInDirection(getLocation(), dir, getMaxSpeed() * time);
        }
        return getLocation();
    }
}
