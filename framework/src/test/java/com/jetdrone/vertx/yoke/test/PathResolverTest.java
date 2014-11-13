package com.jetdrone.vertx.yoke.test;

import org.junit.Ignore;
import org.junit.Test;
import org.vertx.java.core.file.impl.PathAdjuster;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.testtools.TestVerticle;

import java.io.File;

import static org.vertx.testtools.VertxAssert.*;

public class PathResolverTest extends TestVerticle {

    @Test
    @Ignore
    public void testEscape() {

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
