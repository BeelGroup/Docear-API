package org.sciplore.database;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public abstract class AtomicOperation <T> {
	static Logger logger = Logger.getLogger("AtomicOperationLogger");

	private Callable<T> callable;

	protected Callable<T> getCallable() {
		if(callable == null) {
			callable = new Callable<T>() {

				@Override
				public T call() throws Exception {
					Session session = SessionProvider.getNewSession();
					if (session != null) {
						try {
							return exec(session);
						}
						finally {
							if (session.isOpen()) {
								try {
									session.close();
								}
								catch (HibernateException e) {
									logger.warn("An error occurred while closing the session.", e);
								}
							}
						}
					}
					else {
						throw new IOException("could not connect to database");
					}
				}
				
			};
		}
		return callable; 
	}
	
	public abstract T exec(Session session);
	
}
