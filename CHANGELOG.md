# Changelog


## 1.4.3 - not released yet

### Added
- New format for output : parquet
- Export of reporting data variables into a separate table
- Export of last collection date from paradata into root
- Functional tests module
- Add standard unimode VTL script executed before user script(s)
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
