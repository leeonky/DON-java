Feature: support json value: boolean string null number

  Scenario Outline: true / false
    Given the following code:
    """
    <code>
    """
    Then got the following data:
    """
    = <value> and class.simpleName: 'Boolean'
    """
    Examples:
      | code  | value |
      | true  | true  |
      | false | false |

  Scenario: null
    Given the following code:
    """
    null
    """
    Then got the following data:
    """
    = null
    """
