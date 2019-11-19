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
		
		// Parsing log files to get scores
		for(File logFile : logFiles) {
			double totalPlayerScore = 0;
			double totalTeamScore = 0;
			double playerTeamScoreRatio = 0;
			
			Map<String, Integer> scores = logFileParser.getScores(logFile);
			
			System.out.print("Scores for ");
			String teams = "";
			for(String key : scores.keySet()) {
				teams += key + " vs ";
			}
			teams = teams.substring(0, teams.length() - 4);
			System.out.println(teams + ":");
			System.out.println();
			
			for(String key : scores.keySet()) {
				if(key.contains(PLAYER)) {
					totalPlayerScore = scores.get(key);
				}
				totalTeamScore += scores.get(key);
			}
			
			playerTeamScoreRatio = totalPlayerScore/totalTeamScore;
			
			System.out.println("Player Score: " + totalPlayerScore);
			System.out.println("Team Score: " + totalTeamScore);
			System.out.println("Player Team Score Ratio: " + playerTeamScoreRatio);
			
			System.out.println();
			System.out.println("----------------------------------");
			System.out.println();
		}
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
