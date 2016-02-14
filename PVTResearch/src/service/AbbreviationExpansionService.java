package service;

import java.util.*;
import java.io.*;


public class AbbreviationExpansionService {

    int truePositives = 0, falsePositives = 0, falseNegatives = 0, trueNegatives = 0;
    char delimiter = '\t';
	String shortForm, longForm, defnString, str;
	String[] candidate;
	String filename =  null;
	String testList = null;
	Map<String, String> candidates = null;    

	public Map<String, String> extractAbbreviationPairs(String inFile) {

		String str, tmpStr, longForm = "", shortForm = "";
		String currSentence = "";
		int openParenIndex, closeParenIndex = -1, sentenceEnd, newCloseParenIndex, tmpIndex = -1;
		boolean newParagraph = true;
		StringTokenizer shortTokenizer;
		candidates = new HashMap<String, String>();

		try {
		    BufferedReader fin = new BufferedReader(new FileReader (inFile));
		    while ((str = fin.readLine()) != null) {
			if (str.length() == 0 || newParagraph && 
			    ! Character.isUpperCase(str.charAt(0))){
			    currSentence = "";
			    newParagraph = true;
			    continue;
			}
			newParagraph = false;
			str += " ";
			currSentence += str;
			openParenIndex =  currSentence.indexOf(" (");
			do {
			    if (openParenIndex > -1)
				openParenIndex++;
			    sentenceEnd = Math.max(currSentence.lastIndexOf(". "), currSentence.lastIndexOf(", "));
			    if ((openParenIndex == -1) && (sentenceEnd == -1)) {
				//Do nothing
			    }
			    else if (openParenIndex == -1) {
				currSentence = currSentence.substring(sentenceEnd + 2);
			    } else if ((closeParenIndex = currSentence.indexOf(')',openParenIndex)) > -1){
				sentenceEnd = Math.max(currSentence.lastIndexOf(". ", openParenIndex), 
						       currSentence.lastIndexOf(", ", openParenIndex));
				if (sentenceEnd == -1)
				    sentenceEnd = -2;
				longForm = currSentence.substring(sentenceEnd + 2, openParenIndex);
				shortForm = currSentence.substring(openParenIndex + 1, closeParenIndex);
			    }
			    if (shortForm.length() > 0 || longForm.length() > 0) {
				if (shortForm.length() > 1 && longForm.length() > 1) {
				    if ((shortForm.indexOf('(') > -1) && 
					((newCloseParenIndex = currSentence.indexOf(')', closeParenIndex + 1)) > -1)){
					shortForm = currSentence.substring(openParenIndex + 1, newCloseParenIndex);
					closeParenIndex = newCloseParenIndex;
				    }
				    if ((tmpIndex = shortForm.indexOf(", ")) > -1)
					shortForm = shortForm.substring(0, tmpIndex);			    
				    if ((tmpIndex = shortForm.indexOf("; ")) > -1)
					shortForm = shortForm.substring(0, tmpIndex);
				    shortTokenizer = new StringTokenizer(shortForm);
				    if (shortTokenizer.countTokens() > 2 || shortForm.length() > longForm.length()) {
					// Long form in ( )
					tmpIndex = currSentence.lastIndexOf(" ", openParenIndex - 2);
					tmpStr = currSentence.substring(tmpIndex + 1, openParenIndex - 1);
					longForm = shortForm;
					shortForm = tmpStr;
					if (! hasCapital(shortForm))
					    shortForm = "";
				    }
				    if (isValidShortForm(shortForm)){
				    	extractAbbrPair(shortForm.trim(), longForm.trim());
				    }
				}
				currSentence = currSentence.substring(closeParenIndex + 1);
			    } else if (openParenIndex > -1) {
				if ((currSentence.length() - openParenIndex) > 200)
				    // Matching close parenthesis was not found
				    currSentence = currSentence.substring(openParenIndex + 1);
				break; // Read next line
			    }
			    shortForm = "";
			    longForm = "";
			} while ((openParenIndex =  currSentence.indexOf(" (")) > -1);
		    }
		    fin.close();
		} catch (Exception ioe) {
		    ioe.printStackTrace();
		    System.out.println(currSentence);
		    System.out.println(tmpIndex);
		}
		    return candidates;
	 }

    private String findBestLongForm(String shortForm, String longForm) {
		int sIndex;
		int lIndex;
		char currChar;

		sIndex = shortForm.length() - 1;
		lIndex = longForm.length() - 1;
		for ( ; sIndex >= 0; sIndex--) {
		    currChar = Character.toLowerCase(shortForm.charAt(sIndex));
		    if (!Character.isLetterOrDigit(currChar))
			continue;
		    while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar)) ||
			   ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
			lIndex--;
		    if (lIndex < 0)
			return null;
		    lIndex--;
		}
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		return longForm.substring(lIndex);
    }

    private void extractAbbrPair(String shortForm, String longForm) {
		String bestLongForm;
		StringTokenizer tokenizer;
		int longFormSize, shortFormSize;

		if (shortForm.length() == 1)
		    return;
		bestLongForm = findBestLongForm(shortForm, longForm);
		if (bestLongForm == null)
		    return;
		tokenizer = new StringTokenizer(bestLongForm, " \t\n\r\f-");
		longFormSize = tokenizer.countTokens();
		shortFormSize = shortForm.length();
		for (int i=shortFormSize - 1; i >= 0; i--)
		    if (!Character.isLetterOrDigit(shortForm.charAt(i)))
			shortFormSize--;
		if (bestLongForm.length() < shortForm.length() || 
		    bestLongForm.indexOf(shortForm + " ") > -1 ||
		    bestLongForm.endsWith(shortForm) ||
		    longFormSize > shortFormSize * 2 ||
		    longFormSize > shortFormSize + 5 ||
		    shortFormSize > 10)
		    return;
		
		this.candidates.put(shortForm, bestLongForm);  
		
	}
	   
	    
    private boolean isValidShortForm(String str) {
    	return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0)) || (str.charAt(0) == '(')));
    }

    private boolean hasLetter(String str) {
		for (int i=0; i < str.length() ; i++)
		    if (Character.isLetter(str.charAt(i)))
			return true;
		return false;
    }

    private boolean hasCapital(String str) {
		for (int i=0; i < str.length() ; i++)
		    if (Character.isUpperCase(str.charAt(i)))
			return true;
		return false;
    } 
	  
}
