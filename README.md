# leadsDeduper

## Goal
Take a variable number of identically structured json records and de-duplicate the set.

## Requirements
1. The data from the newest date should be preferred
duplicate IDs count as dupes.
2. Duplicate emails count as dupes. Both must be unique in our dataset. Duplicate values elsewhere do not count as dups.
3. If the dates are identical the data from the record provided last in the list should be preferred
4. The application should also provide a log of changes including some representation of the source record, the output record and the individual field changes (value from and value to) for each field.

## Assumptions
The program can do everything in memory.