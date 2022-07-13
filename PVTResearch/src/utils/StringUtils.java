package utils;

import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pubmedresearch.bo.Pair;


public class StringUtils {

	public static final String pvtPattern = "(?i)(PVT|paraventricular ((nucleus of )(the )*)*(thalamus)|(paraventricular)* thalamic (nuclei|nucleus))";
	
	public static final String fullStop = "\\.";
	
	public static final String brainPattern = "(?i)(" +
			"hypothalamus|" + 
			"cerebral cortex" +
			")";
	
	public static String getLineWithoutBrackets(String line){

		while(line.contains("-LRB-")){
			Pattern p = Pattern.compile("(-LRB-)(.*?)(-RRB-)");
			Matcher bracketMatcher = p.matcher(line);
			if (bracketMatcher.find()){
				line = line.substring(0,bracketMatcher.start()) + line.substring(bracketMatcher.end(), line.length());
			} else {
				break;
			}
		}
		
		return line;
	
	}
		
	public static String getSanitizedText(String text){

		//test change
		text = text.replace(",", "");
		text = text.replace(" and ", "");
		text = text.replace(" from ", "");
		
		return text;
	}
	
	public static String removeRedundantTerms(String text){
		
		text = text.replace(",", "");
		text = text.replace(" and ", " ");
		text = text.replace(" from ", " ");
		text = text.replace(" of ", " ");
		text = text.replace(" the ", " ");
		text = text.replace("the ", " ");
		text = text.replace("-LRB- ", "(");
		text = text.replace(" -RRB-", ")");
		text = text.replace("  ", " ");
		
		return text;
	}
	
	public static String getDirection(String comparisonResult, String patternName){
		
		String fromEntity2toEntity1 = "<--";
		String fromEntity1toEntity2 = "-->";
		
		switch (patternName){
			case "receive fiber from": case "projection from": case "projection of": case "project from":
			case "receive input from": case "receive innervation from": case "receive [ae]fferent from":
				if (comparisonResult.equals(Pair.FULL_LOC_MATCH) || comparisonResult.equals(Pair.PARTIAL_LOC_MATCH)){
					return fromEntity2toEntity1;	
				} else if (comparisonResult.equals(Pair.FULL_CROSS_MATCH) || comparisonResult.equals(Pair.PARTIAL_CROSS_MATCH)){
					return fromEntity1toEntity2;	
				}
			case "projection to" : case "project to" : case "project into" : case "terminate in" : 
			case "innervate": case "innervation of": case "projection target of": case "projection from to":	
				if (comparisonResult.equals(Pair.FULL_LOC_MATCH) || comparisonResult.equals(Pair.PARTIAL_LOC_MATCH)){
					return fromEntity1toEntity2;
				} else if (comparisonResult.equals(Pair.FULL_CROSS_MATCH) || comparisonResult.equals(Pair.PARTIAL_CROSS_MATCH)){
					return fromEntity2toEntity1;	
				}
		}
		
		return "";
		
	}
	
	public static String getAbbreviationExpandedText(String abstractText, Map<String, String> expansionPairs){

		Scanner scanner = null;
		StringBuffer abstractExpansion = new StringBuffer();
		Pattern p = null;
		Matcher matcher = null;
		String temp = null;
		boolean matchExists = false;
		
			if (abstractText != null){

				scanner = new Scanner(abstractText);
				
				while(scanner.hasNextLine()) {
					
					String line = scanner.nextLine();
					temp = "";
					for (String shortForm : expansionPairs.keySet()){
					
						p = Pattern.compile(shortForm);
						matcher = p.matcher(line);
						int endIndex = 0;
						matchExists = false;
						
						while (matcher.find()){
							
							int tempStartIndex = 0;
							int tempEndIndex = line.length();
							if (matcher.start()!=0) tempStartIndex = matcher.start()-1;
							if (matcher.end()!= line.length()) tempEndIndex = matcher.end() + 1; 
							
							if(!line.substring(tempStartIndex, tempEndIndex).equalsIgnoreCase("("+shortForm+")")){
								temp += line.substring(endIndex, matcher.start()) + expansionPairs.get(shortForm) + "(" + shortForm + ")" ;
								endIndex = matcher.end();
								matchExists = true;
							}
								
						}
						if (matchExists){
							temp = temp + line.substring(endIndex, line.length());   
							System.out.println(shortForm + " ||| " + line);
							System.out.println(shortForm + " ||| " + temp);
							line = temp;
							temp = "";
						}
						
					}
					abstractExpansion.append(line + "\n");
				}
				
				scanner.close();
			}
			
		return abstractExpansion.toString();
	}
}
