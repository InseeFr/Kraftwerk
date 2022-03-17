Feature: Est-ce qu'une fonctionnalit� de mon application fonctionne ?
  Je d�taille dans "Feature" ce que je veux faire en principe
  Je veux savoir si cette fonctionnalit� sp�cifique fonctionne

  Scenario Outline: Je d�cide de simuler un cas d'utilisation de cette fonctionnalit� sp�cifique, qui consiste � ajouter 1 � un nombre
    Given I have a number, let's say <myFirstNumber>
    When I try to increment that number
    Then I see if the function did increment my number, it should answer <myExpectedNumber>

    Examples:
    |myFirstNumber |myExpectedNumber |
    |91            | 92              |
    |48            | 49              |
    |-1            | 0               |
    