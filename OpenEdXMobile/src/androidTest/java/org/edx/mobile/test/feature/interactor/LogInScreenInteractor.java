package org.edx.mobile.test.feature.interactor;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.test.feature.data.Credentials;
import org.edx.mobile.test.feature.matcher.TextInputLayoutMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.edx.mobile.test.feature.matcher.TextInputLayoutMatcher.inputLayoutWithHint;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;

public class LogInScreenInteractor {

    public LogInScreenInteractor observeLogInScreen() {
        final MainApplication app = MainApplication.instance();
        final CharSequence title = app.getResources().getString(R.string.login_title);
        onView(allOf(isInActionBar(),withText(title.toString()))).check(matches(isCompletelyDisplayed()));
        onUsernameView().check(matches(isDisplayed()));
        onPasswordView().check(matches(isDisplayed()));
        onLogInButton().check(matches(isDisplayed()));
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
        return onView(withParent(inputLayoutWithHint(is(MainApplication.instance().getResources().getString(R.string.email_username)))));
    }

    private ViewInteraction onPasswordView() {
        return onView(withParent(inputLayoutWithHint(is(MainApplication.instance().getResources().getString(R.string.password)))));
    }

    private ViewInteraction onLogInButton() {
        return onView(withContentDescription(MainApplication.instance().getResources().getString(R.string.login_btn)));
    }

}
