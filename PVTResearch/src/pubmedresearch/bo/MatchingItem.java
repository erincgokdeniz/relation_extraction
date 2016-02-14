package pubmedresearch.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingItem {

	private String id;
	private String patternName;
	private String sentence; 
	private List<String> connectedItems;
	private boolean isIncludedInBrainRegionList;
	private String expectedValue;
	private String beforePatternText;
	private String afterPatternText;
	private Map<String, Pair> pairs;

	public MatchingItem(){
		
	}
	
	public MatchingItem(String id, String patternName, String sentence, String beforePatternText, String afterPatternText){
		this.id = id;
		this.patternName = patternName;
		this.sentence = sentence;
		this.beforePatternText = beforePatternText;
		this.afterPatternText = afterPatternText;
		pairs = new HashMap<String, Pair>();
	}
	
	public Map<String, Pair> getPairs() {
		return pairs;
	}

	public void setPairs(Map<String, Pair> pairs) {
		this.pairs = pairs;
	}
	public String getBeforePatternText() {
		return beforePatternText;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}

	public void setBeforePatternText(String beforePatternText) {
		this.beforePatternText = beforePatternText;
	}

	public String getAfterPatternText() {
		return afterPatternText;
	}

	public void setAfterPatternText(String afterPatternText) {
		this.afterPatternText = afterPatternText;
	}

	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	public String getPatternName() {
		return patternName;
	}
	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}
	public String getExpectedValue() {
		return expectedValue;
	}
	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}
	public List<String> getConnectedItems() {
		return connectedItems;
	}
	public void setConnectedItems(List<String> connectedItems) {
		this.connectedItems = connectedItems;
	}
	public boolean isIncludedInBrainRegionList() {
		return isIncludedInBrainRegionList;
	}
	public void setIncludedInBrainRegionList(boolean isIncludedInBrainRegionList) {
		this.isIncludedInBrainRegionList = isIncludedInBrainRegionList;
	}
	
	
	
}
