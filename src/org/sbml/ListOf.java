/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml;

import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ListOf<E extends SBase> extends SBase implements List<E>,
		Serializable, Cloneable {

	/**
	 * 
	 * @author andreas
	 *
	 * @param <E>
	 */
	private static class Entry<E> {
		E element;
		Entry<E> next;
		Entry<E> previous;

		Entry(E element, Entry<E> next, Entry<E> previous) {
			this.element = element;
			this.next = next;
			this.previous = previous;
		}
	}

	/**
	 * 
	 * @author andreas
	 *
	 */
	private class ListItr implements ListIterator<E> {
		private Entry<E> lastReturned = header;
		private Entry<E> next;
		private int nextIndex;
		private int expectedModCount = modCount;

		ListItr(int index) {
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index: " + index
						+ ", Size: " + size);
			if (index < (size >> 1)) {
				next = header.next;
				for (nextIndex = 0; nextIndex < index; nextIndex++)
					next = next.next;
			} else {
				next = header;
				for (nextIndex = size; nextIndex > index; nextIndex--)
					next = next.previous;
			}
		}

		/**
		 * 
		 */
		public void add(E e) {
			checkForComodification();
			lastReturned = header;
			addBefore(e, next);
			nextIndex++;
			expectedModCount++;
		}

		/**
		 * 
		 */
		public boolean hasNext() {
			return nextIndex != size;
		}

		/**
		 * 
		 */
		public boolean hasPrevious() {
			return nextIndex != 0;
		}

		/**
		 * 
		 */
		public E next() {
			checkForComodification();
			if (nextIndex == size)
				throw new NoSuchElementException();

			lastReturned = next;
			next = next.next;
			nextIndex++;
			return lastReturned.element;
		}

		/**
		 * 
		 */
		public int nextIndex() {
			return nextIndex;
		}

		/**
		 * 
		 */
		public E previous() {
			if (nextIndex == 0)
				throw new NoSuchElementException();

			lastReturned = next = next.previous;
			nextIndex--;
			checkForComodification();
			return lastReturned.element;
		}

		/**
		 * 
		 */
		public int previousIndex() {
			return nextIndex - 1;
		}

		/**
		 * 
		 */
		public void remove() {
			checkForComodification();
			Entry<E> lastNext = lastReturned.next;
			try {
				ListOf.this.remove(lastReturned);
			} catch (NoSuchElementException e) {
				throw new IllegalStateException();
			}
			if (next == lastReturned)
				next = lastNext;
			else
				nextIndex--;
			lastReturned = header;
			expectedModCount++;
		}

		/**
		 * 
		 */
		public void set(E e) {
			if (lastReturned == header)
				throw new IllegalStateException();
			checkForComodification();
			lastReturned.element = e;
		}

		/**
		 * 
		 */
		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588467260915307797L;
	private Entry<E> header = new Entry<E>(null, null, null);
	private int size;

	private int modCount;

	public ListOf() {
		super();
		header.next = header.previous = header; // null
		size = modCount = 0;
	}

	public ListOf(ListOf<E> listOf) {
		this();
		addAll(listOf);
		parentSBMLObject = listOf.getParentSBMLObject();
	}

	public boolean add(E e) {
		addBefore(e, header);
		return true;
	}

	public void add(int index, E element) {
		addBefore(element, (index == size ? header : entry(index)));
	}

	public boolean addAll(Collection<? extends E> c) {
		return addAll(size, c);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ size);
		int numNew = c.size();
		if (numNew == 0)
			return false;
		modCount++;

		Entry<E> successor = (index == size ? header : entry(index));
		Entry<E> predecessor = successor.previous;
		for (E e : c) {
			Entry<E> entry = new Entry<E>(e, successor, predecessor);
			predecessor.next = entry;
			predecessor = entry;
		}
		successor.previous = predecessor;

		size += numNew;
		return true;
	}

	public void clear() {
		Entry<E> e = header.next;
		while (e != header) {
			Entry<E> next = e.next;
			e.next = e.previous = null;
			e.element = null;
			e = next;
		}
		header.next = header.previous = header;
		size = 0;
		modCount++;
	}

	// @Override
	public ListOf<E> clone() {
		return new ListOf<E>(this);
	}

	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	public boolean containsAll(Collection<?> c) {
		boolean contains = true;
		for (Object o : c)
			contains = contains && contains(o);
		return contains;
	}

	// @Override
	public boolean equals(Object o) {
		if (o instanceof List) {
			List<?> l = (List<?>) o;
			if (l.containsAll(this) && size == l.size())
				return true;
		}
		return false;
	}

	public E get(int index) {
		return entry(index).element;
	}

	public int indexOf(Object o) {
		int index = 0;
		if (o == null) {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (e.element == null)
					return index;
				index++;
			}
		} else {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (o.equals(e.element))
					return index;
				index++;
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Iterator<E> iterator() {
		return new ListItr(0);
	}

	public int lastIndexOf(Object o) {
		int index = size;
		if (o == null) {
			for (Entry<E> e = header.previous; e != header; e = e.previous) {
				index--;
				if (e.element == null)
					return index;
			}
		} else {
			for (Entry<E> e = header.previous; e != header; e = e.previous) {
				index--;
				if (o.equals(e.element))
					return index;
			}
		}
		return -1;
	}

	public ListIterator<E> listIterator() {
		return new ListItr(0);
	}

	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	public E remove(int index) {
		Entry<E> e = entry(index);
		remove(e);
		return e.element;
	}

	public boolean remove(Object o) {
		if (o == null) {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (e.element == null) {
					remove(e);
					return true;
				}
			}
		} else {
			for (Entry<E> e = header.next; e != header; e = e.next) {
				if (o.equals(e.element)) {
					remove(e);
					return true;
				}
			}
		}
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		boolean removed = true;
		for (Object o : c)
			removed = removed && remove(o);
		return removed;
	}

	public boolean retainAll(Collection<?> c) {
		for (E e : this)
			if (!c.contains(e))
				remove(e);
		return true;
	}

	public E set(int index, E element) {
		Entry<E> e = entry(index);
		E oldVal = e.element;
		e.element = element;
		return oldVal;
	}

	public int size() {
		return size;
	}

	public List<E> subList(int fromIndex, int toIndex) {
		ListOf<E> l = new ListOf<E>();
		for (int i = fromIndex; i < toIndex; i++)
			l.add(get(i));
		return l;
	}

	public Object[] toArray() {
		Object a[] = new Object[size];
		int i = 0;
		for (Object object : this)
			a[i++] = object;
		return a;
	}

	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
					.getComponentType(), size);
		int i = 0;
		Object[] result = a;
		for (Entry<E> e = header.next; e != header; e = e.next)
			result[i++] = e.element;

		if (a.length > size)
			a[size] = null;

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#toString()
	 */
	// @Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append('[');
		int i = 0;
		for (E e : this) {
			if (i > 0)
				string.append(", ");
			string.append(e.toString());
			i++;
		}
		string.append(']');
		return string.toString();
	}

	private void addBefore(E e, Entry<E> entry) {
		Entry<E> newEntry = new Entry<E>(e, entry, entry.previous);
		newEntry.previous.next = newEntry;
		newEntry.next.previous = newEntry;
		size++;
		modCount++;
	}

	/**
	 * Returns the indexed entry.
	 */
	private Entry<E> entry(int index) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ size);
		Entry<E> e = header;
		if (index < (size >> 1)) {
			for (int i = 0; i <= index; i++)
				e = e.next;
		} else {
			for (int i = size; i > index; i--)
				e = e.previous;
		}
		return e;
	}
}
