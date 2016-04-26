package org.edx.mobile.discussions;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.widget.TextView;

import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by miankhalid on 25/04/2016.
 */
public class DiscussionTextUtilsTest extends BaseTestCase {

    private TextView dummyTextView;
    private Runnable dummyClickListener;

    @Before
    public void initValues() {
        dummyTextView = new TextView(context);
        dummyClickListener = new Runnable() {
            @Override
            public void run() {
                logger.debug("Author clicked");
            }
        };
    }

    @Test
    public void test_DuplicatedWhitespaceRemoved() {
        String input = "  start  end  ";
        String expected = " start end ";
        String output = DiscussionTextUtils.removeDuplicateWhitespaces(input).toString();
        assertEquals(expected, output);
    }

    @Test
    public void test_TextInTextViewNotNull() {
        DiscussionTextUtils.setAuthorAttributionText(dummyTextView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData("testAuthor", "testLabel", new Date()),
                dummyClickListener);
        assertNotNull(dummyTextView.getText());
    }

    @Test
    public void test_AuthorIsTappable() {
        DiscussionTextUtils.setAuthorAttributionText(dummyTextView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData("testAuthor", "testLabel", new Date()),
                dummyClickListener);
        SpannableString cs = (SpannableString) dummyTextView.getText();
        ClickableSpan[] spans = cs.getSpans(0, cs.length(), ClickableSpan.class);
        if (config.isUserProfilesEnabled()) {
            assertTrue(spans.length > 0);
        } else {
            assertTrue(spans.length == 0);
        }
    }

    @Test
    public void test_AuthorIsTappable_WhenNoCreationDate() {
        DiscussionTextUtils.setAuthorAttributionText(dummyTextView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData("testAuthor", "testLabel", null),
                dummyClickListener);
        ClickableSpan[] spans = getClickableSpans();
        if (config.isUserProfilesEnabled()) {
            assertTrue(spans.length > 0);
        } else {
            assertTrue(spans.length == 0);
        }
    }

    @Test
    public void test_AuthorIsNotTappable_WhenNoAuthor() {
        DiscussionTextUtils.setAuthorAttributionText(dummyTextView,
                DiscussionTextUtils.AuthorAttributionLabel.POST,
                new StubAuthorData(null, "testLabel", new Date()),
                dummyClickListener);
        ClickableSpan[] spans = getClickableSpans();
        assertTrue(spans.length == 0);
    }

    private ClickableSpan[] getClickableSpans() {
        SpannableString spannableString = (SpannableString) dummyTextView.getText();
        return spannableString.getSpans(0, spannableString.length(), ClickableSpan.class);
    }

    private static class StubAuthorData implements IAuthorData {
        String author, authorLabel;
        Date createdDate;

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
