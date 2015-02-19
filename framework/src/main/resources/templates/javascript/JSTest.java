import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.testtools.ScriptClassRunner;
import org.vertx.testtools.TestVerticleInfo;

/**
 * This is dummy JUnit test class which is used to run any JavaScript test scripts as JUnit tests.
 *
 * The scripts by default go in src/test/resources/integration_tests
 *
 * If you don't have any JavaScript tests in your project you can delete this
 *
 * You do not need to edit this file unless you want it to look for tests elsewhere
 */
@TestVerticleInfo(filenameFilter=".+\\.js", funcRegex="function[\\s]+(test[^\\s(]+)")
@RunWith(ScriptClassRunner.class)
public class JSTest {
    @Test
    public void __vertxDummy() {
    }
}
