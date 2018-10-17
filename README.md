[![Build Status](https://travis-ci.org/edx/edx-app-android.svg?branch=master)](https://travis-ci.org/edx/edx-app-android)

# edX Android

This is the source code for the edX mobile Android app. It is changing rapidly
and its structure should not be relied upon. See http://code.edx.org for other
parts of the edX code base.

It requires the "Dogwood" release of open edX or newer. See
https://openedx.atlassian.net/wiki/display/COMM/Open+edX+Releases for more
information.

License
=======
This software is licensed under version 2 of the Apache License unless
otherwise noted. Please see `LICENSE.txt` for details.

Building
========

This project is meant to be built using Android Studio. It can also be built from the `gradle` command line.

1. Check out the source code:

        git clone https://github.com/edx/edx-app-android

2. Open Android Studio and choose **Open an Existing Android Studio Project**

3. Choose `edx-app-android`.

4. Click the **Run** button.

Configuration
=============
The edX mobile Android app is designed to connect to an Open edX instance. You
must configure the app with the correct server address and supply appropriate
OAuth credentials. We use a configuration file mechanism similar to that of the
Open edX platform. This mechanism is also used to make other values available
to the app at runtime and store secret keys for third party services.

There is a default configuration that points to an edX devstack instance
running on localhost. See the `default_config` directory. For the default
configuration to work, you must add OAuth credentials specific to your
installation.

Setup
-----
To use a custom configuration in place of the default configuration, you will need to complete these tasks:

1. Create your own configuration directory somewhere else on the file system. For example, create `my_config` as a sibling of the `edx-app-android` repository.

2. Create an `edx.properties` file inside the `OpenEdXMobile` directory of `edx-app-android`. In this `edx.properties` file, set the `edx.dir` property to the path to your configuration directory relative to the `OpenEdXMobile` directory. For example, if I stored my configuration side by side with the `edx-app-android` repository at `my_config` then I'd have the following `edx.properties`:

        edx.dir = '../../my_config'

3.  In the configuration directory that you added in step 1, create another
`edx.properties` file. This properties file contains a list of filenames. The files should be in YAML format and are for storing specific keys. These files are specified relative to the configuration directory. Keys in files earlier in the list will be overridden by keys from files later in the list. For example, if I had two files, one shared between ios and android called `shared.yaml` and one with Android specific keys called `android.yaml`, I would have the following `edx.properties`:

        edx.android {
            configFiles = ['shared.yaml', 'android.yaml']
        }


The full set of known keys can be found in the
`org/edx/mobile/util/Config.java` file or see [additional documentation](<https://openedx.atlassian.net/wiki/display/MA/App+Configuration+Flags>).


Build Variants
--------------------

There are 3 Build Variants in this project:

- **prodDebug**: Uses prod flavor for debug builds.
- **prodDebuggable**: Uses prod flavor for debug builds with debugging enabled.
- **prodRelease**: Uses prod flavor for release builds that'll work on devices with Android 4.1.x (Jelly Bean) and above.

Building For Release
--------------------
To build an APK for release, you may specify an application ID and signing key.
Application ID key is optional and if it is not present the package identifier will be used as an application ID.
Create or edit the `gradle.properties` file inside the `OpenEdXMobile` directory of `edx-app-android`. For example:

    APPLICATION_ID=com.example.yourapp
    RELEASE_STORE_FILE=../../path/to/your.keystore
    RELEASE_STORE_PASSWORD=your store password here
    RELEASE_KEY_PASSWORD=your key password here
    RELEASE_KEY_ALIAS=your key alias here

Now you can build a release build from Android Studio. Or, in the directory of `edx-app-android` you can build a release build with this gradle command:


    ./gradlew assembleProdRelease

**Note:** For release branch naming convention take a look at this PR https://github.com/edx/edx-app-android/pull/774 that creates the versionCode automatically based on branch name. The output APK will be named with the version.


Customization
-------------
**Resources**

To customize images, colors, and layouts, you can specify a custom resource directory. Create or edit the `gradle.properties` file inside the `OpenEdXMobile` directory of `edx-app-android`. For example:


    RES_DIR = ../../path/to/your/res

Any resources placed here will override resources of the same name in the `OpenEdXMobile/res` directory.

To remove all edX branding, override the drawables for: `edx_logo`, `edx_logo_login`, `ic_launcher`, `ic_new_cert`, and `profile_photo_placeholder`. These come in a number of resolutions, be sure to replace them all.

If you need to make more in depth UI changes, most of the user interface is specified in standard Android XML files, which you can also override by placing new versions in your `RES_DIR`.

**Assets**

To customize assets such as the End User License Agreement (EULA) you can specify a custom assets directory. Create or edit the `gradle.properties` file inside the `OpenEdXMobile` directory of `edx-app-android`. For example:


    ASSETS = ../../path/to/your/assets

Any assets placed here will override resources of the same name in the `OpenEdXMobile/assets` directory.

Third Party Services
--------------------
The app relies on the presence of several third party services: Facebook, NewRelic, Google+, SegmentIO, and Crashlytics. You may need to remove services you choose not to use. You can comment out the lines of code that mention these services.

We're working on making it easier for Open edX installations to apply customizations and select third party services without modifying the repository itself.


Frequently Asked Questions
==========================
**Q:** I see an error that mentions "Unsupported major.minor version 51.0". How do I fix this?

**A:** Our build system requires Java 7 or later. If you see this error, install Java 7 or later.

	 You will also need to specify the new JDK version in Android Studio. Refer to this Stack Overflow entry for help doing so:

	 http://stackoverflow.com/questions/30631286/how-to-specify-the-jdk-version-in-android-studio

**Q:** After I upgraded to Android Studio v2.3, I've been facing alot of issues while compiling/building the project. How do I fix this?

**A:** We recently upgraded our project to support Android Studio v2.3.x and below. After the upgrade changes done in [PR #938](https://github.com/edx/edx-app-android/pull/938), we too faced some issues.
The fixes for the common issues can be seen in the [Issues section](https://github.com/edx/edx-app-android/issues) of this GitHub project. The most common and helpful issue with the fixes is [Issue #976](https://github.com/edx/edx-app-android/issues/976).

**Q:** I see an error that mentions "Error:Execution failed for task ':OpenEdXMobile:processDevDebugGoogleServices'. File google-services.json is missing.". How do I fix this?

**A:** This file is required for our Firebase analytics implementation to work. If you are not planning to use Firebase just add an empty file named `google-services.json` at the root of the project's folder i.e. under `OpenEdXMobile` folder.
