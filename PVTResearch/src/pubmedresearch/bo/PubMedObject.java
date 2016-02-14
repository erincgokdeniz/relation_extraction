package pubmedresearch.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author EG
 * POJO for each PubMed Publication
 * */
public class PubMedObject {

	public PubMedObject() {
		matchingItemList = new ArrayList<MatchingItem>();
		listOfMatches = new ArrayList<List<MatchingItem>>();
	}
	
	private String id;
	private String abstractText;
	private List<MatchingItem> matchingItemList;
	private List<List<MatchingItem>> listOfMatches;
	private Map<String, List<MatchingItem>> matchingItemMap;
	private boolean includesPVTPattern;
	private boolean includesBrainPattern;
	
	public List<List<MatchingItem>> getListOfMatches() {
		return listOfMatches;
	}
	public void setListOfMatches(List<List<MatchingItem>> listOfMatches) {
		this.listOfMatches = listOfMatches;
	}
	public Map<String, List<MatchingItem>> getMatchingItemMap() {
		return matchingItemMap;
	}
	public void setMatchingItemMap(Map<String, List<MatchingItem>> matchingItemMap) {
		this.matchingItemMap = matchingItemMap;
	}
	
	public List<MatchingItem> getMatchingItemList() {
		return matchingItemList;
	}
	public void setMatchingItemList(List<MatchingItem> matchingItemList) {
		this.matchingItemList = matchingItemList;
	}
	public boolean includesPVTPattern() {
		return includesPVTPattern;
	}
	public void setIncludesPVTPattern(boolean includesPVTPattern) {
		this.includesPVTPattern = includesPVTPattern;
	}
	public boolean includesBrainPattern() {
		return includesBrainPattern;
	}
	public void setIncludesBrainPattern(boolean includesBrainPattern) {
		this.includesBrainPattern = includesBrainPattern;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAbstractText() {
		return abstractText;
	}
	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}
	
}
