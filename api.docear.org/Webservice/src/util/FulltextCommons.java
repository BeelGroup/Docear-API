package util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AlreadyConnectedException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docear.pdf.PdfDataExtractor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.Indexer;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentXref;
import org.sciplore.utilities.concurrent.AsynchUtilities;

import de.intarsys.pdf.cos.COSVisitorException;
import de.intarsys.pdf.parser.COSLoadException;

public class FulltextCommons {

	public static File createDocumentPlainTextFile(String hash, InputStream inputStream, boolean overwrite) throws IOException {
		File file = new File(Indexer.DOCUMENT_PLAINTEXT_DIRECTORY, hash + ".zip");
		if (!overwrite && file.exists() && file.length() > 0) {
			throw new AlreadyConnectedException();
		} else {
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					throw new IOException("could not create directory '" + file.getParentFile() + "'");
				}
			}
			if (!file.exists() && !file.createNewFile()) {
				throw new IOException("could not create file '" + file + "'");
			}

			// creating zip file
			if (inputStream != null) {
				try {
					// initiate zip output stream
					ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
					// prepare entry with the containing document-hash name
					ZipEntry entry = new ZipEntry(hash + ".txt");
					// creating entry in zip archive
					zos.putNextEntry(entry);
					// writing the whole text to the zip archive
					int b = 0;
					while ((b = inputStream.read()) > -1) {
						zos.write(b);
					}
					// closing and finishing everything
					zos.closeEntry();
					zos.finish();
					zos.flush();
					zos.close();
				} catch (IOException e) {
					throw new IOException("could not create zip archive '" + file + "'");
				}

			}
		}
		return file;
	}

	public static String updatePlainText(Document doc, File file) throws IOException {
		try {
			// trying to get a hash code representing the given document
			String hash = extractHash(file);
			if (hash == null) {
				throw new Exception("could not generate a hash code for " + file.getPath());
			}

			// trying to extract the plain text for indexing
			String text = extractText(file);
			if (text == null) {
				throw new Exception("no text extracted" + file.getPath());
			}

			// save the extracted plain text into a file (zip compressed)
			createDocumentPlainTextFile(hash, new ByteArrayInputStream(Charset.forName("UTF-8").encode(text).array()), false);

			// tell the indexer to update the plain text for the given document
			requestPlainTextUpdate(doc, hash);

			return hash;
		} catch (Exception e) {
			throw new IOException("", e);
		}
	}

	private static String extractHash(File file) throws IOException {
		PdfDataExtractor extractor = new PdfDataExtractor(file);
		return extractor.getUniqueHashCode();
	}

	private static String extractText(File file) throws COSVisitorException, IOException, COSLoadException {
		PdfDataExtractor extractor = new PdfDataExtractor(file);
		return extractor.extractPlainText();
	}

	public static void requestPlainTextUpdate(Document document, final String hash, final Integer xref_id) {
		// run the update task as a separate thread to keep the web service more responsive
		final Integer docId = document.getId();
		AsynchUtilities.executeAsynch(new Runnable() {
			int tryCounter = 0;
			public void run() {
				long time = System.currentTimeMillis();
				Session session = Tools.getSession();
				Transaction transaction = session.beginTransaction();
				try {
					Document doc = (Document) session.get(Document.class, docId);
					
					// if no document was found: maybe we have a race condition and will try again later
					if(doc == null) {
						if(tryCounter < 10) {
							tryCounter++;
							try {
								Thread.sleep(20);
							}
							catch (Exception e) {
							}
							AsynchUtilities.executeAsynch(this);
						}
						transaction.rollback();
						return;
					} else {
					
						Tools.getLuceneIndexer().updateDocument(doc, hash).commit();
	
						// update the xref status: set indexed=1
						if(xref_id != null) {
							for (DocumentXref xref : doc.getXrefs()) {
								if (xref.getId().equals(xref_id)) {
									xref.setIndexed(1);
									session.saveOrUpdate(xref);
									session.flush();
									break;
								}
							}
						}
						transaction.commit();
					}
				} catch (Throwable e) {
					transaction.rollback();
					System.err.println("Exception in FulltextCommons.executePlainTextUpdate()@executor.execute(): " + e.getMessage());
				} finally {
					Tools.tolerantClose(session);
					// System.gc();
				}
				System.out.println("requestPlainTextUpdate@executor.execute(): " + (System.currentTimeMillis()-time));
				System.out.println("remaining tasks: " + (AsynchUtilities.getSingleExecTaskCount()-1));
			}
		});

	}

	public static void requestPlainTextUpdate(final Document doc, final String hash) {
		requestPlainTextUpdate(doc, hash, null);
	}

}
