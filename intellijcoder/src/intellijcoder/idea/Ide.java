package intellijcoder.idea;

/**
 * Interface to IDE facilities requiered by IntelliJCoder
 *
 * @author Konstantin Fadeyev
 *         15.01.11
 */
public interface Ide {
    void createModule(String moduleName, String classSource, String testSource);

    String getClassSource(String className);
}
