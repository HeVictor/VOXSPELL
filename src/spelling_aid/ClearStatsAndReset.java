package spelling_aid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClearStatsAndReset implements SpellingAidFunction{
	
	String _currentPath;
	File[] _fileList;
	ArrayList<String> _storedWords = new ArrayList<String>();
	
	
	public ClearStatsAndReset(String currentPath, File[] fileList) {
		_currentPath = currentPath;
		_fileList = fileList;
	}

	@Override
	public String processFunction() {
		
		FileWriter fw;
		PrintWriter pw;
		File wordList = new File(_currentPath,"wordlist");
		
		//Retrieved from http://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-using-java
		try (BufferedReader br = new BufferedReader(new FileReader(wordList))) { // Read the wordlist file
			String word;
			String wordStatSection;
			
			//File writing retrieved from http://stackoverflow.com/questions/2885173/how-to-create-a-file-and-write-to-a-file-in-java
			while ((word = br.readLine()) != null) { // Store each word
				_storedWords.add(word);
			}
			br.close();
			
			// Sort the words alphabetically to display in View Stats later
			Collections.sort(_storedWords);
			
			// Write the default stats of each word to the .wordStats file in current directory
			fw = new FileWriter(_fileList[0]);
			for (String sortedWord: _storedWords) {
				wordStatSection = sortedWord + " 0 0 0 untested%";
				fw.write(wordStatSection);
			}
			fw.close();
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Copies all the words from wordlist to the .untested file in current directory
		copyFile(wordList,_fileList[1]);
		
		// Initialize blank files for .mastered, .faulted and .failed lists in current directory
		for (File f: Arrays.copyOfRange(_fileList, 2, 5)) {
			try {
				pw = new PrintWriter("" + f);
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return "";
	}
	

	//Retrieved from https://crunchify.com/java-file-copy-example-simple-way-to-copy-file-in-java/
	private void copyFile(File f1, File f2) { // This method copies a file content to another
			
		InputStream in = null;
		OutputStream out = null;
			
		try {
				
			in = new FileInputStream(f1);
			out = new FileOutputStream(f2);
			
			byte[] buffer = new byte[1024];
			
			int len;
			while ((len = in.read(buffer)) > 0){
				out.write(buffer, 0, len);
			}
			
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
		
}
