package lunch.sim;

import lunch.sim.Food;
import lunch.sim.FoodType;
import lunch.sim.Point;

public class Agent{

	private boolean is_looking;
	private Integer rem_wait_time;
	private Food held_item;
	private FoodType selected_food_item;
	private boolean is_visible;
	private Integer score;
	private Point location;
	private Integer ID;

	private Food sandwich1, sandwich2, fruit1, fruit2, egg, cookie;

	public Agent(Integer i)
	{
		sandwich1 = new Food(FoodType.SANDWICH1);
		sandwich2 = new Food(FoodType.SANDWICH2);
		fruit1 = new Food(FoodType.FRUIT1);
		fruit2 = new Food(FoodType.FRUIT2);
		egg = new Food(FoodType.EGG);
		cookie = new Food(FoodType.COOKIE);
		location = new Point(0,0);

		held_item= null;
		selected_food_item = null;
		is_visible = false;
		is_looking = false;
		rem_wait_time = 0;
		ID = i;	
		score = 0;

	}


	public void moveAgent(Point new_location)
	{
		if(Point.dist(this.location,new_location)<=(1 + 1e-7) && Point.within_bounds(new_location))
			this.location = new_location;
		else
			Log.record("the location specified in move command is invalid.");
	}


	public boolean retrieve_item(FoodType food)
	{
		switch(food)
		{
			case SANDWICH1: held_item = sandwich1;
							 break;
			case SANDWICH2: held_item = sandwich2;
							 break;

			case FRUIT1: held_item = fruit1;
							 break;
			case FRUIT2: held_item = fruit2;
							 break;
			case EGG: held_item = egg;
							 break;
			case COOKIE: held_item = cookie;
							 break;
			default: Log.record("unidentified food item requested!");
		}

		if (held_item!=null) 
			{
				is_visible = true;
				is_looking = false;
				selected_food_item = null;
				return true;
			}

		return false;
	}

	public void keep_back_item()
	{
		if(held_item==null) return;

		is_visible = false;
		held_item = null;
		is_looking = false;
	}

	public boolean eat_item()
	{
		if(held_item==null) return false;
			held_item.eat();
		if(held_item.check_succ_eaten())
			{
				score += held_item.get_value(); 
				held_item = null;
			}
		return held_item.check_succ_eaten();
	}

	public Point get_location()
	{
		return location;
	}

	public Food get_held_item()
	{
		return held_item;
	}

	public Integer get_id()
	{
		return ID;
	}

	public boolean is_waiting()
	{
		return is_looking;
	}

	public Integer get_remaining_time()
	{
		return rem_wait_time;
	}

	public void update_remaining_time()
	{
		if(rem_wait_time>0)
			rem_wait_time--;
		if(rem_wait_time.equals(0))
		{
			is_looking = false;
		}
	}

	public FoodType get_food_request()
	{
		return selected_food_item;
	}

	public void set_food_request(FoodType food_item)
	{
		selected_food_item = food_item;
	}

	public void start_wait()
	{
		is_looking = true;
		rem_wait_time = 10;
	}

	public void stolen()
	{
		if(held_item!=null)
		{
			held_item.stolen();
		}
		held_item = null;
	}

	public void hide()
	{
		is_visible = false;
	}

	public boolean visible_item()
	{
		return is_visible;
	}

	public void make_visible()
	{
		is_visible = true;
	}

	public void reset_wait_time()
	{
		rem_wait_time = 0;
	}

	public void stop_looking()
	{
		is_looking = false;
	}

	public Integer get_score()
	{
		return score;
	}

	public boolean check_available_item(FoodType food_type)
	{
		switch (food_type)
		{
			case SANDWICH1: return sandwich1.check_available();
			case SANDWICH2: return sandwich2.check_available();
			case FRUIT1: return fruit1.check_available();
			case FRUIT2: return fruit2.check_available();
			case EGG: return egg.check_available();
			case COOKIE: return cookie.check_available();
			default: return false;
		}
	}

	public Integer get_rem_time_item(FoodType food_type)
	{
		switch (food_type)
		{
			case SANDWICH1: return sandwich1.get_rem_time();
			case SANDWICH2: return sandwich2.get_rem_time();
			case FRUIT1: return fruit1.get_rem_time();
			case FRUIT2: return fruit2.get_rem_time();
			case EGG: return egg.get_rem_time();
			case COOKIE: return cookie.get_rem_time();
			default: return -1;
		}
	}

	public boolean is_done()
	{
		return !(sandwich1.check_available()||sandwich2.check_available()||fruit1.check_available()||fruit2.check_available()||egg.check_available()||cookie.check_available());
	}


}