package pubmedresearch;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import pubmedresearch.bo.MatchingItem;
import pubmedresearch.bo.PatternContainer;
import pubmedresearch.bo.PubMedObject;
import pubmedresearch.bo.RelationEntity;
import utils.FileUtils;


public class PubMedProcessor {
	
	List<PubMedObject> listOfPublications;
	List<PatternContainer> patternList;
	LexicalizedParser lexicalParser;
	PubMedStanfordParser stanfordParser;
	Map<String, Tree> lexicalParserCache;
	
	public PubMedProcessor(String option, boolean applyAbbreviationExpansion){
		init(option, applyAbbreviationExpansion);
	}
	
	public void init(String option, boolean applyAbbreviationExpansion){
	
		FileUtils fileUtils = new FileUtils();
		
		// Delete all existing files under "to be processed" folder
		fileUtils.deletePubMedPublications();
		
		// Process the publications with Stanford Parser and convert them to usable txt files
		fileUtils.preProcessPublicationsWithStanfordParser(option, applyAbbreviationExpansion);
		
		// Retrieve the list of patterns
		patternList = fileUtils.getListOfPatterns();
				
		// Retrieve the files one by one
		listOfPublications = fileUtils.readPubMedPublicationsFromFile();
		
		// Lexical Parser taken to memory..
		stanfordParser = new PubMedStanfordParser();
		lexicalParser = stanfordParser.getLexicalParser();
		
		lexicalParserCache = FileUtils.readLexicalParseFromFile();	
		
	}

	
	public List<PubMedObject> getPubMedList(String option) {
		
		Pattern p 		= null;
		Matcher matcher = null;
		Scanner scanner = null;
		List<MatchingItem> matchingItemList = null;
		String beforePatternText = "";
		String afterPatternText = "";
		MatchingItem matchingItem = null;
		List<PubMedObject> listOfPubMedObjects = new ArrayList<PubMedObject>();
		
		// For each publication scan all the patterns..
		for (PubMedObject pubMedObject : listOfPublications){
			
			matchingItemList = new ArrayList<MatchingItem>();
			String abstractText = pubMedObject.getAbstractText();
		    
			if (abstractText != null){

				scanner = new Scanner(abstractText);
				
				while(scanner.hasNextLine()) {
					
					String line = scanner.nextLine();
					matchingItemList = new ArrayList<MatchingItem>();
					
					// For each line try each pattern and evaluate the results
				    for (PatternContainer patternObj : patternList){
				    	
				    	p = Pattern.compile(patternObj.getPattern());
				    	matcher = p.matcher(line);
				    	
				    	while (matcher.find()){
				    		
				    		beforePatternText = "";
				    		afterPatternText = "";
				    		try{
				    			String patternName = patternObj.getPatternName();
					    		String patternDependencyText = patternObj.getDependencyText();
					    		
					    		// Find the Token coming After Pattern 
					    		String prefix 	= line.substring(0, matcher.end());
					    		String postfix = line.substring(matcher.end() + 1, line.length());
					    		 

					    		//Calculate the order of the pattern term..
					    		String prelength = line.substring(0, matcher.start());
					    		int patternIndex = (prelength.split("\\s")).length;
					    		
					    		// SPECIAL PATTERNS THAT ARE ADDED AFTER EVOLUTION
					    		if (patternName.equals("projection from")){
					    			beforePatternText = "";
					    			Pattern projectionPattern = Pattern.compile("(?i)projection(s){0,1} from (.)*? to ");
					    			Matcher projectionMatcher = projectionPattern.matcher(line);
					    			if(projectionMatcher.find()){
					    				patternName = "projection from to";
					    				String midterm = line.substring(projectionMatcher.start(), projectionMatcher.end());
					    	    		beforePatternText = midterm.substring(midterm.trim().indexOf(" "), midterm.trim().lastIndexOf(" "));
					    	    		prefix = line.substring(0, projectionMatcher.end());
					    			}
					    		} else if (patternName.equals("projection to")){
					    			beforePatternText = "";
					    			Pattern projectionPattern = Pattern.compile("(?i)projection(s){0,1} to ((\\w+)\\s){0,8} from ");
					    			Matcher projectionMatcher = projectionPattern.matcher(line);
					    			if(projectionMatcher.find()){
					    				String midterm = line.substring(projectionMatcher.start(), projectionMatcher.end());
					    	    		beforePatternText = midterm.substring(midterm.trim().indexOf(" "), midterm.trim().lastIndexOf(" "));
					    	    		prefix = line.substring(0, projectionMatcher.end());
					    			}
					    		} else if (patternName.equals("project to")){
					    			beforePatternText = "";
					    			Pattern projectionPattern = Pattern.compile("(?i)project(s|ed|ing){0,1} from ((\\w+)\\s){0,8} to ");
					    			Matcher projectionMatcher = projectionPattern.matcher(line);
					    			if(projectionMatcher.find()){
					    				String midterm = line.substring(projectionMatcher.start(), projectionMatcher.end());
					    	    		beforePatternText = midterm.substring(midterm.trim().indexOf(" "), midterm.trim().lastIndexOf(" "));
					    	    		prefix = line.substring(0, projectionMatcher.end());
					    			}
					    		} else if (patternName.equals("exit through")){
					    			beforePatternText = "";
					    			Pattern projectionPattern = Pattern.compile("(?i)exit(s|ing){0,1} (.)*? through ");
					    			Matcher projectionMatcher = projectionPattern.matcher(line);
					    			if(projectionMatcher.find()){
					    				String midterm = line.substring(projectionMatcher.start(), projectionMatcher.end());
					    	    		beforePatternText = midterm.substring(midterm.trim().indexOf(" "), midterm.trim().lastIndexOf(" "));
					    	    		prefix = line.substring(0, projectionMatcher.end());
					    			}
					    		} else if (patternName.equals("travelling to")){
					    			beforePatternText = "";
					    			Pattern projectionPattern = Pattern.compile("(?i)travel(s|ling){0,1} from (.)*? to ");
					    			Matcher projectionMatcher = projectionPattern.matcher(line);
					    			if(projectionMatcher.find()){
					    				String midterm = line.substring(projectionMatcher.start(), projectionMatcher.end());
					    	    		beforePatternText = midterm.substring(midterm.trim().indexOf(" "), midterm.trim().lastIndexOf(" "));
					    	    		prefix = line.substring(0, projectionMatcher.end());
					    			}
					    		}
					    		
					    		
					    		
					    		afterPatternText = getAfterPatternTokens(line, prefix, patternName);
					    		
					    		if (postfix.contains(":")){
					    			postfix = postfix.replace(afterPatternText, "").trim();
						    		if (postfix.startsWith(":")){
						    			afterPatternText += " " + postfix.substring(1, postfix.length());
						    		}
					    		}	
				    		
					    		// Find the Token Coming Before Pattern
					    		if (beforePatternText.equals("")){
					    			beforePatternText = getBeforePatternTokens(line, patternIndex, patternName, patternDependencyText);	
					    		}
					    		 
					    		
					    		// Keep all matching "pattern"-"matching item"pairs for evaluation..
					    		String tempLine = line;
					    		tempLine = tempLine.replace("-LRB- ", "(");
					    		tempLine = tempLine.replace(" -RRB-", ")");
					    		tempLine = tempLine.replace(" , and ", " and ");
					    		tempLine = tempLine.replace(". , ", " , ");
					    		tempLine = tempLine.replace(" - ", "");
					    		
					    		
					    		if (afterPatternText.trim().length()>0 && beforePatternText.trim().length()>0){
					    			afterPatternText = afterPatternText.replace("   ", " ");
					    			beforePatternText = beforePatternText.replace("   ", " ");
					    			afterPatternText = afterPatternText.replace("  ", " ");
					    			beforePatternText = beforePatternText.replace("  ", " ");
					    			matchingItem = new MatchingItem(pubMedObject.getId(), patternName, tempLine, beforePatternText, afterPatternText);
					    			matchingItemList.add(matchingItem);
					    		}
					    		
					    		
					    	} catch(Exception e){
					    		e.printStackTrace();
				    		}
					    }
				    	
				    }
				
				    pubMedObject.getListOfMatches().add(matchingItemList);
				    
				}
				
				scanner.close();
			}
			
			listOfPubMedObjects.add(pubMedObject);
				
		}
		
		FileUtils.writeLexicalParseToFile(lexicalParserCache);
		
		return listOfPubMedObjects;
	}

	
	/**
	 * This method returns the first NP after the matching Pattern
	 * @param String line 		: Sentence from the publication which includes a pattern
	 * @param String prefix  	: The part of the tokens from start to end of matching pattern
	 * @param String pattern 	: Name of the pattern
	 * @return String 			: NP Tree which satisfies the pattern 
	 * */
	public String getAfterPatternTokens(String line, String prefix, String pattern){
		
		String afterPatternText = "";
		
		StringTokenizer tokenizer = new StringTokenizer(prefix);
		int afterPatternIndex = 0;
		int patternTokenCount = tokenizer.countTokens(); 
		for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(line))){
			
			Tree tree = getLexicalParse(line, sentence);
				
			boolean isTokenMatched = false;
			boolean isNPRetrieved = false;
			Iterator<Tree> it = tree.iterator();
			while (it.hasNext()){
				Tree subtree = it.next();
				// When the pattern token is reached we look for the first NP after the pattern..
				if (isTokenMatched){
					if (isNPRetrieved){
						if (subtree.label().value().equals("PP") && it.next().label().value().equals("VBG") && it.next().label().value().equalsIgnoreCase("including")){
							for (Tree leafTree : subtree.getLeaves()){
								afterPatternText += " " + leafTree.label().value() ;	
							}
							break;
						}
					} else if (!isNPRetrieved && subtree.label().value().equals("NP")||subtree.label().value().equals("NNP")){
						for (Tree leafTree : subtree.getLeaves()){
							afterPatternText += leafTree.label().value() + " ";
							it.next(); it.next();
						}
						afterPatternText = afterPatternText.substring(0, afterPatternText.length()-1);
						isNPRetrieved = true;
					} 
					
				} else { // Until the pattern token is reached all leaves are checked
					if (subtree.isLeaf()){
    					afterPatternIndex++;
    					if (patternTokenCount == afterPatternIndex){
	    					isTokenMatched = true;
	    				}	
    				}	
				}
			}
		}
		
		return afterPatternText;
	}

	public String getBeforePatternTokens(String line, int patternIndex, String patternName, String patternDependencyText){
	
		String beforePatternText = "";

		if (beforePatternText.equals("")){
			beforePatternText = getRegularPatternsWithTypedDependency(line, patternIndex, patternDependencyText, patternName);	
		}

		return beforePatternText;	
		
	}
		
	private String getRegularPatternsWithTypedDependency(String line, int patternIndex, String patternDependencyText, String patternName) {
		
		String beforePatternText = "";
		String regularExpression = "(?i)(.*)" + patternDependencyText + "(.*)";
		
		for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(line))){
						
			Tree tree = getLexicalParse(line, sentence);
		    TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		    GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		    
		    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		    
		    for (TypedDependency td : tdl){
		    	 if (td.toString().matches(regularExpression)){
		    		 // If in the same position..
		    		 if (td.gov().index() == patternIndex + 1 || td.dep().index() == patternIndex + 1){
			    		 if (td.reln().equals(EnglishGrammaticalRelations.DIRECT_OBJECT)){
			    			 beforePatternText += " " + getDOBJList(tdl, td);
			    		 } else if (td.reln().toString().equals("nsubj") || td.reln().toString().equals("xsubj") || td.reln().toString().equals("nsubjpass")|| td.reln().toString().equals("nn")){
			    			 
			    			 beforePatternText += " " + getNSUBJList(tdl, td, regularExpression);
			    			 break;
			    			 
			    		 } else if (td.reln().toString().equals("vmod")){
			    			 beforePatternText += " " + getVModList(tdl, td, regularExpression);
			    		 }
		    		 } else {
		    			 // DO NOTHING : System.out.println("double pattern");
		    		 }
		    	 }
		    }	
		    System.out.println("*********************************");
		    System.out.println(line);
		    System.out.println(beforePatternText);
		    for (TypedDependency td : tdl){
		    	System.out.println(td.toString());
		    }
		    
		    if (patternName.equals("projection to")){
		    	List<String> compList = new ArrayList<String>();
		    	String keyTerm = "";
		    	for (TypedDependency td : tdl){
		    		if (td.toString().matches(regularExpression)){
		    			 if (td.reln().toString().equals("nn") || td.reln().toString().equals("amod")){
		    					keyTerm = td.gov().toString();
		    				 	String depWord = td.dep().toString();
		    					compList.add(depWord);
		    			 }
		    		}
		    	}
		    	compList.add(keyTerm);
		    	 beforePatternText += " " + sortList(compList);
		    }
		}
		
		return beforePatternText.trim();
	}

	private String getDOBJList(List<TypedDependency> tdl, TypedDependency td) {
		
		String text = "";
		String govWord = td.gov().toString();
		
		List<String> nsubjList = getNextRelation(tdl, EnglishGrammaticalRelations.NOMINAL_SUBJECT.toString(), govWord, "");
		for(String nsubj : nsubjList){
			List<String> compList = getSupplementaryContent(tdl, nsubj);
			List<String> prepList = getNextRelation(tdl, EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER.toString(), nsubj, "");
			for(String prep : prepList){
				compList.addAll(getSupplementaryContent(tdl, prep));	
			}
			text += " " + sortList(compList);
			
			List<String> negList = getNextRelation(tdl, EnglishGrammaticalRelations.NEGATION_MODIFIER.toString(), nsubj, "");
			if(negList.size()>0){
				// System.out.println("neglected : " + negList.toString());
			}
		}
		
		return text;
	}

	public List<String> getSupplementaryContent(List<TypedDependency> tdl,	String nsubj) {
		
		List<String> compList = getNextRelation(tdl, EnglishGrammaticalRelations.ADJECTIVAL_MODIFIER.toString(), nsubj, "");
		compList.addAll(getNextRelation(tdl, "nn", nsubj, ""));
		
		compList.add(nsubj);
		
		return compList;
	}

	private String getNSUBJList(List<TypedDependency> tdl, TypedDependency td, String regularExpression) {
		
		String text = "";
		String govWord = td.gov().toString();
		String depWord = td.dep().toString();
		
		if (depWord.matches(regularExpression)){
			List<String> nsubjList = getNextRelation(tdl, EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER.toString(), govWord, "");
			for(String nsubj : nsubjList){
				List<String> compList = getSupplementaryContent(tdl, nsubj);
				text = text + " " + sortList(compList);
			}
			List<String> negList = getNextRelation(tdl, EnglishGrammaticalRelations.NEGATION_MODIFIER.toString(), govWord, "");
			if(negList.size()>0){
				// DO NOTHING : System.out.println("neglected : " + negList.toString());
			}
			
		} else if (govWord.matches(regularExpression)){

			List<String> termList = getNextRelation(tdl, null, depWord, govWord);
			termList.add(depWord);
//			for(String nsubj : termList){
//				List<String> compList = getSupplementaryContent(tdl, nsubj);
//				tempList.addAll(compList);
//				text = text + " " + compList;
//			}
			text = sortList(termList);

			List<String> negList = getNextRelation(tdl, EnglishGrammaticalRelations.NEGATION_MODIFIER.toString(), depWord, "");
			
			if(negList.size()>0){
				// DO NOTHING : System.out.println("neglected : " + negList.toString());
			}
		}
		
		return text;
	}

	private String getVModList(List<TypedDependency> tdl, TypedDependency td, String regularExpression) {
		
		String text = "";
		String govWord = td.gov().toString();
		String depWord = td.dep().toString();
		
		if (depWord.matches(regularExpression)){
			List<String> compList = getSupplementaryContent(tdl, govWord);
			text = sortList(compList);
		}
		return text;
	}
	
	private List<String> getNextRelation(Collection<TypedDependency> tdl, String relation, String gov, String original) {
		
		String nextItem = null;
		List<String> listOfItems = new ArrayList<String>();
		for(TypedDependency tdep : tdl){
			if (relation == null || tdep.reln().getShortName().equals(relation)){ 
				if (gov != null && tdep.gov().toString().matches(gov)){
					nextItem = tdep.dep().toString();
					if (!nextItem.equals(original)){
						listOfItems.add(nextItem);
					}
				} 
			}
		}
		
		return listOfItems;
	}
	
	private String sortList(List<String> list){
		
		String term;
		int order = 0;
		List<RelationEntity> unsortedList = new ArrayList<RelationEntity>();
		
		for (String item:list){
			if (item.contains("-")){
				term = item.substring(0, item.lastIndexOf("-"));
				try{
					order = Integer.parseInt(item.substring(item.lastIndexOf("-") + 1, item.length()));	
				} catch(Exception e){
					// DO NOTHING
				}
				unsortedList.add(new RelationEntity(term, order));				
			} 
		}
		Collections.sort(unsortedList, new DependencyComparator());

		String sortedText = "";
		String previousTerm = "", newTerm = ""; 
		for (RelationEntity entity:unsortedList){
			newTerm = entity.getTerm();
			if(!previousTerm.equals(newTerm)){
				sortedText = sortedText +  " " + entity.getTerm();
			}
			previousTerm = newTerm;
		}
		return sortedText;
	}

	private Tree getLexicalParse(String line, List<HasWord> sentence) {
		
		Tree tree = null;
		
		if (lexicalParserCache.containsKey(line)){
			tree = lexicalParserCache.get(line);
		} else {
			tree = lexicalParser.apply(sentence);
			lexicalParserCache.put(line, tree);
		}
		
		return tree;
	}
	
}
