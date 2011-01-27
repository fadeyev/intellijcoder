package intellijcoder.model;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;

import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * Replacement for TestDataBuilders for value objects powered by Nat Pryce's nice library MakeItEasy
 *
 * @author Konstantin Fadeyev
 *         14.01.11
 */
public class ProblemMaker {
    public static final Property<Problem,String> className = newProperty();
    public static final Property<Problem,String> returnType = newProperty();
    public static final Property<Problem,String> methodName = newProperty();
    public static final Property<Problem,String[]> paramTypes = newProperty();
    public static final Property<Problem,String[]> paramNames = newProperty();
    public static final Property<Problem,TestCase[]> testCases = newProperty();

    public static final Property<TestCase,String[]> input = newProperty();
    public static final Property<TestCase,String> output = newProperty();


    public static final Instantiator<Problem> Problem = new Instantiator<Problem>() {
        public Problem instantiate(PropertyLookup<Problem> lookup) {
            return new Problem(
                    lookup.valueOf(className, "BinaryCode"),
                    lookup.valueOf(returnType, "String[]"),
                    lookup.valueOf(methodName, "decode"),
                    lookup.valueOf(paramTypes, new String[0]),
                    lookup.valueOf(paramNames, new String[0]),
                    lookup.valueOf(testCases, new TestCase[0]));
        }
    };

    public static final Instantiator<TestCase> TestCase = new Instantiator<TestCase>() {
        public TestCase instantiate(PropertyLookup<TestCase> lookup) {
            return new TestCase(
                    lookup.valueOf(input, new String[] {"11111"}),
                    lookup.valueOf(output, "00000"));
        }
    };
}
