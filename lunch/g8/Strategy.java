package lunch.g8;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static lunch.g8.PositionUtils.distance;
import lunch.sim.AnimalType;
import lunch.sim.Command;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public abstract class Strategy {

    protected final List<FamilyMember> family;
    protected final List<Animal> animals;
    protected final PlayerState state;
    protected final Random random;

    public Strategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        this.family = family;
        this.animals = animals;
        this.state = state;
        this.random = random;
    }

    public abstract Command run() throws AbortStrategyException;

    /**
     * Determines if there is risk at the next step of food being stole
     *
     * @return true if in the next turn an animal could take the food away
     */
    protected boolean dangerAnimal() {
        if (!state.isHoldingItem() && !(state.isSearching() && state.getTimeToFinishSearching() > 1)) {
            return false;
        }
        FoodType type = state.isHoldingItem() ? state.getItemHeld() : state.getFoodSearched();
        int dangerGoose = 0;
        int dangerMonkey = 0;
        final Point location = state.getLocation();
        for (Animal animal : animals) {
            if (animal.isEating()) {
                continue;
            }
            if (animal.getType() == AnimalType.MONKEY) { //monkey
                if (//distance(animal.predictNextLocation(), location) <= 5.0 + 1e-7 && 
                        distance(animal.getLocation(), location) <= 5.0 + 1e-7) {
                    dangerMonkey += 1;
                }
            } else if (type == FoodType.SANDWICH) { //goose
                if (//distance(animal.predictNextLocation(), location) <= 2.0 + 1e-7 &&
                        distance(animal.getLocation(), location) <= 2.0 + 1e-7) {
                    dangerGoose += 1;
                }
            }
        }
        return (dangerGoose >= 1 || dangerMonkey >= 3);
    }

    public int countAnimalsWithIn(AnimalType type, double radius) {
        int count = 0;
        final Point location = state.getLocation();
        for (Animal a : animals) {
            if (a.getType() == type && distance(location, a.getLocation()) < radius) {
                count++;
            }
        }
        return count;
    }

    public List<Animal> getAnimalsWithIn(AnimalType type, int radius) {
        return this.getAnimalsWithIn(type, radius, state.getLocation());
    }

    public List<Animal> getAnimalsWithIn(AnimalType type, int radius, Point reference) {
        List<Animal> list = new ArrayList<>();
        for (Animal a : animals) {
            if (a.getType() == type && distance(reference, a.getLocation()) < radius) {
                list.add(a);
            }
        }
        return list;
    }

    public List<FamilyMember> getOtherFamilyWithIn(int radius) {
        return getOtherFamilyWithIn(radius, state.getLocation());
    }

    public List<FamilyMember> getOtherFamilyWithIn(int radius, Point reference) {
        List<FamilyMember> list = new ArrayList<>();
        for (FamilyMember fm : family) {
            if (!fm.isOneSelf() && distance(reference, fm.getLocation()) < radius) {
                list.add(fm);
            }
        }
        return list;
    }

}
