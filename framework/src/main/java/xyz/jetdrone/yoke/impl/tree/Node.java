package xyz.jetdrone.yoke.impl.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Node<T> {
  private List<Node<T>> children = new ArrayList<>();

  private String key = "";
  private T value;
  private int priority = 0;

  public T value() {
    return value;
  }

  public void value(T handler) {
    this.value = handler;
  }

  public String key() {
    return key;
  }

  public void key(String value) {
    this.key = value;
    this.priority = priority(key);
  }

  public List<Node<T>> children() {
    return children;
  }

  public void children(List<Node<T>> children) {
    this.children = children;
  }

  public int priority() {
    return priority;
  }

  private int priority(String key) {
    int matchingChars = 0;
    int weight = 1;
    while (matchingChars < key.length()) {
      if (key.charAt(matchingChars) == '*') {
        return 0;
      }
      if (key.charAt(matchingChars) == ':') {
        return 1;
      }
      matchingChars++;
      weight++;
    }
    return weight;
  }

  public void sort() {
    Collections.sort(children, (e1, e2) -> Integer.compare(e2.priority(), e1.priority()));
  }
}
