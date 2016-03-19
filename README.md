# VideoCompare
Compare videos to find common content. Code from a hack event in CenturyLink India 2012.

## Accuracy control
In the compare.config file you can set the property "frame.interval" to lower values to achieve more accuracy. 
The lowest being 0.04 corresponding to 25fps videos. However this setting will take too long to finish.
For acceptable time limits like 30 mins to 2 hours, you can vary this value from 4 to 1.

## Running
This code was written to run using a DB to download videos needed for comparison. It does not run as it is now. Code is put here for archival purpose only.

## Better Solution
The better solution at the hack event was to
1. Sample Videos as accurately as possible using ffmpeg
2. Create image hashes. Ex: http://www.phash.org/
3. Compare videos using image hashes.