package org.docear.query;


public class HashReferenceItem {
	public static final String VALUE_SEPARATOR = "@@|-|@@";

	private final String hash;
	private int count = 0;
	private String title = null;
	private boolean isExtracted = true;

	public HashReferenceItem(String hash) {
		if (hash == null || hash.trim().length() == 0) {
			throw new IllegalArgumentException("empty argument string");
		}
		this.hash = hash;
	}

	public void setTitle(String title, boolean extracted) {
		if (title == null) {
			return;
		}

		if (extracted) {
			if (isExtracted && (this.title == null || this.title.length() < title.trim().length())) {
				this.title = title;
			}
		}
		else {
			if (isExtracted || this.title == null || this.title.length() < title.trim().length()) {
				this.title = title;
			}
			isExtracted = false;
		}
	}

	public void touch() {
		count++;
	}

	public String getDocumentHash() {
		return this.hash;
	}

	public String getDocumentTitle() {
		return this.title;
	}

	public int getCount() {
		return this.count;
	}

	public String getCsvValue() {
		StringBuilder sb = new StringBuilder();
		sb.append(hash);
		sb.append(VALUE_SEPARATOR);
		if (title != null) {
			sb.append(title);
		}
		sb.append(VALUE_SEPARATOR);
		sb.append(Integer.toString(count));
		return sb.toString();
	}
}
