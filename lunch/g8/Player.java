package lunch.g8;

import java.util.ArrayList;
import java.util.Random;
import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;

/**
 *
 * @author group8
 */
public class Player implements lunch.sim.Player {

    private int id;
    private Random random;
    protected final List<FamilyMember> family;
    protected final List<Animal> animals;
    protected PlayerState state;
    private Strategy strategy;
    private int totalTime;

    public Player() {
        family = new ArrayList<>();
        animals = new ArrayList<>();
    }

    /**
     *
     * @param members family members
     * @param id id of the member represented by this player
     * @param f number of family members
     * @param animals list of animals in the simulation
     * @param m number of monkeys
     * @param g number of geese
     * @param t number of turns
     * @param s seed for random number generation purposes
     * @return a family avatar folder name
     */
    @Override
    public String init(
            ArrayList<lunch.sim.Family> members,
            Integer id,
            int f,
            ArrayList<lunch.sim.Animal> animals,
            Integer m,
            Integer g,
            double t,
            Integer s) {
        this.id = id;
        this.totalTime = (int)t;
        random = new Random(s);
        for (int i = 0; i < members.size(); i++) {
            family.add(new FamilyMember(members.get(i), i == id, i));
        }
        for (lunch.sim.Animal ani : animals) {
            this.animals.add(new Animal(ani));
        }
        return "";
    }

    @Override
    public Command getCommand(
            ArrayList<lunch.sim.Family> members,
            ArrayList<lunch.sim.Animal> simAnimals,
            lunch.sim.PlayerState ps) {
        // finish initialization of elements that required some player state
        if (state == null) {
            state = new PlayerState(id, ps, totalTime);
        }
        if (strategy == null) {
            strategy = selectStrategy();
        }

        // update game information
        update(members, simAnimals, ps);

        // execute strategy to obtain a command
        Command command;
        try {
            command = strategy.run();
        } catch (AbortStrategyException ex) {
            // change strategies
            command = new Command();
        }

        // record the type of food we are taking out
        if (command.get_type() == CommandType.TAKE_OUT) {
            state.setFoodSearched(command.get_food_type());
        }

        // increase turn counter and return command
        state.tick();
        if (state.getTurn() > 150) {
            // stop detecting random players after this turn
            FamilyMember.RANDOM_PLAYER_DETECTION = false;
        }
        //System.out.println(describe(command));
        return command;
    }

    /**
     * Updates the game information based on the objects passed by the simulator
     *
     * @param members
     * @param simAnimals
     * @param ps
     */
    protected void update(
            ArrayList<lunch.sim.Family> members,
            ArrayList<lunch.sim.Animal> simAnimals,
            lunch.sim.PlayerState ps) {
        for (int i = 0; i < family.size(); i++) {
            FamilyMember fm = family.get(i);
            fm.update(members.get(i));
        }
        for (int i = 0; i < animals.size(); i++) {
            Animal a = animals.get(i);
            a.update(simAnimals.get(i));
        }
        state.update(ps);
    }

    /**
     * Returns the strategy that the player should execute next
     *
     * @return
     */
    private Strategy selectStrategy() {
        return new DistractIfNeededStrategy(family, animals, state, random);
        //return new MonkeyLureStrategy(family, animals, state, random);
    }

    private String describe(Command command) {
        return "P" + state.getId() + command.get_type();
    }

}
