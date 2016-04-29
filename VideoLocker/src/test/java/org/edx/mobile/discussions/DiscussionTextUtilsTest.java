package org.edx.mobile.discussions;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.ResourceUtil;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class DiscussionTextUtilsTest extends BaseTestCase {
    @Test
    public void testSetAuthorAttributionText_AllCombinations() {
        final TextView textView = new TextView(context);

        // Input values
        final String author = "author";
        final String label = "label";
        final Date creationDate = new Date(1462368150195L); // A day before now

        // Expected output constructs
        final long now = 1462454550195L; // A day after the creationDate
        final String relativeTime = "Yesterday";
        final String outputAuthor = ResourceUtil.getFormattedString(context.getResources(),
                R.string.discussion_post_author_attribution, "author", author).toString();
        final String outputAuthorLbl = ResourceUtil.getFormattedString(context.getResources(),
                R.string.discussion_post_author_label_attribution, "text", label).toString();
        final String endorsePrefix = context.getString(R.string.discussion_post_endorsed);
        final String answerPrefix = context.getString(R.string.discussion_post_marked_as_answer);

        // For post
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(author, label, creationDate), now,
                relativeTime + " " + outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(null, label, creationDate), now,
                relativeTime + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(author, null, creationDate), now,
                relativeTime + " " + outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(author, label, null), now,
                outputAuthor + " " + outputAuthorLbl);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(null, null, creationDate), now,
                relativeTime);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(author, null, null), now,
                outputAuthor);
        assertSetAuthorAttributionText(textView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(null, label, null), now,
                outputAuthorLbl);

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
                SpannableString text = (SpannableString) textView.getText();
                ClickableSpan[] spans = text.getSpans(start, end, ClickableSpan.class);
                if (config.isUserProfilesEnabled()) {
                    assertEquals(1, spans.length);
                    assertEquals(start, text.getSpanStart(spans[0]));
                    assertEquals(end, text.getSpanEnd(spans[0]));
                    spans[0].onClick(textView);
                    Mockito.verify(listener).run();
                } else {
                    assertTrue(spans.length == 0);
                }
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