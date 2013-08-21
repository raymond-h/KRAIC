package se.kayarr.ircclient.shared;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class NotifyingList<E> implements List<E> {
	private List<E> wrappedList;
	
	private Set<Listener<E>> listeners = new HashSet<Listener<E>>();
	
	public NotifyingList(List<E> list) {
		wrappedList = list;
	}

	public void addListener(Listener<E> listener) {
		listeners.add(listener);
	}
	
	public void removeListener(Listener<E> listener) {
		listeners.remove(listener);
	}
	
	public boolean add(E object) {
		add(wrappedList.size(), object);
		return true;
	}
	
	public void add(int location, E object) {
		wrappedList.add(location, object);
		eventOccured(object, Action.ADDED);
	}
	
	public void clear() {
		wrappedList.clear();
		eventOccured(null, Action.CLEARED);
	}

	public E remove(int location) {
		E r = wrappedList.remove(location);
		eventOccured(r, Action.REMOVED);
		return r;
	}

	@SuppressWarnings("unchecked") //I know what I am doing
	public boolean remove(Object object) {
		boolean modified = wrappedList.remove(object);
		if(modified) eventOccured( (E)object, Action.REMOVED);
		return modified;
	}

	protected void eventOccured(E e, Action action) {
		for(Listener<E> l : listeners) {
			l.onListModified(e, action);
		}
	}

	public interface Listener<E> {
		/**
		 * Callback method that is called when the list the {@code Listener} is added to is modified.
		 * 
		 * @author Raymond
		 * @param object The object that the action involved
		 * @param action The action that was taken (see {@link NotifyingList.Action})
		 */
		public void onListModified(E object, Action action);
	}
	
	/**
	 * An Enum describing the action that occured on the NotifyingList. {@code Action.ADDED} and {@code Action.REMOVED}
	 * are always associated with the object in question being added or removed from the list, but {@code Action.CLEARED}
	 * is associated with {@code null}.
	 * 
	 * @author Raymond
	 */
	public enum Action {
		ADDED, REMOVED, CLEARED
	}

	public boolean addAll(Collection<? extends E> arg0) {
		return wrappedList.addAll(arg0);
	}

	public boolean addAll(int arg0, Collection<? extends E> arg1) {
		return wrappedList.addAll(arg0, arg1);
	}

	public boolean contains(Object object) {
		return wrappedList.contains(object);
	}

	public boolean containsAll(Collection<?> arg0) {
		return wrappedList.containsAll(arg0);
	}

	public E get(int location) {
		return wrappedList.get(location);
	}

	public int indexOf(Object object) {
		return wrappedList.indexOf(object);
	}

	public boolean isEmpty() {
		return wrappedList.isEmpty();
	}

	public Iterator<E> iterator() {
		return wrappedList.iterator();
	}

	public int lastIndexOf(Object object) {
		return wrappedList.lastIndexOf(object);
	}

	public ListIterator<E> listIterator() {
		return wrappedList.listIterator();
	}

	public ListIterator<E> listIterator(int location) {
		return wrappedList.listIterator(location);
	}

	public boolean removeAll(Collection<?> arg0) {
		return wrappedList.removeAll(arg0);
	}

	public boolean retainAll(Collection<?> arg0) {
		return wrappedList.retainAll(arg0);
	}

	public E set(int location, E object) {
		return wrappedList.set(location, object);
	}

	public int size() {
		return wrappedList.size();
	}

	public List<E> subList(int start, int end) {
		return wrappedList.subList(start, end);
	}

	public Object[] toArray() {
		return wrappedList.toArray();
	}

	public <T> T[] toArray(T[] array) {
		return wrappedList.toArray(array);
	}
}
