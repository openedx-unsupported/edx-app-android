package org.edx.mobile.util;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;

import org.edx.mobile.R;

import java.util.HashMap;
import java.util.Map;

import static org.edx.mobile.view.dialog.WebViewActivity.PARAM_INTENT_FILE_LINK;

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
     * @param resources     Resources object to use for obtaining strings from strings.xml.
     * @param licenseTextId Resource ID of the license text.
     * @return License text having clickable links to license documents.
     */
    public static CharSequence generateLicenseText(@NonNull Resources resources,
                                                   @StringRes int licenseTextId) {
        final String platformName = resources.getString(R.string.platform_name);
        final CharSequence licenseAgreement = ResourceUtil.getFormattedString(resources, R.string.licensing_agreement, "platform_name", platformName);
        final CharSequence terms = ResourceUtil.getFormattedString(resources, R.string.tos_and_honor_code, "platform_name", platformName);
        final CharSequence privacyPolicy = resources.getString(R.string.privacy_policy);

        final SpannableString agreementSpan = new SpannableString(licenseAgreement);
        agreementSpan.setSpan(new URLSpan(TextUtils.createAppUri(
                resources.getString(R.string.end_user_title),
                resources.getString(R.string.eula_file_link))),
                0, licenseAgreement.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final SpannableString termsSpan = new SpannableString(terms);
        termsSpan.setSpan(new URLSpan(TextUtils.createAppUri(
                resources.getString(R.string.terms_of_service_title),
                resources.getString(R.string.terms_file_link))),
                0, terms.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final SpannableString privacyPolicySpan = new SpannableString(privacyPolicy);
        privacyPolicySpan.setSpan(new URLSpan(TextUtils.createAppUri(
                resources.getString(R.string.privacy_policy_title),
                resources.getString(R.string.privacy_file_link))),
                0, privacyPolicy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        final Map<String, CharSequence> keyValMap = new HashMap<>();
        keyValMap.put("license", agreementSpan);
        keyValMap.put("tos_and_honor_code", termsSpan);
        keyValMap.put("privacy_policy", privacyPolicySpan);

        return ResourceUtil.getFormattedString(resources, licenseTextId, keyValMap);
    }
}
