package targetForNonStrictMode;

import android.support.test.runner.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.Steps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithoutBackgroundTest {

    private final Steps steps_Steps = new Steps();

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
    public void scenarioWithExamples0() throws Throwable {
        steps_Steps.aStepWithAsParameter("1");
    }

    @Test
    public void scenarioWithExamples1() throws Throwable {
        steps_Steps.aStepWithAsParameter("a");
    }
}
