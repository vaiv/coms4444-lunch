package lunch.sim;

import lunch.sim.Point;
import lunch.sim.FoodType;
import lunch.sim.AnimalType;
import lunch.sim.Monkey;

public class Goose extends Monkey
{
	private Point nest_location;
	public Goose(Integer seed)
	{
		super(seed);
		this.speed = 3.0;
		this.nest_location = new Point(55,55);
		this.animal = AnimalType.GOOSE;
	}

	@Override
	public void next_move(Point target)
	{
		
		if(target==null)
		{
			boolean found_valid_move = false;
			while(!found_valid_move)
			{
				Point next_loc = new Point(location.x+speed*Math.cos(bearing), location.y + speed*Math.sin(bearing));
				if(Point.within_bounds(next_loc))
				{
					location = next_loc;
					found_valid_move = true;
					return;
				}
				else
				{
					bearing = generator.nextDouble()*2*Math.PI;
				}

			}	

		}
		else if(target.equals(nest_location))
		{
			Double curr_dist = Point.dist(location, target);
			bearing = Math.atan2((target.y - location.y),(target.x-location.x));

			if(curr_dist>speed)
			{
				location.x += speed*Math.cos(bearing);
				location.y+= speed*Math.sin(bearing);
			}
		}
		else
		{
			Double curr_dist = Point.dist(location, target);
			bearing = Math.atan2((target.y - location.y),(target.x-location.x));

			if(curr_dist>3.0)
			{
				location.x += speed*Math.cos(bearing);
				location.y+= speed*Math.sin(bearing);
			} 
			
			else
			{
				Point next_loc = new Point(location.x+ (3.0-curr_dist)*Math.cos(bearing), location.y+ (3.0-curr_dist)*Math.sin(bearing));
				Point next_loc2 = new Point(location.x+ (3.0-curr_dist)*Math.cos(bearing+Math.PI/2), location.y + (3.0-curr_dist)*Math.sin(bearing+Math.PI/2));
				Point next_loc3 = new Point(location.x+ (3.0-curr_dist)*Math.cos(bearing-Math.PI/2), location.y + (3.0-curr_dist)*Math.sin(bearing-Math.PI/2));
				if(Point.within_bounds(next_loc))
				{
					location = next_loc;
				}
				else if (Point.within_bounds(next_loc2))
				{
					location = next_loc2;
				}
				else if (Point.within_bounds(next_loc3))
				{
					location = next_loc3;
				}
				else
				{
					Log.record("Cornered goose, staying put!");
				}
			}
		}
	}

	public Point get_nest_location()
	{
		return nest_location;
	}


}
