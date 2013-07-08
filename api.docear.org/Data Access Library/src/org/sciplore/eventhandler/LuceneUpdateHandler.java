package org.sciplore.eventhandler;

import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.sciplore.database.SessionProvider;
import org.sciplore.resources.Document;
import org.sciplore.utilities.concurrent.AsynchUtilities;

public class LuceneUpdateHandler implements PostInsertEventListener, PostUpdateEventListener {

	private static final long serialVersionUID = 1L;

	public void onPostInsert(PostInsertEvent event) {
		//DOCEAR: disable to reduce work load of the server
		if (false && event.getEntity() instanceof Document) {
			final Document document =  (Document) event.getEntity();
			AsynchUtilities.executeAsynch(new Runnable() {
				public void run() {
					long time = System.currentTimeMillis();
					try {
						SessionProvider.getLuceneIndexer().addDocument(document);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						System.out.println("onPostInsert@executor.execute(): " + (System.currentTimeMillis()-time));
					}
				}
			});
		}
	}

	public void onPostUpdate(PostUpdateEvent event) {
		//DOCEAR: disable to reduce work load of the server
		if (false && event.getEntity() instanceof Document) {
			final Document document =  (Document) event.getEntity();
			AsynchUtilities.executeAsynch(new Runnable() {
				public void run() {
					long time = System.currentTimeMillis();
					try {
						SessionProvider.getLuceneIndexer().updateDocument(document);		
					} catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						System.out.println("onPostUpdate@executor.execute(): " + (System.currentTimeMillis()-time));
					}
				}
			});
		}
	}

}
