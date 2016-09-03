package spelling_aid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.JTextArea;

public class NewSpellingTest implements SpellingAidFunction{
	
	private JTextArea _txtDisplay;
	
	private File _testFile;
	private String _currentPath;
	
	private ArrayList<String> _words = new ArrayList<String>();
	private int _numTestedWords;
	private String[] _testWords;
	private String _currentTestWord;
	private int _currentWordCount;
	private int _currentAttempt;
	
	private boolean _testFinished = false;
	private boolean _failedInReview = false;
	
	private String _status = "";
	
	
	public NewSpellingTest(File testFile, String currentPath, JTextArea txtDisplay) {
		_txtDisplay = txtDisplay;
		_testFile = testFile;
		_currentPath = currentPath;
		
		try { // Reads the wordlist or .failed file to get the words
			FileReader fr = new FileReader(testFile);
			BufferedReader br = new BufferedReader(fr);
			
			String word;
			
			while ((word = br.readLine()) != null) {
				_words.add(word);
				
				//In case of random blank line
				if (word.equals("")) {
					continue;
				}
			}
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setup();
	}

	// Process the test, asking user to spell a word
	@Override
	public String processFunction() {
		
		if (_status != "") {
			return _status;
		} else {
			if (_currentAttempt == 1) { // Else it is attempt 2, and we only progress to the next word after that has been processed
				askToSpell("Spell word " + _currentWordCount + " of " + _numTestedWords + ": ", _currentTestWord);
			}
		}
		
		return "";
	}
	
	private void askToSpell(String display, String spokenWords) {
		_txtDisplay.append(display);
		festivalCall(spokenWords);
	}
	
	public void doTest(String input) {
		
		_txtDisplay.append(input + "\n");
		
		
		if (!_failedInReview) { // Only process the stats and attempts if the test isn't current in review and failed mode
			if (input.equalsIgnoreCase(_currentTestWord)) {
				festivalCall("Correct");
				
				if (_currentAttempt == 1) {
					update(_currentTestWord,"mastered");
				} else {
					update(_currentTestWord,"faulted");
					_currentAttempt = 1; //Reset attempt for next word
				}
			
			} else {
				
				if (_currentAttempt == 1) {
					askToSpell("Incorrect, try once more: ","Incorrect, try once more.. " + _currentTestWord + ".. " +  _currentTestWord + ".");
					_currentAttempt = 2;
				} else {
					festivalCall("Incorrect");
					update(_currentTestWord,"failed");
					_currentAttempt = 1; //Reset attempt for next word
					
					if (_testFile.getName().equals(".failed")) {
						
						_failedInReview = true;
						
						String spelling = "";
						
						//Retrieved from http://stackoverflow.com/questions/1521921/splitting-words-into-letters-in-java
						for (int i = 0; i < _currentTestWord.length(); i++) {
							spelling = spelling + _currentTestWord.charAt(i) + ".. ";
						}
						
						askToSpell("Try with the spelling (will not be counted towards statistics): ",spelling);
						return;
					}		
					
				}
				
			}
		} else { // Whatever the user spells in the "3rd" attempt after failed word in review won't count
			_failedInReview = false;
			festivalCall("Hope you can get it next time"); // Encouraging message
		}
		
		
		if (_currentAttempt == 1) {
			
			if (_currentWordCount < _numTestedWords) { // Carry on with the test as this isn't the final word
				_currentWordCount++;
				_currentTestWord =  _testWords[_currentWordCount - 1];
				
			} else { // This is the final word, either correct on attempt 1 or correct on attempt 2, so finish test
				finishTest();
			}
			
		} else {
			// Do nothing as they are still on the same word for attempt 2
		}
	
		
	}
	
	// Returns whether the test is currently in review and the user has failed the word again
	public boolean isInReviewAndFailed() {
		return _failedInReview;
	}
	
	
	// Returns whether the current test is finished or not
	public boolean isTestFinished() {
		return _testFinished;
	}
	
	// Finishes the test by printing out appropriate msg to indicate this.
	private void finishTest() {
		
		_testFinished = true; //Indicate that test has completed
		
		String testOption;
		
		if (_testFile.getName().equals("wordlist")) {
			testOption = "Test";
		} else {
			testOption = "Review";
		}
		
		_txtDisplay.append(testOption + " finished. Press one of the four buttons above to proceed...");
		
	}
	
	// Sets up the relevant fields/display etc. for a test
	private void setup() {
		
		int totalWords = _words.size();
		
		// Detect if there are no words to be tested
		if (totalWords == 0) {
			if (_testFile.getName().equals(".failed")) {
				_status = "There are currently no failed words.";
				return;
			}
			_status = "The wordlist is empty! Please load the wordlist file with words for testing and try again...";
			return;
		}
		
		// Sets up the number of words to be tested
		else if (totalWords < 3) {
			_numTestedWords = totalWords;
		}
		else {
			_numTestedWords = 3;
		}
		
		// Display a title for a test or review
		if (_testFile.getName().equals("wordlist")) {
			_txtDisplay.append("*NEW SPELLING QUIZ*\n");
		} else {
			_txtDisplay.append("*REVIEW MISTAKES*\n");
		}
		
		
		// Get random words for testing
		_testWords = new String[_numTestedWords];
		
		for (int i = 0; i < _testWords.length; i++) {
			int rdm = new Random().nextInt(_words.size());
			_testWords[i] = _words.get(rdm);
			_words.remove(rdm);
		}
		
		// Set up current tested word and counter for the word and the attempt
		_currentTestWord = _testWords[0];
		_currentWordCount = 1;
		_currentAttempt = 1;
	}
	
	
	// Calls the Festival TTS in Bash
	private void festivalCall(String spokenText) {
		
		
		ProcessBuilder builder = new ProcessBuilder("/bin/bash","-c", "echo " + spokenText + " | festival --tts");
		
		try {
			Process process = builder.start();
			process.waitFor();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	// This method updates the .wordStats file and transfers the tested word between the different test status files if necessary
	public void update(String word, String testStatus) {
		
		int wordIndex = 0;
		String[] wordStatArray = null;
		List<String> statList = new ArrayList<String>();
		String previousStatus;
		File testStatsFile = new File(_currentPath,".wordStats");	
			
		// Reads the .wordStats file and store them inside a string array
		try {
			BufferedReader br = new BufferedReader(new FileReader(testStatsFile));
			String allStats = br.readLine();
			String[] statArray = allStats.split("%"); // Retrieved from http://stackoverflow.com/questions/10796160/splitting-a-java-string-by-the-pipe-symbol-using-split
			statList = Arrays.asList(statArray);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// Based on the new test status of the current word, determine whether it is mastered, faulted or failed so to update corresponding stat in file	
		int statArrayIndex = 0;
			
		switch (testStatus) {
		case "mastered": statArrayIndex = 1;
		break;
		case "faulted": statArrayIndex = 2;
		break;
		case "failed": statArrayIndex = 3;
		}
		
		// Find the statistics that correspond to the word being tested
		int index = 0;
		for (String aStat: statList) {
			if (aStat.startsWith(word)) {
				wordStatArray = aStat.split(" ");
				wordIndex = index;
				break;
			}
			index++;
		}
	
		// Increment the corresponding stat in the tested word
		int incrementedStat = Integer.parseInt(wordStatArray[statArrayIndex]) + 1;
		wordStatArray[statArrayIndex] = "" + incrementedStat; 
		
		// Get the previous test status and update the current test status of the word
		previousStatus = wordStatArray[4];
		wordStatArray[4] = testStatus;
		
		// Join up the strings of the array indexes together into the original file-format single-line string
		String updatedSingleWordStats;
		
		StringBuilder builder = new StringBuilder();
		for (String stat: wordStatArray) {
			builder.append(stat + " ");
		}
		updatedSingleWordStats = builder.toString().trim();
		statList.set(wordIndex, updatedSingleWordStats); 
		String updatedStats; 
		
		builder = new StringBuilder();
		for (String wordStat: statList) {
			builder.append(wordStat + "%");
		}
		updatedStats = builder.toString();
		
		// Overwrite the original statistics in the .wordStats file
		try {
			FileWriter fw = new FileWriter(testStatsFile);
			fw.write(updatedStats);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// File transfer operations
		listTransfer(word, new File(_currentPath,"." + previousStatus), new File(_currentPath, "." + testStatus));
	}
	
	
	//Deletes a word from a list file and adds it to another one
	private void listTransfer(String word, File from, File to) {
		if (from.equals(to)) {
			//Nothing happens, exit method. Files untouched.
		}
		else {
			deleteWordInFile(word,from);
			try {
				FileWriter fw = new FileWriter(to,true);
				fw.write("" + word + "\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	// This method deletes a line that is a word in a file by copying everything but that word to a new file, deleting the original file, then renaming the new file to the original file's name
	private void deleteWordInFile(String word, File file) {
		
		try {
			
			File originalFile = file;
			
			File tempFile = new File(_currentPath,".tempFile");
			PrintWriter pw;
			pw = new PrintWriter("" + tempFile,"UTF-8");
			
			BufferedReader br = new BufferedReader(new FileReader(originalFile));
			String line = "";
			
			while ((line = br.readLine()) != null) {	
				if (line.startsWith(word)) {
					continue;
				}
				pw.println(line);
			}
			br.close();
			pw.close();
			
			String originalPathName = originalFile.getAbsolutePath();
			originalFile.delete();
			
			tempFile.renameTo(new File(originalPathName));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
