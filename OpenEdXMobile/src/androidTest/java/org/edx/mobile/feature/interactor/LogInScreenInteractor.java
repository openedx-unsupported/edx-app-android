package org.edx.mobile.feature.interactor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;

import org.edx.mobile.R;
import org.edx.mobile.feature.data.Credentials;
import org.edx.mobile.feature.matcher.ActionBarMatcher;

public class LogInScreenInteractor {

    public LogInScreenInteractor observeLogInScreen() {
        final Application app = ApplicationProvider.getApplicationContext();
        final String title = app.getResources().getString(R.string.login_title);
        ActionBarMatcher.isInActionBarInteraction()
                .check(matches(withText(title)))
                .check(matches(isCompletelyDisplayed()));
        onUsernameView().check(matches(isCompletelyDisplayed()));
        onPasswordView().check(matches(isCompletelyDisplayed()));
        onLogInButton().check(matches(isCompletelyDisplayed()));
        return this;
    }

    public MyCoursesScreenInteractor logIn(Credentials credentials) {
        onUsernameEditView().perform(replaceText(credentials.email), ViewActions.closeSoftKeyboard());
        onPasswordEditView().perform(replaceText(credentials.password), ViewActions.closeSoftKeyboard());
        onLogInButton().perform(click());
        return new MyCoursesScreenInteractor();
    }

    public LandingScreenInteractor navigateBack() {
        Espresso.pressBack();
        return new LandingScreenInteractor();
    }

    private ViewInteraction onUsernameView() {
        return onView(withId(R.id.usernameWrapper));
    }

    private ViewInteraction onUsernameEditView() {
        return onView(withId(R.id.email_et));
    }

    private ViewInteraction onPasswordView() {
        return onView(withId(R.id.passwordWrapper));
    }

    private ViewInteraction onPasswordEditView() {
        return onView(withId(R.id.password_et));
    }

    private ViewInteraction onLogInButton() {
        return onView(withContentDescription(R.string.login_btn));
    }
}
