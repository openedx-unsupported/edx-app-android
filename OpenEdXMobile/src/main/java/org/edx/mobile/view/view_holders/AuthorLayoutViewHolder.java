package org.edx.mobile.view.view_holders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.model.discussion.IAuthorData;
import org.edx.mobile.model.user.ProfileImage;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.ProfileImageProvider;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.UiUtils;

import dagger.hilt.android.EntryPointAccessors;

public class AuthorLayoutViewHolder {

    LoginPrefs loginPrefs;

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
        UiUtils.INSTANCE.setTextViewDrawableStart(context, answerTextView, R.drawable.ic_verified,
                R.dimen.edx_base, R.color.successLight);
        loginPrefs = EntryPointAccessors.fromApplication(context,
                EdxDefaultModule.ProviderEntryPoint.class).getLoginPrefs();
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
                /*
                  Background: Currently the POST & PATCH APIs aren't configured to return a user's
                  {@link ProfileImage} in their response. Since, the currently logged-in user is
                  the only one that can POST using the app, so, we use the locally stored
                  {@link ProfileImage} in {@link LoginPrefs} instead.
                  In case of PATCH we just use the image that we got in the initial GET call.
                 */
                if (loginPrefs.getUsername().equals(authorData.getAuthor())) {
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
