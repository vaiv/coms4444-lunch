package lunch.sim;

import lunch.sim.FoodType;

public class Food {

	private FoodType item;
	private Integer remaining_time;
	private Integer value;
	private boolean succ_eaten;
	private boolean taken_away;

	public Food(FoodType it)
	{
		item = it;
		switch(it)
		{
			case SANDWICH1:
			case SANDWICH2: remaining_time = 180;
							 value = 3;
							 break;
			case FRUIT1: 
			case FRUIT2: remaining_time = 120;
						  value = 2;
						  break;
			case EGG: remaining_time = 120;
					  value = 2;
					  break;
			case COOKIE: remaining_time = 60;
						 value = 4;
						 break;
			default: Log.record("invalid food type encountered!");

		}

	}

	public Food(Food f)
	{
		if(f==null)
			return;
		this.item = f.get_food_type();
		this.remaining_time = f.get_rem_time();
		this.value = f.get_value();
		this.succ_eaten = f.check_succ_eaten();
		this.taken_away = f.check_stolen();
	}

	public boolean eat()
	{
		if(!taken_away && remaining_time>=1)
			remaining_time--;

		if(remaining_time==0)
			{
				succ_eaten = true;
			}

		return succ_eaten;

	}

	public void stolen()
	{
		taken_away = true;
	}

	public boolean check_stolen()
	{
		return taken_away;
	}

	public boolean check_succ_eaten()
	{
		return succ_eaten;
	}

	public Integer get_rem_time()
	{
		return remaining_time;
	}

	public boolean check_available()
	{
		return !(succ_eaten ^ taken_away);
	}

	public Integer get_value()
	{
		return value;
	}

	public FoodType get_food_type()
	{
		return item;
	}



}