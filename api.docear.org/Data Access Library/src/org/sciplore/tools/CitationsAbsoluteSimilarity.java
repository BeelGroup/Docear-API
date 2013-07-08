package org.sciplore.tools;

public class CitationsAbsoluteSimilarity {		
		private Long doc1;
		private Long doc2;
		private Double similarity;
		
		public CitationsAbsoluteSimilarity() {		
		}
		
		public CitationsAbsoluteSimilarity(Long doc1, Long doc2, Double similarity) {
			this.doc1 = doc1;
			this.doc2 = doc2;
			this.similarity = similarity;
		}
		
		public Long getDoc1() {
			return doc1;
		}
		public void setDoc1(Long doc1) {
			this.doc1 = doc1;
		}
		public Long getDoc2() {
			return doc2;
		}
		public void setDoc2(Long doc2) {
			this.doc2 = doc2;
		}
		public Double getSimilarity() {
			return similarity;
		}
		public void setSimilarity(Double similarity) {
			this.similarity = similarity;
		}
}
