package main;


import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MainTest {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {		
//		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("/media/stefan/74EBDC6B5FE5CACC/georgia/docear-graph.db");
//		ExecutionEngine engine = new ExecutionEngine( db );
//		
//		ExecutionResult result;
//		try {
//			String query = "";
//			//query = "start n=node(*) where n.name = 'my node' return n, n.name";
//			query = "START r=node(0) MATCH r-[:USER_SET]->us-[:USER]->u RETURN u LIMIT 10;";
//			result = engine.execute(query);
//			int c = 0;
//			while (result.hasNext()) {
//				c++;
//				Map<String, Object> map = result.next();
//				scala.collection.immutable.List o = map.view().toList();
//				System.out.println();
//			}
//			
//			System.out.println("results found: "+c);
//			
		    
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(dateFormat.parse("2013-10-18 12:16:19").getTime());
		System.out.println(dateFormat.parse("2012-02-01 23:45:12").toString());
		
//			System.out.println(result);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
