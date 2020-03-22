package targetDeprecated;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.DeprecatedSteps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithoutBackgroundTest {

    private final DeprecatedSteps steps_DeprecatedSteps = new DeprecatedSteps();

    @Test
    public void scenarioWithOneStepAlsoNonAlphanumericChars_1() throws Throwable {
        steps_DeprecatedSteps.aStepWithAsParameter("1");
    }

    @Test
    public void scenarioWithTwoSteps() throws Throwable {
        steps_DeprecatedSteps.aStepWithAsParameter("2");
        steps_DeprecatedSteps.aStepWithoutParameters();
    }

    @Test
    public void scenarioWithStepsFrom2DefinitionFiles() throws Throwable {
        steps_DeprecatedSteps.aStepWithoutParameters();
        steps_DeprecatedSteps.aStepFromAnotherDefinitionFile();
    }

    @Test
    public void scenarioWithExamples0() throws Throwable {
        steps_DeprecatedSteps.aStepWithAsParameter("1");
    }

    @Test
    public void scenarioWithExamples1() throws Throwable {
        steps_DeprecatedSteps.aStepWithAsParameter("a");
    }
}
