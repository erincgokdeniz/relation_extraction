package pubmedresearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pubmedresearch.bo.BrainRegion;
import pubmedresearch.bo.EvaluationObject;
import pubmedresearch.bo.MatchingItem;
import pubmedresearch.bo.Pair;
import pubmedresearch.bo.PatternContainer;
import pubmedresearch.bo.PubMedObject;
import service.WhitetextService;
import utils.FileUtils;
import utils.StringUtils;

public class PubMedEvaluator {

	Map<String, List<MatchingItem>> matchingItemMap;
	Map<String, BrainRegion> brainRegionsMap;
	Map<String, BrainRegion> acronymsMap;
	Map<String, BrainRegion> synonymsMap;
	WhitetextService whitetextService;
	Map<String, Map<String, BrainRegion>> brainMap;
	List<PatternContainer> patternList;
	String option;
	
	public PubMedEvaluator(String option){
		this.option = option;
		init(option);
	}
	
	public void init(String option){
		
		FileUtils fileUtils = new FileUtils();
		
		// Retrieve the list of patterns
		patternList = fileUtils.getListOfPatterns();
		
		// Whitetext Corpus initiation
		if (option != null && option.startsWith("whitetext")){
			whitetextService = new WhitetextService();
			whitetextService.init(option, patternList);	
		} else {
		// Retrieve the list of brain regions incl. acronyms and synonyms
			brainMap = fileUtils.getBrainRegions();
			brainRegionsMap = brainMap.get("brainRegions");
			acronymsMap = brainMap.get("acronymsMap");
			synonymsMap = brainMap.get("synonymsMap");
		}
		
	}
	
	
	public List<String> isInBrainRegions(String text, Map<String, BrainRegion> brainRegionMap){
		
		text = StringUtils.getLineWithoutBrackets(text);
		
		List<String> termList = new ArrayList<String>();
		Map<String,String> termMap = new HashMap<String, String>();
		
		if (brainRegionMap.containsKey(text.trim().toLowerCase())){
			termMap.put(text, text);
		} else {
			String[] tokensVal = text.split(",|;| (?i)and | (?i)or |  ");
			for (String token : tokensVal){
				if (token != null && token.trim().length() >0){
					token = token.trim().toLowerCase();
					token = FileUtils.smoothText(token);
					if (brainRegionMap.containsKey(token)){
						termMap.put(token, token);
					} else {
						String[] tokenList = token.split(" ");
						for (String term : tokenList){
							if (term != null && term.trim().length() >0 && brainRegionMap.containsKey(term.trim().toLowerCase())){
								termMap.put(term, term);
							}
						}
						for (String region : brainRegionMap.keySet()){
							if (region != null && region.split(" ").length > 1 && token.toLowerCase().contains(region)){
								termMap.put(region, region);
							}
						}
					}
				}
			}
		}

		for (String key : termMap.keySet()){
			boolean isKeyValid = true;
			for (String value : termMap.values()){
				if (!key.equalsIgnoreCase(value)){
					if (value.contains(key)){
						isKeyValid = false;
					}
				}
			}
			if (isKeyValid){
				termList.add(key);
			}
		}
		
		return (termList);
	}
	
	
	/**
	 * This method is used for PVT-specific evaluation..
	 * */
	public List<EvaluationObject> calculateMatch(String id, List<MatchingItem> matchingItemList, Map<String, BrainRegion> brainRegionMap){
		
		List<String> beforePatternList = new ArrayList<String>();
		List<String> afterPatternList = new ArrayList<String>();
		
		List<EvaluationObject> evaluationList = new ArrayList<EvaluationObject>();
		EvaluationObject evaluation = null;
		
		for(MatchingItem item : matchingItemList){
			System.out.println("********************************");
			System.out.println(item.getId() + " : " + item.getPatternName() + " : " + item.getSentence());
			System.out.println("X : " + item.getBeforePatternText());
			beforePatternList = isInBrainRegions(item.getBeforePatternText(), brainRegionMap); 
			System.out.println("Y : " + item.getAfterPatternText());
			afterPatternList = isInBrainRegions(item.getAfterPatternText(), brainRegionMap);
			System.out.print(beforePatternList);
			System.out.println(afterPatternList);
			
			for(String entityX : beforePatternList){
				for (String entityY : afterPatternList){
					if (!entityX.equalsIgnoreCase(entityY) && (entityX != null || entityY != null)){
						//item.getPairs().put(entityX+ "-" +entityY, new Pair(entityX, entityY));
						System.out.println(entityX+ "-" +entityY);
						evaluation = new EvaluationObject();
						evaluation.setLine(item.getSentence());
						evaluation.setId(item.getId());
						evaluation.setPatternName(item.getPatternName());
						evaluation.getPairList().add(new Pair(entityX, entityY, "TRUE", StringUtils.getDirection(Pair.FULL_LOC_MATCH, item.getPatternName())));
						evaluationList.add(evaluation);
					}
				}
			}
		}
		
		// FileUtils.writeMatchingItemsToFile(id, matchingItemList);
		
		return evaluationList;
	}

	public int calculateRecall(List<PubMedObject> listOfPublications, Map<String, BrainRegion> brainRegionMap){
		
		Scanner scanner = null;
		int numberOfRelations = 0;
		
		for (PubMedObject pubMedObject : listOfPublications){
			
			String abstractText = pubMedObject.getAbstractText();
			
			if (abstractText != null){
				
				scanner = new Scanner(abstractText);
				
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine();
					//line = StringUtils.getLineWithoutBrackets(line);
					List<String> listOfBrainRegions = isInBrainRegions(line, brainRegionMap);
					if (listOfBrainRegions.size()>1){
						numberOfRelations++;
					}
				}
				
				scanner.close();
			}

				
		}
			
		return numberOfRelations;		
	}
	
	public void evaluate(List<PubMedObject> listOfPubMedObjects){
		
		List<EvaluationObject> evaluationList = new ArrayList<EvaluationObject>();
		List<List<MatchingItem>> listOfMatches; 
		
		for (PubMedObject pubMed : listOfPubMedObjects){
			
			listOfMatches = pubMed.getListOfMatches();
			if (this.option.startsWith("whitetext")){
		    	for (List<MatchingItem> matchingItemList : listOfMatches){
					if (matchingItemList.size()>0){
			    		List<EvaluationObject> temp = whitetextService.compare(matchingItemList);
		    			evaluationList.addAll(temp);
				    }	
				}
			} else {
				for (List<MatchingItem> matchingItemList : listOfMatches){
			    	if(matchingItemList.size()>0){
			    		List<EvaluationObject> temp = calculateMatch(pubMed.getId(), matchingItemList, this.brainRegionsMap);
				    	evaluationList.addAll(temp);	
			    	}
			    }
			}
			

//			// Use if No Brain Region is applied
//			//FileUtils.writeMatchingItemsToFile(pubMed.getId(), pubMed.getMatchingItemList());
		}
		
//		System.out.println(evaluator.calculateRecall(listOfPubMedObjects, processor.getBrainRegionMap()));
				
		
		// I have all the list of matches at this point for the paper..
		Map<String, Pair> evaluationMap = new HashMap<String, Pair>();
		int truePositiveCount = 0;
		int falsePositiveCount = 0;
		for(EvaluationObject eval:evaluationList){
			for (Pair pair: eval.getPairList()){
				String key = eval.getId() + eval.getLine().substring(0,15) + pair.getEntityX() + pair.getEntityY();
				if (evaluationMap.containsKey(key)){
					//System.out.println("*** NOT INCLUDED " + key);
				} else {
					evaluationMap.put(key, pair);	
					System.out.print(eval.getId() + "| ");
					System.out.print(eval.getPatternName() + "| ");
					System.out.print(pair.getEntityX() + "| ");
					System.out.print(pair.getDirection() + "| ");
					System.out.print(pair.getEntityY() + "| ");
					System.out.print(pair.getMatch() + "| ");
					System.out.print("\"" + eval.getLine()+ "\"\n");
					if (pair.getMatch().equalsIgnoreCase("true")){
						truePositiveCount++;	
					} else if (pair.getMatch().equalsIgnoreCase("false")){
						falsePositiveCount++;
					}
				}
			}
		}
		System.out.println("TRUE POSITIVE : " + truePositiveCount);
		System.out.println("FALSE POSITIVE : " + falsePositiveCount);
		
	}

}
