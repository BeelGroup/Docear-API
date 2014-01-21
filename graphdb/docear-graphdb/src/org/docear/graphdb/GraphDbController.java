package org.docear.graphdb;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MultivaluedMap;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.apache.commons.configuration.Configuration;
import org.docear.Logging.DocearLogger;
import org.docear.graphdb.relationship.Type;
import org.docear.graphdb.relationship.UserRelationship;
import org.docear.graphdb.threading.ControlServer;
import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.GraphJobManager;
import org.docear.lucene.LuceneController;
import org.docear.nanoxml.DefaultElementHandlerManager;
import org.docear.nanoxml.IElementHandler;
import org.docear.nanoxml.IElementHandlerManager;
import org.docear.nanoxml.MindMapReader;
import org.docear.parser.handler.AttributeElementHandler;
import org.docear.parser.handler.CloudElementHandler;
import org.docear.parser.handler.DuplicateRevisionException;
import org.docear.parser.handler.EdgeElementHandler;
import org.docear.parser.handler.FontElementHandler;
import org.docear.parser.handler.IconElementHandler;
import org.docear.parser.handler.MapElementHandler;
import org.docear.parser.handler.NodeElementHandler;
import org.docear.parser.handler.PDFAnnotationElementHandler;
import org.docear.parser.handler.RichcontentElementHandler;
import org.docear.parser.handler.StyleNodeElementHandler;
import org.docear.parser.handler.StylesManager;
import org.docear.query.HashReferenceItem;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.PropertyFileConfigurator;
import org.neo4j.server.configuration.ServerConfigurator;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;


public class GraphDbController implements KernelEventHandler, TransactionEventListener {
	private ExecutorService serviceRequestExecutor = Executors.newSingleThreadExecutor();
	public static Client client = Client.create();
	public static final String DOCEAR_SERVICES = "https://api.docear.org/";
	private static int MAX_PARSER_THREADS = Runtime.getRuntime().availableProcessors();
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String dbPath;
	private File workingDirectory;
	private File processingDirectory;
	private File failureDirectory;
	private File indexDirectory;

	private AbstractGraphDatabase graphDb;
	private List<File> files = new ArrayList<File>();
	private FileFilter mapFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			if (pathname.isDirectory()) {
				return true;
			}
			if (name.endsWith(".mm") || name.endsWith(".xml")) {
				return true;
			}
			return false;
		}
	};
	
	private GraphJobManager jobManager;
	private LuceneController luceneCtrl;
	private final ParserWorkerThread[] worker = new ParserWorkerThread[MAX_PARSER_THREADS];
	private final GraphDBInserterThread graphDBWorker = new GraphDBInserterThread();
	private final FileListAppender fla = new FileListAppender();

	private WrappingNeoServerBootstrapper service;

	private final Set<Transaction> openTransactions = new HashSet<Transaction>();
	private Boolean fromHook = false;
	
	private Thread shutdownHook = new Thread() {
		public void run() {
			synchronized (fromHook) {
				fromHook = true;
			}
			shutdown();
			
		}
	};	

	public GraphDbController(String dbPath, String workingDirectory) {
		this.dbPath = dbPath;
		this.workingDirectory = new File(workingDirectory, "new");
		this.processingDirectory = new File(workingDirectory, "processing");
		this.failureDirectory = new File(workingDirectory, "failure");
		this.indexDirectory = new File(dbPath, "mm-fulltext-index");
	}

	public void init() {
		graphDb = (AbstractGraphDatabase) new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);

		Configurator config = new ServerConfigurator(graphDb); 
		config.configuration().setProperty(Configurator.WEBSERVER_PORT_PROPERTY_KEY, 47474 );
		config.configuration().setProperty(Configurator.SECURITY_RULES_KEY, "org.docear.graphdb.DocearSecurityRule");
		
		service = new WrappingNeoServerBootstrapper(graphDb, config);

		registerShutdownHook(graphDb);
		ControlServer ctrlServer;
		try {
			ctrlServer = new ControlServer(this);
			jobManager = new GraphJobManager(100);
			
			luceneCtrl = new LuceneController(this.indexDirectory, graphDb);
			graphDb.registerKernelEventHandler(this);
			graphDBWorker.start();
			graphDBWorker.registerTransactionEventListener(this);
			
			service.start();
			ctrlServer.start();			
		} catch (IOException e) {
			DocearLogger.error(e);
		}
	}
	
	
	@Override
	public void beforeShutdown() {
		synchronized (openTransactions) {
			if(openTransactions.size() > 0) {
				for(Transaction t : openTransactions) {
					t.failure();
					t.finish();
				}
			}
		}
	}

	@Override
	public Object getResource() {
		return null;
	}

	@Override
	public void kernelPanic(ErrorState arg0) {		
	}

	@Override
	public ExecutionOrder orderComparedTo(KernelEventHandler arg0) {
		return null;
	}
	
	@Override
	public void startedTransaction(Transaction transaction) {
		synchronized (openTransactions ) {
			openTransactions.add(transaction);
		}
	}
	
	@Override
	public void finishedTransaction(Transaction transaction) {
		synchronized (openTransactions ) {
			openTransactions.remove(transaction);
		}
	}

	public void start() {
		if (!this.workingDirectory.exists()) {
			this.workingDirectory.mkdirs();
		}
		if (!this.processingDirectory.exists()) {
			this.processingDirectory.mkdirs();
		}
		if (!this.failureDirectory.exists()) {
			this.failureDirectory.mkdirs();
		}
	
		fla.start();
		startParseWorker();
	}
	
	public File nextFile() {
		synchronized (files) {
			if (files.size() > 0) {
				return files.remove(0);
			}
			throw new IndexOutOfBoundsException();
		}
	}

	public void addFile(File file) {
		synchronized (files) {
			if (this.files.contains(file)) {
				return;
			}		
			this.files.add(file);
//			notifyAll();
		}
	}

	public synchronized boolean filesAvailable() {
		synchronized (files) {
			return files.size() > 0;
		}
	}

	protected void initFileList(File searchFolder) {
		if (searchFolder.isDirectory()) {
			for (File file : searchFolder.listFiles(mapFilter)) {
				if (file.isDirectory()) {
//					initFileList(file);
					continue;
				}				
				
				try {
					addFile(move(file, processingDirectory));
				}
				catch (IOException e) {					
					DocearLogger.error(e);
				}
				
			}
		}
	}

	private File move(File file, File destDirectory) throws IOException {
		File propFile = new File(file.getAbsolutePath()+".properties");
		if (propFile.exists() && !propFile.renameTo(new File(destDirectory, propFile.getName()))) {
			throw new IOException("could not move properties file ("+propFile+")");
		}
		
		File ret = new File(destDirectory, file.getName());
		
		if (!file.renameTo(ret)) {
			throw new IOException("could not move mm file("+file+")");
		}
		
		return ret;
	}

	protected void startParseWorker() {
		DocearLogger.info("start parsing with " + MAX_PARSER_THREADS + " threads ...");
		for (int i = 0; i < MAX_PARSER_THREADS; i++) {
			worker[i] = new ParserWorkerThread();
			worker[i].setName("ParseWorker["+i+"]");
			worker[i].start();
		}
	}
	
	protected void stopParseWorker() {
		for (ParserWorkerThread pw : worker) {
			pw.shutdown();
		}
	}
	
	private void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(shutdownHook );
	}
	
	private void resetParserQueue() throws IOException {		
		synchronized (jobManager) {
			while (this.jobManager.hasJobs()) {
				try {
					move(this.jobManager.next().getFile(), this.workingDirectory);
				}
				catch (Exception e) {
					System.gc();
					move(this.jobManager.next().getFile(), this.workingDirectory);
				}
			}
		}
	}

	public void clearDb() {
		try {
			FileUtils.deleteRecursively(new File(dbPath));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void freeze() {
		graphDBWorker.shutdown();
		while (graphDBWorker.isAlive()) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void shutdown() {
		synchronized (fromHook) {
			if(!fromHook) {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
		}
		fla.shutdown();
		stopParseWorker();				
		graphDBWorker.shutdown();
		service.stop();
		while(graphDb.transactionRunning()) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
			}
		}
		graphDb.shutdown();
		try {
			resetParserQueue();
		}
		catch(Exception e) {
			DocearLogger.error(e);
		}
		while (graphDBWorker.isAlive()) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
			}
		}
		
		synchronized (fromHook) {
			if(!fromHook) {
				System.exit(0);
			}
		}
	}

	protected class ParserWorkerThread extends Thread {
		private boolean closed;

		public void run() {
			closed = false;
			while (!closed) {
				try {
					while (filesAvailable()) {
						if(closed) {
							return;
						}
						if (jobManager.acceptJobs()) {
							try {
								File file = nextFile();
								processMapFile(file);
							}
							catch (Exception e) {						
							}
						}
						else {
							try {
								sleep(500);
							}
							catch (InterruptedException e) {		
							}
						}
						
					}
					
					sleep(5000);
				}
				catch (InterruptedException e) {
				}				
			}
		}
		
		public void shutdown() {
			this.closed = true;
			this.interrupt();
		}

		private void processMapFile(final File file) {
			try {
				List<IElementHandler> handlerList = new ArrayList<IElementHandler>();
				Properties properties = new Properties();
				InputStream is = new FileInputStream(new File(file.getAbsolutePath()+".properties"));
				try {
					properties.load(is);
				} finally {
					try {
						is.close();
					}
					catch (Exception e) {
					}
				}
				properties.setProperty(GraphCreatorJob.PROPERTY_FILE_KEY, file.getAbsolutePath());
				
				GraphCreatorJob currentJob = new GraphCreatorJob(file.getAbsolutePath(), properties);
				currentJob.setContext(GraphCreatorJob.CONTEXT_STYLE_MAP, new StylesManager(graphDb));
				handlerList.add(new MapElementHandler(currentJob));
				handlerList.add(new NodeElementHandler(currentJob));
				handlerList.add(new AttributeElementHandler(currentJob));
				handlerList.add(new PDFAnnotationElementHandler(currentJob));
				handlerList.add(new StyleNodeElementHandler(currentJob));
				handlerList.add(new RichcontentElementHandler(currentJob));
				handlerList.add(new CloudElementHandler());
				handlerList.add(new FontElementHandler());
				handlerList.add(new EdgeElementHandler());
				handlerList.add(new IconElementHandler());				
				IElementHandlerManager manager = new DefaultElementHandlerManager(handlerList);
				MindMapReader mapReader = new MindMapReader(manager);
				IXMLReader reader = StdXMLReader.fileReader(file.getPath());

				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
				parser.setBuilder(mapReader);
				parser.setReader(reader);
				parser.parse();
				jobManager.addJob(currentJob);
			}
			catch (Exception e) {
				DocearLogger.error(e);
				MoveToFailure(file);
			}
			finally {
				//System.gc();
			}
		}

		private void MoveToFailure(final File file) {
			try {
				move(file, failureDirectory);
			}
			catch (IOException e1) {
				System.gc();				
				new Thread() {
					public void run()  {
						while(true) {
							try {
								move(file, failureDirectory);
								return;
							}
							catch (IOException e) {								
							}
							try {								
								sleep(100);
							} catch (InterruptedException e) {
							}
						}
					}
				}.start();
			}
		}
	}
	
	protected class FileListAppender extends Thread {
		private boolean closed = false;
		public void run() {
			
			while (!closed) {
				initFileList(workingDirectory);
				try {
					sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}
		
		public void shutdown() {
			this.closed = true;
			this.interrupt();
		}
	}
	

	protected class GraphDBInserterThread extends Thread {
		private int counter = 0;
		private long avgTime = 0;
		private boolean shutdown = false;
		
		private final Set<TransactionEventListener> transactionListeners = new HashSet<TransactionEventListener>();
		private int reduceToNumber = 1;
		
		public GraphDBInserterThread() {
			this.setName("GraphDBInserterThread");
		}
		
		public void registerTransactionEventListener(TransactionEventListener listener) {
			synchronized (transactionListeners) {
				transactionListeners.add(listener);
			}
		}
		
		public void removeTransactionEventListener(TransactionEventListener listener) {
			synchronized (transactionListeners) {
				transactionListeners.remove(listener);
			}
		}
		
		private void fireTransactionStarted(Transaction transaction) {
			synchronized (transactionListeners) {
				for(TransactionEventListener listener : transactionListeners) {
					listener.startedTransaction(transaction);
				}
			}
		}
		
		private void fireTransactionFinished(Transaction transaction) {
			synchronized (transactionListeners) {
				for(TransactionEventListener listener : transactionListeners) {
					listener.finishedTransaction(transaction);
				}
			}
		}
		
 		public void run() {
			while (!shutdown) {
				while (jobManager.hasJobs() && !shutdown) {
					GraphCreatorJob job = jobManager.next();
					DocearLogger.info("working on " + job.getName() + " with " + job.size() + " steps");
					DocearLogger.info(jobManager.remaining() + " jobs remaining");
					if (job.isAborted()) {
						failedJob(job);
						continue;
					}
					long start = System.currentTimeMillis();
					Transaction tx = graphDb.beginTx();
					fireTransactionStarted(tx);
					boolean success = false;
					try {
						long stepsTime = System.currentTimeMillis();
						while (job.hasNext()) {
							job.next().run(graphDb);
							if (job.isAborted()) {
								break;
							}
						}
						DocearLogger.info("steps (job: " + job.getName() + ") took: " + (System.currentTimeMillis()-stepsTime));
						if (job.isAborted()) {
							tx.failure();
							failedJob(job);
						}
						else {
							reduceRevisionsTo(job, reduceToNumber);
							tx.success();							
							finishedJob(job);
							success = true;
						}
					}
					catch(DuplicateRevisionException e) {
						tx.failure();
						finishedJob(job);
						DocearLogger.info("Revision ("+job.getName()+") already exists.");
					}
					catch(Exception e) {
						tx.failure();
						failedJob(job);
						DocearLogger.error(e);
					}
					finally {
						tx.finish();
						fireTransactionFinished(tx);
						if(success) {
							updateFulltextIndex(job);
							postMetaData(job);
						}
					}
					Long consumption = (System.currentTimeMillis() - start);
					DocearLogger.info("["+dateFormat.format(new Date())+"] Time consumption for (job: " + job.getName() + "): " + (consumption) + "/" + getAvgTime(consumption));					
				}

				try {
					sleep(100);

				}
				catch (InterruptedException e) {
				}
			}
			DocearLogger.info("GraphDBWorker Thread stopped");
		}
 		
 		public synchronized long getAvgTime(Long consumption) { 			
 			this.avgTime = (this.avgTime * this.counter + consumption) / ++this.counter;
			return this.avgTime;			
		}

		private void failedJob(GraphCreatorJob job) {
			final File file =  job.getFile();
			final File to = new File(failureDirectory, file.getName());
			
			if(to.exists()) {
				finishedJob(job);
				return;
			}
			
			try {
				move(file, failureDirectory);
			}
			catch (IOException e1) {
				System.gc();				
				new Thread() {
					public void run()  {
						while(true) {
							try {
								move(file, failureDirectory);
								return;
							}
							catch (IOException e) {								
							}
							try {								
								sleep(100);
							} catch (InterruptedException e) {
							}
						}
					}
				}.start();
			}
			
		}

		private void finishedJob(GraphCreatorJob job) {
			final File file =  job.getFile();
			if(!file.delete()) {
				System.gc();
				
				new Thread() {
					public void run()  {
						while(!file.delete()) {
							try {
								sleep(100);
							} catch (InterruptedException e) {
							}
							System.gc();
						}
					}
				}.start();
				
			}
			final File propFile = new File(file.getAbsolutePath()+".properties");
			if(!propFile.delete()) {
				System.gc();
				
				new Thread() {
					public void run()  {
						while(!propFile.delete()) {
							try {
								sleep(100);
							} catch (InterruptedException e) {
							}
						}
					}
				}.start();
				
			}
		}

		 public void shutdown() {
			 this.shutdown  = true;
		 }
	}


	private void updateFulltextIndex(GraphCreatorJob job) {
		try {
			this.luceneCtrl.updateUserMap(job.getUserID(), (Node) job.getFromContext("revisionNode"));
		} 
		catch (Exception e) {
			DocearLogger.error(e);
		}
	}

	private void postMetaData(GraphCreatorJob job) {
		@SuppressWarnings("unchecked")
		Map<String, HashReferenceItem> hashOccurences = (Map<String, HashReferenceItem>)job.getFromContext("hashOccurences");
		if(hashOccurences != null && hashOccurences.size()>0) {
			String text = prepareHashCSV(hashOccurences);
			addServicePost(job.getRevision(), text);
		}
	}
	
	private Integer servicePostCounter = 0; 
	private void addServicePost(final String revision, final String text) {
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true"))  return;
		
		serviceRequestExecutor.execute(new Runnable() {
			
			public void run() {
				long time = System.currentTimeMillis();
				try {
					// construct data					
					MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
					formParams.add("username", "pdfdownloader");
					formParams.add("pdfHashes", text);					 
					
					WebResource webResource = client.resource(DOCEAR_SERVICES + "/internal/mindmaps/"+revision+"/pdf_hashes");
					Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");

					ClientResponse response = builder.post(ClientResponse.class, formParams);

					if (response.getStatus() != 200) {
						DocearLogger.error("pdf_hashes for revision("+revision+") Exception: " + response.getStatus());
					}
				}
				catch (Exception e) {
					DocearLogger.error(e);
				}
				finally {
					DocearLogger.info("---- post pdf_hashes for revision("+revision+") time: " + (System.currentTimeMillis()-time));
				}
				synchronized (servicePostCounter) {
					servicePostCounter--;
					DocearLogger.info("==== POSTS IN QUEUE: " + servicePostCounter);
				}	
				
			}
		});
		synchronized (servicePostCounter) {
			servicePostCounter++;
			DocearLogger.info("==== POSTS IN QUEUE: " + servicePostCounter);
		}
		
	}

	private String prepareHashCSV(Map<String, HashReferenceItem> hashOccurences) {
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for(Entry<String, HashReferenceItem> entry : hashOccurences.entrySet()) {
			count++;
			if(sb.length() > 0) {
				sb.append("@@|.|@@");
			}
			sb.append(entry.getValue().getCsvValue());
		}
		DocearLogger.info("build csv from "+count+" entries with length of "+sb.length());
		return sb.toString();
	}

	Comparator<String> revisionKeyComparator = new Comparator<String>() {
		public int compare(String revision1, String revision2 ) {
			return revision1.compareTo(revision2);
		}
	};
	
	private void reduceRevisionsTo(GraphCreatorJob job, int keepNumber) {
		long time = System.currentTimeMillis();
		//skip it when either no user node or no map node exists
		if (job.getFromContext("userNode") == null || job.getFromContext("mapNode") == null) {
			return;
		}
		
		//retrieve the necessary nodes from the job context
		Node userNode = (Node) job.getFromContext("userNode");
		Node mapNode = (Node) job.getFromContext("mapNode");
		
		Map<String, Node> orderedRevisions = new TreeMap<String, Node>();
		
		//iterate through all map revisions
		Iterator<Relationship> iter = mapNode.getRelationships(Type.REVISION).iterator();
		int revCounter = 0;
		while (iter.hasNext()) {
			try {
				revCounter++;
				Relationship rs = iter.next();
				
				//the revision node is at the end of the relationship
				Node revisionNode = rs.getEndNode();
				
				//if revision belongs to user
				if(belongsTo(revisionNode, userNode)) {
					Node previous = orderedRevisions.put(String.valueOf(revisionNode.getProperty("CREATED")), revisionNode);
					// in case of 2 revisions with the same created date appear
					if(previous != null) {
						String pId = previous.getProperty("ID").toString();
						String cId = revisionNode.getProperty("ID").toString();
						if(pId.compareTo(cId) <= 0) {
							orderedRevisions.put(String.valueOf(previous.getProperty("CREATED"))+"_1", previous);
						}
						else {
							orderedRevisions.put(String.valueOf(previous.getProperty("CREATED")), previous);
							orderedRevisions.put(String.valueOf(revisionNode.getProperty("CREATED"))+"_1", revisionNode);
						}					
					}
				}
			}
			catch (Exception e) {
				DocearLogger.error(e);
			}
		}
		
		Set<Node> markedRemoveTargets = new LinkedHashSet<Node>();
		
		//remove the revision that are too much
		Iterator<Node> revisionIter = orderedRevisions.values().iterator();
		for (int i = orderedRevisions.size(); revisionIter.hasNext() && i > keepNumber; i--) {
			Node revisionNode = revisionIter.next();
			Traverser traverser = getRevisionsTraversal(revisionNode);
			for (Node node : traverser.nodes()) {				
				if(markedForRemoval(node)) {
					markedRemoveTargets.add(node);
				}
			}
		}
		for (Node node : markedRemoveTargets) {
			try {
				//remove all relations
				for(Relationship rel : node.getRelationships()) {
					rel.delete();
				}
				//remove node 
				node.delete();
			}
			catch (Exception e) {
				DocearLogger.error(e);
			}
		}
		DocearLogger.info("reduction from "+revCounter+" to "+keepNumber+" revisions finished. ("+(System.currentTimeMillis()-time)+")");
	}
	
	private boolean markedForRemoval(Node node) {
		Iterator<Relationship> incomings = node.getRelationships(Direction.INCOMING).iterator();
		boolean mark = true;
		while(incomings.hasNext()) {
			Relationship rel = incomings.next();
			//ignore the node if and only if there are no incoming relationships with a revision id anymore 
			if(rel.isType(Type.CHILD) || rel.isType(Type.ROOT)) {
				if(rel.getPropertyKeys().iterator().hasNext()) {
					mark = false;
				}
				else {
					rel.delete();
				}
			}
		}
		return mark;
	}

	private void removeRevisionId(Relationship rel, String revisionId) {
		rel.removeProperty(revisionId);
	}

	private boolean belongsTo(Node revisionNode, Node userNode) {
		try {
			Iterator<Relationship> iter = revisionNode.getRelationships(Direction.INCOMING, UserRelationship.OWNS).iterator();
			while (iter.hasNext()) {
				Relationship rs = iter.next();
				if(rs.getStartNode().equals(userNode)) {
					return true;
				}
			}			
		}
		catch (Exception e) {
			DocearLogger.error(e);
		}
		return false;
	}

	private Traverser getRevisionsTraversal(final Node revisionNode) {
		final String revisionId = revisionNode.getProperty("ID").toString();		
		TraversalDescription td = Traversal.description()
				.relationships(Type.CHILD, Direction.OUTGOING)
				.relationships(Type.ROOT, Direction.OUTGOING)
				.uniqueness(Uniqueness.NODE_PATH).depthFirst().evaluator(new Evaluator() {
			public Evaluation evaluate(final Path path) {
				if (path.length() == 0) {
					return Evaluation.EXCLUDE_AND_CONTINUE;
				} 
				else if (path.lastRelationship().hasProperty(revisionId)) {
					removeRevisionId(path.lastRelationship(), revisionId);
					return Evaluation.INCLUDE_AND_CONTINUE;
				}
				return Evaluation.EXCLUDE_AND_PRUNE;
			}
		});

		return td.traverse(revisionNode);
	}


}
