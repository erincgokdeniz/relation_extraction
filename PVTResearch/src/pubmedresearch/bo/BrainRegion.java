package pubmedresearch.bo;

import java.util.ArrayList;
import java.util.List;

public class BrainRegion {

	private String name;
	private List<String> acronyms;
	private List<String> synonyms;
	
	public BrainRegion(){
		name = new String("");
		acronyms = new ArrayList<String>();
		synonyms = new ArrayList<String>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getAcronyms() {
		return acronyms;
	}
	public void setAcronyms(List<String> acronyms) {
		this.acronyms = acronyms;
	}
	public List<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}
	
	
}
