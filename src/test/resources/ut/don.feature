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

  Scenario Outline: numbers
    Given the following code:
    """
    <input>
    """
    Then got the following data:
    """
    = <result> and class.simpleName: '<type>'
    """
    Examples:
      | input | type    | result |
      | 1     | Integer | 1      |
      | 1.0   | Double  | 1.0    |
