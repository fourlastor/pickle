package steps;

import cucumber.api.java.en.When;

public class Steps {

    @When("^A step with (\\w+) as parameter$")
    public void aStepWithAsParameter(String parameter) {

    }

    @When("^A step without parameters$")
    public void aStepWithoutParameters() {

    }
}
