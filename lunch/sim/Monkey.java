package lunch.sim;

import lunch.sim.Point;
import lunch.sim.FoodType;
import lunch.sim.AnimalType;
import java.util.Random;

public class Monkey
{
	protected double speed;
	protected double bearing;
	protected boolean is_eating;
	protected Integer wait_time;
	protected Point location;
	protected Random generator;
	protected FoodType stolen_item;
	protected AnimalType animal;

	public Monkey(Integer seed)
	{
		generator = new Random(seed);
		is_eating = false;
		speed = 1.0;
		bearing = generator.nextDouble()*2*Math.PI;
		location = new Point(getRandomDoubleBetweenRange(-50.0,50.0),getRandomDoubleBetweenRange(-50.0,50.0));
		wait_time = 0;
		animal = AnimalType.MONKEY;

	}

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
		else
		{
			Double curr_dist = Point.dist(location, target);
			bearing = Math.atan2((target.y - location.y),(target.x-location.x));

			if(curr_dist>3.0)
			{
				location.x += speed*Math.cos(bearing);
				location.y+= speed*Math.sin(bearing);
			} 
			else if(curr_dist>=2.0)
			{
				location.x += (curr_dist-2.0)*Math.cos(bearing);
				location.y += (curr_dist-2.0)*Math.sin(bearing);
			}
			else
			{
				Point next_loc = new Point(location.x- (2.0-curr_dist)*Math.cos(bearing), location.y - (2.0-curr_dist)*Math.sin(bearing));
				Point next_loc2 = new Point(location.x- (2.0-curr_dist)*Math.cos(bearing+Math.PI/2), location.y - (2.0-curr_dist)*Math.sin(bearing+Math.PI/2));
				Point next_loc3 = new Point(location.x- (2.0-curr_dist)*Math.cos(bearing-Math.PI/2), location.y - (2.0-curr_dist)*Math.sin(bearing-Math.PI/2));
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
					Log.record("Cornered monkey, staying put!");
				}
			}
		}
	}

	public boolean busy_eating()
	{
		return is_eating;
	}

	public Integer get_rem_time()
	{
		return wait_time;
	}

	public void update_rem_time()
	{
		if(wait_time>0)
			wait_time--;
		if(wait_time.equals(0))
			{
				is_eating=false;
				stolen_item = null;
			}

	}

	public void steal(FoodType item)
	{
		wait_time = 30;
		is_eating = true;
		stolen_item = item;
	}

	public FoodType check_stolen_item()
	{
		return stolen_item;
	}

	public Point get_location()
	{
		return location;
	}

	public double get_max_speed()
	{
		return speed;
	}


	public double getRandomDoubleBetweenRange(double min, double max)
	{
    	double x = (generator.nextDouble()*((max-min)+1))+min;
    	return x;
	}


}