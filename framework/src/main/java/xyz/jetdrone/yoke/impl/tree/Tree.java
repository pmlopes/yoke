package xyz.jetdrone.yoke.impl.tree;

import java.util.ArrayList;
import java.util.Map;

public final class Tree<T> {

  private final Node<T> root = new Node<>();

  public Node<T> root() {
    return root;
  }

  public Tree<T> add(String path, T v) {
    add(path, root(), v);
    return this;
  }

  public T find(String path, Map<String, String> map) {
    return find(path, map, root());
  }

  private T find(String path, Map<String, String> ctx, Node<T> node) {
    int keyIndex = 0;
    int pathIndex = 0;
    int keyLength = node.key().length();
    int pathLength = path.length();

    // ----------------
    // iterate chars

    while (isNext(node.key(), path, keyIndex, keyLength, pathIndex, pathLength)) {

      // --------------------------
      // check wildcard parameter

      if (node.key().charAt(keyIndex) == '*') {

        // ----------------------------
        // extract wildcard parameter

        String name = node.key().substring(keyIndex, keyLength);
        String victor = path.substring(pathIndex, pathLength);

        if (victor.isEmpty()) {
          // :>
          victor = "/";
        } else {
          if (victor.charAt(0) != '/') {
            victor = "/" + victor;
          }
        }

        // ------------------------------------------------
        // set parameter victor, remove * in param name

        ctx.put(name.substring(1), victor);

        // ------------------------------------
        // wildcard is always an endpoint
        return node.value();
      }

      // -----------------------
      // check named parameter

      if (node.key().charAt(keyIndex) == ':') {

        // -------------------------
        // extract named parameter

        int eofParamInPrefix = findEndOfParameter(path, pathIndex);
        int eofParamInKey = findEndOfParameter(node.key(), keyIndex);

        String name = node.key().substring(keyIndex, eofParamInKey);
        String value = path.substring(pathIndex, eofParamInPrefix);

        // ------------------------------------------------
        // set parameter value, remove : in param name

        ctx.put(name.substring(1), value);

        // --------------------------------------------------
        // set indexes to first char after eof parameter

        pathIndex = eofParamInPrefix;
        keyIndex = eofParamInKey;

      } else {
        // -------------
        // isNext char

        keyIndex += 1;
        pathIndex += 1;
      }
    }

    // ------------------------------
    // trailing slash

    boolean isTrailingSlash = false;
    if (!node.key().isEmpty()
      && pathLength - pathIndex == 1
      && path.charAt(pathLength - 1) == '/') {
      isTrailingSlash = true;
    }

    // ------------
    //  check end

    if (pathLength == 1 && keyLength == 1 && path.charAt(0) == '/' && node.value() == null) {

      // ---------------------------
      // find child node..

      return findChildNode(node, "/", ctx);

    } else if (pathLength == pathIndex || isTrailingSlash) {

      // -----
      // end
      return node.value();

    } else {

      // ---------------------------
      // find matching child node..

      String newPath = path.substring(pathIndex, pathLength);
      return findChildNode(node, newPath, ctx);
    }
  }

  private T findChildNode(Node<T> node, String path, Map<String, String> ctx) {
    for (Node<T> child : node.children()) {
      if (child.key().startsWith(path.charAt(0) + "")
        || child.key().startsWith(":")
        || child.key().startsWith("*")) {

        // ---------------
        // found node..
        return find(path, ctx, child);
      }
    }

    return null;
  }

  private boolean isNext(String key, String path, int keyIndex, int keyLength, int pathIndex, int pathLength) {
    return keyIndex < keyLength
      && (key.charAt(keyIndex) == ':'
      || key.charAt(keyIndex) == '*'
      || pathLength > pathIndex
      && path.charAt(pathIndex) == key.charAt(keyIndex));
  }

  private void add(String path, Node<T> node, T v) {
    int matchingChars = 0;

    while (matchingChars < path.length()
      && matchingChars < node.key().length()) {
      if (path.charAt(matchingChars) != node.key().charAt(matchingChars)) {
        break;
      }
      matchingChars++;
    }
    if (node.key().equals("")
      || matchingChars == 0
      || (matchingChars < path.length()
      && matchingChars >= node.key().length())) {

      boolean flag = false;
      String newText = path.substring(matchingChars, path.length());
      for (Node<T> child : node.children()) {
        if (child.key().startsWith(newText.charAt(0) + "")) {
          flag = true;
          add(newText, child, v);
          break;
        }
      }

      if (!flag) {
        Node<T> n = new Node<>();
        n.key(newText);
        n.value(v);
        node.children().add(n);
        node.sort();
      }
    } else if (matchingChars == path.length() && matchingChars == node.key().length()) {
      throw new IllegalStateException("Duplicate trail: '" + path + "'");
    } else if (matchingChars > 0 && matchingChars < node.key().length()) {
      Node<T> n1 = new Node<>();
      n1.key(node.key().substring(matchingChars, node.key().length()));
      n1.value(node.value());
      n1.children(node.children());
      node.value(null);
      node.key(path.substring(0, matchingChars));
      node.children(new ArrayList<>());
      node.children().add(n1);
      node.sort();

      if (matchingChars < path.length()) {
        Node<T> n2 = new Node<>();
        n2.key(path.substring(matchingChars, path.length()));
        n2.value(v);
        node.value(null);
        node.children().add(n2);
        node.sort();
      } else {
        node.value(v);
      }
    } else {
      Node<T> n = new Node<>();
      n.key(node.key().substring(matchingChars, node.key().length()));
      n.value(node.value());
      n.children(node.children());
      node.value(null);
      node.key(path);
      node.children().add(n);
      node.sort();
    }
  }

  private static int findEndOfParameter(String path, int start) {
    int index;
    for (index = start; index < path.length(); index++) {
      if (path.charAt(index) == '/') {
        return index;
      }
    }
    return index;
  }
}
