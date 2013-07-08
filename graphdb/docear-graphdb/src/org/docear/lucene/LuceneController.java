package org.docear.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.docear.graphdb.GraphDbWorker;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.AbstractGraphDatabase;

public class LuceneController {

	public enum DirtyUserUpdateType {
		ALL, SELECTION
	}

	private static LuceneController self;
	
	private IndexWriter iw;
	private File indexDirFile;
	private Set<DirtyUserItem> dirtyUsers = new HashSet<DirtyUserItem>();	

	private GraphDbWorker worker;
	
	public LuceneController(File indexDirectory, AbstractGraphDatabase graphDb) {
		if(self == null) {
			self = this;
		}
		// file has to be a path to an existing directory or a yet not existing path
		if (indexDirectory == null || (indexDirectory.exists() && !indexDirectory.isDirectory())) {
			throw new IllegalArgumentException();
		}		
		// create path to the directory if necessary
		if (!indexDirectory.exists()) {
			indexDirectory.mkdirs();
		}
		indexDirFile = indexDirectory;
		worker = new GraphDbWorker(graphDb);		
	}
	
	public static LuceneController getCurrentController() {
		return self;
	}
	
	public GraphDbWorker getGraphDbWorker() {
		return worker;
	}

	/**
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	private void updateIndex() throws IOException, CorruptIndexException {
		long time;
		DirtyUserItem item = null;
		while ((item = peekDirtyUser()) != null) {
			time = System.currentTimeMillis();
			List<Map<String, String>> mapData;
			switch(item.getUpdateType()) {
				case SELECTION: mapData = getGraphDbWorker().getUserMapDataList(item.getUserID(), item.getDirtyMaps(), false); break;
				default: mapData = getGraphDbWorker().getUserMapDataList(item.getUserID(), false);				
			}
			System.out.println("retrieve mapData("+mapData.size()+"): "+(System.currentTimeMillis()-time));
			time = System.currentTimeMillis();
			for (Map<String, String> data : mapData) {
				try {
					updateCounter++;
					getIndexWriter().updateDocument(new Term("uid", data.get("id")), createMapDocument(String.valueOf(item.getUserID()), data.get("id"), data.get("dcr_id"), data.get("text")));
					getIndexWriter().commit();
				}
				catch (Exception e) {
					e.printStackTrace();
					getIndexWriter().rollback();
				}					
			}
			System.out.println("add mapDocuments("+mapData.size()+"): "+(System.currentTimeMillis()-time));
			
			removeDirtyUser(item);
		}
		
		close();
	}

	private DirtyUserItem peekDirtyUser() {
		synchronized (dirtyUsers) {
			try {
				return dirtyUsers.iterator().next();
			}
			catch (Exception e) {
			}
			return null;
		}
	}
	
	private void addDirtyUser(DirtyUserItem item) {
		synchronized (dirtyUsers) {
			try {
				dirtyUsers.add(item);
			}
			catch (Exception e) {
			}
		}
	}

	private boolean removeDirtyUser(DirtyUserItem item) {
		synchronized (dirtyUsers) {
			try {
				return dirtyUsers.remove(item);
			}
			catch (Exception e) {
			}
			return false;
		}
	}
	
	private Document createMapDocument(String userId, String uid, String mapId, String text) {
		Document doc = new Document();
		doc.add(new Field("text", text, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
		doc.add(new Field("user", userId, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("uid", uid, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("dcr_id", mapId, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	private IndexWriter getIndexWriter() throws IOException {
		if (iw == null) {
			iw = new IndexWriter(FSDirectory.open(indexDirFile),
					new IndexWriterConfig(Version.LUCENE_35, new DocearAnalyzer(Version.LUCENE_35)).setRAMBufferSizeMB(64));
		}
		return iw;
	}
	
	private synchronized void close() throws CorruptIndexException, IOException {	
		if (iw != null) {
			iw.close();
		}
		iw = null;
	}
	int updateCounter = 0;
	public void updateUserMap(Integer userID, Node map) {
		if(userID == null || map == null) {
			throw new NullPointerException();
		}
		DirtyUserItem item = new DirtyUserItem(userID);
		item.addDirtyMap(map);
		addDirtyUser(item);		
		try {
			updateIndex();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("lucene updates: "+updateCounter);
	}
	
	public IndexReader getIndexReader() throws IOException {
		return IndexReader.open(getIndexWriter(), true); //true=apply deletes (slower/costly); false=ignore deletes(if deleted docs are no problem)
	}
	
	class DirtyUserItem {
		private final int userID;
		private DirtyUserUpdateType updateType;
		private List<Node> maps = new ArrayList<Node>();
		
		public DirtyUserItem(Integer userID) {
			this.userID = userID;
			this.updateType = DirtyUserUpdateType.ALL;
		}

		public int getUserID() {
			return userID;
		}

		public DirtyUserUpdateType getUpdateType() {
			return updateType;
		}
		
		public void addDirtyMap(Node map) {
			if(map == null) {
				throw new NullPointerException();
			}
			maps .add(map);
			this.updateType = DirtyUserUpdateType.SELECTION;
		}
		
		public List<Node> getDirtyMaps() {
			return maps;
		}
	}
}
