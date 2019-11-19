package lunch.g8;

import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class FamilyMember extends Agent<Family> {

    public FamilyMember(Family original) {
        super(original);
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
}
