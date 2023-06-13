# Changelog

## Not released yet

### Added

### Changed

### Fixed

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