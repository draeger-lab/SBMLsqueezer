package sabiork.util;

/**
 * A class for representing a key-value pair.
 * 
 * @author Matthias Rall
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public class Pair<K, V> {

	private K key;
	private V value;

	/**
	 * Creates a new empty key-value pair.
	 */
	public Pair() {
	}

	/**
	 * Creates a new key-value pair.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public Pair(final K key, final V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the key of the key-value pair.
	 * 
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * Returns the value of the key-value pair.
	 * 
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Sets the key of the key-value pair.
	 * 
	 * @param key
	 *            the key
	 * @return the key-value pair
	 */
	public Pair<K, V> setKey(final K key) {
		this.key = key;
		return this;
	}

	/**
	 * Sets the value of the key-value pair.
	 * 
	 * @param value
	 *            the value
	 * @return the key-value pair
	 */
	public Pair<K, V> setValue(final V value) {
		this.value = value;
		return this;
	}

	/**
	 * Sets the key and the value of the key-value pair.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the key-value pair
	 */
	public Pair<K, V> setPair(final K key, final V value) {
		this.key = key;
		this.value = value;
		return this;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String toString() {
		return "[" + key + "=" + value + "]";
	}

}