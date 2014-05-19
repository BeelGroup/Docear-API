package main;


import java.util.List;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import scala.collection.immutable.Map;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("/media/stefan/74EBDC6B5FE5CACC/georgia/docear-graph.db");
		ExecutionEngine engine = new ExecutionEngine( db );
		
		ExecutionResult result;
		try {
			String query = "";
			//query = "start n=node(*) where n.name = 'my node' return n, n.name";
			query = "START r=node(0) MATCH r-[:USER_SET]->us-[:USER]->u RETURN u LIMIT 10;";
			result = engine.execute(query);
			int c = 0;
			while (result.hasNext()) {
				c++;
				Map<String, Object> map = result.next();
				scala.collection.immutable.List o = map.view().toList();
				System.out.println();
			}
			
			System.out.println("results found: "+c);
			
		    
		    
//			System.out.println(result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
