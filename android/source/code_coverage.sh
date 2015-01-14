#!/bin/bash

# Set this shell script to fail in case of error
set -e

# This shell script runs instrument on VideoLocker project.
# Copies reports to Coverage folder.


# define directory names
appDir=VideoLocker
testDir=VideoLockerTestsTest


# create report directory
now="$(date +'%d-%m-%Y')"
timestamp=$now-"$( date +'%T' )"
reportDir=Coverage/reports/$timestamp

# create directory for this report
mkdir -p $reportDir
echo "[INFO] Created directory $reportDir"

# enter to VideoLocker folder and run instrument 
pwd
cd $appDir 
ant clean instrument 
cd ..
cp $appDir/bin/*-instrumented.apk $appDir/$testDir/bin/

# install test app
pwd
cd $appDir/$testDir
ant emma installi
cd ../..
echo "[INFO] ant instrument completed"

# run instrument
pkg=org.edx.mobile
# -e class $pkg.test.module.DbTests
adb shell am instrument -w -e coverage true -e coverageFile /sdcard/coverage.ec $pkg.test/android.test.InstrumentationTestRunner

# send a broadcast to end code coverage
adb shell am broadcast -a $pkg.END_EMMA

echo "[INFO] Instrumentation run is completed"

# copy reports to report directory
adb pull /sdcard/coverage.ec $reportDir/
emfile=$appDir/bin/coverage.em
cp $emfile $reportDir/
echo "[INFO] Copied coverage files to $reportDir"

# cd $reportDir
#echo "[INFO] Entered into report directory"

# Run Ant "report" Target
# ant report
#echo "[INFO] ant report completed"

# Generate Report
cd $reportDir
pwd
java -cp /android-sdk/tools/lib/emma_device.jar emma report -r html -in coverage.em,coverage.ec -sp ../../../$appDir/src
echo "[INFO] Generated report using emma"

# Get back to the folder containing this shell script
cd ../../..

# Done
echo "[INFO] Report generated!"

