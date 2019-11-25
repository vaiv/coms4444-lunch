package lunch.g5.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogFileParser {
	
	private String SUMMARY_START_TOKEN = "Summary of results";
	private static String PLAYER = "g5";
	
	public static void main(String[] args) {
		LogFileParser logFileParser = new LogFileParser();
		
		// Getting current directory
		String currentDirectory = System.getProperty("user.dir");
		File currDir = new File(currentDirectory);
		File[] allFiles = currDir.listFiles();
		List<File> logFiles = new ArrayList<File>();
		
		// Gathering all log files after simulation
		for(File file : allFiles) {
			if(file.getName().endsWith("txt")) {
				logFiles.add(file);
			}
		}
		
		Map<Integer, List<Map<String, Integer>>> scoreMap = new HashMap<Integer, List<Map<String, Integer>>>();
		for(int i = 1; i <= 8; i++) {
			List<Map<String, Integer>> scores = new ArrayList<Map<String, Integer>>();
			scoreMap.put(i, scores);
		}
		
		// Parsing log files to get scores
		for(File logFile : logFiles) {
			double totalPlayerScore = 0;
			double totalTeamScore = 0;
			double playerTeamScoreRatio = 0;
			
			Map<String, Integer> matchScore = logFileParser.getScores(logFile);
			
			String teams = "";
			for(String key : matchScore.keySet()) {
				teams += key + " vs ";
			}
			try {
				teams = teams.substring(0, teams.length() - 4);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				continue;
			}
			
			// Add scores to list for later analysis
			List<Map<String, Integer>> scores = scoreMap.get(matchScore.size());
			if(scores == null) {
				scores = new ArrayList<Map<String, Integer>>();
			}
			scores.add(matchScore);
			scoreMap.put(matchScore.size(), scores);
			
			// Print scores of every match
			//printMatchScore(totalPlayerScore, totalTeamScore, matchScore, teams);
		}
		
		// Print overall scores of tournament per team size
		//printOverAllScore(scoreMap);
		
		List<Integer> teamSizes = new ArrayList<Integer>();
		List<Double> averageTeamScores = new ArrayList<Double>();
		List<Double> averagePlayerScores = new ArrayList<Double>();
		
		for(Integer numPlayers : scoreMap.keySet()) {
			System.out.println("Team Size: " + numPlayers);
			teamSizes.add(numPlayers);
			List<Map<String, Integer>> scores = scoreMap.get(numPlayers);
			double totalPlayerScore = 0;
			double totalTeamScore = 0;
			for(Map<String, Integer> matchScore : scores) {
				for(String matchScoreKey : matchScore.keySet()) {
					if(matchScoreKey.contains(PLAYER)) {
						totalPlayerScore += matchScore.get(matchScoreKey);
					}
					totalTeamScore += matchScore.get(matchScoreKey);
				}
			}
			System.out.println("Total Number of Tournaments: " + scores.size());
			System.out.println("Total Team Score: " + totalTeamScore);
			System.out.println("Average Team Score: " + totalTeamScore/scores.size());
			System.out.println("Average Team Score per Player: " + (totalTeamScore/scores.size())/numPlayers);
			System.out.println("Total Player Score: " + totalPlayerScore);
			System.out.println("Average Player Score: " + totalPlayerScore/scores.size());
			averageTeamScores.add((double) ((totalTeamScore/scores.size())/numPlayers));
			averagePlayerScores.add((double) (totalPlayerScore/scores.size()));
			System.out.println();
			System.out.println();
		}
		
		System.out.println(teamSizes);
		System.out.println(averageTeamScores);
		System.out.println(averagePlayerScores);
		
	}

	/**
	 * Prints the tournament scores per team size
	 * @param scoreMap
	 */
	private static void printOverAllScore(Map<Integer, List<Map<String, Integer>>> scoreMap) {
		System.out.println("-------------------------------------------");
		
		for(Integer key : scoreMap.keySet()) {
			System.out.println("Team Size: " + key);
			List<Map<String, Integer>> scores = scoreMap.get(key);
			int totalPlayerScore = 0;
			int totalTeamScore = 0;
			for(Map<String, Integer> matchScore : scores) {
				for(String matchScoreKey : matchScore.keySet()) {
					if(matchScoreKey.contains(PLAYER)) {
						totalPlayerScore += matchScore.get(matchScoreKey);
					}
					totalTeamScore += matchScore.get(matchScoreKey);
				}
			}
			System.out.println("Total Number of Tournaments: " + scores.size());
			System.out.println("Total Team Score: " + totalTeamScore);
			System.out.println("Average Team Score: " + totalTeamScore/scores.size());
			System.out.println("Total Player Score: " + totalPlayerScore);
			System.out.println("Average Player Score: " + totalPlayerScore/scores.size());
			System.out.println();
			System.out.println();
		}
	}

	/**
	 * Prints each match metrics
	 * @param totalPlayerScore
	 * @param totalTeamScore
	 * @param matchScore
	 * @param teams
	 */
	private static void printMatchScore(double totalPlayerScore, double totalTeamScore, Map<String, Integer> matchScore,
			String teams) {
		double playerTeamScoreRatio;
		System.out.print("Scores for ");
		System.out.println(teams + ":");
		System.out.println();
		
		for(String key : matchScore.keySet()) {
			if(key.contains(PLAYER)) {
				totalPlayerScore = matchScore.get(key);
			}
			totalTeamScore += matchScore.get(key);
		}
		
		playerTeamScoreRatio = totalPlayerScore/totalTeamScore;
		
		System.out.println("Player Score: " + totalPlayerScore);
		System.out.println("Total Team Score: " + totalTeamScore);
		System.out.println("Player Team Score Ratio: " + playerTeamScoreRatio);
		
		System.out.println();
		System.out.println("----------------------------------");
		System.out.println();
	}

	/**
	 * Parses the log file to extract scores
	 * @param logFile
	 * @return
	 */
	public Map<String, Integer> getScores(File logFile){
		Map<String, Integer> scoreMap = new HashMap<String, Integer>();
		
		boolean scoreFlag = false;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(logFile.getAbsolutePath()));
			String line = reader.readLine();
			while (line != null) {
				if(line.contains(SUMMARY_START_TOKEN)) {
					scoreFlag = true;
					line = reader.readLine();
					continue;
				}
				
				if(scoreFlag == true) {
					String[] tokens = line.split("\t");
					if(tokens.length == 3) {
						//System.out.println(tokens[0] + " : " + tokens[2]);
						String teamName = tokens[0].split(" ")[2];
						scoreMap.put(teamName, Integer.parseInt(tokens[2]));
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scoreMap;
	}
}
