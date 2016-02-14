package pubmedresearch.bo;

import java.util.ArrayList;
import java.util.List;

public class EvaluationObject {

	private String id;
	private String line;
	private String patternName;
	private boolean isConnectivityExists;
	private boolean documentInWhitetext;
	private List<Pair> pairList;
	private int truePositive;
	private int falsePositive;
		
	public int getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(int truePositive) {
		this.truePositive = truePositive;
	}

	public int getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(int falsePositive) {
		this.falsePositive = falsePositive;
	}

	public EvaluationObject(){
		this.isConnectivityExists = false;
		this.pairList = new ArrayList<Pair>();
		this.documentInWhitetext = true;
		this.truePositive = 0;
		this.falsePositive = 0;
	}
	
	public boolean isDocumentInWhitetext() {
		return documentInWhitetext;
	}

	public void setDocumentInWhitetext(boolean documentInWhitetext) {
		this.documentInWhitetext = documentInWhitetext;
	}

	public String getPatternName() {
		return patternName;
	}

	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public boolean isConnectivityExists() {
		return isConnectivityExists;
	}
	public void setConnectivityExists(boolean isConnectivityExists) {
		this.isConnectivityExists = isConnectivityExists;
	}

	public List<Pair> getPairList() {
		return pairList;
	}

	public void setPairList(List<Pair> pairList) {
		this.pairList = pairList;
	}
	
}
