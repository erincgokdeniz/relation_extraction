package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pubmedresearch.bo.WhitetextDocument;
import pubmedresearch.bo.WhitetextEntity;
import pubmedresearch.bo.WhitetextSentence;
import pubmedresearch.bo.WhitetextPair;

public class SAXHandler extends DefaultHandler{

	Map<String, WhitetextDocument> documentMap = new HashMap<String, WhitetextDocument>();
	List<WhitetextSentence> sentences = new ArrayList<WhitetextSentence>();
	List<String> texts = new ArrayList<String>();
	List<String> brainRegions = new ArrayList<String>();	
	List<WhitetextEntity> entities = new ArrayList<WhitetextEntity>();
	Map<String, WhitetextEntity> entitiesMap = new HashMap<String, WhitetextEntity>();
	List<WhitetextPair> pairs = new ArrayList<WhitetextPair>();
	
	private Stack<String> elementStack = new Stack<String>();
    private Stack<Object> objectStack  = new Stack<Object>();
	
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {

		this.elementStack.push(qName);
		
		if (DOCUMENT.equalsIgnoreCase(qName)) {
		
			WhitetextDocument document = new WhitetextDocument();
			document.setId(attributes.getValue(ORIGINAL_ID));
			this.objectStack.push(document);
			
		} else if (SENTENCE.equalsIgnoreCase(qName)) {
			
			WhitetextSentence sentence = new WhitetextSentence();
			sentence.setText(attributes.getValue(TEXT));
			sentence.setId(attributes.getValue(ID));
			this.objectStack.push(sentence);
			
		} else if (ENTITY.equalsIgnoreCase(qName)) {
			
			WhitetextEntity entity = new WhitetextEntity();
			entity.setText(attributes.getValue(TEXT));
			entity.setId(attributes.getValue(ID));
			entity.setOffset(attributes.getValue("charOffset"));
			entitiesMap.put(attributes.getValue(ID), entity);
			this.objectStack.push(entity);
			
		} else if (PAIR.equalsIgnoreCase(qName)) {
			
			WhitetextPair pair = new WhitetextPair();
			pair.setId(attributes.getValue(ID));
			pair.setInteraction(attributes.getValue(INTERACTION));
			pair.setEntity1(attributes.getValue(ENTITY_1));
			pair.setEntity2(attributes.getValue(ENTITY_2));
			this.pairs.add(pair);
			
		}
	}
	
	public void endElement(String uri, String localName,
			String qName) throws SAXException {
		
		this.elementStack.pop();
		
		if(DOCUMENT.equals(qName) || SENTENCE.equals(qName) || ENTITY.equals(qName)){
            Object object = this.objectStack.pop();
            
            if(ENTITY.equals(qName)){
            	
            	WhitetextEntity entity = (WhitetextEntity) object;
            	this.entities.add(entity);
            	
            } else if(SENTENCE.equals(qName)){
            	
            	WhitetextSentence sentence= (WhitetextSentence) object;
            	sentence.setEntities(this.entities);
            	sentence.setEntitiesMap(entitiesMap);
            	sentence.setPairs(pairs);
            	this.sentences.add(sentence);
            	this.entities = new ArrayList<WhitetextEntity>();
            	this.pairs = new ArrayList<WhitetextPair>();
            	this.texts.add(sentence.getText());
            	
            } else if (DOCUMENT.equals(qName)){
            	
            	WhitetextDocument document = (WhitetextDocument) object;
            	document.setSentences(this.sentences);
            	this.documentMap.put(document.getId(),document);
            	this.sentences = new ArrayList<WhitetextSentence>();
            
            }
            
		}
	}
	
	private final String DOCUMENT 	 = "document";
	private final String SENTENCE 	 = "sentence";
	private final String ID			 = "id";
	private final String TEXT		 = "text";
	private final String ENTITY		 = "entity";
	private final String ENTITY_1	 = "e1";
	private final String ENTITY_2	 = "e2";
	private final String INTERACTION = "interaction";
	private final String ORIGINAL_ID = "origID";
	private final String PAIR 		 = "pair";
	
}
