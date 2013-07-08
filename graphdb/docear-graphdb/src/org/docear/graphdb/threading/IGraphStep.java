package org.docear.graphdb.threading;

import org.neo4j.graphdb.GraphDatabaseService;

public interface IGraphStep {
    public void run(GraphDatabaseService graphDb) throws Exception;
    
    /**
     * can be used to transfer information to this graph step instance before it
     * is processed by the <b>GraphDBInserter</b>
     * <br/>
     * <br/>
     * e.g. pass attributes one level up to the parent node
     * 
     * @param key name of the property/attribute
     * @param value value that belongs to the given name
     */
    public void addProperty(String key, String value);
}
