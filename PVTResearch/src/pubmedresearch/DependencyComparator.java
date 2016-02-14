package pubmedresearch;

import java.util.Comparator;

import pubmedresearch.bo.RelationEntity;

public class DependencyComparator implements Comparator<RelationEntity>{
	
	 @Override
	    public int compare(RelationEntity a, RelationEntity b) {
		 	return a.getOrder() < b.getOrder() ? -1 : a.getOrder() == b.getOrder() ? 0 : 1;
	    }

}
