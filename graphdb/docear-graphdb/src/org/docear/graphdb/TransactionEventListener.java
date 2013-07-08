package org.docear.graphdb;

import org.neo4j.graphdb.Transaction;

public interface TransactionEventListener {
	public void startedTransaction(Transaction transaction);
	public void finishedTransaction(Transaction transaction);
}
