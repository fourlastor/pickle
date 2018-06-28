package targetWithHooks;

import android.support.test.runner.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.OtherStepsWithHooks;
import steps.Steps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithBackgroundTest {

    private final OtherStepsWithHooks steps_OtherStepsWithHooks = new OtherStepsWithHooks();
    private final Steps steps_Steps = new Steps();

    @Test
    public void scenarioWithOneStepAndBackground() throws Throwable {
        steps_OtherStepsWithHooks.beforeHook();
        steps_Steps.aStepWithoutParameters();
        steps_OtherStepsWithHooks.aStepFromAnotherDefinitionFile();
        steps_OtherStepsWithHooks.afterHook();
    }
}
