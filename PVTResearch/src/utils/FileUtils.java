package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import pubmedresearch.bo.BrainRegion;
import pubmedresearch.bo.MatchingItem;
import pubmedresearch.bo.Pair;
import pubmedresearch.bo.PatternContainer;
import pubmedresearch.bo.PubMedObject;
import service.AbbreviationExpansionService;


public class FileUtils {

	private static final String processedPublicationsStanfordPath = "./processedPublicationsStanford/";
	private static final String patternsPath = "./toolkit/patterns.txt";
	public static final String whitetextPath = "./toolkit/airola.xml";
	public static final String whitetextPath_training = "./toolkit/airola_1377.xml";
	private static final String lexicalParserPath = "./toolkit/lexicalParse.txt";
	private static final String brainRegionsPath = "./toolkit/BrainRegions.csv";
	public static final String outputFolderPath = "./output/pairs.csv";
	
	Map<String, Map<String, BrainRegion>> brainMap; 
	Map<String, BrainRegion> brainRegionsMap;
	Map<String, BrainRegion> acronymsMap;
	Map<String, BrainRegion> synonymsMap;
	List<String> brainRegionList;
	
	private String getPublicationPath(String option){
		
		String publicationPath = "./abstracts/";
		
		switch (option){
			case "whitetext training" 	: publicationPath = "./corpus_whitetext_training_abstracts/"; break;  
			case "whitetext testing" 	: publicationPath = "./corpus_whitetext_testing_abstracts/"; break;
			case "pvt abstract" 		: publicationPath = "./corpus_pvt_abstracts/"; break;
			case "pvt fulltext" 		: publicationPath = "./corpus_pvt_full_texts/"; break;
			case "pvt annotated" 		: publicationPath = "./corpus_pvt_annotated_full_texts/"; break;
			case "corpus test"	 		: publicationPath = "./corpus_test/"; break;
		}
		
		return publicationPath;
	}
	
	public void preProcessPublicationsWithStanfordParser(String option, boolean applyAbbreviationExpansion){
		
		AbbreviationExpansionService abbExpansion = null;
		Map<String, String> expansionPairs = null;
		String publicationsPath = getPublicationPath(option);
		File folder = new File(publicationsPath);
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        
		    	String fileName = listOfFiles[i].getName();
		    	 
		    	if (applyAbbreviationExpansion){
		    		abbExpansion = new AbbreviationExpansionService();
			    	expansionPairs = abbExpansion.extractAbbreviationPairs(publicationsPath + fileName);	
		    	}
		    	
		    	
				try {
					File outputFile = new File(processedPublicationsStanfordPath + fileName);
					
					BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));

					Path path = FileSystems.getDefault().getPath(publicationsPath, fileName);
			        String abstractText = new String (Files.readAllBytes(path), StandardCharsets.UTF_8 );
					
			        if (applyAbbreviationExpansion){
			        	abstractText = StringUtils.getAbbreviationExpandedText(abstractText, expansionPairs);
			        	System.out.println(abstractText);
			        }
			        
			        for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(abstractText))){
			        	writer.append(Sentence.listToString(sentence) + "\n");
			        }
				
				
			        writer.close();
				
				} catch(IOException ie){
					ie.printStackTrace();
				}	
		      }
		    }
	}

	
	
	public List<PatternContainer> getListOfPatterns(){
		
			List<PatternContainer> patternList = new ArrayList<PatternContainer>();
			
			File inputFile;
			String item;
			PatternContainer pattern;
			
			try {
				inputFile = new File(patternsPath);
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				
				while ((item = reader.readLine()) != null){
					
					String[] patternLine = item.split(";");
					
					pattern = new PatternContainer();
					
					pattern.setPatternName(patternLine[0]);
					pattern.setPattern(patternLine[1]);
					pattern.setMultiple(Boolean.parseBoolean(patternLine[2]));
					pattern.setDependencyText(patternLine[3]);
					
					patternList.add(pattern);
				}
				
				reader.close();
				
			} catch (IOException ie){
				ie.printStackTrace();
			}
			
			return patternList;
		
	}


	
	
	public void deletePubMedPublications(){
		File folder = new File(processedPublicationsStanfordPath);
		for(File file: folder.listFiles()) {
			file.delete();
		}
	}
	
	public List<PubMedObject> readPubMedPublicationsFromFile(){

		List<PubMedObject> pubMedList = new ArrayList<PubMedObject>();
		
		try {
			
			File folder = new File(processedPublicationsStanfordPath);
			File[] listOfFiles = folder.listFiles();

			    for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			        
			    	String fileName = listOfFiles[i].getName();
			    	 
			    	PubMedObject pubMed = new PubMedObject();
			    	pubMed.setId(fileName.replace(".txt", ""));
			    	
			    	Path path = FileSystems.getDefault().getPath(processedPublicationsStanfordPath, fileName);
			        String abstractText = new String (Files.readAllBytes(path), StandardCharsets.UTF_8 );
			        pubMed.setAbstractText(abstractText);
									
					pubMedList.add(pubMed);
			      } 
			    }
				
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return pubMedList;
		
	}

	
	
	public Map<String, Map<String, BrainRegion>> getBrainRegions(){
		
		brainMap 		= new HashMap<String, Map<String, BrainRegion>>(); 
		brainRegionsMap = new HashMap<String, BrainRegion>();
		acronymsMap  	= new HashMap<String, BrainRegion>();
		synonymsMap  	= new HashMap<String, BrainRegion>();
		brainRegionList = new ArrayList<String>();
		
		File inputFile;
		String item;
		BrainRegion brainRegion;
		
		try {
			inputFile = new File(brainRegionsPath);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			int index = 0;
			while ((item = reader.readLine()) != null){
				index++; System.out.println(index);
				brainRegion = new BrainRegion();
				String[] tokens = item.split(";");
				brainRegion.setName(tokens[0]);
				brainRegionList.add(brainRegion.getName().toLowerCase());
				if (tokens.length >= 2){
					brainRegion.setAcronyms(Arrays.asList(tokens[1].split(",")));
				} 
				if (tokens.length == 3){
					List<String> synonyms = Arrays.asList(tokens[2].split(","));
					brainRegion.setSynonyms(synonyms);
					brainRegionList.addAll(synonyms);
				}

				// Brain Regions 
				String brainRegionKey = brainRegion.getName().toLowerCase();
				brainRegionKey = smoothText(brainRegionKey); 
				brainRegionsMap.put(brainRegionKey, brainRegion);
				
				// Acronyms - Case Sensitive 
				for (String token : brainRegion.getAcronyms()){
					if (token != null && token.trim().length() > 1){
						acronymsMap.put(token, brainRegion);
						brainRegionsMap.put(token.trim().toLowerCase(), brainRegion);
					}
				}
				
				// Synonyms - Case Insensitive
				for (String token : brainRegion.getSynonyms()){
					if (token != null && token.trim().length() > 0){
						brainRegionsMap.put(token.trim().toLowerCase(), brainRegion);	
					}
				}
				
			}
			
			reader.close();
			
		} catch (IOException ie){
			ie.printStackTrace();
		}
		
		brainMap.put("brainRegions", brainRegionsMap);
		brainMap.put("acronymsMap", acronymsMap);
		brainMap.put("synonymsMap", synonymsMap);
		
		return brainMap;
	}


	public static String smoothText(String brainRegionKey) {
		brainRegionKey = brainRegionKey.replace(" and ", " "); 
		brainRegionKey = brainRegionKey.replace(" of ", " "); 
		brainRegionKey = brainRegionKey.replace(" the ", " "); 
		brainRegionKey = brainRegionKey.replace(" area ", " "); 
		brainRegionKey = brainRegionKey.replace(" part ", " "); 
		brainRegionKey = brainRegionKey.replace(" parts ", " ");
		brainRegionKey = brainRegionKey.replace(" pole ", " ");
		brainRegionKey = brainRegionKey.replace(" its ", " ");
		if (brainRegionKey.startsWith("the ")){
			brainRegionKey = brainRegionKey.substring(4, brainRegionKey.length());
		}
		
		return brainRegionKey;
	}

	
	public static void writeMatchingItemsToFile(String id, List<MatchingItem> matchingItemList){

		try {
			File outputFile;
			BufferedWriter writer;
			boolean hasEntry = false;
			
			outputFile = new File(outputFolderPath);
			
			writer = new BufferedWriter(new FileWriter(outputFile, true));
			
				if (matchingItemList != null){
					for(MatchingItem matchingItem : matchingItemList){
						hasEntry = false;
						for (String key : matchingItem.getPairs().keySet()){
							Pair pair = matchingItem.getPairs().get(key);
							hasEntry = true;
							writer.append(id + "|");
							writer.append(matchingItem.getSentence() + "|");
							writer.append(matchingItem.getPatternName() + "|");
							writer.append(matchingItem.getBeforePatternText() + "|");
							writer.append(matchingItem.getAfterPatternText() + "|");
							writer.append(pair.getEntityX() + "|");
							writer.append(pair.getEntityY() + "|");
							writer.append("\n");
						}
						if (!hasEntry){
							writer.append(id + "|");
							writer.append(matchingItem.getSentence() + "|");
							writer.append(matchingItem.getPatternName() + "|");
							writer.append(matchingItem.getBeforePatternText() + "|");
							writer.append(matchingItem.getAfterPatternText() + "|");
							writer.append("\n");
						}
						
					}
				}
			writer.close();

			
		} catch (IOException ie){
			ie.printStackTrace();
		}
		
		
		
			
	}
	
	
	public static List<MatchingItem> readBeforePatternTestFile(String path, String fileName){
		
		String item;
		List<MatchingItem> listOfObjects = new ArrayList<MatchingItem>();
		MatchingItem matchingItem = null;
		
		try {
			File inputFile = new File(path + "/" + fileName);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));

			while ((item = reader.readLine()) != null){
				matchingItem = new MatchingItem();
				matchingItem.setSentence(item);
				matchingItem.setExpectedValue(reader.readLine());
				listOfObjects.add(matchingItem);
			}
			
			reader.close();
			
		} catch (IOException ie){
			ie.printStackTrace();
		}
		
		return listOfObjects;
	}
	
	public static List<MatchingItem> readAfterPatternTestFile(String path, String fileName){
		
		String item;
		List<MatchingItem> listOfObjects = new ArrayList<MatchingItem>();
		MatchingItem matchingItem = null;
				
		try {
			File inputFile = new File(path + "/" + fileName);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));

			while ((item = reader.readLine()) != null){
					matchingItem = new MatchingItem();
					matchingItem.setSentence(item);
					matchingItem.setExpectedValue(reader.readLine());
					listOfObjects.add(matchingItem);
			}
			
			reader.close();
			
		} catch (IOException ie){
			ie.printStackTrace();
		}
		
		return listOfObjects;
	}
	
	public static void writeLexicalParseToFile(Map<String, Tree> lexicalParserCache){
		
		File outputFile;
		BufferedWriter writer = null;

			try {
				
				outputFile = new File(lexicalParserPath);
				writer = new BufferedWriter(new FileWriter(outputFile, false));
				
				
				for(String line : lexicalParserCache.keySet()){
	            	
					writer.append(line + ";;" + lexicalParserCache.get(line));
	        		writer.append("\n");
	        		
			    }

				writer.close();
				
			} catch (IOException ie){
				ie.printStackTrace();
			} 
		
	}

	public static Map<String, Tree> readLexicalParseFromFile(){
		
		Map<String, Tree> lexicalParserCache = new HashMap<String, Tree>();
		String item;		
		try {
			File inputFile = new File(lexicalParserPath);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));

			while ((item = reader.readLine()) != null){
				
				String[] entity = item.split(";;");
				lexicalParserCache.put(entity[0], Tree.valueOf(entity[1]));
				
			}
			
			reader.close();
			
		} catch (IOException ie){
			ie.printStackTrace();
		} 
		
		return lexicalParserCache;
	}

	
}
