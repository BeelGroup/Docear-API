package util.recommendations;

public abstract class UserModelItem {
	private String item;
	private Double weight;
	
	public UserModelItem(String term, Double weight) {
		this.item = term;
		this.weight = weight;
	}
	
	public String getItem() {
		return item;
	}
	public void setItem(String term) {
		this.item = term;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}	
	
	public abstract String toString();
}
