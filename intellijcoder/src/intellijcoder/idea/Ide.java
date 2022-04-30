package intellijcoder.idea;

import intellijcoder.main.IntelliJCoderException;

/**
 * Interface to IDE facilities requiered by IntelliJCoder
 *
 * @author Konstantin Fadeyev
 *         15.01.11
 */
public interface Ide {
    void createModule(String moduleName, String className, String classSource, String testSource, String htmlSource, int memLimit) throws IntelliJCoderException;

    String getClassSource(String className);
}
