package org.edx.mobile.test.feature.interactor;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.test.feature.data.Credentials;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.ResourceUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

public class LogInScreenInteractor {

    public LogInScreenInteractor observeLogInScreen() {
        final MainApplication app = MainApplication.instance();
        final CharSequence title = app.getResources().getString(R.string.login_title);
        onView(allOf(isInActionBar(), withText(title.toString()))).check(matches(isCompletelyDisplayed()));
        onUsernameView().check(matches(isCompletelyDisplayed()));
        onPasswordView().check(matches(isCompletelyDisplayed()));
        onLogInButton().check(matches(isCompletelyDisplayed()));
        return this;
    }

    public MyCoursesScreenInteractor logIn(Credentials credentials) {
        onUsernameView().perform(replaceText(credentials.email));
        onPasswordView().perform(replaceText(credentials.password));
        onLogInButton().perform(click());
        return new MyCoursesScreenInteractor();
    }

    public LandingScreenInteractor navigateBack() {
        Espresso.pressBack();
        return new LandingScreenInteractor();
    }

    private ViewInteraction onUsernameView() {
        return onView(withHint(R.string.email_username));
    }

    private ViewInteraction onPasswordView() {
        return onView(withHint(R.string.password));
    }

    private ViewInteraction onLogInButton() {
        return onView(withContentDescription(R.string.login_btn));
    }
}
