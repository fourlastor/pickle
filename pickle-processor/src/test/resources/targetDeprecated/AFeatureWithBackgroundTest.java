package targetDeprecated;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.Throwable;
import org.junit.Test;
import org.junit.runner.RunWith;
import steps.DeprecatedSteps;

@RunWith(AndroidJUnit4.class)
public class AFeatureWithBackgroundTest {

    private final DeprecatedSteps steps_DeprecatedSteps = new DeprecatedSteps();

    @Test
    public void scenarioWithOneStepAndBackground() throws Throwable {
        steps_DeprecatedSteps.aStepWithoutParameters();
        steps_DeprecatedSteps.aStepFromAnotherDefinitionFile();
    }
}
