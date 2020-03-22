package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class OtherStepsWithHooks {

    @Before
    public void beforeHook() {

    }

    @After
    public void afterHook() {

    }

    @When("^A step from another definition file$")
    public void aStepFromAnotherDefinitionFile() {

    }
}
