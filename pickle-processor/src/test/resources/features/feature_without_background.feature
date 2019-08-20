Feature: A Feature without background!

  Scenario: Scenario with one step, also non       alphanumeric chars _ 1!
    Given A step with 1 as parameter

  Scenario: Scenario with two steps
    Given A step with 2 as parameter
    And A step without parameters

  Scenario: Scenario with steps from 2 definition files
    Given A step without parameters
    And A step from another definition file

  Scenario Outline: Scenario with examples
    Given A step with <parameter> as parameter

    Examples:
      | parameter |
      | 1         |
      | a         |
