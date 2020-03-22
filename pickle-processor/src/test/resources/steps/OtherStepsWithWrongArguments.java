package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class OtherStepsWithWrongArguments {

    @When("^A step from another definition file$")
    public void aStepFromAnotherDefinitionFile(String argument) {

    }
}
