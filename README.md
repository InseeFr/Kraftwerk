<img align="right" src="logo/kraftwerk-logo.png" alt="Kraftwerk logo"/>

# Kraftwerk

Kraftwerk is a Java Spring Batch application designed to validate and process data from multimode surveys, to generate data tables ready-to-use for statistical purposes.
It heavily relies on metadata described using the [DDI](http://ddialliance.org) standard.
The batch automated processes can be enriched with specification written in [VTL](https://sdmx.org/?page_id=5096), thanks to [Trevas](https://github.com/InseeFr/Trevas) implementation.

Developer documentation can be found in the [wiki](https://github.com/InseeFr/Kraftwerk/wiki).

User documentation and batch functional tests are still in an [inhouse project](https://gitlab.insee.fr/sic/service-agregation-echange-de-donnees/kraftwerk).

## Requirements

* JDK 11 +
* Maven 3.6 +

Kraftwerk uses [Lombok](https://projectlombok.org/).
