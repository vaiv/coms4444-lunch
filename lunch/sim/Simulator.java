package lunch.sim;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.Scanner; 

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.util.HashMap; 
import java.util.Map; 
import java.util.*;
import java.io.*;

public class Simulator {


private static final String root = "lunch";
private static final String statics_root = "statics";
private static boolean gui = false;
private static double fps = 30;
private static Integer m;
private static Integer g;
private static Integer f;
private static Integer seed = 42;
private static Integer runs = 1;
private static Integer turns = 10000;
private static Integer count_down = turns;
private static Integer arbitrary_const = 100000;
private static long timeout = 1000;
private static String version = "1.0";
private static String avatars="jetson";

private static List<String> playerNames;
private static HashMap<String, Integer> player_scores;
private static ArrayList<PlayerWrapper> players; 
private static ArrayList<Agent> agents;

private static ArrayList<Monkey> monkeys;
private static ArrayList<Goose> geese;

private static Random generator;


public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
	 	parseArgs(args);
	 	generator = new Random(seed);
	 	player_scores = new HashMap<String,Integer>();
	 	agents = new ArrayList<Agent>();
	 	monkeys = new ArrayList<Monkey>();
	 	geese =  new ArrayList<Goose>();
	 	
       
        Log.log("parsing done");

        HTTPServer server = null;
        if (gui) {
            server = new HTTPServer();
            Log.record("Hosting HTTP Server on " + server.addr());
            if (!Desktop.isDesktopSupported())
                Log.record("Desktop operations not supported");
            else if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Log.record("Desktop browse operation not supported");
            else {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            
            // gui(server, state(fps));
        }

        HashMap<String, Integer> player_curr_names = new HashMap<String, Integer>();

        for(Integer i=0;i<playerNames.size();i++)
        {
            System.out.println(playerNames.get(i));
            if (player_curr_names.containsKey(playerNames.get(i)))
            {
                Log.log("player at position " + i.toString() + " is named similarly to another player.");

                String mod_name = " ";
                for(Integer j=0;j<100;j++)
                {
                    mod_name = playerNames.get(i) + "_" + j.toString();
                    if (player_curr_names.containsKey(mod_name))
                        continue;
                    else
                        {
                            playerNames.set(i, mod_name);
                            break;
                        }
                }

                Log.log("player at position " + i.toString() + " is being renamed to " + mod_name );
            }

            player_curr_names.put(playerNames.get(i),i);

            player_scores.put(playerNames.get(i), 0);
        }

        Log.log("###############################################################################################################");
	 	Log.log("Simulation Beginning");
	 	Log.log("number of players: " + f.toString());
	 	Log.log("number of monkeys: " + m.toString());
	 	Log.log("number of geese: " + g.toString());
	 	Log.log("total time: " + turns.toString());
		Log.log("random seed used: " + seed.toString());

	 	try
	 	{
	 		players = new ArrayList<PlayerWrapper>();

	 		for(Integer i=0;i<playerNames.size();i++)
	 		{
	 			PlayerWrapper player = loadPlayerWrapper(cleanName(playerNames.get(i)),playerNames.get(i));
	 			Agent a = new Agent(i);
	 			players.add(player);
	 			agents.add(a);
	 		}
	 	}
	 	catch (Exception ex) 
		{
			Log.log("Unable to load players. " + ex.getMessage());
			System.exit(0);
		}


		for(Integer i=0;i<g;i++)
		{
			Goose g = new Goose(generator.nextInt(arbitrary_const));
			if(g==null)
				Log.record("g initialized to null");
			geese.add(g);
		}

		for(Integer i=0;i<m;i++)
		{
			Monkey m = new Monkey(generator.nextInt(arbitrary_const));
			monkeys.add(m);
		}


		//Initialize all players

		ArrayList<Animal> init_animals = new ArrayList<Animal>();
 		ArrayList<Family> init_family = new ArrayList<Family>();

 		for(Integer j=0;j<Math.max(g,m);j++)
 		{
 			if(j<g)
 			{
 				Animal a = new Animal(geese.get(j));
 				init_animals.add(a);
 			}

 			if(j<m)
 			{
 				Animal a = new Animal(monkeys.get(j));
 				init_animals.add(a);
 			}
 		}

 		for(Integer j=0;j<agents.size();j++)
 		{
 			Family member = new Family(agents.get(j));
 			init_family.add(member);
 		}

 		for(Integer j=0;j<agents.size();j++)
 		{
 			try
 			{
 				Integer s_player = generator.nextInt(arbitrary_const);
 				String s = players.get(j).init(init_family,j,f,init_animals,m,g,turns,s_player);
 				if(s.length()>0)
 				{
 					avatars = s;
 				}
 			}
 			catch(Exception ex)
 			{
 				Log.record(ex.getMessage());
 			}
 		}

 		//initialize gui
		if(gui)
		{
			gui(server,state(fps));
		}

	 	for(Integer i=0;i<turns;i++)
	 	{
	 		count_down--;
	 		//geese move first
	 		for(Integer j=0;j<g;j++)
	 		{
	 			Integer idx = get_nearest_agent(geese.get(j).get_location());
	 			Double dist = Point.dist(agents.get(idx).get_location(),geese.get(j).get_location());
	 			Food item = agents.get(idx).get_held_item();

	 			//has stolen item flying back to nest
	 			// Log.log(geese.get(j).get_location().toString());
	 			if(geese.get(j).busy_eating())
	 			{
	 				geese.get(j).next_move(geese.get(j).get_nest_location());
	 			}
	 			//steal food item
	 			else if(dist< 2+10e-7 && item!=null && agents.get(idx).visible_item() && (item.get_food_type()==FoodType.SANDWICH||item.get_food_type()==FoodType.SANDWICH1 || item.get_food_type()==FoodType.SANDWICH2))
	 			{
	 				// Log.log("goose stole sandwich");
	 				geese.get(j).steal(item.get_food_type());
	 				agents.get(idx).stolen();
	 				geese.get(j).next_move(geese.get(j).get_nest_location());
	 				
	 			}
	 			//look for nearest target with sandwich out
	 			else if(dist < 20 + 10e-7 && item!=null && agents.get(idx).visible_item() && (item.get_food_type()==FoodType.SANDWICH||item.get_food_type()==FoodType.SANDWICH1 || item.get_food_type()==FoodType.SANDWICH2))
	 			{
	 				geese.get(j).next_move(agents.get(idx).get_location());
	 				// Log.log("goose approaching food");
	 			}
	 			// move in random direction
	 			else
	 			{
	 				geese.get(j).next_move(null);
	 			}

	 		}

	 		//monkeys move next

	 		int[] count = new int[agents.size()];
	 		for(Integer j=0;j<m;j++)
	 		{
	 			Integer idx = get_nearest_agent(monkeys.get(j).get_location());
	 			Double dist = Point.dist(agents.get(idx).get_location(),monkeys.get(j).get_location());
	 			if(dist<5+10e-7 && !monkeys.get(j).busy_eating())
	 				count[idx]++;
	 		}

	 		for(Integer j=0;j<m;j++)
	 		{
	 			Integer idx = get_nearest_agent(monkeys.get(j).get_location());
	 			Double dist = Point.dist(agents.get(idx).get_location(),monkeys.get(j).get_location());
	 			Food item = agents.get(idx).get_held_item();
	 			
	 			if(count[idx]>=3 && dist<5+10e-7 && !monkeys.get(j).busy_eating() && item!=null && agents.get(idx).visible_item())
	 			{

	 				// Log.log("monkeys stole food item");
	 				monkeys.get(j).steal(item.get_food_type());
	 				monkeys.get(j).next_move(null);
	 			}
	 			else if(monkeys.get(j).busy_eating())
	 			{
	 				monkeys.get(j).update_rem_time();
	 				monkeys.get(j).next_move(null);
	 			}
	 			else if(dist<40.0 + 10e-7 && item!=null && agents.get(idx).visible_item())
	 			{
	 				// Log.log("monkey approaching food");
	 				monkeys.get(j).next_move(agents.get(idx).get_location());
	 			}
	 			else
	 				monkeys.get(j).next_move(null);
	 		}

	 		for(Integer j=0;j<players.size();j++)
	 		{
	 			if(count[j]>=3 && agents.get(j).get_held_item()!=null && agents.get(j).visible_item())
	 				agents.get(j).stolen();
	 		}

	 		//Agents/Players move last

	 		ArrayList<Animal> animals = new ArrayList<Animal>();
	 		ArrayList<Family> family = new ArrayList<Family>();

	 		for(Integer j=0;j<Math.max(g,m);j++)
	 		{
	 			if(j<g)
	 			{
	 				Animal a = new Animal(geese.get(j));
	 				animals.add(a);
	 			}

	 			if(j<m)
	 			{
	 				Animal a = new Animal(monkeys.get(j));
	 				animals.add(a);
	 			}
	 		}

	 		for(Integer j=0;j<agents.size();j++)
	 		{
	 			Family member = new Family(agents.get(j));
	 			family.add(member);
	 		}

	 		for(Integer j=0;j<agents.size();j++)
	 		{
	 			try
	 			{
	 				PlayerState ps = new PlayerState(agents.get(j));
	 				Command c = players.get(j).getCommand(family,animals,ps);
	 				execute_command(c,agents.get(j));
	 			}
	 			catch(Exception e)
	 			{
	 				Log.record("Could not execute last command from player " + playerNames.get(j));
	 				Log.record(e.getMessage());
	 			}
	 		}

	 		//update simulator gui state
	 		if(gui)
	 		{
	 			gui(server, state(fps));
	 		}


	 		// check if converged

	 		boolean is_done = true;
			Integer total_score = 0;
	 		for(Integer j=0; j<agents.size();j++)
	 		{
	 			is_done = is_done && agents.get(j).is_done();
	 		}

	 		if(is_done)
	 			break;


	 	}


	 	Log.log("simulation concluded!");
	 	Log.log("----------------------------------------------Summary of results------------------------------------------");
	 	Log.log("player name \t score");
		Integer total_score=0;
	 	for(int l=0;l<agents.size();l++)
	 	{
	 		Log.log(playerNames.get(l) + "\t\t" + agents.get(l).get_score().toString());
			total_score+= agents.get(l).get_score();
	 	}
		Log.log("total score: " + total_score.toString());
	 	Log.log("----------------------------------------------End of log.-------------------------------------------------");

        Log.end();

         if (gui) 
          {
             gui(server, state(fps));
             Scanner in = new Scanner(System.in); 
             String s = in.nextLine(); 
          }
        System.exit(0);



    }


 private static void execute_command(Command c, Agent a)
 {
 	if(c!=null && c.get_type()!=null)
 	switch(c.get_type())
 	{
 		case ABORT: 
 			// Log.log("abort command issued.");
 			// if(a.get_held_item()!=null && a.is_waiting())
 			// {
 			// 	a.make_visible();
 			// 	a.reset_wait_time();
 			// 	a.stop_looking();
 			// }
 			if(a.is_waiting() && a.get_held_item()==null)
 			{
 				a.set_food_request(null);
 				a.reset_wait_time();
 				a.stop_looking();
 			}
 			else
 			{
 				Log.record("Abort failed, all previous commands have already been completed.");
 			}
 			break;
 		case TAKE_OUT:
 			// Log.log("take out command issued.");
 			if(!a.is_waiting() && a.get_held_item()==null)
 			{
 				if(a.check_available_item(c.get_food_type()))
 				{
 					// Log.log("start looking for food.");
 					a.set_food_request(c.get_food_type());
 					a.start_wait();
 				}
 				else
 				{
 					Log.record("requested food item is no longer avialable!");
 				}
 				
 			}
 			else
 				Log.record("take out command failed.");
 			break;
 		case KEEP_BACK:
 		    // Log.log("keep back command issued.");
 			if(!a.is_waiting() && a.get_held_item()!=null)
 			{
 				a.hide();
 				a.start_wait();
 			}
 			else
 				Log.record("keep back command failed.");
 			break;
 		case MOVE_TO: 
 		    // Log.log("move to command issued.");
 			if (a.is_waiting() || c.get_location()==null)
 				{
 					Log.record("cannot move to specified location as either the player is waiting or the location specified was null.");
 					return;
 				}
 			else
 			{
 				a.moveAgent(c.get_location());
 			}
 			break;
 		case EAT:
 			// Log.log("eat command issued.");
 			if(a.get_held_item()==null || !a.get_held_item().check_available())
 			{
 				Log.record("cannot eat. Food item not avialable.");
 			}
 			else
 			{
 				Log.record("taking a bite, time to complete comsumption is " + a.get_held_item().get_rem_time().toString());
 				a.eat_item();
 			}
 			break;
 		case WAIT:
 			// Log.log("waiting");
 			if(a.is_waiting())
 				{
 					// Log.log("searching for food.");
 					a.update_remaining_time();
 					if(!a.is_waiting() && a.get_held_item()==null)
 					{
 						// Log.log("retrieving food.");
 						a.retrieve_item(a.get_food_request());
 					}
 					else if(!a.is_waiting())
 					{
 						a.keep_back_item();
 					}
 				}
 			 break;

 		default:  
 			 Log.log("unknown command encountered.");
 	}


 	if(a.is_waiting())
	{
		// Log.log("searching for food.");
		a.update_remaining_time();
		if(!a.is_waiting() && a.get_held_item()==null)
		{
			// Log.log("retrieving food.");
			a.retrieve_item(a.get_food_request());
		}
		else if(!a.is_waiting())
		{
			a.keep_back_item();
		}
	}


 }

private static Integer get_nearest_agent(Point p)
{
	Double min_dist = Double.MAX_VALUE;
	Integer idx = -1;
	for(Integer i=0;i<agents.size();i++)
	{
		if(Point.dist(p,agents.get(i).get_location()) < min_dist)
		{
			min_dist = Point.dist(p,agents.get(i).get_location());
			idx = i;
		}
	}
	return idx;
}

private static String cleanName(String s)
     {
        String res = " ";
        if(s.contains("_"))
        {
            Integer idx = s.lastIndexOf("_");
            res = s.substring(0,idx);
        }
        else
            return s;

        return res;
     }


private static void parseArgs(String[] args) 
	 {
        int i = 0;
        playerNames = new ArrayList<String>();
        for (; i < args.length; ++i) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].equals("-p") || args[i].equals("--players")) 
                    {
                        while (i + 1 < args.length && args[i + 1].charAt(0) != '-') 
                        {
                            ++i;
                            playerNames.add(args[i]);
                        }

                        if (playerNames.size() < 1) 
                        {
                            throw new IllegalArgumentException("Invalid number of players, you need atleast 1 player to start a game.");
                        }

                    } 
                    else if (args[i].equals("--gui")) 
                    {
                        gui = true;
                    } 
                    else if (args[i].equals("-l") || args[i].equals("--logfile")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing logfile name");
                        }
                        Log.setLogFile(args[i]);
                        Log.activate();
                    } 
                     else if (args[i].equals("-v") || args[i].equals("--verbose")) 
                    {
                        Log.verbose();
                    } 
                    else if (args[i].equals("--fps")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing frames per second.");
                        }
                        fps = Double.parseDouble(args[i]);
                    } 
                    else if (args[i].equals("-m") || args[i].equals("--num_monkeys")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing number of monkeys.");
                        }
                        m = Integer.parseInt(args[i]);

                    } 
                    else if (args[i].equals("-g") || args[i].equals("--num_geese")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing number of geese.");
                        }
                        g = Integer.parseInt(args[i]);

                    } 
                    else if (args[i].equals("-f") || args[i].equals("--num_family")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing number of family members.");
                        }
                        f = Integer.parseInt(args[i]);

                    } 
                    else if (args[i].equals("-s") || args[i].equals("--seed")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing seed number.");
                        }
                        seed = Integer.parseInt(args[i]);
                    }

                    else if (args[i].equals("-t") || args[i].equals("--simulation_time")) 
                    {
                        if (++i == args.length) 
                        {
                            throw new IllegalArgumentException("Missing number of turns.");
                        }			
                        turns = Integer.parseInt(args[i]);
                        count_down = turns;
                    }
                    else 
                    {
                        throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
            }
        }

        Log.log("Project: Lunch. \n Simulator Version:" + version);
        Log.log("Players: " + playerNames.toString());
        Log.log("GUI " + (gui ? "enabled" : "disabled"));

        if (gui)
            Log.log("FPS: " + fps);
    }

    private static PlayerWrapper loadPlayerWrapper(String name, String mod_name) throws Exception {
        Log.log("Loading player " + name);
        Player p = loadPlayer(name);
        if (p == null) {
            Log.log("Cannot load player " + name);
            System.exit(1);
        }

        return new PlayerWrapper(p, mod_name, timeout);
    }

    public static Player loadPlayer(String name) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException 
    {
        String sep = File.separator;
        Set<File> player_files = directory(root + sep + name, ".java");
        File class_file = new File(root + sep + name + sep + "Player.class");
        long class_modified = class_file.exists() ? class_file.lastModified() : -1;
        if (class_modified < 0 || class_modified < last_modified(player_files) ||
                class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new IOException("Cannot find Java compiler");
            StandardJavaFileManager manager = compiler.
                    getStandardFileManager(null, null, null);
//            long files = player_files.size();
            Log.log("Compiling for player " + name);
            if (!compiler.getTask(null, manager, null, null, null,
                    manager.getJavaFileObjectsFromFiles(player_files)).call())
                throw new IOException("Compilation failed");
            class_file = new File(root + sep + name + sep + "Player.class");
            if (!class_file.exists())
                throw new FileNotFoundException("Missing class file");
        }
        ClassLoader loader = Simulator.class.getClassLoader();
        if (loader == null)
            throw new IOException("Cannot find Java class loader");
        @SuppressWarnings("rawtypes")
        Class raw_class = loader.loadClass(root + "." + name + ".Player");
        return (Player)raw_class.newInstance();
    }

    private static long last_modified(Iterable<File> files) 
    {
        long last_date = 0;
        for (File file : files) 
        {
            long date = file.lastModified();
            if (last_date < date)
                last_date = date;
        }
        return last_date;
    }

    private static Set<File> directory(String path, String extension) {
        Set<File> files = new HashSet<File>();
        Set<File> prev_dirs = new HashSet<File>();
        prev_dirs.add(new File(path));
        do {
            Set<File> next_dirs = new HashSet<File>();
            for (File dir : prev_dirs)
                for (File file : dir.listFiles())
                    if (!file.canRead()) ;
                    else if (file.isDirectory())
                        next_dirs.add(file);
                    else if (file.getPath().endsWith(extension))
                        files.add(file);
            prev_dirs = next_dirs;
        } while (!prev_dirs.isEmpty());
        return files;
    }

    private static <T extends Object> T deepClone(T object) {
        if (object == null) {
            return null;
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(bais);
            return (T) objectInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void gui(HTTPServer server, String content) {
        if (server == null) return;
        String path = null;
        for (;;) {
            for (;;) {
                try {
                    path = server.request();
                    break;
                } catch (IOException e) {
                    Log.record("HTTP request error " + e.getMessage());
                }
            }
            if (path.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch (IOException e) {
                    Log.record("HTTP dynamic reply error " + e.getMessage());
                }
                return;
            }
            if (path.equals("")) path = "webpage.html";
            else if (!Character.isLetter(path.charAt(0))) {
                Log.record("Potentially malicious HTTP request \"" + path + "\"");
                break;
            }

            File file = new File(statics_root + File.separator + path);
            if (file == null) {
                Log.record("Unknown HTTP request \"" + path + "\"");
            } else {
                try {
                    server.reply(file);
                } catch (IOException e) {
                    Log.record("HTTP static reply error " + e.getMessage());
                }
            }
        }
    }



    // The state that is sent to the GUI. (JSON)
    private static String state(double fps) 
    {
        String json = "{ \"refresh\":" + (1000.0/fps)  + ",\"remaining_time\":" + (int)count_down + ",\"num_players\":" + (int) f + ",\"num_monkeys\":"+ (int)m + ",\"num_geese\":"+ (int)g + ",";

        //json+= "\"player1\":" + "\"" + player1.getName() + "\"" + ",\"player2\":" + "\"" + player2.getName() + "\"" + ",\"player1_score\":" + (int)player1_score + ",\"player2_score\":" + (int)player2_score + ",";  
        // for(Integer i=0; i<players.size();i++)
        // {

        // 	json+= "\"player_" + i.toString() +"_name\":" playerNames.get(i) + ",\"player_" + i.toString() +"_score\":" + agents.get(i).get_score().toString() +","; 
        // }

        json += "\"player_locations\":[";
        for (int i = 0; i < agents.size(); i++)
        {
            Point p =  agents.get(i).get_location();
            boolean s_1=false,s_2=false,f_1=false,f_2=false,e=false,c=false;
            s_1 = agents.get(i).check_available_item(FoodType.SANDWICH1);
            s_2 = agents.get(i).check_available_item(FoodType.SANDWICH2);
            f_1 = agents.get(i).check_available_item(FoodType.FRUIT1);
            f_2 = agents.get(i).check_available_item(FoodType.FRUIT2);
            e = agents.get(i).check_available_item(FoodType.EGG);
            c = agents.get(i).check_available_item(FoodType.COOKIE);
            String eating="";
            Double percent_remaining = -1.0;
            if(agents.get(i).get_held_item()!=null)
            {
            	eating = agents.get(i).get_held_item().get_food_type().toString();
            	percent_remaining = (double)agents.get(i).get_rem_time_item(agents.get(i).get_held_item().get_food_type());
            	FoodType item = agents.get(i).get_held_item().get_food_type();
            	switch(item)
            	{
            		case SANDWICH1:
            		case SANDWICH2:
            		case SANDWICH: percent_remaining/= 180; break;
            		case FRUIT1:
            		case FRUIT2:
            		case FRUIT: percent_remaining/= 120; break;
            		case EGG: percent_remaining/= 120; break;
            		case COOKIE: percent_remaining/= 60;break;
            		default: ;
            	}
            }
            json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + ",\"id\":"+ i +  ",\"name\":" + "\"" + playerNames.get(i) + "\"" + ",\"score\":"+ agents.get(i).get_score() + ",\"s_1\":" + s_1 + ",\"s_2\":" + s_2 + ",\"f_1\":" + f_1 +",\"f_2\":" + f_2  + ",\"e\":" + e + ",\"c\":" + c+",\"eating\":" + "\""+ eating + "\"" + ",\"rem_time\":" + (percent_remaining*100) +  ",\"avatars\":" +"\"" + avatars + "\""  +"}";
            if (i !=  agents.size() - 1)
            {
                json += ",";
            }
        }
        json += "],";
        
         json += "\"monkey_locations\":[";
        for (int i = 0; i < monkeys.size(); i++)
        {
            Point p =  monkeys.get(i).get_location();
            json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + "}";
            if (i !=  monkeys.size() - 1)
            {
                json += ",";
            }
        }

         json += "],";

        json += "\"geese_locations\":[";
        for (int i = 0; i < geese.size(); i++)
        {
            Point p =  geese.get(i).get_location();
            json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + "}";
            if (i !=  geese.size() - 1)
            {
                json += ",";
            }
        }
         json += "],";
        

        // 1:sandwich, 2:half-eaten sandwich, 3: fruit, 4:eaten fruit, 5:egg, 6:eaten_egg, 7:cookie, 8:eaten_cookie, 9: bag



        json += "\"food_locations\":[";
        for (int i = 0; i < geese.size(); i++)
        {
        	if(geese.get(i).busy_eating())
        	{
        		Point p =  geese.get(i).get_location();
        		FoodType item = geese.get(i).check_stolen_item();
        		Integer z = -1;
        		if(item!=null)
	        	{	switch(item)
	        		{
	        			case SANDWICH:
	        			case SANDWICH1:
	        			case SANDWICH2: z = 2; break;
	        			case FRUIT:
	        			case FRUIT1:
	        			case FRUIT2: z=4; break;
	        			case EGG: z= 6; break;
	        			case COOKIE: z= 8; break;
	        			default: z = 0;
	        		}
	            	json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + ",\"id\" : "+z.toString() + "},";
	            }
        	}
            
        }
        for (int i = 0; i < monkeys.size(); i++)
        {
        	if(monkeys.get(i).busy_eating())
        	{
        		Point p =  monkeys.get(i).get_location();
        		FoodType item = monkeys.get(i).check_stolen_item();
        		Integer z = -1;
        		if(item!=null)
        		{
        			switch(item)
	        		{
	        			case SANDWICH:
	        			case SANDWICH1:
	        			case SANDWICH2: z = 2; break;
	        			case FRUIT:
	        			case FRUIT1:
	        			case FRUIT2: z=4; break;
	        			case EGG: z= 6; break;
	        			case COOKIE: z= 8; break;
	        			default: z = 0;
	        		}
            		json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + ",\"id\" : "+z.toString() + "},";
            	}
        	}
            
        }
        for (int i = 0; i < agents.size(); i++)
        {
        	Point p =  agents.get(i).get_location();
        	if(agents.get(i).visible_item() && agents.get(i).get_held_item()!=null)
        	{
        		
        		FoodType item = agents.get(i).get_held_item().get_food_type();
        		Integer z = -1;
        		if(item!=null)
        		{
        			switch(item)
        			{
	        			case SANDWICH:
	        			case SANDWICH1:
	        			case SANDWICH2: z = (agents.get(i).get_held_item().get_rem_time()>90) ? 1 : 2; break;
	        			case FRUIT:
	        			case FRUIT1:
	        			case FRUIT2: z = (agents.get(i).get_held_item().get_rem_time()>60)? 3 : 4; break;
	        			case EGG: z = (agents.get(i).get_held_item().get_rem_time()>60)? 5 : 6; break;
	        			case COOKIE: z = (agents.get(i).get_held_item().get_rem_time()>30)? 7 : 8; break;
	        			default: z = 0;
        			}
            		json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + ",\"id\" : "+z.toString() + "},";
            	}
        	}
        	else if(agents.get(i).is_waiting())
        	{
        		json += "{\"x\" : " + p.x + ",\"y\" : " + p.y + ",\"id\" : "+ 9  + "},";
        	}
            
        }
        json+= "{\"x\" : " + -1000 + ",\"y\" : " + -1000 + ",\"id\" : "+ -1000 + "}";
        // json += "],";
        
        json += "]}";


        return json;
    }



}
