package lunch.sim;

import lunch.sim.Point;
import lunch.sim.FoodType;
import lunch.sim.Agent;
import lunch.sim.Food;

public class Family_Old{

protected Point location;
protected FoodType item;
protected Integer ID;
protected boolean item_visible;

public Family(Agent p)
{
	location = new Point(p.get_location());
	item_visible = false;

	ID = p.get_id();
	if(p.get_held_item()!=null && p.get_held_item().get_food_type()!=null )
	{
		item_visible = p.visible_item();
		switch(p.get_held_item().get_food_type())
		{
			case SANDWICH1:
			case SANDWICH2:
			case SANDWICH:   item = FoodType.SANDWICH;
									  break;
			case FRUIT1:
			case FRUIT2:
			case FRUIT: item = FoodType.FRUIT;
								 break;
			case EGG: item = FoodType.EGG;
							   break;
			case COOKIE: item = FoodType.COOKIE;
								  break;
			default: item = null;

		}
	}
}

public FoodType get_held_item_type()
{
	return item;
}

public Point get_location()
{
	return location;
}

public boolean check_visible_item()
{
	return item_visible;
}

// public void update_location(Point p)
// {
// 	location.x = p.x;
// 	location.y = p.y;
// }

// public void update_held_item(FoodType f)
// {
// 	item = f;
// }

}
