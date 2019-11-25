package lunch.g8;

import java.util.ArrayList;
import java.util.List;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class PlayerState extends Agent<lunch.sim.PlayerState> {

    public static final int TIME_TO_SEARCH = 10;
    public static final int TOTAL_EATING_TIME = 780;

    private final int id;
    private FoodType foodSearched;
    private int turn;

    public PlayerState(int id, lunch.sim.PlayerState original) {
        super(original);
        this.id = id;
        turn = 0;
    }

    public int getId() {
        return id;
    }

    @Override
    public Point getLocation() {
        return lastest.get_location();
    }

    public int getTurn() {
        return turn;
    }

    /**
     * Increases the turn counter
     */
    public void tick() {
        this.turn++;
    }

    /**
     * Returns the item that the player is currently holding
     *
     * @return the type of item or null if no item is held
     */
    public FoodType getItemHeld() {
        return lastest.get_held_item_type();
    }

    public boolean isHoldingItem() {
        return lastest.get_held_item_type() != null;
    }

    public int getTimeToFinishCurrentFoodItem() {
        return lastest.time_to_eat_remaining();
    }

    public int getTimeToFinish(FoodType item) {
        return lastest.get_time_for_item(item);
    }

    /**
     * Returns the amount of time left to finish a search (putting food in or
     * out of the bag)
     *
     * @return time left, 0 if no search is in progress
     */
    public int getTimeToFinishSearching() {
        return lastest.time_to_finish_search();
    }

    public boolean isSearching() {
        return lastest.is_player_searching();
    }

    public boolean hasItemOfType(FoodType type) {
        if (type == FoodType.FRUIT) {
            return lastest.check_availability_item(FoodType.FRUIT1) || lastest.check_availability_item(FoodType.FRUIT2);
        }
        if (type == FoodType.SANDWICH) {
            return lastest.check_availability_item(FoodType.SANDWICH1) || lastest.check_availability_item(FoodType.SANDWICH2);
        }
        return lastest.check_availability_item(type);
    }

    /**
     * Returns the percentage [0, 100] of food, that has been eaten by this
     * player so far
     *
     * @return the percentage of food eaten
     */
    public double getPercentageOfFoodEaten() { //percent Food Eaten for function in strat
        //returns the amount of food, a Double out of 100, that has been eaten by this player so far
        int totalTimeSpentEating = TOTAL_EATING_TIME; //amount of time needed to eat all food in bag
        for (FoodType f : getAvailableFood()) {
            totalTimeSpentEating -= getTimeToFinish(f);
        }
        return (totalTimeSpentEating * 100.0 / TOTAL_EATING_TIME);
    }

    /**
     * Returns the amount of time needed to finish eating all the food left
     *
     * @return time needed to finish eating
     */
    public int getTimeToFinish() {
        int total = 0;
        for (FoodType f : getAvailableFood()) {
            total += getTimeToFinish(f);
        }
        return total;
    }

    public List<FoodType> getAvailableFood() {
        ArrayList<FoodType> food = new ArrayList<>();
        for (FoodType type : FoodType.values()) {
            if (type != FoodType.FRUIT && type != FoodType.SANDWICH && hasItemOfType(type)) {
                food.add(type);
            }
        }
        return food;
    }

    public FoodType getFoodSearched() {
        return foodSearched;
    }

    public void setFoodSearched(FoodType foodSearched) {
        this.foodSearched = foodSearched;
    }

}
