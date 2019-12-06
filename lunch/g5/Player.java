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
    private Integer nMonkeys;
    private Integer nGeese;
    private Integer overallTime;
    private ArrayList<ArrayList<Point>> playerMovement = new ArrayList<>();
    private boolean thereIsRandomPlayer = false;

    MatrixPredictor matrixPredictor;
    EatingStatus eatingStatus;
    FamilyBehaviorPredictor familyBehaviorPredictor;

    // An array to store the animals in previous turn (Mainly to know their positions, so we know where they are going)
    private ArrayList<Animal> previousAnimals;
    private ArrayList<Family> previousMembers;
    private GreedyEater greedyEater;
    private GeeseShield geeseShield;
    private SandwichFlasher sandwichFlasher;

    private BehaviorType previousBehaviourType = BehaviorType.AGGRESSIVE;
    private boolean beAggressiveTillTheEnd = false;
    private int nTimestepsWithNoDistractor = -2;
    private int nTimestepsSomeoneIsDistractor = 0;
    private int nTimesSwitchedToDistraction = 0;

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
        this.overallTime = (int)Math.round(t);
        this.nMonkeys = m;
        for (int i = 0; i < f; i++) {
            playerMovement.add(new ArrayList<Point>());
        }
        this.nGeese = g;
        this.random = new Random(this.seed + id);

        this.previousAnimals = animals;
        this.previousMembers = members;
        this.greedyEater = new GreedyEater();
        this.mDistraction = new DistractionStrategy();
        this.familyBehaviorPredictor = new FamilyBehaviorPredictor(f, m);
        this.sandwichFlasher = new SandwichFlasher();

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
                return false;
            }
        }
        return true;
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
        HashMap<FoodType, Integer> es = EatingStatus.getEatingStatusLeft(ps);
        for (FoodType food : es.keySet()) {
            if (es.get(food) > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean weHaveOnlySandwiches(PlayerState ps) {
        HashMap<FoodType, Integer> es = EatingStatus.getEatingStatusLeft(ps);
        for (FoodType food : es.keySet()) {
            if (food == FoodType.SANDWICH1 || food == FoodType.SANDWICH2) {
                continue;
            }
            if (es.get(food) > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean weReallyHaveOnlySandwiches(PlayerState ps) {
        HashMap<FoodType, Integer> es = EatingStatus.getEatingStatusLeft(ps);
        for (FoodType food : es.keySet()) {
            if (food == FoodType.SANDWICH1 || food == FoodType.SANDWICH2) {
                continue;
            }
            if (es.get(food) > 0) {
                return false;
            }
        }
        return true;
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

    private int estimatedTimeToEatEverythingButSandwiches(PlayerState ps) {
        HashMap<FoodType, Integer> es = EatingStatus.getEatingStatusLeft(ps);
        int timeToEat = 0;
        for (FoodType food : es.keySet()) {
            int timeLeft = es.get(food);
            if(timeLeft < 1) {
                continue;
            }
            if (food == FoodType.SANDWICH1 || food == FoodType.SANDWICH2) {
                continue;
            } else {
                timeToEat += (int) Math.round(timeLeft * (120.0 + nMonkeys * 1.0) / 120.0) + 70;
            }
        }
        return timeToEat;
    }

    private int estimatedTimeToEatEverything(PlayerState ps) {
        HashMap<FoodType, Integer> es = EatingStatus.getEatingStatusLeft(ps);
        int timeToEat = 0;
        for (FoodType food : es.keySet()) {
            int timeLeft = es.get(food);
            if(timeLeft < 1) {
                continue;
            }
            if (food == FoodType.SANDWICH1 || food == FoodType.SANDWICH2) {
                timeToEat += (int) Math.round(timeLeft * (120.0 + nMonkeys * 1.0 + nGeese * 2.0) / 120.0) + 70;
            } else {
                timeToEat += (int) Math.round(timeLeft * (120.0 + nMonkeys * 1.0) / 120.0) + 70;
            }
        }
        return timeToEat;
    }

    private boolean weWillBeAbleToFinishSandwich(PlayerState ps) {
        // TODO: Implement
        return true;
    }

    private boolean weAreDistracting() {
        return previousBehaviourType == BehaviorType.DISTRACTION_EAT || previousBehaviourType == BehaviorType.DISTRACTION_NOEAT;
    }

    private int timeLeft() {
        return overallTime - turn;
    }

    public BehaviorType getNextBehaviorType(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        //System.out.println("[" + this.id + "v] =======================================");
        //System.out.println("[|] timeLeft: \t\t\t" + timeLeft());
        //System.out.println("[|] weAreDistracting:\t\t" + weAreDistracting());
        //System.out.println("[|] noDistractor:\t\t" + noDistractor(members, animals, ps));
        //System.out.println("[|] beAggressiveTillTheEnd:\t" + beAggressiveTillTheEnd);
        //System.out.println("[|] weHaveEatenOurFood: \t" + weHaveEatenOurFood(ps));
        //System.out.println("[|] didEveryoneEat:\t\t" + didEveryoneEat(members, ps));
        //System.out.println("[|] nTimestepsWithNoDistractor:\t" + nTimestepsWithNoDistractor);
        //System.out.println("[|] nTimestepsSomeoneIsDistr.:\t" + nTimestepsSomeoneIsDistractor);
        //System.out.println("[|] estTime2EatEvrthg.ButSand.:\t" + estimatedTimeToEatEverythingButSandwiches(ps));
        //System.out.println("[|] estTime2EatEvrthg.:\t\t" + estimatedTimeToEatEverything(ps));
        //System.out.println("[|] thereIsRandomPlayer:\t" + thereIsRandomPlayer);
        if(thereIsRandomPlayer) {
            return BehaviorType.AGGRESSIVE;
        }
        if(nTimesSwitchedToDistraction > 5) {
            return BehaviorType.AGGRESSIVE;
        }
        // Zero it down if we are distracting
        if(weAreDistracting()) {
            nTimestepsWithNoDistractor = 0;
        }
        // Remember the number of steps with no distractor
        if (!weAreDistracting() && noDistractor(members, animals, ps)) {
            nTimestepsWithNoDistractor += 1;
        }
        // Remember the number of steps of other distractor
        if (!noDistractor(members, animals, ps)) {
            nTimestepsSomeoneIsDistractor += 1;
        } else {
            nTimestepsSomeoneIsDistractor = 0;
        }
        // Check if we have decided to be aggressive till the end
        if (beAggressiveTillTheEnd) {
            return BehaviorType.AGGRESSIVE;
        }
        // If we are alone => go aggressive
        if (nFamily == 1) {
            return BehaviorType.AGGRESSIVE;
        }
        // Check if overall time is small
        if (overallTime <= 1200) {
            // If there are not too many monkeys => go aggresive
            if (nMonkeys < 100 || nFamily <= 4) {
                return BehaviorType.AGGRESSIVE;
            } else {
                return BehaviorType.DISTRACTION_EAT;
            }
        }
        // If we have little food that needs to be eaten (1 seconds) && there is less than 100 seconds left?
        if (weHaveEatenOurFood(ps) && timeLeft() < 200) {
            beAggressiveTillTheEnd = true;
            return BehaviorType.AGGRESSIVE;
        }
        // If everyone ate => go aggressive
        if (didEveryoneEat(members, ps)) {
            return BehaviorType.AGGRESSIVE;
        }

        // ================== INITIAL CHECKS DONE ============================

        // If we have almost eaten our food with aggresive and there is still some time => go distract
        if (!weReallyHaveOnlySandwiches(ps) && weHaveEatenOurFood(ps) && timeLeft() > 400) {
            return BehaviorType.DISTRACTION_NOEAT;
        }
        // Check if we are distacting
        if (weAreDistracting()) {
            if (nGeese > 25 || nMonkeys > 90) {
                if (!weHaveOnlySandwiches(ps)) {
                    if (timeLeft() < estimatedTimeToEatEverythingButSandwiches(ps)) {
                        beAggressiveTillTheEnd = true;
                        return BehaviorType.AGGRESSIVE;
                    }
                }
            } else {
                if (!weHaveEatenOurFood(ps)) {
                    if (timeLeft() < estimatedTimeToEatEverything(ps)) {
                        beAggressiveTillTheEnd = true;
                        return BehaviorType.AGGRESSIVE;
                    }
                }
            }
        }
        // If we are distraction and there is a distractor
        int somebodyDistractingThreshold = 30 + random.nextInt(30);
        //System.out.println("[|] somebodyDistractingThresh.:\t" + somebodyDistractingThreshold);
        if (weAreDistracting() && nTimestepsSomeoneIsDistractor >= somebodyDistractingThreshold){
            return BehaviorType.AGGRESSIVE;
        }
        // Is there is no other distractor => go be one
        if(!weReallyHaveOnlySandwiches(ps) && (weAreDistracting() || nTimestepsWithNoDistractor == -1 || nTimestepsWithNoDistractor >= 30)) {
            if(weHaveEatenOurFood(ps)) {
                return BehaviorType.DISTRACTION_NOEAT;
            } else {
                return BehaviorType.DISTRACTION_EAT;
            }
        }
        // Check if we have eaten our food
        if (weHaveEatenOurFood(ps)) {
            if (everyoneHasOnlySandwiches(members)) {
                return BehaviorType.SANDWICH_FLASHING;
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
                    return BehaviorType.SANDWICH_FLASHING;
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
        if (this.nFamily == 1 || this.nMonkeys < 10) {
            command = greedyEater.getCommandCornerEating(members, animals, ps, previousAnimals, turn);
        } else if(thereIsRandomPlayer) {
            command = greedyEater.getCommandCornerEating(members, animals, ps, previousAnimals, turn);
        } else if (didEveryoneEat(members, ps) || weHaveOnlySandwiches(ps)) {
            command = greedyEater.getCommandCornerEating(members, animals, ps, previousAnimals, turn);
        } else {
            command = mDistraction.getCommand(members, animals, previousAnimals, ps, true);
        }
        // Check if there is random player
        if (turn < 10) {
            for (int i = 0; i < members.size(); i++) {
                if(i == this.id) {
                    continue;
                }
                ArrayList<Point> onePlayerMovement = playerMovement.get(i);
                Point currentMember = members.get(i).get_location();
                Point previousMember = previousMembers.get(i).get_location();
                Point movement = PointUtilities.substract(currentMember, previousMember);
                if(!onePlayerMovement.contains(movement)){
                    onePlayerMovement.add(movement);
                }
            }
        }
        if (turn == 10) {
            for (ArrayList<Point> onePlayerMovement: playerMovement) {
                //System.out.println(onePlayerMovement.size());
                if(onePlayerMovement.size() > 9) {
                    thereIsRandomPlayer = true;
                }
            }
        }
        // Record things for the next turn
        previousAnimals = animals;
        previousMembers = members;
        turn++;
        return command;
    }
}