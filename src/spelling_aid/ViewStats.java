package spelling_aid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ViewStats implements SpellingAidFunction{ // This function uses a JTable to display the stats of the words
	
	private JScrollPane _scrollPane;
	private File _statsFile;
	private JTable _statsTable;
	private String[] _columnNames = {"Word","Mastered","Faulted","Failed"};
	private String[][] _wordStats;
	
	public ViewStats(JScrollPane scrollPane,File statsFile) {
		_scrollPane = scrollPane;
		_statsFile = statsFile;
	}

	@Override
	public String processFunction() {
		
		BufferedReader br;
		String[] allStats;
		int numWords = 0;
		
		try {
			
			// Read the .wordStats file for all the word stats into an array
			br = new BufferedReader(new FileReader(_statsFile));
			allStats = br.readLine().split("%");
			br.close();
			numWords = allStats.length;
			
			_wordStats = new String[numWords][4]; // Initialize that there are 4 columns in the 2D array for the JTable
			
			// Put each word's statistics into the JTable array
			String[] singleWordStatArray;
			int tableRow = 0;
			for (int i = 0; i < numWords; i++) {
				
				singleWordStatArray = allStats[i].split(" ");
				
				// If the word is untested skip it
				if (singleWordStatArray[4].equals("untested")) {
					continue;
				}
				
				for (int j = 0; j < 4; j++) {
					_wordStats[tableRow][j] = singleWordStatArray[j];
				}
				tableRow++;
			}
			
			// Detect if there haven't been any tested words yet
			if (tableRow == 0) {
				return "No attemted words yet! Please take a test first and then view the stats on the words you attempted.";
			}
			
			// Initialize the JTable
			_statsTable = new JTable(Arrays.copyOfRange(_wordStats, 0, tableRow),_columnNames);
			
			
			//Retrieved from http://stackoverflow.com/questions/8608902/the-correct-way-to-swap-a-component-in-java
			_scrollPane.setViewportView(_statsTable); // Make JTable be the port view for the main GUI scroll pane
			
			// Fill the entire scroll pane with the table
			_statsTable.setFillsViewportHeight(true);
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
