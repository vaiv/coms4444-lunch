package lunch.sim;

import lunch.sim.Family;
import lunch.sim.Point;
import lunch.sim.Food;
import lunch.sim.FoodType;
import lunch.sim.Agent;


public class PlayerState extends Family
{
	private Food held_food_item;
	private boolean is_looking;
	private boolean is_visible;
	private Integer rem_wait_time;

	private boolean sandwich1_flag, sandwich2_flag, fruit1_flag, fruit2_flag, egg_flag, cookie_flag;
	private Integer sandwich1_time, sandwich2_time, fruit1_time, fruit2_time, egg_time, cookie_time;


	public PlayerState(Agent p)
	{
		super(p);
		held_food_item = p.get_held_item();
		is_looking = p.is_waiting();
		is_visible = p.visible_item();
		rem_wait_time = p.get_remaining_time(); 

		sandwich1_flag = p.check_available_item(FoodType.SANDWICH1);
		sandwich2_flag = p.check_available_item(FoodType.SANDWICH2);
		fruit1_flag = p.check_available_item(FoodType.FRUIT1);
		fruit2_flag = p.check_available_item(FoodType.FRUIT2);
		egg_flag =  p.check_available_item(FoodType.EGG);
		cookie_flag  = p.check_available_item(FoodType.COOKIE);


		sandwich1_time = p.get_rem_time_item(FoodType.SANDWICH1);
		sandwich2_time = p.get_rem_time_item(FoodType.SANDWICH2);
		fruit1_time = p.get_rem_time_item(FoodType.FRUIT1);
		fruit2_time = p.get_rem_time_item(FoodType.FRUIT2);
		egg_time = p.get_rem_time_item(FoodType.EGG);
		cookie_time = p.get_rem_time_item(FoodType.COOKIE);

	}

	public Integer time_to_eat_remaining()
	{
		if(held_food_item==null)
			return -1;
		return held_food_item.get_rem_time();
	}

	public boolean is_player_searching()
	{
		return is_looking;
	}

	public Integer time_to_finish_search()
	{
		return rem_wait_time;
	}

	public Integer get_time_for_item(FoodType item_type)
	{
		switch (item_type)
		{
			case SANDWICH1: return sandwich1_time;
			case SANDWICH2: return sandwich2_time;
			case FRUIT1: return fruit1_time;
			case FRUIT2: return fruit2_time;
			case EGG: return egg_time;
			case COOKIE: return cookie_time;
			default: return -1;
		}

	}

	public boolean check_availability_item(FoodType item_type)
	{
		switch (item_type)
		{
			case SANDWICH1: return sandwich1_flag;
			case SANDWICH2: return sandwich2_flag;
			case FRUIT1: return fruit1_flag;
			case FRUIT2: return fruit2_flag;
			case EGG: return egg_flag;
			case COOKIE: return cookie_flag;
			default: return false;
		}
	}

}