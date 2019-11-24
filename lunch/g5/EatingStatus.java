package lunch.g5;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

import lunch.sim.Point;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;


public class EatingStatus {

    private ArrayList<HashMap<FoodType, Integer>> previousEatingStatus;

    public EatingStatus() { }

    /**
     * A function that calculates the eating status of each player.
     *
     * @param previousMembers: An array of previous family members in the field
     * @param members: An array of current family members in the field
     */
    public ArrayList<HashMap<FoodType, Integer>> get(ArrayList<Family> previousMembers, ArrayList<Family> members) {
        if (previousMembers == null) {
            throw new RuntimeException("No previous family members saved => cannot calculate the eating status");
        }
        ArrayList<HashMap<FoodType, Integer>> eatingStatus = new ArrayList<>();
        // Go through each family member and predict the command
        for (int i = 0; i < members.size(); i++) {
            HashMap<FoodType, Integer> memberStatus;
            if(previousEatingStatus != null && members.size() == previousEatingStatus.size()) {
                HashMap<FoodType, Integer> previousMemberStatus = previousEatingStatus.get(i);
                memberStatus = new HashMap<>(previousMemberStatus);
            } else {
                memberStatus = new HashMap<>();
                memberStatus.put(FoodType.SANDWICH, 0);
                memberStatus.put(FoodType.FRUIT, 0);
                memberStatus.put(FoodType.EGG, 0);
                memberStatus.put(FoodType.COOKIE, 0);
            }
            Family member = members.get(i);
            Family previousMember = previousMembers.get(i);
            Point currentLocation = member.get_location();
            Point previousLocation = previousMember.get_location();
            if(Point.dist(currentLocation, previousLocation) < 0.01) {
                FoodType heldFoodType = member.get_held_item_type();
                if(heldFoodType != null) {
                    memberStatus.put(heldFoodType, memberStatus.get(heldFoodType) + 1);
                }
            }
            eatingStatus.add(memberStatus);
        }
        previousEatingStatus = eatingStatus;
        return eatingStatus;
    }
}