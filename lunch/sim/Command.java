package lunch.sim;
import lunch.sim.Point;
import lunch.sim.CommandType;
import lunch.sim.FoodType;

public class Command {
    private CommandType type;
    private Point location;
    private FoodType food_type;
    
    public Command(CommandType type, Point location)
    {
        this.type = type;
        this.location = location;
        this.food_type = null;
    }

     public Command(CommandType type, FoodType food_type)
    {
        this.type = type;
        this.location = null;
        this.food_type = food_type;
    }
    
    public Command(CommandType type)
    {
        this.type = type;
        this.location = null;
        this.food_type = null;
    }

    public Command(){
        this.type = null;
        this.location = null;
        this.food_type = null;
    }
    
    public static Command createMoveCommand(Point location)
    {
        return new Command(CommandType.MOVE_TO, new Point(location.x, location.y));
    }

    public static Command createRetrieveCommand(FoodType food)
    {
        return new Command(CommandType.TAKE_OUT, food);
    }
    
    public Point get_location() {
        return location;
    }
    
    public CommandType get_type() {
        return type;
    }

    public FoodType get_food_type()
    {
    	return food_type;
    }
}