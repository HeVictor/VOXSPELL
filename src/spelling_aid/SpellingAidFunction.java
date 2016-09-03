package spelling_aid;

public interface SpellingAidFunction { // An interface for Spelling Aid app functions
	
	/*
	 * This interface method executes the Spelling Aid function of the relevant subclass. 
	 * By default it returns an empty string if execution is successful, otherwise the error message
	 * should be printed to a JOptionPane.
	 */
	public String processFunction();

}
