package lunch.g1;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import javafx.util.Pair; 
import java.util.ArrayList;
import java.util.*;

import lunch.sim.Point;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Animal;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.AnimalType;
import lunch.sim.PlayerState;

import java.lang.Math;

public class Player implements lunch.sim.Player
{
	private int seed;
	private Random random;
	private Integer id;
	private Integer turn;
	private String avatars;
    private double t;
    private Integer num_monkey;
    private Integer num_geese;

	Double eps = 10e-7;

    List<Point> animalLocations;
    List<Boolean> animalMovement;
	List<Double> animalDirections;

	List<FoodType> priorityList;

    private Point corner_direction = new Point(0, 0);
	
	{
		turn = 0;
	}

	public String init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
	{
		this.id = id;
		this.t = t;
		this.animalLocations = new ArrayList<>();
		this.animalMovement = new ArrayList<>();
		this.animalDirections = new ArrayList<>();
		for(Animal animal : animals){
			animalLocations.add(animal.get_location());
			animalMovement.add(false);
			animalDirections.add(0.0);
		}
		this.priorityList = getPriority();
//		this.animals = animals;
		this.num_monkey = m;
		this.num_geese = g;
		avatars = "flintstone";
		random = new Random(s);
		return avatars;
	}


	public ArrayList<FoodType> getPriority() {
		   ArrayList<FoodType> priorityList = new ArrayList<>();
		   // keep order that most family members are keeping
		   // time left: 1674
           priorityList.add(FoodType.COOKIE);
           priorityList.add(FoodType.FRUIT1);
           priorityList.add(FoodType.FRUIT2);
           priorityList.add(FoodType.EGG);
           priorityList.add(FoodType.SANDWICH1);
           priorityList.add(FoodType.SANDWICH2);


		   // time left: 1552
//		   priorityList.add(FoodType.COOKIE);
//		   priorityList.add(FoodType.FRUIT1);
//		   priorityList.add(FoodType.FRUIT2);
//		   priorityList.add(FoodType.SANDWICH1);
//		   priorityList.add(FoodType.SANDWICH2);
//		   priorityList.add(FoodType.EGG);

//		   System.out.println(t);
		//if time is less than half an hour
//		   if (t <= 30 *60 ) {
//               // time left: 1656
//               priorityList.add(FoodType.COOKIE);
//               priorityList.add(FoodType.FRUIT1);
//               priorityList.add(FoodType.FRUIT2);
//               priorityList.add(FoodType.EGG);
//               priorityList.add(FoodType.SANDWICH1);
//               priorityList.add(FoodType.SANDWICH2);
//		   }

               // time left: 1656
			   //check the number of Monkeys and geese, if moderate than distract
//			   if (num_monkey >= 50 && num_geese >= 30){
//				   priorityList.add(FoodType.COOKIE);
//				   priorityList.add(FoodType.FRUIT1);
//				   priorityList.add(FoodType.FRUIT2);
//				   priorityList.add(FoodType.EGG);
//				   priorityList.add(FoodType.SANDWICH1);
//				   priorityList.add(FoodType.SANDWICH2);
//              }
//		   else{
//				   priorityList.add(FoodType.COOKIE);
//				   priorityList.add(FoodType.SANDWICH1);
//				   priorityList.add(FoodType.SANDWICH2);
//				   priorityList.add(FoodType.FRUIT1);
//				   priorityList.add(FoodType.FRUIT2);
//				   priorityList.add(FoodType.EGG);
//			   }

		   return priorityList;

	}

	//check if eating is in risk
	//if a third Monkey is coming near
	public boolean checkMonkey(ArrayList<Animal> animals, PlayerState ps){
		int num_monkey_close = 0;
		for (Animal animal: animals){
			if (animal.which_animal() ==AnimalType.MONKEY && Point.dist(ps.get_location(),animal.get_location()) < 6 ){
				num_monkey_close++;
			}
		}
		if (num_monkey_close>=3){
			return true;}
		return false;
	}

	public boolean checkMonkeyAway(ArrayList<Animal> animals, PlayerState ps){
		int num_monkey_away = 0;
		for (int i = 0; i < animals.size(); i++){
			Animal animal = animals.get(i);
			if (animal.which_animal() ==AnimalType.MONKEY && Point.dist(ps.get_location(),animal.get_location()) < 32.0 ){
			//	System.out.print("check monkey away ");
			//	System.out.println(animalMovement);
				if(animalMovement.get(i)){
					num_monkey_away += 1;
					System.out.println(num_monkey_away);
				}
			}
		}
		if (num_monkey_away>=3){
			return true;}
		return false;
	}



	//if there is goose less than 3 meters away DANGER!
	public boolean checkGeese(ArrayList<Animal> animals, PlayerState ps){
		double dist;
		for(Animal animal: animals){
			//only matter if eating sandwich
			if (animal.which_animal() == AnimalType.GOOSE &&
					ps.get_held_item_type() == FoodType.SANDWICH ){
				dist = Point.dist(ps.get_location(),animal.get_location());
				if (dist < 6.5){
//					System.out.println("goose detected");
					return true;
				}
			}
		}
		return false;
	}

	public Command getCommand( ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps ) {
        turn++;
		this.getAnimalMovement(animals, ps);
        // make one random move (to get away from initial point 0,0)
        if (turn == 1) {
            //Point next_move = getRandomMove(ps);
            Point next_move = getRandomCornerMove(ps);
            return Command.createMoveCommand(next_move);
        }
        
        // move towards corner
        //if (turn < 70 && id != members.size() - 1) {
        if (turn <= 71) {
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

		FoodType foodType = ps.check_availability_item(FoodType.COOKIE) ? FoodType.COOKIE :
				ps.check_availability_item(FoodType.FRUIT1) ? FoodType.FRUIT1 :
						ps.check_availability_item(FoodType.FRUIT2) ? FoodType.FRUIT2 :
								ps.check_availability_item(FoodType.EGG) ? FoodType.EGG :
										ps.check_availability_item(FoodType.SANDWICH1) ? FoodType.SANDWICH1 :
												ps.check_availability_item(FoodType.SANDWICH2) ? FoodType.SANDWICH2	:
														null;

//		System.out.println(min_dist);
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
		if(min_dist < 4.0 && ps.is_player_searching() && ps.get_held_item_type()==null)
		{
			// System.out.println("abort command issued");
			// System.out.println(min_dist.toString());
			return new Command(CommandType.ABORT);
		}
		// keep food item back if animal is too close
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null && min_dist < 4.0 &&
				(checkMonkey(animals, ps) || checkGeese(animals,ps)))
		{
			return new Command(CommandType.KEEP_BACK);
		}
		// move away from animal 
//		else if(min_dist<3.0)
//		{
//			boolean found_valid_move= false;
//			Point next_move = new Point(-1,-1);
//			while(!found_valid_move)
//			{
//				Double bearing = random.nextDouble()*2*Math.PI;
//				next_move = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
//				found_valid_move = Point.within_bounds(next_move);
//			}
//			return Command.createMoveCommand(next_move);
//
//		}


		// if no animal is near then take out food
		else if (!ps.is_player_searching() &&  min_dist>=5 && ps.get_held_item_type()==null )
		{
			if(checkMonkeyAway(animals, ps) && (priorityList.size() > 2 || this.num_geese <= 30)) return new Command(CommandType.KEEP_BACK);
			for(FoodType food_type: priorityList)
			{
				if(ps.check_availability_item(food_type))
				{
					Command c = new Command(CommandType.TAKE_OUT, food_type);
					return c;
				}
				priorityList.remove(food_type);
			}
		}
		// if no animal in vicinity then take a bite
		//distract if we have 1 second of SANDWICH2 left
		else if(!ps.is_player_searching() && ps.get_held_item_type()!=null)
		{
			if(t-turn > 200 && foodType == FoodType.SANDWICH2 && ps.get_time_for_item(foodType) == 1){
				return new Command(CommandType.WAIT);
			}
			else if (t-turn < 500  && foodType == FoodType.SANDWICH2 && ps.get_time_for_item(foodType) == 1){
				return new Command(CommandType.EAT);}
			else{
				return new Command(CommandType.EAT);
			}
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

        // all players (except for last (-1)) will move to a corner
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
    
    private Point getRandomCornerMove( PlayerState ps ) {
        boolean found_valid_move= false;
		Point next_move = new Point(-1,-1);
		while(!found_valid_move)
		{
		    
			next_move = new Point(ps.get_location().x + getOneOrNegOne(), ps.get_location().y + getOneOrNegOne());
			found_valid_move = Point.within_bounds(next_move);
		}
		// System.out.println("move command issued");
		return next_move;
    }
    
    public double getOneOrNegOne() {
        Random random = new Random();
        boolean isOne = random.nextBoolean();
        if (isOne) return 1.;
        else return -1.;
    }

	private double getAngle(Point oldPoint, Point newPoint){
		double xDelt = newPoint.x - oldPoint.x;
		double yDelt = -1 * (newPoint.y - oldPoint.y);
		double theta;
		if( yDelt == 0 && xDelt == 0) theta = 42;
		else if(yDelt == 0 && xDelt > 0) theta = 0;
		else if(yDelt == 0 && xDelt < 0) theta = Math.PI;
		else if(yDelt > 0 && xDelt == 0) theta = Math.PI / 2;
		else if(yDelt < 0 && xDelt == 0) theta = - Math.PI / 2;
		else{
			theta = Math.atan(yDelt / xDelt);
			if(yDelt > 0.0 && xDelt < 0.0) theta = Math.PI + theta;
			else if(yDelt < 0.0 && xDelt < 0.0) theta = -Math.PI + theta;
		}

		return theta;
	}

	private void getAnimalMovement( ArrayList<Animal> animals, PlayerState ps){
		int ind = 0;
		for(Animal animal : animals){
			Point oldPoint = animalLocations.get(ind);
			Point newPoint = animal.get_location();
			double oldDist = Point.dist(oldPoint, ps.get_location());
			double newDist = Point.dist(newPoint, ps.get_location());
			animalMovement.set(ind, newDist > oldDist);
			animalLocations.set(ind, newPoint);
			double theta = getAngle(oldPoint, newPoint);
			//System.out.println(theta);
			animalDirections.set(ind, theta);
			ind++;
		}
		//System.out.print("get movement ");
		//System.out.println(animalMovement);
	}
}
