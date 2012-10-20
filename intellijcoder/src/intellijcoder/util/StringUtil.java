package intellijcoder.util;

import java.util.Collection;
import java.util.Iterator;


public class StringUtil {

    public static final char URL_PATH_SEPARATOR = '/';

    public static String join(Collection<String> s, String delimiter) {
        if (s == null || s.isEmpty()) return "";
        Iterator<String> iter = s.iterator();
        StringBuilder builder = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            builder.append(delimiter).append(iter.next());
        }
        return builder.toString();
    }

    public static String getFileName(String url) {
        return url.substring( url.lastIndexOf(URL_PATH_SEPARATOR)+1, url.length() );
    }
}
