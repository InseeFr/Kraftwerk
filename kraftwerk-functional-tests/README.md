| Test Campaign                    | Description                                        | Used for                    | Survey Unit(s)                                     |
|----------------------------------|----------------------------------------------------|-----------------------------|----------------------------------------------------|
| SAMPLETEST-DATAONLY-V1           | Only 1 data file                                   | Test data export            | 0000004 to 0000006                                 |
| SAMPLETEST-ERROR                 | SAMPLETEST-DATAONLY-V1 but with invalid VTL script | Test VTL error export       | //                                                 |
| SAMPLETEST-MULTIPLEDATA-V1       | Multiple data files                                | Test multiple data export   | 0000004 to 0000009                                 |
| SAMPLETEST-MULTIPLEDATA-V2       | Multiple data files, first one has no SU,          | //                          | 0000007 to 0000009                                 |
| SAMPLETEST-PAPERDATA-V1          | FAF and PAPI data                                  | Test paper data import      | 0000004 to 0000006 (FAF) 1000004 to 1000006 (PAPI) |
| SAMPLETEST-PARADATA-V1           | Has paradata                                       | Test paradata parsing       | 0000007                                            |
| SAMPLETEST-REPORTINGDATA-MOOG-V1 | Uses Moog XML reporting data structure             | Test moog xml data import   | 0000001 to 0000003                                 |
| SAMPLETEST-REPORTINGDATA-V1      | Data with reporting data                           | Test reporting data parsing | //                                                 |
| SAMPLETEST-REPORTINGDATA-V2      | Data with reporting data + Identification data     | //                          | //                                                 |