package org.sciplore.deserialize.mapper;

import org.sciplore.deserialize.creator.CitationCreator;
import org.sciplore.deserialize.creator.CitationOccurenceMapCreator;
import org.sciplore.deserialize.creator.DefaultListCreator;
import org.sciplore.deserialize.creator.DefaultStringCreator;
import org.sciplore.deserialize.creator.DocumentCreator;
import org.sciplore.deserialize.creator.DocumentXrefCategoryCreator;
import org.sciplore.deserialize.creator.DocumentXrefCreator;
import org.sciplore.deserialize.creator.FeedbackCreator;
import org.sciplore.deserialize.creator.FulltextUrlCreator;
import org.sciplore.deserialize.creator.InstitutionCreator;
import org.sciplore.deserialize.creator.ObjectCreator;
import org.sciplore.deserialize.creator.AuthorObjectCreator;
import org.sciplore.deserialize.creator.PersonXrefCreator;
import org.sciplore.deserialize.creator.ReferencesCreator;
import org.sciplore.deserialize.creator.ShortCreator;
import org.sciplore.deserialize.reader.ObjectCreatorMapper;

public class MrDlibXmlMapper extends ObjectCreatorMapper {

	protected MrDlibXmlMapper() {
	}

	public static ObjectCreatorMapper getDefaultMapper() {
		return new MrDlibXmlMapper().initializeDefault();
	}

	private ObjectCreatorMapper initializeDefault() {
		// initialize Mapper
		ObjectCreator stringCreator = new DefaultStringCreator();
		ObjectCreator shortCreator = new ShortCreator();
		ObjectCreator listCreator = new DefaultListCreator();

		this.addCreator("homonyms", listCreator);
		this.addCreator("authorxrefs", listCreator);
		this.addCreator("documents", listCreator);
		this.addCreator("authors", listCreator);
		this.addCreator("fulltexts", listCreator);
		this.addCreator("xrefs", listCreator);
		this.addCreator("comments", listCreator);
		this.addCreator("categories", listCreator);
		this.addCreator("references", new ReferencesCreator());
		this.addCreator("occurences", listCreator);
		this.addCreator("authorxrefs", listCreator);

		this.addCreator("author", new AuthorObjectCreator());
		this.addCreator("homonym", new AuthorObjectCreator());
		this.addCreator("name_first", stringCreator);
		this.addCreator("name_middle", stringCreator);
		this.addCreator("name_last", stringCreator);
		this.addCreator("name_last_prefix", stringCreator);
		this.addCreator("name_last_suffix", stringCreator);

		this.addCreator("authorxref", new PersonXrefCreator());
		
		this.addCreator("reference", new CitationCreator());
		this.addCreator("occurence", new CitationOccurenceMapCreator());
		
		this.addCreator("document", new DocumentCreator());
		this.addCreator("year", shortCreator);
		this.addCreator("title", stringCreator);
		this.addCreator("doi", stringCreator);
		this.addCreator("abstract", stringCreator);

		this.addCreator("fulltext", new FulltextUrlCreator());

		this.addCreator("comment", new FeedbackCreator());
		
		this.addCreator("xref", new DocumentXrefCreator());
		this.addCreator("sourceid", stringCreator);
		this.addCreator("releasedate", stringCreator);
		this.addCreator("category", new DocumentXrefCategoryCreator());
		this.addCreator("id", stringCreator);
		this.addCreator("organization", new InstitutionCreator());
		this.addCreator("name", stringCreator);
		this.addCreator("url", stringCreator);
		return this;
	}

}
