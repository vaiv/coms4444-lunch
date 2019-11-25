package lunch.g8;

import static lunch.g8.PlayerState.TOTAL_EATING_TIME;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class FamilyMember extends Agent<Family> {

    private final int id;
    private final boolean isOneSelf;
    private int timeEating;

    public FamilyMember(Family original, boolean isPlayer, int id) {
        super(original);
        this.isOneSelf = isPlayer;
        this.id = id;
        timeEating = 0;
    }

    public boolean isOneSelf() {
        return isOneSelf;
    }

    public int getId() {
        return id;
    }

    @Override
    public Point getLocation() {
        return lastest.get_location();
    }

    public FoodType getItemHeld() {
        return lastest.get_held_item_type();
    }

    public boolean isHoldingItem() {
        return lastest.get_held_item_type() != null;
    }

    @Override
    public void update(Family original) {
        super.update(original);
        if (isHoldingItem() && history.size() > 1 
                && history.get(1).get_held_item_type() != null
                && history.get(1).get_held_item_type() == lastest.get_held_item_type()) {
            timeEating++;
        }
    }

    /**
     * Returns the maximum percentage [0,100] of food that the family member has
     * eaten. The actual amount could be less
     *
     * @return maximum percentage of food eaten by the family member.
     */
    public Double getMaxPercentageOfFoodEaten() {
        return (timeEating * 100.0 / TOTAL_EATING_TIME);
    }
}
