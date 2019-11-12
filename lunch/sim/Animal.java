package lunch.sim;

import lunch.sim.Monkey;
import lunch.sim.Goose;
import lunch.sim.AnimalType;
import lunch.sim.Point;

public class Animal{

	private Point location;
	private AnimalType animal;
	private double animal_max_speed;
	private boolean is_eating;

	public Animal(Monkey m)
	{
		location = new Point(m.get_location());
		animal = AnimalType.MONKEY;
		animal_max_speed = m.get_max_speed();
		is_eating = m.busy_eating();
	}

	public Animal(Goose g)
	{
		location = new Point(g.get_location());
		animal = AnimalType.GOOSE;
		animal_max_speed = g.get_max_speed();
		is_eating = g.busy_eating();
	}

	public Point get_location()
	{
		return location;
	}

	public AnimalType which_animal()
	{
		return animal;
	}

	public double get_max_speed()
	{
		return animal_max_speed;
	}

	public boolean busy_eating()
	{
		return is_eating;
	}
}

