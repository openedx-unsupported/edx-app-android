package org.edx.mobile.test.feature.interactor;

import androidx.test.espresso.ViewInteraction;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.test.feature.data.Credentials;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;
import org.hamcrest.CoreMatchers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

public class RegistrationScreenInteractor {
    public RegistrationScreenInteractor observeRegistrationScreen() {
        final CharSequence title = "Register";
        onView(allOf(isInActionBar(), withText(title.toString()))).check(matches(isCompletelyDisplayed()));
        return this;
    }

    public MyCoursesScreenInteractor createAccount(Credentials credentials) {
        onEmailView().perform(typeText(credentials.email), closeSoftKeyboard());
        onNameView().perform(typeText("Test Account"), closeSoftKeyboard());
        onUsernameView().perform(typeText(credentials.username), closeSoftKeyboard());
        onPasswordView().perform(typeText(credentials.password), closeSoftKeyboard());
        onCountryView().perform(click());
        onData(allOf(is(instanceOf(String.class)))).atPosition(1).perform(click());
        onCreateAccountButton().perform(click());
        return new MyCoursesScreenInteractor();
    }

    private ViewInteraction onEmailView() {
        return onView(withTagValue(is((Object) "email")));
    }

    private ViewInteraction onNameView() {
        return onView(withTagValue(is((Object) "name")));
    }

    private ViewInteraction onUsernameView() {
        return onView(withTagValue(is((Object) "username")));
    }

    private ViewInteraction onPasswordView() {
        return onView(withTagValue(is((Object) "password")));
    }

    private ViewInteraction onCountryView() {
        return onView(withTagValue(is((Object) "country")));
    }

    private ViewInteraction onCreateAccountButton() {
        return onView(withId(R.id.create_account_tv));
    }
}
