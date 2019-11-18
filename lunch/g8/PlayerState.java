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
    
    private final int id;
    private FoodType foodSearched;
    
    public PlayerState(int id, lunch.sim.PlayerState original) {
        super(original);
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    @Override
    public Point getLocation() {
        return lastest.get_location();
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
