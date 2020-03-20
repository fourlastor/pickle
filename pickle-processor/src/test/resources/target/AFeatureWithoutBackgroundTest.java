package target;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.OtherSteps;
import steps.Steps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithoutBackgroundTest {

    private final Steps steps_Steps = new Steps();
    private final OtherSteps steps_OtherSteps = new OtherSteps();

    @Test
    public void scenarioWithOneStepAlsoNonAlphanumericChars_1() throws Throwable {
        steps_Steps.aStepWithAsParameter("1");
    }

    @Test
    public void scenarioWithTwoSteps() throws Throwable {
        steps_Steps.aStepWithAsParameter("2");
        steps_Steps.aStepWithoutParameters();
    }

    @Test
    public void scenarioWithStepsFrom2DefinitionFiles() throws Throwable {
        steps_Steps.aStepWithoutParameters();
        steps_OtherSteps.aStepFromAnotherDefinitionFile();
    }

    @Test
    public void scenarioWithExamples0() throws Throwable {
        steps_Steps.aStepWithAsParameter("1");
    }

    @Test
    public void scenarioWithExamples1() throws Throwable {
        steps_Steps.aStepWithAsParameter("a");
    }
}
