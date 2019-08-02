package targetWithHooks;

import android.support.test.runner.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.OtherStepsWithHooks;
import steps.Steps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithBackgroundTest {
    private final Steps steps_Steps = new Steps();

    private final OtherStepsWithHooks steps_OtherStepsWithHooks = new OtherStepsWithHooks();

    @Before
    public void setUp() throws Throwable {
        steps_OtherStepsWithHooks.beforeHook();
    }

    @After
    public void tearDown() throws Throwable {
        steps_OtherStepsWithHooks.afterHook();
    }

    @Test
    public void scenarioWithOneStepAndBackground() throws Throwable {
        steps_Steps.aStepWithoutParameters();
        steps_OtherStepsWithHooks.aStepFromAnotherDefinitionFile();
    }
}
