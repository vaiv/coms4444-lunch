package lunch.sim;

import lunch.sim.Point;
import lunch.sim.FoodType;
import lunch.sim.Agent;
import lunch.sim.Food;

public class Family{

protected Point location;
protected FoodType item;
protected Integer ID;

public Family(Agent p)
{
	location = new Point(p.get_location());

	ID = p.get_id();
	if(p.get_held_item()!=null && p.get_held_item().get_food_type()!=null )
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

public FoodType get_held_item_type()
{
	return item;
}

public Point get_location()
{
	return location;
}

public Integer get_id(){
	return this.ID; 
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