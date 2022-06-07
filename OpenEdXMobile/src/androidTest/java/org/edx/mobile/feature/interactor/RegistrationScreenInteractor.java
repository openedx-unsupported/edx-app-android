package org.edx.mobile.feature.interactor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;

import org.edx.mobile.R;
import org.edx.mobile.feature.data.Credentials;
import org.edx.mobile.feature.matcher.ActionBarMatcher;

public class RegistrationScreenInteractor {
    public RegistrationScreenInteractor observeRegistrationScreen() {
        final CharSequence title = "Register";
        ActionBarMatcher.isInActionBarInteraction()
                .check(matches(withText(title.toString())))
                .check(matches(isCompletelyDisplayed()));
        return this;
    }

    public MyCoursesScreenInteractor createAccount(Credentials credentials) {
        onEmailView().perform(ViewActions.typeText(credentials.email), ViewActions.closeSoftKeyboard());
        onNameView().perform(ViewActions.typeText("Test Account"), ViewActions.closeSoftKeyboard());
        onUsernameView().perform(ViewActions.typeText(credentials.username), ViewActions.closeSoftKeyboard());
        onPasswordView().perform(ViewActions.typeText(credentials.password), ViewActions.closeSoftKeyboard());
        onCountryView().perform(ViewActions.typeText("Albania"), ViewActions.closeSoftKeyboard());
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
        return onView(withId(R.id.createAccount_button_layout));
    }
}
