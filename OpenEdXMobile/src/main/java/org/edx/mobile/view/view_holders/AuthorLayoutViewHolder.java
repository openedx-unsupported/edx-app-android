package org.edx.mobile.view.view_holders;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.IAuthorData;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.ProfileImageProvider;
import org.edx.mobile.util.Config;

import roboguice.RoboGuice;

public class AuthorLayoutViewHolder {
    @Inject
    private LoginPrefs loginPrefs;

    public final ViewGroup profileRow;
    public final ImageView profileImageView;
    public final TextView authorTextView;
    public final TextView dateTextView;
    public final TextView answerTextView;

    public AuthorLayoutViewHolder(View itemView) {
        profileRow = (ViewGroup) itemView;
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        authorTextView = (TextView) itemView.findViewById(R.id.discussion_author_text_view);
        dateTextView = (TextView) itemView.findViewById(R.id.discussion_date_text_view);
        answerTextView = (TextView) itemView.findViewById(R.id.discussion_responses_answer_text_view);

        final Context context = answerTextView.getContext();
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                answerTextView,
                new IconDrawable(context, FontAwesomeIcons.fa_check_square_o)
                        .sizeRes(context, R.dimen.edx_base)
                        .colorRes(context, R.color.successLight),
                null, null, null);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    public void populateViewHolder(@NonNull Config config, @NonNull IAuthorData authorData,
                                   @NonNull ProfileImageProvider provider,
                                   long initialTimeStampMs,
                                   @NonNull final Runnable listener) {
        final Context context = profileImageView.getContext();
        final ProfileImage profileImage;
        {
            if (provider.getProfileImage() != null && provider.getProfileImage().hasImage()) {
                profileImage = provider.getProfileImage();
            } else {
                /**
                 * Background: Currently the POST & PATCH APIs aren't configured to return a user's
                 * {@link ProfileImage} in their response. Since, the currently logged-in user is
                 * the only one that can POST using the app, so, we use the locally stored
                 * {@link ProfileImage} in {@link LoginPrefs} instead.
                 * Incase of PATCH we just use the image that we got in the initial GET call.
                 */
                ProfileModel profileModel = loginPrefs.getCurrentUserProfile();
                if (profileModel != null && authorData.getAuthor().equals(profileModel.username)) {
                    profileImage = loginPrefs.getProfileImage();
                } else {
                    profileImage = null;
                }
            }
            if (profileImage != null && profileImage.hasImage()) {
                Glide.with(context)
                        .load(profileImage.getImageUrlMedium())
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.profile_photo_placeholder);
            }
        }

        DiscussionTextUtils.setAuthorText(authorTextView, authorData);
        if (authorData.getCreatedAt() != null) {
            CharSequence relativeTime = DiscussionTextUtils.getRelativeTimeSpanString(context,
                    initialTimeStampMs, authorData.getCreatedAt().getTime());
            dateTextView.setText(relativeTime);
        } else {
            dateTextView.setVisibility(View.GONE);
        }
        if (config.isUserProfilesEnabled() && !authorData.isAuthorAnonymous()) {
            profileRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.run();
                }
            });
        }
    }
}
