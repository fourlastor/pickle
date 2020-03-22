package steps;

import cucumber.api.java.en.When;

public class DeprecatedSteps {

    @When("^A step with (\\w+) as parameter$")
    public void aStepWithAsParameter(String parameter) {

    }

    @When("^A step without parameters$")
    public void aStepWithoutParameters() {

    }

    @When("^A step from another definition file$")
    public void aStepFromAnotherDefinitionFile() {

    }
}
