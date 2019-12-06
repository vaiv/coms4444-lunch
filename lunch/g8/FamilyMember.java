package lunch.g8;

import static lunch.g8.PlayerState.TOTAL_EATING_TIME;
import static lunch.g8.PositionUtils.getDirection;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.Point;

/**
 *
 * @author group8
 */
public class FamilyMember extends Agent<Family> {

    public static boolean RANDOM_PLAYER_DETECTION = true;
    
    private final int id;
    private final boolean isOneSelf;
    private int timeEating;
    private boolean randomPlayer = false;
    private Double lastDirection = 0.0;
    private int timesDirectionChangedInARow;
    private boolean directionJustChanged;

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
        return lastest.get_id();
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
        if (RANDOM_PLAYER_DETECTION && !randomPlayer) {
            detectRandom();
        }
    }

    protected void detectRandom() {
        try {
            if (history.size() > 1) {
                Double currentDirection = getDirection(history.get(1).get_location(), lastest.get_location());
                if (history.size() > 2 && currentDirection != null && lastDirection != null) {
                    if (Math.abs(lastDirection - currentDirection) > 0.05) {
                        if (directionJustChanged) {
                            timesDirectionChangedInARow++;
                        }
                        directionJustChanged = true;
                    } else {
                        timesDirectionChangedInARow = 0;
                        directionJustChanged = false;
                    }
                }
                lastDirection = currentDirection;
            }
            if (timesDirectionChangedInARow > 3) {
                //System.out.println(getId());
                randomPlayer = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
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

    public boolean isRandomPlayer() {
        return randomPlayer;
    }

}
