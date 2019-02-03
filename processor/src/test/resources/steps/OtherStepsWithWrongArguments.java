package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class OtherStepsWithWrongArguments {

    @When("^A step from another definition file$")
    public void aStepFromAnotherDefinitionFile(String argument) {

    }
}
