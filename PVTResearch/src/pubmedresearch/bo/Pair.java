package pubmedresearch.bo;

public class Pair {

	private String entityX;
	private String entityY;
	private String match;
	private String direction;
	
	public static final String FULL_LOC_MATCH = "full_location_match";
	public static final String FULL_CROSS_MATCH = "full_cross_match";
	public static final String PARTIAL_LOC_MATCH = "partial_location_match";
	public static final String PARTIAL_CROSS_MATCH = "partial_cross_match";
	public static final String NO_MATCH = "no_match";
	
	public Pair(String entityX, String entityY, String match, String direction){
		this.entityX = entityX;
		this.entityY = entityY;
		this.match = match;
		this.direction = direction;
	}
	
	public String getDirection(){
		return direction;
	}
	public void setDirection(String direction){
		this.direction = direction;
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

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}
	
	
}
