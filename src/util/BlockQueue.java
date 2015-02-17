package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class BlockQueue<T> implements Queue<T>{
	
	private Queue<T> queue = new LinkedList<T>();

	public synchronized int size() {
		return queue.size();
	}

	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	public boolean contains(Object o) {
		throw new RuntimeException("contains is called");
	}

	public Iterator<T> iterator() {
		throw new RuntimeException("iterator is called");
	}

	public Object[] toArray() {
		throw new RuntimeException("toArray is called");
	}

	public <T> T[] toArray(T[] a) {
		throw new RuntimeException("toArray is called");
	}

	public boolean remove(Object o) {
		throw new RuntimeException("remove is called");
	}

	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("containsAll is called");
	}

	public boolean addAll(Collection<? extends T> c) {
		throw new RuntimeException("addAll is called");
	}

	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("removeAll is called");
	}

	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("retainAll is called");
	}

	public void clear() {
		throw new RuntimeException("clear is called");
		
	}

	public boolean add(T e) {
		throw new RuntimeException("add is called");
	}

	public synchronized boolean offer(T e) {
		queue.offer(e);
		notifyAll();
		return true;
	}

	public T remove() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized T poll() {
		while (queue.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				//DO NOTHING
			}
		}
		return queue.poll();
	}

	public T element() {
		throw new RuntimeException("element is called");
	}

	public synchronized T peek() {
		while (queue.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				//DO NOTHING
			}
		}
		return queue.peek();
	}

}
