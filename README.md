[![Coverage Status](https://coveralls.io/repos/github/InseeFr/Kraftwerk/badge.svg?branch=main)](https://coveralls.io/github/InseeFr/Kraftwerk?branch=main)

<img align="right" src="logo/kraftwerk-logo.png" alt="Kraftwerk logo"/>

# Kraftwerk

:gb:

Kraftwerk is a Java Spring application designed to validate and process data from multimode surveys, to generate data tables ready-to-use for statistical purposes.
It heavily relies on metadata described using the [DDI](http://ddialliance.org) standard.
The automated processes can be enriched with specification written in [VTL](https://sdmx.org/?page_id=5096), thanks to [Trevas](https://github.com/InseeFr/Trevas) implementation.

Developer documentation can be found in the [wiki](https://github.com/InseeFr/Kraftwerk/wiki).

User documentation and functional tests are still in an [inhouse project](https://gitlab.insee.fr/sic/service-agregation-echange-de-donnees/kraftwerk).

## Requirements

* JDK 21 +
* Maven 3.6 +

Kraftwerk uses [Lombok](https://projectlombok.org/).

## Launch

If no argument is specified in the `java -jar` command, Kraftwerk will launch
as a REST API.
Otherwise, it will launch on batch mode and apply treatments on one campaign
with the specified arguments.
The required arguments for batch mode are as follows (in order) :
1. Service to use (`MAIN`,`FILEBYFILE`,`GENESIS`,`LUNATIC_ONLY`)
2. Archive at end of execution (`false` or `true`)
3. Integrate all reporting datas (`false` or `true`)
4. Campaign name (or path to campaign folder)

:fr:

Kraftwerk est une application Java Spring conçue pour valider et traiter des données provenant d'enquêtes multimodes, afin de générer des tableaux de données prêts à être utilisés à des fins statistiques.
Elle s'appuie fortement sur les métadonnées décrites à l'aide de la norme [DDI](http://ddialliance.org).
Les processus automatisés peuvent être enrichis par des spécifications écrites en [VTL](https://sdmx.org/?page_id=5096), grâce à l'implémentation de [Trevas](https://github.com/InseeFr/Trevas).

La documentation destinée aux développeurs est disponible sur le [wiki](https://github.com/InseeFr/Kraftwerk/wiki).

La documentation utilisateur et les tests fonctionnels sont encore dans un [projet interne](https://gitlab.insee.fr/sic/service-agregation-echange-de-donnees/kraftwerk).

## Configuration requise

* JDK 21 +
* Maven 3.6 +
  
Kraftwerk utilise [Lombok](https://projectlombok.org/).

## Lancement

Si aucun paramètre n'est spécifié dans la commande `java -jar`, Kraftwerk se lancera
en tant qu'API REST.
Sinon, il va se lancer en mode batch et appliquer les traitements sur une campagne
avec les paramètres spécifiés. Les paramètres requis pour le mode batch sont les suivants (dans l'ordre) :
1. Service à utiliser (`MAIN`,`FILEBYFILE`,`GENESIS`,`LUNATIC_ONLY`)
2. Archiver à la fin de l'exécution (`false` ou `true`)
3. Integrate all reporting datas (`false` ou `true`)
4. Nom de la campagne (ou chemin du dossier de la campagne)