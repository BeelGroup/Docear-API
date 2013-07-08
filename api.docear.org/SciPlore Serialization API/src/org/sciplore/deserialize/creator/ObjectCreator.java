package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Node;

public interface ObjectCreator {
	/**
	 * 
	 * @param path the <code>TreePath</code> in the traversed hierarchical structure
	 * @param element the <code>Node</code> for which this creator was called
	 * @param children all the child objects that have been created before.
	 * @return <code>Object</code> from the given creator, or <code>null</code> if an exception occurred or the creator decides it is not a valid object.
	 */
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children);
}
