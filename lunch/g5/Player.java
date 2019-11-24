package lunch.g5;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.ArrayList;

import lunch.sim.*;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;

    MatrixPredictor matrixPredictor;
    
    // An array to store the animals in previous turn (Mainly to know their positions, so we know where they are going)
    private ArrayList<Animal> previousAnimals;
    private GreedyEater greedyEater;

	private DistractionStrategy mDistraction;

    public Player() {
        turn = 0;
        matrixPredictor = new MatrixPredictor(5.0, 6.0, 0);
	}

	public String init(
		ArrayList<Family> members, Integer id, int f,
		ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id   = id;
		this.seed = s;

		this.random = new Random(this.seed);

        this.greedyEater  = new GreedyEater();
		this.mDistraction = new DistractionStrategy();

		mDistraction.init(members, id, f, animals, m, g, t, s);
		return "guardians";
	}

	public Command getCommand(
        ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
    {
        Command command = greedyEater.getCommandCornerEating(
            members, animals, ps, previousAnimals, turn);
        System.out.println(command.get_type());
        previousAnimals = animals;
        turn++;
        return command;

		// return mDistraction.getCommand(members, animals, ps);
	}
}