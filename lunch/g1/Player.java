package lunch.g1;

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

import java.lang.Math;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;

    Double min_dist = Double.MAX_VALUE;

    private Point corner_direction = new Point(0, 0);
	
	{
		turn = 0;
	}

	public void init(ArrayList<Family> members, Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		random = new Random(s);
	}

	public Command getCommand( ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps ) {
        turn++;

        // make one random move (to get away from initial point 0,0)
        if (turn == 1) {
            Point next_move = getRandomMove(ps);
            return Command.createMoveCommand(next_move);
        }
        
        // move towards corner
        if (turn < 70) {
            // find and store direction to go towards corner
            if (turn == 2) setCornerDirection(members);
            
            Point current_location = ps.get_location();
            Point next_move = new Point(current_location.x + corner_direction.x, current_location.y + corner_direction.y);
            return Command.createMoveCommand(next_move);
        }


		Double min_dist = Double.MAX_VALUE;

		for(Integer i=0;i<animals.size();i++)
		{
			min_dist = Math.min(min_dist,Point.dist(ps.get_location(),animals.get(i).get_location()));
		}

/*
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
*/

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

    private Point getFamilyMean( ArrayList<Family> members ) {
        int numMembers = members.size();
        Double sum_x_coord = 0.,
               sum_y_coord = 0.;
        
        for (Family member : members) {
            Point location = member.get_location();
            sum_x_coord += location.x;
            sum_y_coord += location.y;
        }
        Point mean = new Point(sum_x_coord /numMembers, sum_y_coord /numMembers);
        
        return mean;
    }
    
    private void setCornerDirection( ArrayList<Family> members ) {
        final double absolute_corner_coordinate = 50;
        final Point[] corners = { new Point(-absolute_corner_coordinate,  absolute_corner_coordinate),
                                  new Point( absolute_corner_coordinate,  absolute_corner_coordinate),
                                  new Point( absolute_corner_coordinate, -absolute_corner_coordinate),
                                  new Point(-absolute_corner_coordinate, -absolute_corner_coordinate) };

        // initialize distance array (used for keeping track of distances and if a member is already assigned to a corner)
        boolean[] isUnassigned = new boolean[members.size()];
        for (int i = 0; i < isUnassigned.length; i++) isUnassigned[i] = true;

        for (int i = 0; i < members.size(); i++) {
            // cycle through corners for all family members
            Point corner = corners[i % 4];

            // start the close-distance threshold at a large number
            double closest_distance = absolute_corner_coordinate *3;
            int closest_member_index = -1;

            // find which member is closest to this corner (if me, I'll move; otherwise, I'm just looking)
            if (isUnassigned[i]) {
                Point member_location = members.get(i).get_location();
                double distance_x = member_location.x - corner.x,
                       distance_y = member_location.y - corner.y;
                double distance = Math.sqrt(distance_x*distance_x + distance_y*distance_y);
                
                // find the closest member
                if (distance < closest_distance) {
                    closest_distance = distance;
                    closest_member_index = i;
                }
            }
            isUnassigned[closest_member_index] = false;

            // if I was that closest unassigned member, move me to that closest corner
            if (closest_member_index == id) {
                Point member_location = members.get(id).get_location();
                double direction_x = corner.x - member_location.x,
                       direction_y = corner.y - member_location.y;
                double magnitude = Math.sqrt(direction_x*direction_x + direction_y*direction_y);
                // mormalize direction vector and turn into Point
                corner_direction = new Point(direction_x /magnitude, direction_y /magnitude);
                break;
            }
        }
    }
    
    private Point getRandomMove( PlayerState ps ) {
        boolean found_valid_move= false;
		Point next_move = new Point(-1,-1);
		while(!found_valid_move)
		{
			Double bearing = random.nextDouble()*2*Math.PI;
			next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
			found_valid_move = Point.within_bounds(next_move);
		}
		// System.out.println("move command issued");
		return next_move;
    }
}
