package lunch.g5;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair;
import java.util.ArrayList;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.Log;
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;

	private DistractionStrategy mDistraction;
	public Player() {
		turn = 0;
	}

	public String init(
		ArrayList<Family> members, Integer id, int f,
		ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
		this.id   = id;
		this.seed = s;

		this.random = new Random(this.seed);

		this.mDistraction = new DistractionStrategy();

		mDistraction.init(members, id, f, animals, m, g, t, s);
		return "guardians";
	}

	public Command getCommand(
		ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
		
		return mDistraction.getCommand(members, animals, ps);
	}


}
