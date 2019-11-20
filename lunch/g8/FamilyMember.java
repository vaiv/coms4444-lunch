package lunch.g8;

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

    public FamilyMember(Family original, boolean isPlayer, int id) {
        super(original);
        this.isOneSelf = isPlayer;
        this.id = id;
    }

    public boolean isIsOneSelf() {
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
}
