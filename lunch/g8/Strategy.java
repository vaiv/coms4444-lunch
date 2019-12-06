package lunch.g8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static lunch.g8.PositionUtils.centerOfGravity;
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
    protected final int numMonkeys;
    protected final int numGuesse;

    public Strategy(List<FamilyMember> family, List<Animal> animals, PlayerState state, Random random) {
        this.family = family;
        this.animals = animals;
        this.state = state;
        this.random = random;
        numMonkeys = (int) animals.stream().filter(a -> a.getType() == AnimalType.MONKEY).count();
        numGuesse = (int) animals.stream().filter(a -> a.getType() == AnimalType.GOOSE).count();
    }

    public abstract Command run() throws AbortStrategyException;

    /**
     * Determines if there is risk at the next step of food being stole
     *
     * @return true if in the next turn an animal could take the food away
     */
    protected boolean dangerAnimal() {
        return dangerAnimal(0);
    }

    /**
     * Determines if there is risk at the next step of food being stole
     *
     * @return true if in the next turn an animal could take the food away
     */
    protected boolean dangerAnimal(double margin) {
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
                        distance(animal.getLocation(), location) <= 5.0 + 1e-7 + margin) {
                    dangerMonkey += 1;
                }
            } else if (type == FoodType.SANDWICH) { //goose
                if (//distance(animal.predictNextLocation(), location) <= 2.0 + 1e-7 &&
                        distance(animal.getLocation(), location) <= 2.0 + 1e-7 + margin) {
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
        return getAnimalsWithIn(type, radius, state.getLocation());
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

    public double getMonkeyConcentration() {
        List<Point> locations = animals.stream()
                .filter(a -> a.getType() == AnimalType.MONKEY)
                .map(m -> m.predictNextLocation())
                .collect(Collectors.toList());
        Point cOG = centerOfGravity(locations);
        double totalDis = 0;
        for (Point p : locations) {
            totalDis += distance(p, cOG);
        }
        double concentration = 1 - totalDis / (50 * animals.size());
        return concentration;
    }

    public int getMonkeyDensity() {
        int numMonkeys = 0;
        for (Animal a : animals) {
            if (a.getType() == AnimalType.MONKEY) {
                numMonkeys++;
            }
        }
        return numMonkeys / family.size();
    }

    public int getGeeseDensity() {
        int numGeese = 0;
        for (Animal a : animals) {
            if (a.getType() == AnimalType.GOOSE) {
                numGeese++;
            }
        }
        return numGeese / family.size();
    }

    protected int countDistractors(double concentration) {
        //double[] densities = new double[this.family.size()];
        int count = 0;
        for (FamilyMember fm : family) {
            if (fm.isRandomPlayer()) {
                continue;
            }
            //localized num of monkeys in 40
            double localDensity = getAnimalsWithIn(AnimalType.MONKEY, 40, fm.getLocation()).size();
            if (localDensity / numMonkeys > concentration) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sorts the food by prioritizing the points obtained by eating it, with
     * Sandwiches last
     *
     * @param unordered the list of food without any order
     * @return the ordered list of food
     */
    protected List<FoodType> orderFoodSandwichLast(List<FoodType> unordered) {
        ArrayList<FoodType> ordered = new ArrayList<>();
        ArrayList<Double> ord = new ArrayList<>();
        for (FoodType f : unordered) {
            if (f == FoodType.SANDWICH1) {
                ord.add(1.1);
            } else if (f == FoodType.SANDWICH2) {
                ord.add(1.2);
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
            if (d == 1.1) {
                ordered.add(FoodType.SANDWICH1);
            } else if (d == 1.2) {
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

}
