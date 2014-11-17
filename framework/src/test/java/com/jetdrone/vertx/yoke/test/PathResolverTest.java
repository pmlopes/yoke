package com.jetdrone.vertx.yoke.test;

import org.junit.Test;
import org.vertx.java.core.file.impl.PathAdjuster;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.testtools.TestVerticle;

import java.io.File;
import java.io.IOException;

import static org.vertx.testtools.VertxAssert.*;

public class PathResolverTest extends TestVerticle {

    @Test
    public void testEscape() throws IOException {

        String base = PathAdjuster.adjust((VertxInternal) vertx, ".");

        File file1 = new File(base, "static/dir1/file.1");
        file1.getParentFile().mkdirs();
        file1.createNewFile();

        File file2 = new File(base, "static/dir1/new file.1");
        file2.getParentFile().mkdirs();
        file2.createNewFile();

        System.out.println(PathAdjuster.adjust((VertxInternal) vertx, "static/dir1/file.1"));
        // /home/paulo/Projects/yoke/framework/build/resources/test/static/dir1/file.1

        System.out.println(PathAdjuster.adjust((VertxInternal) vertx, "static/dir1/new file.1"));
        // /home/paulo/Projects/yoke/framework/build/resources/test/static/dir1/new%20file.1

        File file = new File(PathAdjuster.adjust((VertxInternal) vertx, "static/dir1/file.1"));
        assertTrue(file.exists());

        file = new File(PathAdjuster.adjust((VertxInternal) vertx, "static/dir1/new file.1"));
        assertTrue(file.exists());

        testComplete();
    }
}
