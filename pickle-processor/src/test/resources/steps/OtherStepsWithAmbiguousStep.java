package steps;

import io.cucumber.java.en.When;

public class OtherStepsWithAmbiguousStep {

    @When("^A step with (\\w?) as parameter$")
    public void aStepWithAsParameter(String parameter) {

    }
}
