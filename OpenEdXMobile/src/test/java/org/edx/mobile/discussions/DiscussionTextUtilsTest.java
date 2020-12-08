package org.edx.mobile.discussions;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.test.util.TimeUtilsForTests;
import org.edx.mobile.util.ResourceUtil;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DiscussionTextUtilsTest extends BaseTestCase {
    @Test
    public void testSetAuthorAttributionText_AllCombinations() {
        final TextView textView = new TextView(context);

        // Input values
        final String author = "author";
        final String label = "label";
        final Date creationDate = new Date(TimeUtilsForTests.DEFAULT_TIME);

        // Expected output constructs
        final long now = creationDate.getTime() + TimeUnit.DAYS.toMillis(1); // A day after the creationDate
        final String relativeTime = "Yesterday";
        final String outputAuthor = ResourceUtil.getFormattedString(context.getResources(),
                R.string.discussion_post_author_attribution, "author", author).toString();
        final String outputAuthorLbl = ResourceUtil.getFormattedString(context.getResources(),
                R.string.discussion_post_author_label_attribution, "text", label).toString();
        final String endorsePrefix = context.getString(R.string.discussion_post_endorsed);
        final String answerPrefix = context.getString(R.string.discussion_post_marked_as_answer);

        // For endorsed
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(author, label, creationDate), now,
                endorsePrefix + " " + relativeTime + " " + outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(null, label, creationDate), now,
                endorsePrefix + " " + relativeTime + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(author, null, creationDate), now,
                endorsePrefix + " " + relativeTime + " " + outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(author, label, null), now,
                endorsePrefix + " " + outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(null, null, creationDate), now,
                endorsePrefix + " " + relativeTime);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(author, null, null), now,
                endorsePrefix + " " + outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ENDORSEMENT,
                new StubAuthorData(null, label, null), now,
                endorsePrefix + " " + outputAuthorLbl);

        // For answer
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(author, label, creationDate), now,
                answerPrefix + " " + relativeTime + " " + outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(null, label, creationDate), now,
                answerPrefix + " " + relativeTime + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(author, null, creationDate), now,
                answerPrefix + " " + relativeTime + " " + outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(author, label, null), now,
                answerPrefix + " " + outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(null, null, creationDate), now,
                answerPrefix + " " + relativeTime);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(author, null, null), now,
                answerPrefix + " " + outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(null, label, null), now,
                answerPrefix + " " + outputAuthorLbl);

        // Empty Case
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.ANSWER,
                new StubAuthorData(null, null, null), now,
                null);
    }

    private void assertSetAuthorAttributionText(TextView textView,
                                                DiscussionTextUtils.AuthorAttributionLabel type,
                                                IAuthorData input,
                                                long now, String expectedOutput) {
        final Runnable listener = Mockito.mock(Runnable.class);
        DiscussionTextUtils.setAuthorAttributionText(textView, type, input, now, listener);
        if (expectedOutput == null) {
            assertTrue(textView.getVisibility() == View.GONE);
        } else {
            String output = textView.getText().toString();
            assertEquals(expectedOutput, output);
            if (!input.isAuthorAnonymous()) {
                // Test whether author span is clickable or not
                int start = output.indexOf(input.getAuthor());
                int end = start + input.getAuthor().length();
                Spanned text = (Spanned) textView.getText();
                StyleSpan[] styleSpans = text.getSpans(start, end, StyleSpan.class);
                ForegroundColorSpan[] colorSpans = text.getSpans(start, end, ForegroundColorSpan.class);
                if (config.isUserProfilesEnabled()) {
                    // Verify that the author text is bold
                    assertEquals(1, styleSpans.length);
                    assertEquals(start, text.getSpanStart(styleSpans[0]));
                    assertEquals(end, text.getSpanEnd(styleSpans[0]));
                    assertEquals(Typeface.BOLD, styleSpans[0].getStyle());

                    // Verify that the correct foreground color is set
                    assertEquals(1, colorSpans.length);
                    assertEquals(start, text.getSpanStart(colorSpans[0]));
                    assertEquals(end, text.getSpanEnd(colorSpans[0]));
                    assertEquals(context.getResources().getColor(R.color.primaryBaseColor),
                            colorSpans[0].getForegroundColor());

                    // Verify that the whole text view is clickable
                    textView.performClick();
                    Mockito.verify(listener).run();
                } else {
                    assertEquals(0, styleSpans.length);
                    assertEquals(0, colorSpans.length);
                    assertFalse(textView.isClickable());
                }
            }
        }
    }

    @Test
    public void testSetAuthor_AllCombinations() {
        final TextView textView = new TextView(context);

        // Input values
        final String author = "author";
        final String label = "label";
        final Date creationDate = Mockito.mock(Date.class);

        // Expected output constructs
        final String outputAuthorLbl = ResourceUtil.getFormattedString(context.getResources(),
                R.string.discussion_post_author_label_attribution, "text", label).toString();

        assertSetAuthorText(textView,
                new StubAuthorData(author, label, creationDate),
                author + " " + outputAuthorLbl);
        assertSetAuthorText(textView,
                new StubAuthorData(author, null, creationDate),
                author);
        assertSetAuthorText(textView,
                new StubAuthorData(null, label, creationDate),
                outputAuthorLbl);
        assertSetAuthorText(textView,
                new StubAuthorData(null, null, creationDate),
                outputAuthorLbl);
    }

    private void assertSetAuthorText(TextView textView, IAuthorData input,
                                                String expectedOutput) {
        DiscussionTextUtils.setAuthorText(textView, input);
        if (expectedOutput == null) {
            assertTrue(textView.getVisibility() == View.GONE);
        } else {
            String output = textView.getText().toString();
            assertEquals(expectedOutput, output);

            if (!input.isAuthorAnonymous()) {
                int start = output.indexOf(input.getAuthor());
                int end = start + input.getAuthor().length();
                Spanned text = (Spanned) textView.getText();
                StyleSpan[] styleSpans = text.getSpans(start, end, StyleSpan.class);
                ForegroundColorSpan[] colorSpans = text.getSpans(start, end, ForegroundColorSpan.class);

                // Verify that the author text is bold
                assertEquals(1, styleSpans.length);
                assertEquals(start, text.getSpanStart(styleSpans[0]));
                assertEquals(end, text.getSpanEnd(styleSpans[0]));
                assertEquals(Typeface.BOLD, styleSpans[0].getStyle());

                // Verify that the correct foreground color is set
                assertEquals(1, colorSpans.length);
                assertEquals(start, text.getSpanStart(colorSpans[0]));
                assertEquals(end, text.getSpanEnd(colorSpans[0]));
                assertEquals(context.getResources().getColor(R.color.primaryBaseColor),
                        colorSpans[0].getForegroundColor());
            }
        }
    }

    private static class StubAuthorData implements IAuthorData {
        private final String author, authorLabel;
        private final Date createdDate;

        public StubAuthorData(String author, String authorLabel, Date createdDate) {
            this.author = author;
            this.authorLabel = authorLabel;
            this.createdDate = createdDate;
        }

        @Override
        public String getAuthor() {
            return author;
        }

        @Override
        public String getAuthorLabel() {
            return authorLabel;
        }

        @Override
        public Date getCreatedAt() {
            return createdDate;
        }

        @Override
        public boolean isAuthorAnonymous() {
            return TextUtils.isEmpty(author);
        }
    }
}
