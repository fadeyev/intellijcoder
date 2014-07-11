package intellijcoder.arena;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.topcoder.client.contestant.ProblemComponentModel;
import com.topcoder.client.contestant.ProblemModel;
import com.topcoder.client.contestant.RoomModel;
import com.topcoder.client.contestant.RoundModel;
import com.topcoder.client.contestant.view.LeaderListener;
import com.topcoder.client.contestant.view.PhaseListener;
import com.topcoder.client.contestant.view.RoomListListener;
import com.topcoder.client.contestant.view.RoundProblemsListener;
import com.topcoder.netCommon.contest.round.RoundProperties;
import com.topcoder.netCommon.contest.round.RoundType;
import com.topcoder.netCommon.contestantMessages.response.data.LeaderboardItem;
import com.topcoder.netCommon.contestantMessages.response.data.PhaseData;
import com.topcoder.shared.language.JavaLanguage;
import com.topcoder.shared.problem.DataType;
import com.topcoder.shared.problem.Problem;
import com.topcoder.shared.problem.ProblemComponent;
import com.topcoder.shared.problem.TestCase;

import java.util.HashMap;

import static com.natpryce.makeiteasy.Property.newProperty;

/**
 * @author Konstantin Fadeyev
 *         22.01.11
 */
public class ProblemComponentModelMaker {
    public static final Property<ProblemComponentModel,String> contestName = newProperty();
    public static final Property<ProblemComponentModel,String> className = newProperty();
    public static final Property<ProblemComponentModel,DataType> returnType = newProperty();
    public static final Property<ProblemComponentModel,String> methodName = newProperty();
    public static final Property<ProblemComponentModel,DataType[]> paramTypes = newProperty();
    public static final Property<ProblemComponentModel,String[]> paramNames = newProperty();
    public static final Property<ProblemComponentModel,TestCase> testCase = newProperty();
    public static final Property<ProblemComponentModel,Integer> memLimit = newProperty();
    public static final Property<ProblemComponentModel,Integer> timeLimit = newProperty();


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

                @Override
                public ProblemModel getProblem() {
                    return new ProblemModel() {

                        public Long getProblemID() {
                            return null;
                        }

                        public RoundModel getRound() {
                            return new RoundModel() {
                                public int getRoundCategoryID() {
                                    return 0;
                                }

                                public Long getRoundID() {
                                    return null;
                                }

                                public String getContestName() {
                                    return lookup.valueOf(contestName, "SRM 144 DIV 1");
                                }

                                public String getRoundName() {
                                    return null;
                                }

                                public String getDisplayName() {
                                    return null;
                                }

                                public String getSingleName() {
                                    return null;
                                }

                                public Integer getRoundTypeId() {
                                    return null;
                                }

                                public RoundType getRoundType() {
                                    return null;
                                }

                                public RoundProperties getRoundProperties() {
                                    return null;
                                }

                                public Integer getPhase() {
                                    return null;
                                }

                                public boolean getMenuStatus() {
                                    return false;
                                }

                                public int getSecondsLeftInPhase() {
                                    return 0;
                                }

                                public boolean isInChallengePhase() {
                                    return false;
                                }

                                public void addPhaseListener(PhaseListener phaseListener) {

                                }

                                public void removePhaseListener(PhaseListener phaseListener) {

                                }

                                public boolean containsPhaseListener(PhaseListener phaseListener) {
                                    return false;
                                }

                                public void addRoomListListener(RoomListListener roomListListener) {

                                }

                                public void removeRoomListListener(RoomListListener roomListListener) {

                                }

                                public void addRoundProblemsListener(RoundProblemsListener roundProblemsListener) {

                                }

                                public void removeRoundProblemsListener(RoundProblemsListener roundProblemsListener) {

                                }

                                public void addLeaderListener(LeaderListener leaderListener) {

                                }

                                public void removeLeaderListener(LeaderListener leaderListener) {

                                }

                                public boolean hasAdminRoom() {
                                    return false;
                                }

                                public RoomModel getAdminRoom() {
                                    return null;
                                }

                                public boolean hasCoderRooms() {
                                    return false;
                                }

                                public RoomModel[] getCoderRooms() {
                                    return new RoomModel[0];
                                }

                                public boolean hasProblems(Integer integer) {
                                    return false;
                                }

                                public ProblemModel[] getProblems(Integer integer) {
                                    return new ProblemModel[0];
                                }

                                public ProblemComponentModel[] getAssignedComponents(Integer integer) {
                                    return new ProblemComponentModel[0];
                                }

                                public ProblemComponentModel getAssignedComponent(Integer integer, Long aLong) {
                                    return null;
                                }

                                public ProblemModel getProblem(Integer integer, Long aLong) {
                                    return null;
                                }

                                public ProblemComponentModel getComponent(Integer integer, Long aLong) {
                                    return null;
                                }

                                public boolean hasLeaderboard() {
                                    return false;
                                }

                                public LeaderboardItem[] getLeaderboard() {
                                    return new LeaderboardItem[0];
                                }

                                public boolean hasSchedule() {
                                    return false;
                                }

                                public PhaseData[] getSchedule() {
                                    return new PhaseData[0];
                                }

                                public boolean isRoomLeader(String s) {
                                    return false;
                                }

                                public RoomModel getRoomByCoder(String s) {
                                    return null;
                                }

                                public boolean canDisplaySummary() {
                                    return false;
                                }
                            };
                        }

                        public Integer getDivision() {
                            return null;
                        }

                        public Integer getProblemType() {
                            return null;
                        }

                        public String getName() {
                            return null;
                        }

                        public boolean hasComponents() {
                            return false;
                        }

                        public ProblemComponentModel[] getComponents() {
                            return new ProblemComponentModel[0];
                        }

                        public ProblemComponentModel getPrimaryComponent() {
                            return null;
                        }

                        public boolean hasIntro() {
                            return false;
                        }

                        public String getIntro() {
                            return null;
                        }

                        public boolean hasProblemStatement() {
                            return false;
                        }

                        public String getProblemStatement() {
                            return null;
                        }

                        public Problem getProblem() {
                            return null;
                        }

                        public void addListener(Listener listener) {

                        }

                        public void removeListener(Listener listener) {

                        }
                    };
                }

                @Override
                public com.topcoder.shared.problem.ProblemComponent getComponent() {
                    return new ProblemComponent() {
                        @Override
                        public int getExecutionTimeLimit() {
                            return lookup.valueOf(timeLimit, 2000);
                        }

                        @Override
                        public int getMemLimitMB() {
                            return lookup.valueOf(memLimit, 256);
                        }
                    };
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
