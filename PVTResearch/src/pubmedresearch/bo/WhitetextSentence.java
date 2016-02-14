package pubmedresearch.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhitetextSentence {

	String id;
	String text;
	List<WhitetextEntity> entities = new ArrayList<WhitetextEntity>();
	Map<String, WhitetextEntity> entitiesMap = new HashMap<String, WhitetextEntity>();
	List<WhitetextPair> pairs = new ArrayList<WhitetextPair>();
	
	
	String entityX;
	String entityY;
	String beforeText;
	String afterText;
	String pattern;
	
	public String getSanitizedText(){
		String airolaText = this.getText();
		airolaText = airolaText.replace(", and ", " and ");
		airolaText = airolaText.replace(". , ", " , ");
		airolaText = airolaText.replace(" - ", "");
		airolaText = airolaText.replace(" ", "");
		
		return airolaText;
	}
	
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getBeforeText() {
		return beforeText;
	}

	public void setBeforeText(String beforeText) {
		this.beforeText = beforeText;
	}

	public String getAfterText() {
		return afterText;
	}

	public void setAfterText(String afterText) {
		this.afterText = afterText;
	}

	public String getEntityX() {
		return entityX;
	}

	public void setEntityX(String entityX) {
		this.entityX = entityX;
	}

	public String getEntityY() {
		return entityY;
	}

	public void setEntityY(String entityY) {
		this.entityY = entityY;
	}

	public String getId(){
		return id;
	}
	public void setId(String id){
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public List<WhitetextEntity> getEntities() {
		return entities;
	}
	public void setEntities(List<WhitetextEntity> entities) {
		this.entities = entities;
	}
	public List<WhitetextPair> getPairs() {
		return pairs;
	}
	public void setPairs(List<WhitetextPair> pairs) {
		this.pairs = pairs;
	}

	public Map<String, WhitetextEntity> getEntitiesMap(){
		return this.entitiesMap;
	}
	
	public void setEntitiesMap(Map<String, WhitetextEntity> entitiesMap){
		this.entitiesMap = entitiesMap;
	}
	
}
