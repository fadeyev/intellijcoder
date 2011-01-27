package intellijcoder.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Date: 14.01.11
 *
 * @author Konstantin Fadeyev
 */
public class TestCase implements Serializable {
    private String[] input;
    private String output;

    public TestCase(String[] input, String output) {
        this.input = input;
        this.output = output;
    }

    public String[] getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestCase that = (TestCase) o;

        if (!Arrays.equals(input, that.input)) return false;
        //noinspection RedundantIfStatement
        if (output != null ? !output.equals(that.output) : that.output != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = input != null ? Arrays.hashCode(input) : 0;
        result = 31 * result + (output != null ? output.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "input=" + (input == null ? null : Arrays.asList(input)) +
                ", output='" + output + '\'' +
                '}';
    }
}
