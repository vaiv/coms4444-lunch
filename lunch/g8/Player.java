package lunch.g8;

import java.util.ArrayList;
import java.util.Random;
import lunch.sim.Animal;
import lunch.sim.Command;
import lunch.sim.CommandType;
import lunch.sim.Family;
import lunch.sim.FoodType;
import lunch.sim.PlayerState;
import lunch.sim.Point;
import java.util.Collections;
import lunch.sim.AnimalType;

/**
 *
 * @author group8
 */
public class Player implements lunch.sim.Player {

    private Integer turn;
    private Random random;

    /**
     *
     * @param members family members
     * @param id id of the member represented by this player
     * @param f number of family members
     * @param animals list of animals in the simulation
     * @param m number of monkeys
     * @param g number of geese
     * @param t number of turns
     * @param s seed for random number generation purposes
     */
    @Override
    public void init(ArrayList<Family> members, Integer id, int f, ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s) {
        random = new Random(s);
        turn = 0;
    }

    @Override
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps) {
        Double minDist = Double.MAX_VALUE;

        for (Integer i = 0; i < animals.size(); i++) {
            minDist = Math.min(minDist, Point.dist(ps.get_location(), animals.get(i).get_location()));
        }

        if (turn < 100) {
            boolean validMoveFound = false;
            Point nextMove = new Point(-1, -1);
            while (!validMoveFound) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                validMoveFound = Point.within_bounds(nextMove);
            }
            // System.out.println("move command issued");
            turn++;
            return Command.createMoveCommand(nextMove);
        }

        // abort taking out if animal is too close
        if (minDist < 3.0 && ps.is_player_searching() && ps.get_held_item_type() == null) {
            // System.out.println("abort command issued");
            // System.out.println(min_dist.toString());
            return new Command(CommandType.ABORT);
        } // keep food item back if animal is too close
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null && minDist < 2.0) {
            return new Command(CommandType.KEEP_BACK);
        } // move away from animal 
        else if (minDist < 3.0) {
            boolean validMoveFound = false;
            Point nextMove = new Point(-1, -1);
            while (!validMoveFound) {
                Double bearing = random.nextDouble() * 2 * Math.PI;
                nextMove = new Point(ps.get_location().x + Math.cos(bearing), ps.get_location().y + Math.sin(bearing));
                validMoveFound = Point.within_bounds(nextMove);
            }
            return Command.createMoveCommand(nextMove);

        } // if no animal is near then take out food
        else if (!ps.is_player_searching() && minDist >= 5 && ps.get_held_item_type() == null) {
            ArrayList<FoodType> unorderedFood = new ArrayList<>();
            for (FoodType food_type : FoodType.values()) {
                if(ps.check_availability_item(food_type)){
                    unorderedFood.add(food_type);
                }
            }
            //now, order the food:
            ArrayList<FoodType> orderedFood = orderFood(unorderedFood);
            for(FoodType f : orderedFood){
                System.out.print(" " + f + " ");
            }
            //and then get the first element in the list
            return new Command(CommandType.TAKE_OUT, orderedFood.get(0));
            
        } // if no animal in vicinity then take a bite
        else if (!ps.is_player_searching() && ps.get_held_item_type() != null) {
            return new Command(CommandType.EAT);
        }

        // System.out.println("player is searching");
        return new Command();
    }

    public ArrayList<FoodType> orderFood(ArrayList<FoodType> unordered){
        ArrayList<FoodType> ordered = new ArrayList<FoodType>();
        ArrayList<Double> ord = new ArrayList<>();
        for(FoodType f : unordered){
            if(f == FoodType.SANDWICH1){
                ord.add(3.1);
            }
            else if(f == FoodType.SANDWICH2){
                ord.add(3.2);
            }
            else if(f == FoodType.COOKIE){
                ord.add(4.0);
            }
            else if(f == FoodType.FRUIT1){
                ord.add(2.1);
            }
            else if(f == FoodType.FRUIT2){
                ord.add(2.2);
            }
            else if (f == FoodType.EGG){ 
                ord.add(2.0);
            }
            else {
                ord.add(1.0); //should never happen
            }
        }
        Collections.sort(ord, Collections.reverseOrder());
        System.out.println("ordered ");
        for(Double d : ord){
            if(d == 3.1){ ordered.add(FoodType.SANDWICH1);}
            else if(d == 3.2){ ordered.add(FoodType.SANDWICH2);}
            else if(d == 4.0){ ordered.add(FoodType.COOKIE);}
            else if(d == 2.1){ ordered.add(FoodType.FRUIT1);}
            else if(d == 2.2){ ordered.add(FoodType.FRUIT2);}
            else if(d == 2.0){ ordered.add(FoodType.EGG);}
            else{
                System.out.println("There is an error - this food type is invalid");
            }
        }
        return ordered;
    }

    //helper function: if (1). 3 monkeys, dist <=2 or (2) goose dist<=2, return true indicating animals are 
    //dangerous and the input fmaily mamber should keep items back 
    public static boolean dangerAnimal(Family member, ArrayList<Animal> animals){
        int dangerGoose=0;
        int dangerMonkey=0; 
        for(Animal animal: animals){
            if (animal.busy_eating()){continue;}
            if (animal.which_animal()==AnimalType.MONKEY){//monkey
                if (Point.dist(animal.get_location(), member.get_location())<=5.0){
                    dangerMonkey+=1;
                }
            }else{//goose
                if (Point.dist(animal.get_location(), member.get_location())<=2.0 && member.get_held_item_type()==FoodType.SANDWICH){
                    dangerGoose+=1;
                }
            }
        }
        return (dangerGoose>=1 || dangerMonkey>=3);
    }
    //helper function: whether put food away 
    //if hold food and is in danger: put food away; 
    //else do nothing. 
    public Command putFoodBack(Family member, ArrayList<Animal> animals){
        if(dangerAnimal(member, animals)&& member.get_held_item_type()!=null){return new Command(CommandType.KEEP_BACK);}
        return null;
    }
  
}
