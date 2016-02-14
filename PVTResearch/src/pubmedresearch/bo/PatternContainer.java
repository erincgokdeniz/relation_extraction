package pubmedresearch.bo;

public class PatternContainer {

	String patternName;
	String pattern;
	boolean isMultiple;
	String dependencyText;
	
	public String getPatternName(){
		return patternName;
	}
	public void setPatternName(String patternName){
		this.patternName = patternName;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public boolean isMultiple() {
		return isMultiple;
	}
	public void setMultiple(boolean isMultiple) {
		this.isMultiple = isMultiple;
	}
	public String getDependencyText() {
		return dependencyText;
	}
	public void setDependencyText(String dependencyText) {
		this.dependencyText = dependencyText;
	}

}
