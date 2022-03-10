Feature: Est-ce qu'une fonctionnalité de mon application fonctionne ?
  Je détaille dans "Feature" ce que je veux faire en principe
  Je veux savoir si cette fonctionnalité spécifique fonctionne

  Scenario Outline: Je décide de simuler un cas d'utilisation de cette fonctionnalité spécifique, qui consiste à ajouter 1 à un nombre
    Given I have a number, let's say <myFirstNumber>
    When I try to increment that number
    Then I see if the function did increment my number, it should answer <myExpectedNumber>

    Examples:
    |myFirstNumber |myExpectedNumber |
    |91            | 92              |
    |48            | 49              |
    |-1            | 0               |
    