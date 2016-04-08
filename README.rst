This is the source code for the edX mobile Android app. It is changing rapidly
and its structure should not be relied upon. See http://code.edx.org for other
parts of the edX code base.

It requires the "Dogwood" release of open edX or newer. See
https://openedx.atlassian.net/wiki/display/COMM/Open+edX+Releases for more
information.

License
=======
This software is licensed under version 2 of the Apache License unless
otherwise noted. Please see ``LICENSE.txt`` for details.

Building
========

This project is meant to be built using Android Studio. It can also be built from the ``gradle`` command line.

1. Check out the source code: ::

	git clone https://github.com/edx/edx-app-android

2. Open Android Studio and choose **Open an Existing Android Studio Project**

3. Choose ``edx-app-android``.

4. Click the **Run** button.

*Note: Our build system requires Java 7 or later.  If you see an error
mentioning "Unsupported major.minor version 51.0 " then you should install a
newer Java SDK.*

Configuration
=============
The edX mobile Android app is designed to connect to an Open edX instance. You
must configure the app with the correct server address and supply appropriate
OAuth credentials. We use a configuration file mechanism similar to that of the
Open edX platform. This mechanism is also used to make other values available
to the app at runtime and store secret keys for third party services.

There is a default configuration that points to an edX devstack instance
running on localhost. See the ``default_config`` directory. For the default
configuration to work, you must add OAuth credentials specific to your
installation.

Setup
-----
To use a custom configuration in place of the default configuration, you will need to complete these tasks:

1. Create your own configuration directory somewhere else on the file system. For example, create ``my_config`` as a sibling of the ``edx-app-android`` repository.

2. Create an ``edx.properties`` file inside the ``VideoLocker`` directory of ``edx-app-android``. In this ``edx.properties`` file, set the ``edx.dir`` property to the path to your configuration directory relative to the ``VideoLocker`` directory. For example, if I stored my configuration side by side with the ``edx-app-android`` repository at `my_config`` then I'd have the following ``edx.properties``:

::

    edx.dir = '../../my_config'

3.  In the configuration directory that you added in step 1, create another
``edx.properties`` file. This properties file contains a list of filenames. The files should be in YAML format and are for storing specific keys. These files are specified relative to the configuration directory. Keys in files earlier in the list will be overridden by keys from files later in the list. For example, if I had two files, one shared between ios and android called ``shared.yaml`` and one with Android specific keys called ``android.yaml``, I would have the following ``edx.properties``:

::

    edx.android {
        configFiles = ['shared.yaml', 'android.yaml']
    }


The full set of known keys can be found in the
``org/edx/mobile/util/Config.java`` file or see `additional documentation <https://openedx.atlassian.net/wiki/display/MA/App+Configuration+Flags>`_.

Additional Customization
------------------------
Right now this code is constructed specifically to build the *edx.org* app. We're working on making it easier for Open edX installations to apply customizations and select third party services without modifying the repository itself. Until that work is complete, you will need to modify or replace files within your fork of the repo.

To remove the edX branding you will need to replace the ``edx_logo`` files in standard locations under ``res/drawables``. These come in a number of resolutions. See Google's documentation for more information on different app icon sizes. Additionally, you will need to replace the ``edx-map`` images used in the login screen.

If you need to make more in depth UI changes, most of the user interface is specified in standard Android XML files, editable from within Android Studio.

As mentioned, the app relies on the presence of several third party services:
Facebook, NewRelic, Google+, SegmentIO, and Crashlytics. You must remove references to each of these services you choose not to use. You can comment out the lines that mention these services. We're working to make those dependencies optional.
