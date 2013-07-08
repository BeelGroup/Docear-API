package util.recommendations;

public class UserModelReferenceItem extends UserModelItem {

	public UserModelReferenceItem(String term, Double weight) {
		super(term, weight);
	}

	@Override
	public String toString() {		
		return this.getItem();
	}

}
