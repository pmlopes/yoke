package xyz.jetdrone.yoke.impl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ConcatList<T> implements List<T> {

  private final List<T> a;
  private final List<T> b;

  public ConcatList(List<T> a, List<T> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public int size() {
    return a.size() + b.size();
  }

  @Override
  public boolean isEmpty() {
    return a.isEmpty() && b.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return a.contains(o) || b.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T get(int index) {
    if (index < a.size()) {
      return a.get(index);
    } else {
      return b.get(index);
    }
  }

  @Override
  public T set(int index, T element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, T element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int lastIndexOf(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<T> listIterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }
}
