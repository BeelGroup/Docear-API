package org.docear.graphdb.relationship;

import org.neo4j.graphdb.RelationshipType;

public enum Type implements RelationshipType {
	CHILD, MAP, REVISION, ROOT, DOCUMENT_ANNOTATION, DOCUMENT_NULL_ANNOTATION, DOCUMENT_LINK, DOCUMENTS, DOCUMENT
}
