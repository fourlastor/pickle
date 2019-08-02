package targetForNonStrictMode;

import android.support.test.runner.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.OtherSteps;
import steps.Steps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithBackgroundTest {

    private final Steps steps_Steps = new Steps();
    private final OtherSteps steps_OtherSteps = new OtherSteps();

    @Test
    @Ignore("Missing steps for scenario \"Scenario with one step and background\"")
    public void scenarioWithOneStepAndBackground() throws Throwable {
    }
}
