package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import pubmedresearch.bo.EvaluationObject;
import pubmedresearch.bo.MatchingItem;
import pubmedresearch.bo.Pair;
import pubmedresearch.bo.PatternContainer;
import pubmedresearch.bo.WhitetextDocument;
import pubmedresearch.bo.WhitetextEntity;
import pubmedresearch.bo.WhitetextSentence;
import pubmedresearch.bo.WhitetextPair;
import utils.FileUtils;
import utils.StringUtils;

public class WhitetextService {

	private Map<String, WhitetextDocument> documentMap;
	
	public void init(String option, List<PatternContainer> patternList){
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			SAXHandler handler   = new SAXHandler();
	        if (option.equalsIgnoreCase("whitetext training")){
	        	saxParser.parse(FileUtils.whitetextPath_training, handler);
	        } else {
	        	saxParser.parse(FileUtils.whitetextPath, handler);	
	        }

	        documentMap = handler.documentMap;
	        
	        removeDoubleRecords(); 
	        
	        count(patternList);
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
	
	private void count(List<PatternContainer> patternList){
		
		int index = 0;
		int totalSentence = 0;
		Pattern p = null;
		Matcher matcher = null;
		
		if(this.documentMap != null){
			
			for (WhitetextDocument document:documentMap.values()){
				
				for (WhitetextSentence sentence : document.getSentences()){
					
					boolean match = false; 
					boolean pairMatch = false;
					for (int i=0; i<sentence.getPairs().size(); i++){
						
						WhitetextPair pair = sentence.getPairs().get(i);
						if (pair.getInteraction().equalsIgnoreCase("True")){
							pairMatch = true;
							totalSentence++;
							for (PatternContainer patternObj : patternList){
						    	
						    	p = Pattern.compile(patternObj.getPattern());
						    	matcher = p.matcher(sentence.getText());
						    	
						    	if (matcher.find()){
						    		index++; match = true;
						    		break;
						    	}
							}
						}
					}
					if (!match && pairMatch) {
						//System.out.println(sentence.getText());
					}
						
				}
				
			}
		}
		System.out.println("TOTAL SENTENCE : " + totalSentence);
		System.out.println("TOTAL TRUE COUNT : " + index);
	}
	
	private void removeDoubleRecords(){
		
		List<WhitetextPair> newPairs = new ArrayList<WhitetextPair>();
		
		int total = 0;
		
		if(this.documentMap != null){
			
			for (WhitetextDocument document:documentMap.values()){
				
				for (WhitetextSentence sentence : document.getSentences()){
					
					newPairs = new ArrayList<WhitetextPair>();
					
					for (int i=0; i<sentence.getPairs().size(); i++){
						
						WhitetextPair pair = sentence.getPairs().get(i);
						String entity1 = sentence.getEntitiesMap().get(pair.getEntity1()).getText();
						String entity2 = sentence.getEntitiesMap().get(pair.getEntity2()).getText();
						
						boolean toBeAdded = true;
						for (int j=0; j<sentence.getPairs().size(); j++){
							
							if (i !=j){
								WhitetextPair tempPair = sentence.getPairs().get(j);
								
								String tempEntity1 = sentence.getEntitiesMap().get(tempPair.getEntity1()).getText();
								String tempEntity2 = sentence.getEntitiesMap().get(tempPair.getEntity2()).getText();
								
								if (entity1.equalsIgnoreCase(tempEntity1) && entity2.equalsIgnoreCase(tempEntity2)){
									
									if (!pair.getInteraction().toLowerCase().equals(tempPair.getInteraction().toLowerCase())){
										if (pair.getInteraction().equalsIgnoreCase("False")){
											toBeAdded = false;
											break;
										}
									} else {
										if (j>i){
											toBeAdded = false;
											break;
										}
									}
									
								} else if (entity1.equalsIgnoreCase(tempEntity2) && entity2.equalsIgnoreCase(tempEntity1)){
									
									if (!pair.getInteraction().toLowerCase().equals(tempPair.getInteraction().toLowerCase())){
										if (pair.getInteraction().equalsIgnoreCase("False")){
											toBeAdded = false;
											break;
										}
									} else {
										if (j>i){
											toBeAdded = false;
											break;
										}
									}
									
								}
							}
						}
						if (toBeAdded && (!entity1.equals(entity2))){
							newPairs.add(pair);
							if (pair.getInteraction().equalsIgnoreCase("True")){
								total++;
							}
						}
						
					}
					sentence.setPairs(newPairs);
				}
			}
			
		}
		System.out.println("**********************" + total);
		
	}
	
	public List<EvaluationObject> compare(List<MatchingItem> matchingItemList){
		
		List<EvaluationObject> evaluationList = new ArrayList<EvaluationObject>();
		EvaluationObject evaluation = null;
		String id   = null; 
		String line = null;
		MatchingItem matchingItem = null;
		
		
		if (matchingItemList.size() > 0){
			matchingItem = matchingItemList.get(0);
			id = matchingItem.getId();
			line = matchingItem.getSentence();
			line = line.replace(" ", "");
			
	
		
		WhitetextDocument document = documentMap.get(id);
	
		if (document != null){
			
			for (WhitetextSentence sentence : document.getSentences()){
				String text = sentence.getSanitizedText();
				if (text.contains(line.length()>30?line.substring(0, 30):line) || text.contains(line.length()>30?line.substring(line.length()-30, line.length()):line)){
					
					for (WhitetextPair pair : sentence.getPairs()){
						
						WhitetextEntity pairEntityObj1 = sentence.getEntitiesMap().get(pair.getEntity1());
						WhitetextEntity pairEntityObj2 = sentence.getEntitiesMap().get(pair.getEntity2());
						String pairEntity1 = pairEntityObj1.getText();
						String pairEntity2 = pairEntityObj2.getText();
						pairEntity1 = StringUtils.removeRedundantTerms(pairEntity1);
						pairEntity2 = StringUtils.removeRedundantTerms(pairEntity2);
						boolean checkLocation = false;
						
						if (matchingItemList.size()>0){
							
							if (matchingItemList.size()>1) checkLocation = true;
						
							for (MatchingItem item: matchingItemList){
							
								String entity1 = item.getBeforePatternText();
								String entity2 = item.getAfterPatternText();
								entity1 = StringUtils.removeRedundantTerms(entity1);
								entity2 = StringUtils.removeRedundantTerms(entity2);
						
								String comparisonResult = null;
								String matchingText = null;
								if (entity1 != null && entity2 != null){
									
									if (pair.getInteraction().equalsIgnoreCase("true")){
										 matchingText = comparePairs(pair, entity1, entity2, pairEntity1, pairEntity2, pairEntityObj1, pairEntityObj2, sentence, checkLocation);
										if (matchingText != null){
											comparisonResult = "TRUE";
										}
									} else {
										matchingText = comparePairs(pair, entity1, entity2, pairEntity1, pairEntity2, pairEntityObj1, pairEntityObj2, sentence, checkLocation);
										if (matchingText != null){
											comparisonResult = "FALSE";
										}
									} 
								}
								evaluation = new EvaluationObject();
								
								if (comparisonResult != null){
									evaluation.setLine(item.getSentence());
									evaluation.setId(item.getId());
									evaluation.setPatternName(item.getPatternName());
									evaluation.getPairList().add(new Pair(pairEntity1, pairEntity2, comparisonResult, StringUtils.getDirection(matchingText, evaluation.getPatternName())));
									evaluationList.add(evaluation);
								} 
								else {
									evaluation = null;
									if (isEntityAcronym(entity1, entity2, pairEntity1, pairEntity2)){
//										System.out.println(" ---- PROBLEM ---- " + pair.getInteraction());
//										System.out.println("E1 : " + entity1 + " || E2 : " + entity2);
//										System.out.println("P1 : " + pairEntity1 + " || P2 : " + pairEntity2);	
									}
								}
							}
						}
					}	
					break;
					
				}
			}
			
		}
		
		}
		
		return evaluationList;
	}

	
	
	private String getBeforeText(WhitetextEntity entity, String sentence){
		
		int offset = Integer.parseInt(entity.getOffset().substring(0, entity.getOffset().indexOf("-")));
		String beforeTemp = sentence.substring(0, offset).trim();
		String before = beforeTemp.substring(beforeTemp.lastIndexOf(" ") + 1, beforeTemp.length()); 

		if (before.equalsIgnoreCase("the")||before.equalsIgnoreCase("of")||before.equalsIgnoreCase("and")){
			before = "";
		}
		
		return StringUtils.removeRedundantTerms(before);
	}
	
	private String getAfterText(WhitetextEntity entity, String sentence){
		
		int offset = Integer.parseInt(entity.getOffset().substring(entity.getOffset().indexOf("-")+1, entity.getOffset().length()));
		String afterTemp = sentence.substring(offset +1 , sentence.length()).trim();
		if (afterTemp.indexOf(" ")>-1){
			String after = afterTemp.substring(0, afterTemp.indexOf(" "));
			if (after.equalsIgnoreCase("the")||after.equalsIgnoreCase("of")||after.equalsIgnoreCase("and")){
				return "";
			}
		}
		 
		return StringUtils.removeRedundantTerms(afterTemp);
	}
	

	
	private String comparePairs(WhitetextPair pair, String entity1,
			String entity2, String pairEntity1, String pairEntity2,
			WhitetextEntity pairEntityObj1, WhitetextEntity pairEntityObj2, WhitetextSentence sentence, boolean checkLocation) {
		
		String beforeText1 = getBeforeText(pairEntityObj1, sentence.getText());
		String afterText1 = getAfterText(pairEntityObj1, sentence.getText());
		String beforeText2 = getBeforeText(pairEntityObj2, sentence.getText());
		String afterText2 = getAfterText(pairEntityObj2, sentence.getText());
		
		if ((pairEntity1.equalsIgnoreCase(entity1)&&pairEntity2.equalsIgnoreCase(entity2))){
			return Pair.FULL_LOC_MATCH;
	
		} else if ((pairEntity1.equalsIgnoreCase(entity2)&&pairEntity2.equalsIgnoreCase(entity1))){
			return Pair.FULL_CROSS_MATCH;
		} else if ((entity1.contains(pairEntity1)&&entity2.contains(pairEntity2))){
			
			if (checkLocation){
//				System.out.println("******************************************");
//				System.out.println(entity1 + "--" + beforeText1 + " | " + pairEntity1 + " | " + afterText1);
//				System.out.println(entity2 + "--" + beforeText2 + " | " + pairEntity2 + " | " + afterText2);
				if ((entity1.contains(beforeText1 + " " + pairEntity1)|| entity1.contains(pairEntity1 + " " + afterText1))||
						(entity2.contains(beforeText2 + " " + pairEntity2)|| entity2.contains(pairEntity2 + " " + afterText2))){
//					System.out.println("*** PARTIAL MATCH *** : " + pair.getInteraction());
					return Pair.PARTIAL_LOC_MATCH;
				}
//				System.out.println("*** NOT ACCEPTED *** : " + pair.getInteraction());	
			} else {
				return Pair.PARTIAL_LOC_MATCH;
			}
			
		} else if ((entity2.contains(pairEntity1)&&entity1.contains(pairEntity2))){
			
			if (checkLocation){
//				System.out.println("******************************************");
//				System.out.println(entity2 + "--" + beforeText1 + " | " + pairEntity1 + " | " + afterText1);
//				System.out.println(entity1 + "--" + beforeText2 + " | " + pairEntity2 + " | " + afterText2);
				if ((entity2.contains(beforeText1 + " " + pairEntity1)|| entity2.contains(pairEntity1 + " " + afterText1))
						|| (entity1.contains(beforeText2 + " " + pairEntity2)|| entity1.contains(pairEntity2 + " " + afterText2)))
				{
//					System.out.println("*** PARTIAL MATCH *** : " + pair.getInteraction());
					return Pair.PARTIAL_CROSS_MATCH;
				}
//				System.out.println("*** NOT ACCEPTED *** : " + pair.getInteraction());
			} else {
				return Pair.PARTIAL_CROSS_MATCH;
			}
			//return Pair.PARTIAL_MATCH;
		} 
		
		return null;
		
	}

	private boolean isEntityAcronym(String entity1, String entity2, String pairEntity1, String pairEntity2){
		
		if (!entity1.equals(entity1.toLowerCase())||!entity2.equals(entity2.toLowerCase())){
			return true;
		} else if (!pairEntity1.equals(pairEntity1.toLowerCase())||!pairEntity2.equals(pairEntity2.toLowerCase())){
			return true;
		} else if (entity1.contains("(")||entity2.contains("(")||pairEntity1.contains("(")||pairEntity2.contains("(")){
			return true;
		}
		
		return false;
		
	}
}
