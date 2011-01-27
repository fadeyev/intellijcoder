package intellijcoder.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;

/**
 * Date: 17.01.11
 *
 * @author Konstantin Fadeyev
 */
public class TestUtil {
    public static void assertExceptionMessage(Throwable e, String expectedMessageSubstring) {
        assertThat(e.getClass().getSimpleName() + " message", e.getMessage(), containsString(expectedMessageSubstring));
    }

    public static Matcher<String> hasTemplate(final String... parts) {
        return new BaseMatcher<String>() {
            public boolean matches(Object o) {
                String s = (String) o;
                StringBuilder regexp = new StringBuilder("(?s).*");
                for (int i = 0; i < parts.length; i++) {
                    if (i != 0) {
                        regexp.append(".*");
                    }
                    regexp.append(Pattern.quote(parts[i]));
                }
                regexp.append(".*");
                return s.matches(regexp.toString());
            }

            public void describeTo(Description description) {
                description.appendText("\"");
                for (String part : parts) {
                    description.appendText(" " + part);
                }
                description.appendText("\"");
            }
        };
    }

    public static <T> Matcher<T[]> hasItemsInArray(Matcher<T>... items) {
        Collection<Matcher<? extends T[]>> matchers = new ArrayList<Matcher<? extends T[]>>(items.length);
        for (Matcher<T> item : items) {
            matchers.add(hasItemInArray(item));
        }
        return Matchers.allOf(matchers);
    }
}
