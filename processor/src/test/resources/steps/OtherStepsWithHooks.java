package steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

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
