package lunch.random;

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
import lunch.sim.PlayerState;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;

	public Player()
	{
		turn = 0;
	}

	public void init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		random = new Random(s);
	}

	public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
	{

		Double min_dist = Double.MAX_VALUE;

		for(Integer i=0;i<animals.size();i++)
		{
			min_dist = Math.min(min_dist,Point.dist(ps.get_location(),animals.get(i).get_location()));
		}

		if(turn<100)
		{
			boolean found_valid_move= false;
			Point next_move = new Point(-1,-1);
			while(!found_valid_move)
			{
				Double bearing = random.nextDouble()*2*Math.PI;
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			// System.out.println("move command issued");
			turn++;
			return Command.createMoveCommand(next_move);
		}

		// abort taking out if animal is too close
		if(min_dist<3.0 && ps.is_player_searching() && ps.get_held_item_type()==null)
		{
			// System.out.println("abort command issued");
			// System.out.println(min_dist.toString());
			return new Command(CommandType.ABORT);
		}
		// keep food item back if animal is too close
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null && min_dist<2.0)
		{
			return new Command(CommandType.KEEP_BACK);
		}
		// move away from animal 
		else if(min_dist<3.0)
		{
			boolean found_valid_move= false;
			Point next_move = new Point(-1,-1);
			while(!found_valid_move)
			{
				Double bearing = random.nextDouble()*2*Math.PI;
				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
				found_valid_move = Point.within_bounds(next_move);
			}
			return Command.createMoveCommand(next_move);
			
		}
		// if no animal is near then take out food
		else if (!ps.is_player_searching() &&  min_dist>=5 && ps.get_held_item_type()==null )
		{
			for(FoodType food_type: FoodType.values())
			{
				if(ps.check_availability_item(food_type))
				{
					Command c = new Command(CommandType.TAKE_OUT, food_type);
					return c;
				}
			}
		}
		// if no animal in vicinity then take a bite
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null)
		{
			return new Command(CommandType.EAT);
		}

		// System.out.println("player is searching");
		return new Command();

	}


}
