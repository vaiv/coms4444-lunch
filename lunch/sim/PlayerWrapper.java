package lunch.sim;

import lunch.sim.Player;
import lunch.sim.PlayerState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.*;

public class PlayerWrapper {
    private Timer timer;
    private Player player;
    private String name;
    private long timeout;


    public PlayerWrapper(Player player, String name, long timeout) 
    {
        this.player = player;
        this.name = name;
        this.timeout = timeout;
        this.timer = new Timer();
    }

   // Initialization function.
    // members: other family members collborating with your player.
    // id: player id
    // f: Number of family members.
    // animals: list of animals
    // m: number of monkeys
    // g: number of geese 
    // t: Time limit for simulation.
    // s: seed for random 

    public String init(ArrayList<Family> members,Integer id, int f,ArrayList<Animal> animals, Integer m, Integer g, double t, Integer s)
    {
        String str="";
        Log.record("Initializing player " + this.name);
         try {
            if (!timer.isAlive()) timer.start();

            timer.call_start(new Callable<String>() 
            {
                @Override
                public String call() throws Exception 
                {
                    return player.init(members, id, f, animals, m, g, t, s);
                }
            });

            str = timer.call_wait(timeout);
        }
        catch (Exception ex) 
        {
            System.out.println("Player " + this.name + " has possibly timed out.");
	    System.out.println(ex.getMessage());
            Log.record("Player " + this.name + " has possibly timed out.");
            Log.record(ex.getMessage());
            // throw ex;
        }       
        
        return str;
    }
    // Gets the moves from the player. Number of moves is specified by first parameter.
    public Command getCommand(ArrayList<Family> members, ArrayList<Animal> animals, PlayerState ps)
    {
        Log.record("Getting moves for player " + this.name);
        Command c = new Command();

        try 
        {
            if (!timer.isAlive()) timer.start();

            timer.call_start(new Callable<Command>() 
            {
                @Override
                public Command call() throws Exception 
                {
                    return player.getCommand(members, animals, ps);
                }
            });

            c = timer.call_wait(timeout);
        }
        catch (Exception ex) 
        {
            System.out.println("Player " + this.name + " has possibly timed out.");
	    System.out.println(ex.getMessage());
            Log.record("Player " + this.name + " has possibly timed out.");
	    Log.record(ex.getMessage());
            // throw ex;
        }       

        return c;
    }

    public String getName() {
        return name;
    }
}
