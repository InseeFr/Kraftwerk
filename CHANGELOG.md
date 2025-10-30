# Changelog

## 3.9.2 [2025-10-24]
### Fixed
- Inverse the order of files

### Updated
- BPM 1.0.18
## 3.9.1 [2025-10-24]
### Fixed
- Read files by their names (which are containing the date of extraction)

## 3.9.0 [2025-10-20]
### Added
- Extraction in json

## 3.8.0 [2025-10-16]
### Added
- Survey unit id export

## 3.7.0 [2025-09-25]
### Changed
- Reporting data outcome spotting calculation for IASCO and HOUSEF2F

### Updated
- BPM 1.0.17
## 3.6.4 [2025-09-11]
### Updated
- DuckDB 1.3.2.1
- Springdoc openapi webmvc 2.8.13
- Cucumber 7.28.2


## 3.6.3 [2025-08-28]
### Updated
- Springboot 3.5.5
- Springdoc openapi webmvc 2.8.11


## 3.6.2 [2025-08-21]
### Updated
- Springdoc 2.8.10

## 3.6.1 [2025-08-19]
### Updated
- BPM 1.0.13
- Pitest 1.20.2
- Cucumber 7.27.2

## 3.6.0 [2025-08-07]
### Added
- Load metadata from Genesis and save into it if not exists

## 3.5.0 [2025-08-07]
### Added
- Extraction with QuestionnaireId

## 3.4.3 [2025-08-07]
### Updated
- BPM 1.0.12
- Trevas 1.11.0

## 3.4.2 [2025-07-31]
### Updated
- BPM 1.0.9

## 3.4.1 [2025-07-31]
### Fixed
- Reporting data not found on MinIO
- Spring doc missing description
### Updated
- DuckDB 1.3.2.0
- Trevas 1.10.0
- Spring boot 3.5.4
- OpenCSV 5.12.0
- Cucumber 7.27.0
- Pitest 1.20.1
- Commons-compress 1.28.0

## 3.4.0 [2025-07-08]
### Added
- First version of JSON export

### Changed
- Speed optimizations (Block processing, useless calls...)
- Possibility to launch on batch mode with workers

## 3.3.6 [2025-06-03]

### Added
- reporting data : new calculation for OUTCOME_SPOTTING
- reporting data : all variables are in capital

### Fixed
- symmetrical encryption

### Updated
- pitest 1.2.3
- pitest-maven 1.19.4
- springboot 3.5.0
- trevas 1.9.0
- duckdb 1.3.0.0
- opencsv 5.11.1

## 3.3.5 [2025-05-19]

### Added
- symmetrical encryption of output files
- reporting data : add variables NoLog, NoGrep, Nole, Autre, ClosingCause, ClosingDate, TYPE_SPOTTING, individualStatus, interviewerCanProcess
- reporting data : no more renaming of OUTCOME et STATE modalities
- reporting data : add specific endpoint to extract them and remove them from "classic" data extraction

### Updated
- bpm 1.0.7
- saxon-he 12.7
- opencsv 5.11
- springdoc 2.8.8
- cucumber 7.22.2
- pitest 1.19.3

## 3.3.4 [2025-04-29}
### Fixed
- Resolve problem concerning the reading of vtl files on MinIo

## 3.3.3 [2025-04-25]
### Added
- Add possibility to read VTL script when using Genesis data

### Updated
- springboot 3.4.5
- commons-collections4 4.5.0
- duckdb 1.2.2.0
- pitest-maven 1.9.1
- jacoco-maven-plugin 0.8.13

## 3.3.2 [2025-03-25]
### Fixed
- Bug with scheduler : add a role for it

### Updated
- springdoc-openapi-starter-webmvc-ui : 2.8.6

## 3.3.1 [2025-03-21]
### Fixed
- Bug : no roles in token of service account

## 3.3.0 [2025-03-21]
### Added
- Role-based permissions to endpoints
- New tests

### Fixed
- DuckDb in-memory (for docker)

### Updated
- springboot 3.4.4
- pitest-maven 1.19.0
- duckdb-jdbc 1.2.1

## 3.2.2 [2025-03-05]
### Fixed
- Bug on large-scale surveys : fix in-memory duckdb
- Bug on token check : if 401 we force the refresh of the token

## 3.2.1 [2025-02-27]
### Fixed
- Bug of expiration of user token : use a service account to communicate with other API
### Updated
- pitest-junit5-plugin 1.2.2
- spingboot 3.4.3

## 3.2.0 [2025-02-18]
### Added
- Adaptation to the new Genesis model
### Fixed
- Warning when cucumber tests are executed
### Updated
- pitest 1.18.2
- springdoc 2.8.5
- duckdb 1.2.0
- cucumber 7.21.1


## 3.1.1 [2025-01-31]
### Added
- External variables in loop\tables are now exported correctly
### Updated
- bpm : 1.0.5
- pitest : 1.17.3
- springboot : 3.4.2
- springdoc : 2.8.4

## 3.1.0 [2024-12-13]
### Added
- OIDC Authentication
### Fixed
- DDI required in genesis lunatic only endpoint

## 3.0.8 [2024-10-29] 
### Added
- Endpoint for genesis data without DDI

### Changed
- Improve Kubernetes deployment
- Genesis endpoint (new names)

### Fixed
- Logs

### Upgraded
- BPM to 1.0.3 that adds support for dynamic tables and roundabouts from Lunatic specification, and refactor lunatic spec reading
- Trevas 1.6.0, then 1.7.0
- DuckDB, Cucumber, Coverall, Minio
- Springboot 3.3.5

## 3.0.7 [2024-09-25] - DDI : add support for Dynamic tables and Roundabouts
### Added
- Upgrade BPM version to 1.0.2 that adds support for dynamic tables and roundabouts from DDI specification
### Fixed
- Fix : misreading of dates from reporting data in csv format

## 3.0.6 [2024-09-09] - Fix : DDI xslt transformation
### Changed
- Fixed conflict : remove xslt transformation from Kraftwerk as it is now in BPM library.

## 3.0.5 [2024-09-06] - Genesis service fix
### Changed
- Update BPM version

### Fixed
- Execution crash when vtl error on genesis service (kraftwerkExecutionContext null)

## 3.0.4 [2024-08-22] - BPM integration
### Changed
- Metadata parsing shared with Genesis using BPM (Basic Parser of Metadata)
- Regrouped kraftwerkError list and kraftwerk execution log into kraftwerkExecutionContext object
- kraftwerk.properties renamed to kraftwerk_exemple.properties and now optional

### Fixed
- No reporting data when withAllReportingData = false

## 3.0.3 [2024-07-31] - Fixes for VPP
### Fixed
- Fixed genesis not looking in default directory to find specs

## 3.0.2 [2024-07-29] - Various fixes
### Fixed
- \r\n in lunatic calculated variables breaks the CSV
- Temporary data file directories creation (for local execution) 

## 3.0.1 [2024-07-19] - Fix IOException due to permission on temp directory

### Fixed
- IOException due to permission on temp directory

## 3.0.0 [2024-07-16] - DuckDB implementation for output and Kubernetes support
### Added
- Launch treatment with command line
- S3/MinIO support : All file system calls goes through an interface with both OS file system and MinIO implementations
- DuckDB implementation for output
- Transfer Vtl datasets into DuckDB before output step
- SQL util class for SQL operations
- Health-check

### Changed
- (File-by-file) Kraftwerk now exports only one .parquet file
- Output is now made from DuckDB instead of VTL dataset

### Fixed
- Loop without name

### Removed
- Avro

## 2.1.0 [2024-06-11] - Change Lunatic reader
### Changed
- Refactor of Lunatic reader

### Fixed
- SAS import script : bad format on two STRING variables
- New pairwise link group (from new DDI model) is ignored
- Improve quality of code

## 2.0.9 [2024-05-16] - Fix genesis calls
### Added
- Error 204 when no data in Genesis database when using the Genesis endpoint

### Changed
- Kraftwerk now get modes from Genesis by campaign
- Kraftwerk genesis endpoint now uses idCampaign like the other endpoints 
- Changed some error message to be more explicit

### Fixed
- Fix crash when empty reporting data file
- Genesis response not parsed from JSON when calling on get QuestionnaireModelIds endpoint

## 2.0.8 [2024-05-03] - Fix id for genesis 

### Fixed
- Genesis uses questionnaireModelId since Kraftwerk used idCampaign. Now the first step is to get questionnaireMOdelIds from idCampaigne to loop.

## 2.0.7 [2024-05-03] - Hotfix configure genesis

### Fixed
- Add property to configure Genesis
- Fix encoding for properties


## 2.0.6 [2024-05-03] - Hotfix genesis endpoint

### Fixed
- Pass idQuestionnaire param as body for genesis service
- Fix CVE >8

### Added
- Prepare dockerfile

## 2.0.5 [2024-04-24] - Numbered parquet files and fix logs

### Changed
- Parquet files are numbered and then merged in the import script

### Fixed
- Log and error files are created with output files
- Rolling logs

## 2.0.4 [2024-04-22]

### Changed
- Output files are stored in separate execution time folders

### Fixed
- Fixed crash when no COLLECTED tag in .xml data file


## 2.0.3 [2024-04-02] - Update some dependencies

### Changed
- Update some dependencies

## 2.0.2 [2024-03-19]
### Added
- Add a utility service to split large data files
- Configure VTL execution for some specifics sequences (TCM)
- Add outcome spotting variable with reporting data

### Fix
- Remove json-simple dependency
- Fix files in log output

## 2.0.1 - [2024-02-22]

### Added
- Log for functional purpose

### Fixed
- Update dependencies, including Trevas (error with nvl())

## 2.0.0 - [2023-12-18]

### Added
- Look for data in Genesis

### Changed
- Migrated to Java 21

### Fixed
- Fix issue with null interviewerId
- Fix exception when reporting VTL script not found

## 1.5.0 - [2023-11-24]

### Added
- New format for output : parquet
- Add new reporting data in output
- Add possibility to calculate variables from reporting data with VTL script
- Export of reporting data variables into a separate table
- Export of last survey validation date from paradata into root
- Functional tests module
- Add Pairwise

## 1.4.2 - [2023-09-26]

### Changed
- Add VTL user script execution between two variable calculation (VTL from DDI)

### Fixed
- Fix issue with rank in loop names
- Fix issue CERTFR-2023-AVI-0763

## 1.4.1 - [2023-09-12]

### Fixed
- Fix issue with empty paradata (missing start session)

## 1.4.0 - [2023-08-22]

### Added
- Service to read metadata from Lunatic in case no DDI is available

### Changed
- Add date for contact-outcome and last attempt from reporting Data, instead of current day, month, year

### Fixed
- In case of file by file execution, reportingData without questionnaires were duplicated. Fix with skipping these reportingData

## 1.3.3 - [2023-06-13]

### Fixed

- Paradata : fix the method for calculating sessions time
- Exclude boolean variables from length errors

## 1.3.2 - [2023-05-30]

### Fixed
- Security issue by updating Springboot

## 1.3.1 - [2023-05-02]

### Changed
- Read paradata file by file (survey unit by surveyunit based on filename)
- Remove SLF4J and use Log4j2 instead

### Fixed
- Security issue

## 1.3.0 - [2023-04-19]

### Added
- Warning when variable length is not compatible with associated metadata (error + SAS script)
- Endpoint to process data file by file

### Changed
- Remove XOM to read DDI. DOM is used

### Fixed
- Read external variables in subgroups
- Fix format for numeric variables from paradata
- Fix memory error with temporary Json files

## 1.2.1 - [2023-03-10]

### Added
- Statistical identifier concatenates some other variables, first version

### Changed
- Variables are added only if they are present in the specification
- Improve script classes

## 1.2.0 - Never released

### Added
-  Lunatic V2 input data (Lunatic-Model v2.3+) are now supported

## 1.1.7 - [2023-02-27]

### Added
-  Add possibility to specify a path for data, paradater and reporting data outside in folder
   
### Changed
	- Change to Java 17 and Spring boot 3
	- Packaging as JAR instead of WAR
	
## 1.1.6 - [2023-01-06]
- Initialize API
