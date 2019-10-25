package steps;

import cucumber.api.java.en.When;

public class OtherStepsWithAmbiguousStep {

    @When("^A step with (\\w?) as parameter$")
    public void aStepWithAsParameter(String parameter) {

    }
}
