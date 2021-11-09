package org.edx.mobile.util;

import static org.edx.mobile.view.dialog.WebViewActivity.PARAM_INTENT_FILE_LINK;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.URLSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.edx.mobile.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TextUtils {
    private TextUtils() {
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param delimiter The delimiter to use while joining.
     * @param tokens    An array of {@link CharSequence} to be joined using the delimiter.
     * @return A {@link CharSequence} joined using the provided delimiter.
     */
    public static CharSequence join(CharSequence delimiter, Iterable<CharSequence> tokens) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean firstTime = true;
        for (CharSequence token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb;
    }

    /**
     * Creates a URI that will only be understandable for our app when broadcasted.
     * <br/>
     * Example URI: org.edx.mobile.innerlinks://a-title?fileLink=link_to_a_file
     *
     * @param title Title parameter for the broadcast.
     * @param uri   URI parameter for the broadcast.
     * @return App specific URI string.
     */
    public static String createAppUri(@NonNull String title, @NonNull String uri) {
        final StringBuilder uriString = new StringBuilder(AppConstants.APP_URI_SCHEME);
        uriString.append(title).append("?").append(PARAM_INTENT_FILE_LINK).append("=").append(uri);
        return uriString.toString();
    }

    /**
     * Generates the license text for displaying in app that includes clickable links to license
     * documents shipped with app.
     *
     * @param config        Configurations object to use for obtaining agreement URLs in config.
     * @param context       Context object to use for obtaining strings from strings.xml.
     * @param licenseTextId Resource ID of the license text.
     * @return License text having clickable links to license documents.
     */
    public static CharSequence generateLicenseText(@NonNull Config config,
                                                   @NonNull Context context,
                                                   @StringRes int licenseTextId) {
        final String platformName = context.getResources().getString(R.string.platform_name);
        final CharSequence eula = ResourceUtil.getFormattedString(context.getResources(), R.string.licensing_agreement, "platform_name", platformName);
        final CharSequence tos = ResourceUtil.getFormattedString(context.getResources(), R.string.tos_and_honor_code, "platform_name", platformName);
        final CharSequence privacyPolicy = context.getResources().getString(R.string.privacy_policy);

        final SpannableString eulaSpan = new SpannableString(eula);
        final SpannableString tosSpan = new SpannableString(tos);
        final SpannableString privacyPolicySpan = new SpannableString(privacyPolicy);

        String eulaUri = ConfigUtil.getAgreementUrl(context, config.getAgreementUrlsConfig(), AgreementUrlType.EULA);
        String tosUri = ConfigUtil.getAgreementUrl(context, config.getAgreementUrlsConfig(), AgreementUrlType.TOS);
        String privacyPolicyUri = ConfigUtil.getAgreementUrl(context, config.getAgreementUrlsConfig(), AgreementUrlType.PRIVACY_POLICY);

        if (!android.text.TextUtils.isEmpty(eulaUri)) {
            eulaSpan.setSpan(new URLSpan(TextUtils.createAppUri(
                    context.getResources().getString(R.string.end_user_title), eulaUri)),
                    0, eula.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (!android.text.TextUtils.isEmpty(tosUri)) {
            tosSpan.setSpan(new URLSpan(TextUtils.createAppUri(
                    context.getResources().getString(R.string.terms_of_service_title), tosUri)),
                    0, tos.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (!android.text.TextUtils.isEmpty(privacyPolicyUri)) {
            privacyPolicySpan.setSpan(new URLSpan(TextUtils.createAppUri(
                    context.getResources().getString(R.string.privacy_policy_title), privacyPolicyUri)),
                    0, privacyPolicy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        final Map<String, CharSequence> keyValMap = new HashMap<>();
        keyValMap.put("license", eulaSpan);
        keyValMap.put("tos_and_honor_code", tosSpan);
        keyValMap.put("platform_name", platformName);
        keyValMap.put("privacy_policy", privacyPolicySpan);

        return ResourceUtil.getFormattedString(context.getResources(), licenseTextId, keyValMap);
    }

    /**
     * Returns displayable styled text from the provided HTML string.
     * <br/>
     * Note: Also handles the case when a String has multiple HTML entities that translate to one
     * character e.g. {@literal &amp;#39;} which is essentially an apostrophe that should normally
     * occur as {@literal &#39;}
     *
     * @param html The source string having HTML content.
     * @return Formatted HTML.
     */
    @NonNull
    public static Spanned formatHtml(@NonNull String html) {
        final String REGEX = "(&#?[a-zA-Z0-9]+;)";
        final Pattern PATTERN = Pattern.compile(REGEX);

        Spanned formattedHtml = new SpannedString(html);
        String previousHtml = null;

        // Break the loop if there isn't an HTML entity in the text or when all the HTML entities
        // have been decoded. Also break the loop in the special case when a String having the
        // same format as an HTML entity is left but it isn't essentially a decodable HTML entity
        // e.g. &#asdfasd;
        while (PATTERN.matcher(html).find() && !html.equals(previousHtml)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                formattedHtml = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            } else {
                formattedHtml = Html.fromHtml(html);
            }
            previousHtml = html;
            html = formattedHtml.toString();
        }

        return formattedHtml;
    }

    public static void setTextAppearance(Context context, TextView textView, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(context, resId);
        }
    }

    /**
     * Converts and returns the provided duration into a readable format (i.e. 1 hour 55 mins)
     * OR returns null if the duration is zero or negative.
     *
     * @param duration Video duration in seconds.
     * @return Formatted duration.
     */
    @Nullable
    public static String getVideoDurationString(Context context, long duration) {
        if (duration <= 0) {
            return null;
        }
        long d = duration;
        int hours = (int) (d / 3600f);
        d = d % 3600;
        int mins = (int) (d / 60f);
        int secs = (int) (d % 60);
        if (secs >= 30) {
            mins += 1;
        } else if (mins == 0 && secs > 0) {
            // below 1 minute = 1 minute
            mins = 1;
        }
        if (hours <= 0) {
            return context.getResources().getQuantityString(R.plurals.video_duration_minutes, mins, mins);
        }
        return String.format("%s %s", context.getResources().getQuantityString(R.plurals.video_duration_hour, hours, hours), context.getResources().getQuantityString(R.plurals.video_duration_minutes, mins, mins));
    }
}
