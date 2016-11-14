package xyz.jetdrone.yoke.handler;

import io.vertx.core.Handler;
import org.junit.Test;
import xyz.jetdrone.yoke.Context;
import xyz.jetdrone.yoke.impl.tree.Tree;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TreeTest {

  @Test
  public void testRoot() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/dir1/*filepath1", handler)
      .add("/dir2/*filepath2", handler)
      .add("/*filepath3", handler);

    TreePrinter.print(tree);

    testRoute(tree, "/", true, params("filepath3", "/"));
  }

  @Test
  public void testTrailingSlashes1() {
    Tree<Handler<Context>> tree = new Tree<>();

    tree.add("/search/", ctx -> {});

    testRoute(tree, "/search", true, null);
  }

  @Test
  public void testTrailingSlashes2() {
    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/search", ctx -> {});
    testRoute(tree, "/search/", true, null);
  }

  @Test
  public void testSet1() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    testRoute(tree, "/", true, null);
    testRoute(tree, "/cmd/test/", true, params("tool", "test"));
    testRoute(tree, "/cmd/test", true, params("tool", "test"));
    testRoute(tree, "/cmd/test/3", true, params("tool", "test", "sub", "3"));
    testRoute(tree, "/src/", true, params("filepath", "/"));
    testRoute(tree, "/src/some/file.png", true, params("filepath", "/some/file.png"));
    testRoute(tree, "/search/", true, null);
    testRoute(tree, "/search/someth!ng+in+ünìcodé", true, params("query", "someth!ng+in+ünìcodé"));
    testRoute(tree, "/search/someth!ng+in+ünìcodé/", true, params("query", "someth!ng+in+ünìcodé"));
    testRoute(tree, "/user_gopher", true, params("name", "gopher"));
    testRoute(tree, "/user_gopher/about", true, params("name", "gopher"));
    testRoute(tree, "/files/js/inc/framework.js", true, params("dir", "js", "filepath", "/inc/framework.js"));
    testRoute(tree, "/info/gordon/public", true, params("user", "gordon"));
    testRoute(tree, "/info/gordon/project/java", true, params("user", "gordon", "project", "java"));
  }

  @Test
  public void testCase1() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    // whats so special about this??
    testRoute(tree, "/src/", true, params("filepath", "/"));
  }

  @Test
  public void testCase2() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    // whats so special about this??
    testRoute(tree, "/cmd/test", true, params("tool", "test"));
  }

  @Test
  public void testCase3() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    // whats so special about this??
    testRoute(tree, "/cmd/test/3", true, params("tool", "test", "sub", "3"));
  }

  @Test
  public void testCase4() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();


    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    // whats so special about this??
    testRoute(tree, "/search/someth!ng+in+ünìcodé/", true, params("query", "someth!ng+in+ünìcodé"));
  }

  @Test
  public void testCase5() {
    Handler<Context> handler = Context::end;

    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/", handler)
      .add("/cmd/:tool/:sub", handler)
      .add("/cmd/:tool/", handler)
      .add("/src/*filepath", handler)
      .add("/search/", handler)
      .add("/search/:query", handler)
      .add("/user_:name", handler)
      .add("/user_:name/about", handler)
      .add("/files/:dir/*filepath", handler)
      .add("/doc/", handler)
      .add("/doc/go_faq.html", handler)
      .add("/doc/go1.html", handler)
      .add("/info/:user/public", handler)
      .add("/info/:user/project/:project", handler);

    TreePrinter.print(tree);

    // whats so special about this??
    testRoute(tree, "/", true, null);
  }

  @Test
  public void testPriority() {
    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/dir/*filepath", ctx -> {})
      .add("/*filepath", ctx -> {});

    testRoute(tree, "/dir/file1", true, params("filepath", "/file1"));
    testRoute(tree, "/file1", true, params("filepath", "/file1"));

  }

  @Test
  public void testPriority1() {
    AtomicInteger integer = new AtomicInteger();
    Tree<Handler<Context>> tree = new Tree<>();

    tree
      .add("/dir1/*filepath1", ctx -> integer.set(0))
      .add("/dir2/*filepath2", ctx -> integer.set(1))
      .add("/*filepath3", ctx -> integer.set(2));

    TreePrinter.print(tree);

    final Map<String, String> ctx = new HashMap<>();
    Handler<Context> route = tree.find("/", ctx);

    route.handle(null);
    assertEquals(2, integer.intValue());
  }

  private void testRoute(Tree<Handler<Context>> tree, String path, boolean handler, Map<String, String> params) {
    final Map<String, String> ctx = new HashMap<>();
    Handler<Context> route = tree.find(path, ctx);

    if (handler) {
      assertNotNull(route);
    } else {
      assertNull(route);
    }
    if (params != null) {
      assertEquals(params.size(), ctx.size());
      ctx.forEach((k, v) -> assertEquals(params.get(k), v));
    } else {
      assertEquals(0, ctx.size());
    }
  }

  private Map<String, String> params(String... strings) {
    Map<String, String> map = new HashMap<>();
    for (int i = 0, l = strings.length; i < l; i = i + 2) {
      map.put(strings[i], strings[i + 1]);
    }
    return map;
  }
}
