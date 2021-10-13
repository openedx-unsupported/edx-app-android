SHELL := /usr/bin/env bash
.PHONY: help requirements clean emulator quality test validate e2e artifacts

help :
	@echo ''
	@echo 'Makefile for the edX Android application'
	@echo 'Usage:'
	@echo '    make help            show this information'
	@echo '    make clean           remove artifacts from previous usage'
	@echo '    make requirements    install python requirements'
	@echo '    make emulator        create and initialize an android emulator'
	@echo '    make quality         check coding style'
	@echo '    make test            run unit tests'
	@echo '    make validate        run all local tests (linting, unit tests)'
	@echo '    make e2e             run all emulator tests (e2e, screenshot tests)'
	@echo '    make artifacts       gather artifacts from testing (reports, screenhsots)'
	@echo 'Requirements:'
	@echo '    You must have the `tools` directory in the Android SDK available'
	@echo '    in your path'
	@echo ''

clean :
	@echo 'Cleaning the workspace and any previously created AVDs'
	./gradlew clean
	rm -Rf $$HOME/.android/avd/screenshotDevice.avd
	rm -f $$HOME/.android/avd/screenshotDevice.ini

requirements :
	@echo 'Installing python requirements'
	@pip install -r requirements.txt --exists-action w

emulator :
	@echo 'Creating and initializing an Android emulator for testing the app'
	# ARM architecture is used instead of x86 (which is 10x faster) of the lack
	# of support from CI due to complications of creating a virtual machine
	# within a virtual machine. This may be solved eventually and would
	# significantly speed some things up.
	@android create avd --force --name screenshotDevice --target android-21 \
    --abi armeabi-v7a --device "Nexus 4" --skin 768x1280 --sdcard 250M
	@echo "runtime.scalefactor=auto" >> \
    $$HOME/.android/avd/screenshotDevice.avd/config.ini
	# Boot up the emulator in the background. This can take a fair amount of
	# time.
	@emulator -avd screenshotDevice -no-audio -no-window &

# `--debug` or `--info` info flags can be suffixed to the commands in the
# following targets for more detailed logs of tests. It comes in handy to see
# stacktraces from test failures, which otherwise aren't printed. P.S. --debug
# flag prints too many logs that are mostly not needed, which make it exceed
# the 4mb limit enforced by travis (so use it with caution)
quality:
	@./gradlew assembleDebug

test:
	@./gradlew jacocoTestProdDebugUnitTestReport

validate: quality test

e2e :
	@./gradlew verifyProdDebuggableAndroidTestScreenshotTest -PdisablePreDex

artifacts:
	@./gradlew copyLintBuildArtifacts
	@./gradlew copyUnitTestBuildArtifacts
	@./gradlew pullProdDebuggableAndroidTestScreenshots -PdisablePreDex
