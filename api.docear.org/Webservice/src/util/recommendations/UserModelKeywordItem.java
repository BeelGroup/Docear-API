package util.recommendations;

public class UserModelKeywordItem extends UserModelItem {	
	public UserModelKeywordItem(String term, Double weight) {
		super(term, weight);
	}

	@Override
	public String toString() {
		return this.getItem();
	}

}
