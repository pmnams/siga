package br.gov.jfrj.siga.base;

import java.util.Map.Entry;

public class Par<K extends Comparable<K>, V> implements
		Comparable<Par<K, V>>, Entry<K, V> {

	private final K key;
	private V value;

	public Par(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public Par(Entry<? extends K, ? extends V> entry) {
		this.key = entry.getKey();
		this.value = entry.getValue();
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public int compareTo(Par<K, V> o) {
		return getKey().compareTo(o.getKey());
	}

}
