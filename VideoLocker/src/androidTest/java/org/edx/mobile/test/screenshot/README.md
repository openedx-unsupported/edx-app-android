# Screenshot Tests

## Requirements

* **Requires an emulator or device to run.** Having two devices connected may cause some issues. 

* Screenshot tests should be made against the Prod build.

* Requires python package PIL
```
"ImportError: No module named PIL" 
```
You need the python imaging library, or pillow.
Try "pip install image" or "sudo pip install image"
If you don't have pip, try "sudo easy_install pip"

## CI
Screenshot tests will be run on travis every time a commit in a PR is made against master. The command used by travis to verify screenshots is `./gradlew verifyMode screenshotTests`. This will run the screenshot tests and then run a diff against the screenshots stored in the repository. If the screenshot are different, the task will fail.

## Record all screenshots

To run all screenshot tests and save the screenshots to the screenshot directory use `./gradlew recordMode screenshotTests`.

## Developing screenshot tests

### Setting up the emulator

Screenshots need to be consistent for pixel by pixel comparison. In order to set up a local emulator, which will share the same dpi and screen size as the CI emulator setup in travis.yml, follow these steps:

```
android create avd --force --name screenshotDevice --target android-21 --abi x86 --device "Nexus 4" --skin 768x1280 --sdcard 250M
echo "hw.gpu.enabled=yes" >> $HOME/.android/avd/screenshotDevice.avd/config.ini
echo "hw.gpu.mode=auto" >> $HOME/.android/avd/screenshotDevice.avd/config.ini
echo "runtime.scalefactor=auto" >> $HOME/.android/avd/screenshotDevice.avd/config.ini
emulator -avd screenshotDevice
```

Alternatively, you can use Android Studio's AVD Manager to create a "Nexus 4" device targeting API 21.

### How to create the screenshot test

Example template
```
@RunWith(AndroidJUnit4.class)
public class ExampleScreenshotTest {

    @Rule
    public ActivityTestRule<{ExampleActivity}> mActivityRule =
            new ActivityTestRule<>({ExampleActivity.class});

    @Test
    public void takeScreenshotOf_exampleActivity() throws Throwable {
        View view = mActivityRule.getActivity().findViewById(R.id.{id of the root view});
        Screenshot.snap(view).record();
    }
}
```
### How to record and save a subset of screenshots

1. Use `./gradlew clearScreenshots` to delete any screenshots in the emulator/device.
1. Use the Android Studio interface to run a specific screenshot test or class. This screenshot test (if successful) will be saved to the emulator/device.
  * If not set already, in "Edit Configurations" for the test, set the "Specific instrumentation runner" value to "org.edx.mobile.test.EdXTestRunner"
2. Use `./gradlew recordMode pullScreenshots` to save the screenshot to the screenshots directory.


