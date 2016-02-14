package pubmedresearch.bo;

public class RelationEntity {

	public RelationEntity(String term, int order){
		this.term 	= term;
		this.order 	= order;
	}
	
	private String term;
	private int order;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
}
