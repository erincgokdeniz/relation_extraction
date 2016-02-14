package pubmedresearch.bo;

import java.util.ArrayList;
import java.util.List;

public class WhitetextDocument {

	String id;
	List<WhitetextSentence> sentences = new ArrayList<WhitetextSentence>();

	public List<WhitetextSentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<WhitetextSentence> sentences) {
		this.sentences = sentences;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
