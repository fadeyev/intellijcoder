package intellijcoder.arena;

import intellijcoder.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArenaAppletInfo {
    private Set<String> classPathItems = new HashSet<String>();
    private String mainClass;
    private List<String> arguments = new ArrayList<String>();

    public void addClassPathItem(String classPathItem) {
        classPathItems.add(classPathItem);
    }

    public String getClassPath() {
        return StringUtil.join(classPathItems, System.getProperty("path.separator"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArenaAppletInfo that = (ArenaAppletInfo) o;
        if (classPathItems != null ? !classPathItems.equals(that.classPathItems) : that.classPathItems != null)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ArenaAppletInfo{" +
                "classPathItems=" + classPathItems +
                ", mainClass='" + mainClass + '\'' +
                ", arguments=" + arguments +
                '}';
    }

    @Override
    public int hashCode() {
        return classPathItems != null ? classPathItems.hashCode() : 0;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void addArgument(String argument) {
        arguments.add(argument);
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void addArguments(List<String> arguments) {
        this.arguments.addAll(arguments);
    }


    public Set<String> getClassPathItems() {
        return classPathItems;
    }
}
