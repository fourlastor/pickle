package targetForNonStrictModeAllStepsMissing;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@Ignore
public class AFeatureWithoutBackgroundTest {
    @Test
    @Ignore("Missing steps for scenario \"Scenario with one step, also non       alphanumeric chars _ 1!\"")
    public void scenarioWithOneStepAlsoNonAlphanumericChars_1() throws Throwable {
    }

    @Test
    @Ignore("Missing steps for scenario \"Scenario with two steps\"")
    public void scenarioWithTwoSteps() throws Throwable {
    }

    @Test
    @Ignore("Missing steps for scenario \"Scenario with steps from 2 definition files\"")
    public void scenarioWithStepsFrom2DefinitionFiles() throws Throwable {
    }

    @Test
    @Ignore("Missing steps for scenario \"Scenario with examples\"")
    public void scenarioWithExamples0() throws Throwable {
    }

    @Test
    @Ignore("Missing steps for scenario \"Scenario with examples\"")
    public void scenarioWithExamples1() throws Throwable {
    }
}
