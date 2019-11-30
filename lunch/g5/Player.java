package lunch.g5;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import javafx.util.Pair;

import java.util.ArrayList;

import lunch.sim.*;

public class Player implements lunch.sim.Player {
    private int seed;
    private Random random;
    private Integer id;
    private Integer turn;
    private Integer nFamily;

    MatrixPredictor matrixPredictor;
    EatingStatus eatingStatus;
    FamilyBehaviorPredictor familyBehaviorPredictor;

    // An array to store the animals in previous turn (Mainly to know their positions, so we know where they are going)
    private ArrayList<Animal> previousAnimals;
    private ArrayList<Family> previousMembers;
    private GreedyEater greedyEater;
    private GeeseShield geeseShield;

    private DistractionStrategy mDistraction;

    public Player() {
        turn = 0;
        matrixPredictor = new MatrixPredictor(5.0, 6.0, 0);
        eatingStatus = new EatingStatus();
    }

    public String init(
            ArrayList<Family> members, Integer id, int f,
            ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        this.id = id;
        this.seed = s;

        this.nFamily = f;
        this.random = new Random(this.seed);

        this.greedyEater = new GreedyEater();
        this.mDistraction = new DistractionStrategy();
        this.familyBehaviorPredictor = new FamilyBehaviorPredictor(f, m);

        mDistraction.init(members, id, f, animals, m, g, t, s);
        return "guardians";
    }

    private boolean noDistractor(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        ArrayList<BehaviorType> familyBehavior = familyBehaviorPredictor.predict(members, animals, previousAnimals);
        Integer ourId = ps.get_id();
        for (int i = 0; i < familyBehavior.size(); i++) {
            if (i == ourId) {
                continue;
            }
            BehaviorType behaviorType = familyBehavior.get(i);
            if (behaviorType == BehaviorType.DISTRACTION) {
                return true;
            }
        }
        return false;
    }

    private boolean didEveryoneEat(ArrayList<Family> members, PlayerState ps) {
        ArrayList<HashMap<FoodType, Double>> es = eatingStatus.getPercentages(previousMembers, members);
        Integer ourId = ps.get_id();
        for (int i = 0; i < es.size(); i++) {
            if (i == ourId) {
                continue;
            }
            HashMap<FoodType, Double> esPerMember = es.get(i);
            Double cumEsPerMember = esPerMember.get(FoodType.SANDWICH) + esPerMember.get(FoodType.COOKIE) + esPerMember.get(FoodType.FRUIT) + esPerMember.get(FoodType.EGG);
            cumEsPerMember = cumEsPerMember / 4.0;
            if (cumEsPerMember < 0.99) {
                return false;
            }
        }
        return true;
    }

    private boolean weHaveEatenOurFood(PlayerState ps) {
        // TODO: Get this state from aggressive player.
        return false;
    }

    private boolean weHaveOnlySandwiches(PlayerState ps) {
        // TODO: Get this state from aggressive player.
        return false;
    }

    private boolean everyoneHasOnlySandwiches(ArrayList<Family> members) {
        ArrayList<HashMap<FoodType, Double>> es = eatingStatus.getPercentages(previousMembers, members);
        for (int i = 0; i < es.size(); i++) {
            HashMap<FoodType, Double> esPerMember = es.get(i);
            Double cumEsPerMember = esPerMember.get(FoodType.COOKIE) + esPerMember.get(FoodType.FRUIT) + esPerMember.get(FoodType.EGG);
            cumEsPerMember = cumEsPerMember / 3.0;
            if (cumEsPerMember < 0.99) {
                return false;
            }
        }
        return true;
    }

    private boolean someoneIsInTheCornerEatingSandwich(ArrayList<Family> members, PlayerState ps) {
        // TODO: Implement
        return false;
    }

    private boolean weWillBeAbleToFinishSandwich(PlayerState ps) {
        // TODO: Implement
        return true;
    }

    public BehaviorType getNextBehaviorType(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        // If this is first turn => just go aggresive
        if (turn == 1) {
            return BehaviorType.AGGRESSIVE;
        }
        // If we are alone => go aggressive
        if (nFamily == 1) {
            return BehaviorType.AGGRESSIVE;
        }
        // If everyone ate => go aggressive
        if (didEveryoneEat(members, ps)) {
            return BehaviorType.AGGRESSIVE;
        }
        // Is there is no other distractor => go be one
        if (noDistractor(members, animals, ps)) {
            return BehaviorType.DISTRACTION;
        }
        // Check if we have eaten our food
        if (weHaveEatenOurFood(ps)) {
            if (everyoneHasOnlySandwiches(members)) {
                // If someone is eating a sandwich in the corner => geese shild
                if (someoneIsInTheCornerEatingSandwich(members, ps)) {
                    return BehaviorType.GEESE_SHIELD;
                } else {
                    // If there are uneaten sandwiches, but noone is in the corner => we can't help from geese => monkey distraction
                    return BehaviorType.DISTRACTION;
                }
            }
        } else {
            // We have not eaten our food
            // Check if we have only sandiwches left
            if (weHaveOnlySandwiches(ps)) {
                // If we are able to finish our sandwich => aggressive
                if (weWillBeAbleToFinishSandwich(ps)) {
                    return BehaviorType.AGGRESSIVE;
                } else {
                    // If we know we can't finish our sandwich => help other's => geese shield
                    return BehaviorType.GEESE_SHIELD;
                }
            } else {
                // If we still have food that is not sandwiches left => aggresive
                return BehaviorType.AGGRESSIVE;
            }
        }
        return BehaviorType.AGGRESSIVE;
    }

    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        Command command;
        // Get the bahivour typ to execute
        BehaviorType type = getNextBehaviorType(members, animals, ps);
        // Depending on the type generate the appropriate command
        switch (type) {
            case DISTRACTION:
                command = mDistraction.getCommand(members, animals, ps);
                break;
            case GEESE_SHIELD:
                command = geeseShield.getCommandGeeseShield(members, animals, previousAnimals, ps, turn);
                break;
            default:
            case AGGRESSIVE:
                command = greedyEater.getCommandCornerEating(members, animals, ps, previousAnimals, turn);
                break;
        }
        // Record things for the next turn
        previousAnimals = animals;
        previousMembers = members;
        turn++;
        return command;
    }
}