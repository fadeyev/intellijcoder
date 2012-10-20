package intellijcoder.arena;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.TestCase;

import java.util.HashMap;

import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * @author Konstantin Fadeyev
 *         22.01.11
 */
public class ProblemComponentModelMaker {
    public static final Property<ProblemComponentModel,String> className = newProperty();
    public static final Property<ProblemComponentModel,DataType> returnType = newProperty();
    public static final Property<ProblemComponentModel,String> methodName = newProperty();
    public static final Property<ProblemComponentModel,DataType[]> paramTypes = newProperty();
    public static final Property<ProblemComponentModel,String[]> paramNames = newProperty();
    public static final Property<ProblemComponentModel,TestCase> testCase = newProperty();


    public static final Instantiator<ProblemComponentModel> ProblemComponentModel = new Instantiator<ProblemComponentModel>() {
        public ProblemComponentModel instantiate(final PropertyLookup<ProblemComponentModel> lookup) {
            return new ProblemComponentModelStub() {
                @Override
                public String getClassName() {
                    return lookup.valueOf(className, "BinaryCode");
                }

                @Override
                public DataType getReturnType() {
                    return lookup.valueOf(returnType, dataType("int"));
                }

                @Override
                public String getMethodName() {
                    return lookup.valueOf(methodName, "decode");
                }

                @Override
                public DataType[] getParamTypes() {
                    return lookup.valueOf(paramTypes, new DataType[0]);
                }

                @Override
                public String[] getParamNames() {
                    return lookup.valueOf(paramNames, new String[0]);
                }

                @Override
                public TestCase[] getTestCases() {
                    String[] testInput = {};
                    String testOutput = "1";
                    return new TestCase[] {lookup.valueOf(testCase, testCase(testInput, testOutput))};
                }
            };
        }
    };

    public static DataType dataType(String descriptor) {
        HashMap<Integer, String> descriptorsMap = new HashMap<Integer, String>();
        descriptorsMap.put(JavaLanguage.JAVA_LANGUAGE.getId(), descriptor);
        return new DataType(1, descriptor, descriptorsMap);
    }

    public static TestCase testCase(String[] testInput, String testOutput) {
        return new TestCase(1, testInput, testOutput, false);
    }
}
