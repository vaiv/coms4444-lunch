package lunch.g5.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lunch.sim.Simulator;

public class TestSuite {

	private static List<List<Integer>> allCombinations;
	private static String PLAYER = "g5";
	private static int TIME = 3600;
	private static int NUM_MONKEYS = 10;
	private static int NUM_GEESE = 30;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		allCombinations = new ArrayList<List<Integer>>();

		// Getting current directory
		String currentDirectory = System.getProperty("user.dir");
		File currDir = new File(currentDirectory);
		
		// Getting all files in the players directory
		File[] playersDirs = new File(currDir.getAbsoluteFile() + File.separator + "lunch").listFiles();
		List<String> players = new ArrayList<String>();
		
		// Get all player names
		for(File player : playersDirs) {
			if(player.getName().contains("sim")) {
				continue;
			}
			players.add(player.getName());
		}
		
		// ArrayList to Array Conversion 
		int[] arr = new int[players.size()]; 
        for (int i =0; i < players.size(); i++) {
            arr[i] = i;
        }
        
		// Generate all possible combinations of players
		for(int r = 1; r <= arr.length; r++) {
			int n = arr.length; 
	        generateCombination(arr, n, r); 
		}
		
		// Print all combinations
		/*for(List<Integer> combination : allCombinations) {
			for(Integer element : combination) {
				System.out.print(players.get(element) + ", ");
			}
			System.out.println();
		}*/
		
		runTests(players);
	}
	
	/**
	 * Run tests on the players
	 * @param players
	 */
	public static void runTests(List<String> players) {
		
		int index = 1;
		for(List<Integer> combination : allCombinations) {
			boolean combinationHasG5 = false;
			
			// Generate the command line arguments
			List<String> argumentList = new ArrayList<String>();
			argumentList.add("java");
			argumentList.add("lunch.sim.Simulator");
			argumentList.add("-t");
			argumentList.add(TIME + "");
			argumentList.add("--players");
			for(Integer element : combination) {
				argumentList.add(players.get(element));
				if(players.get(element).contains(PLAYER)) {
					combinationHasG5 = true;
				}
			}
			if(!combinationHasG5) {
				continue;
			}
			argumentList.add("-m");
			argumentList.add(NUM_MONKEYS + "");
			argumentList.add("-g");
			argumentList.add(NUM_GEESE + "");
			argumentList.add("-f");
			argumentList.add(players.size() + "");
			argumentList.add("-s");
			argumentList.add("42");
			argumentList.add("-l");
			argumentList.add("log" + index++ + ".txt");
			
			String[] arguments = new String[argumentList.size()]; 
	        for (int i = 0; i < argumentList.size(); i++) {
	        	arguments[i] = argumentList.get(i);
	        }
			//String[] arguments = {"-t", "3600", "--players", "random", "random", "random", "random", "-m", "10", "-g", "4", "-f", "4", "-s", "42", "-l", "log.txt"};
			
	        ProcessBuilder processBuilder = new ProcessBuilder();
	        processBuilder.command(arguments);

	        try {
	            Process process = processBuilder.start();
				// blocked :(
	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	            String line;
	            while ((line = reader.readLine()) != null) {
	                System.out.println(line);
	            }

	            int exitCode = process.waitFor();
	            System.out.println("\nExited with error code : " + exitCode);

	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        
	        /*try {
				Simulator.main(arguments);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	/* arr[]  ---> Input Array 
    data[] ---> Temporary array to store current combination 
    start & end ---> Staring and Ending indexes in arr[] 
    index  ---> Current index in data[] 
    r ---> Size of a combination to be printed */
    static void combinationUtil(int arr[], int data[], int start, 
                                int end, int index, int r) 
    { 
        // Current combination is ready to be printed, print it 
        if (index == r) 
        { 
        	List<Integer> combination = new ArrayList<Integer>();
            for (int j=0; j<r; j++) {
                //System.out.print(data[j]+" "); 
                combination.add(data[j]);
            }
            //System.out.println("");
            allCombinations.add(combination);
            return; 
        } 
  
        // replace index with all possible elements. The condition 
        // "end-i+1 >= r-index" makes sure that including one element 
        // at index will make a combination with remaining elements 
        // at remaining positions 
        for (int i=start; i<=end && end-i+1 >= r-index; i++) 
        { 
            data[index] = arr[i]; 
            combinationUtil(arr, data, i+1, end, index+1, r); 
        } 
    } 
  
    // The main function that prints all combinations of size r 
    // in arr[] of size n. This function mainly uses combinationUtil() 
    static void generateCombination(int arr[], int n, int r) 
    { 
        // A temporary array to store all combination one by one 
        int data[]=new int[r]; 
  
        // Print all combination using temprary array 'data[]' 
        combinationUtil(arr, data, 0, n-1, 0, r); 
    } 

}
