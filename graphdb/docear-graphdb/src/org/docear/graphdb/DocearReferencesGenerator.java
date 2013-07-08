package org.docear.graphdb;

import org.docear.database.AlgorithmArguments;
import org.docear.lucene.LuceneController;
import org.docear.query.ResultGenerator;
import org.docear.xml.UserModel;

public class DocearReferencesGenerator implements ResultGenerator {	
	private GraphDbWorker worker;
	private final AlgorithmArguments args;	
	
	public DocearReferencesGenerator(AlgorithmArguments args) {
		this(args, LuceneController.getCurrentController().getGraphDbWorker());
	}
	
	public DocearReferencesGenerator(AlgorithmArguments args, GraphDbWorker graphDbWorker) {		
		if(args == null) {
			throw new NullPointerException();
		}
		this.args = args;
		worker = graphDbWorker;
	}

	@Override
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception {
		long sTime = System.currentTimeMillis();
		try {			
			worker.fillUserReferences(userId, args, userModel, excludePdfHash);
			if(userModel.getReferences().getReferences().size() == 0) {
				throw new Exception("not enough data gathered for (" + args + ")");
			}
		}
		finally {
			System.out.println("references for user: "+userId+" (" + (System.currentTimeMillis() - sTime) + ")");
		}
	}

	/**
	 * @param args
	 * @param termlist
	 * @return
	 * @throws Exception
	 */
//	protected List<HashReferenceItem> clipResults(AlgorithmArguments args, Collection<HashReferenceItem> references) throws Exception {		
//		List<HashReferenceItem> results = new ArrayList<HashReferenceItem>(references);
//		Collections.sort(results, new Comparator<HashReferenceItem>() {
//			public int compare(HashReferenceItem o1, HashReferenceItem o2) {
//				return o2.getCount()-o1.getCount();
//			}
//		});
//		
//		int amount = new Random().nextInt(Math.min(results.size(), AlgorithmArguments.MAX_RESULT_AMOUNT)) + 1;		
//		List<HashReferenceItem> sub = results.subList(0, amount);
//		return sub;
//	}
}
