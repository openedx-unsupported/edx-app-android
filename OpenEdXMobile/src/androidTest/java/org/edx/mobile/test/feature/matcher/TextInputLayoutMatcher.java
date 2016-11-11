package org.edx.mobile.test.feature.matcher;

import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;


/**
 * Matcher for EditText items located within TextInputLayout
 * This will allow to match the EditText component whenever it is
 * nested in a TextInputLayout entity.
 * https://code.google.com/p/android/issues/detail?id=191261
 */

public class TextInputLayoutMatcher {
    public static Matcher<View> inputLayoutWithHint(final Matcher<String> stringMatcher) {
        checkNotNull(stringMatcher);
        return new BoundedMatcher<View, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("with hint: ");
                stringMatcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(TextInputLayout textView) {
                return stringMatcher.matches(textView.getHint());
            }
        };
    }
}
