package lunch.sim;

import lunch.sim.Command;
import lunch.sim.CommandType;
import java.util.List;
import java.util.ArrayList;
import lunch.sim.PlayerState;

public interface Player {
    // Initialization function.
    // members: other family members collborating with your player.
    // id: player id
    // f: Number of family members.
    // animals: list of animals
    // m: number of monkeys
    // g: number of geese 
    // t: Time limit for simulation.
    // s: seed for random 
    public String init(ArrayList<Family> members,Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s);

    // Gets the moves from the player. Number of moves is specified by first parameter.
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps);
}