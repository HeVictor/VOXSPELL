package spelling_aid;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * @author victor
 *
 */
@SuppressWarnings("serial")
public class SpellingAidGUI extends JFrame implements ActionListener{
	
	private final String WORDLIST_FILENAME = "wordlist";
	
	private final String TEXT_NEW_QUIZ = "New Spelling Quiz";
	private final String TEXT_REVIEW_MISTAKES = "Review Mistakes";
	private final String TEXT_VIEW_STATS = "View Statistics";
	private final String TEXT_CLEAR_STATS = "Clear Statistics";
	
	private JButton new_quiz_button = new JButton(TEXT_NEW_QUIZ); 
	private JButton review_mistakes_button = new JButton(TEXT_REVIEW_MISTAKES);
	private JButton view_stats_button = new JButton(TEXT_VIEW_STATS);
	private JButton clear_stats_button = new JButton(TEXT_CLEAR_STATS);
	
	private JButton[] buttons = {new_quiz_button,review_mistakes_button,view_stats_button,clear_stats_button};
	
	private JPanel buttonPane = new JPanel();
	
	private JTextField txtField = new JTextField();
	
	private JTextArea txtDisplay = new JTextArea(10,20);
	
	private JScrollPane scrollPane;
	
	private String currentPath; 
	
	private File[] fileList;
	
	private SpellingAidFunction currentFunction; 
	
	private boolean currentlyTesting = false; // This boolean indicates whether the GUI is currently in a test.
	
	
	public SpellingAidGUI() { // This sets up the main GUI 
		
		super("Spelling Aid");
		setSize(700,700);
		
		this.setLayout(new BorderLayout());
				
		buttonPane.setLayout(new GridLayout());
		
		for (JButton button: buttons) {
			buttonPane.add(button);
			button.addActionListener(this); // Adds the GUI to be the buttons' listener
			button.setActionCommand(button.getText().substring(0,1)); // Sets the action commands for each button to differentiate them in an event
		}
		
		add(buttonPane, BorderLayout.PAGE_START);	
		
		add(txtField, BorderLayout.SOUTH);
		
		txtDisplay.setEditable(false);
		scrollPane = new JScrollPane(txtDisplay);
		add(scrollPane, BorderLayout.CENTER);
		
		txtDisplay.append("Welcome to the Spelling Aid! Press one of the four buttons above to proceed...");
		
		try {
			// Retrieved from http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
			File jarFile = new File(SpellingAidGUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			currentPath = jarFile.getParentFile().getPath(); // This is the current path where the jar will be located
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		// This is the stats file as well as the files that lists each word in their test status (untested,mastered,faulted or failed).
		fileList = new File[] {new File(currentPath,".wordStats"),new File(currentPath,".untested"),
				   new File(currentPath,".mastered"),new File(currentPath,".faulted"),new File(currentPath,".failed")};
		
		//Retrieved from http://stackoverflow.com/questions/1816673/how-do-i-check-if-a-file-exists-in-java
		if (!(new File(currentPath,".wordStats").exists())) {
			new ClearStatsAndReset(currentPath,fileList).processFunction(); // This calls the Clear Stats function automatically to initialize the hidden stats files if neede
		}
		
		// Sets this GUI as the listener for the JTextField, and responds to any "Enter" key presses
		txtField.setActionCommand("EnterPressed"); 
		txtField.addActionListener(this);
		
		// Application quits when this JFrame GUI is closed.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		scrollPane.setViewportView(txtDisplay); // Set the JTextArea as the view for the scrollpane
		
		String action = e.getActionCommand();
		
		String execStatus; //Status of how a function executed
		
		if (!action.equals("EnterPressed")) {
			txtDisplay.setText(""); // Only refresh the display on non-enter key action events (e.g buttons)
		}
		
		switch(action) {
		case "N": // The New Spelling Quiz function
			
			currentFunction = new NewSpellingTest(new File(currentPath,WORDLIST_FILENAME),currentPath,txtDisplay);
			currentlyTesting = true;
			
		break;
		case "R": // The Review Mistakes function
			
			currentFunction = new NewSpellingTest(fileList[4],currentPath,txtDisplay);  //fileList[4] refers to the .failed file
			currentlyTesting = true;
			
		break;
		case "C": // The Clear Stats function
			
			currentFunction = new ClearStatsAndReset(currentPath,fileList);
			txtDisplay.setText("Stats cleared. Press one of the four buttons above to proceed...");
			
		break;
		case "V": // The View Stats function
			
			currentFunction = new ViewStats(scrollPane,fileList[0]);
			
		break;
		
		case "EnterPressed": // Enter key pressed detected, if in test currently then take user input and continues testing, otherwise does nothing.
			if (currentlyTesting == false) { 
				break;
			} else {
				
				// Gets the user input, check it against the real spelling, and continue the test.
				String txtInput = txtField.getText();
				NewSpellingTest test = (NewSpellingTest) currentFunction;
				test.doTest(txtInput);
				txtField.setText("");
				
				// If user is in review and failed a word we halt the next word to let the user spell the word with the spelling
				if (test.isInReviewAndFailed()) {
					break;
				}
				
				// If test is finished, make Enter key do nothing in GUI until in test again
				if (test.isTestFinished()) {
					currentlyTesting = false;
					break;
				}
				
				// Proceeds to next word
				test.processFunction();
			}
		break;
		
		}
		
		
		// Executes the SpellingAidFunction object and prints out error handling messages if needed
		if (!action.equals("EnterPressed")) {
			execStatus = currentFunction.processFunction();
			
			if (execStatus != "") {
				JOptionPane.showMessageDialog(txtDisplay, execStatus);
			}
		}
		
	}
	
	public static void main(String[] args) { //Taken from the framework in Lab 2, creates and runs the GUI
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SpellingAidGUI frame = new SpellingAidGUI();
				frame.setVisible(true);
			}
		});

	}

}
