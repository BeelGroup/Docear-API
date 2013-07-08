package org.sciplore.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MultiValueMap<K, V> implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	private HashMap<K, ArrayList<V>> keys = new HashMap<K, ArrayList<V>>();	
	
	public MultiValueMap() {
		this.keys = new HashMap<K, ArrayList<V>>();		
	}
	
	public void put(K key, V value) {
		ArrayList<V> values = this.keys.get(key);
		if (values == null) {
			values = new ArrayList<V>();
		}
		values.add(value);
		this.keys.put(key, values);
	}
	
	public Set<K> keySet() {
		return this.keys.keySet();
	}
	
	public List<V> getSortedList(boolean desc) {
		List<V> list = new ArrayList<V>();
		for (K key : this.keys.keySet()) {
			for (V value : this.keys.get(key)) {
				if (desc) {
					list.add(0, value);
				}
				else {
					list.add(value);
				}
			}
		}
		return list;
	}	
	
	public int getSize() {
		int size=0;
		
		for (K key : this.keys.keySet()) {
			for (@SuppressWarnings("unused") V value : this.keys.get(key)) {
				size++;
			}
		}
		
		return size;
	}
	
	public List<V> get(K key) {
		return this.keys.get(key);
	}
	
	public V get(K key, int index) {
		return this.keys.get(key).get(index);
	}
}
