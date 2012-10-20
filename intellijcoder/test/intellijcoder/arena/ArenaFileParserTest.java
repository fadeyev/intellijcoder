package intellijcoder.arena;


import intellijcoder.main.IntelliJCoderException;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArenaFileParserTest {
    @Test
    public void basicParsing() throws IntelliJCoderException {
        InputStream file = getClass().getClassLoader().getResourceAsStream("ContestAppletProd.jnlp");
        ArenaFileParser parser = new ArenaFileParser();

        ArenaAppletInfo appletInfo = parser.parse(file);

        assertThat("jars", appletInfo.getClassPath(),
                equalTo("http://www.topcoder.com/contest/classes/7.0/arena-client-7.0.0.jar;http://www.topcoder.com/contest/classes/7.0/basic_type_serialization-1.0.1.jar"));
        assertThat("main class", appletInfo.getMainClass(), equalTo("com.topcoder.client.contestApplet.runner.generic"));
        assertThat("arguments", appletInfo.getArguments(), contains("www.topcoder.com", "5001"));
    }
}
