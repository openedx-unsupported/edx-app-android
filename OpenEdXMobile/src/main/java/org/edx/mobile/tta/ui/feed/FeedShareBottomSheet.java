package org.edx.mobile.tta.ui.feed;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.enums.SourceName;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.utils.AppUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.utils.Tools;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.ShareUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.edx.mobile.util.BrowserUtil.config;

public class FeedShareBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = FeedShareBottomSheet.class.getCanonicalName();

    private ShareUtils.ShareMenuItemListener listener;
    private Feed feed;

    public static FeedShareBottomSheet newInstance(ShareUtils.ShareMenuItemListener listener, Feed feed){
        FeedShareBottomSheet fragment = new FeedShareBottomSheet();
        fragment.listener = listener;
        fragment.feed = feed;
        return fragment;
    }

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        int layoutId;
        try {
            switch (Action.valueOf(feed.getAction())){
                case CourseLike:
                case ShareCourse:
                case Certificate:
                case GenerateCertificate:
                case CertificateGenerate:
                case Badge:
                    layoutId = R.layout.t_fragment_feed_share;
                    break;

                case LikePost:
                case MostPopular:
                case CommentPost:
                case Share:
                case SharePostApp:
                case NewPost:
                case DBComment:
                case DBLike:
                    if (feed.getMeta_data().getUser_name() != null) {
                        layoutId = R.layout.t_fragment_feed_share_with_user;
                    } else {
                        layoutId = R.layout.t_fragment_feed_share;
                    }
                    break;

                default:
                    layoutId = R.layout.t_fragment_feed_share;

            }
        } catch (IllegalArgumentException e) {
            layoutId = R.layout.t_fragment_feed_share;
        }

        View contentView = View.inflate(getContext(), layoutId, null);

        if (layoutId == R.layout.t_fragment_feed_share_with_user){

            Glide.with(getContext())
                    .load(config.getApiHostURL() +
                            feed.getMeta_data().getUser_icon().getLarge())
                    .placeholder(R.drawable.profile_photo_placeholder)
                    .into((ImageView) contentView.findViewById(R.id.feed_user_image));

            ((TextView) contentView.findViewById(R.id.feed_user_name)).setText(feed.getMeta_data().getUser_name());
            ((TextView) contentView.findViewById(R.id.feed_user_classes)).setText(getUserClasses(feed.getMeta_data().getTag_label()));

        } else {

            ((TextView) contentView.findViewById(R.id.feed_title)).setText(feed.getMeta_data().getSource_title());
            contentView.findViewById(R.id.play_icon).setVisibility(
                    feed.getMeta_data().getSource_name().equalsIgnoreCase(SourceName.course.name()) ?
                            View.GONE : View.VISIBLE
            );

        }

        contentView.findViewById(R.id.ivClose).setOnClickListener(v -> dismiss());

        ((TextView) contentView.findViewById(R.id.feed_meta_text)).setText(feed.getMeta_data().getText());
        Glide.with(getContext())
                .load(feed.getMeta_data().getIcon())
                .placeholder(R.drawable.placeholder_course_card_image)
                .into((ImageView) contentView.findViewById(R.id.feed_content_image));

        LinearLayout shareOptionsLayout = contentView.findViewById(R.id.feed_share_options_layout);

        addTTAOption(shareOptionsLayout);
        addFbOption(shareOptionsLayout);
        addWhatsappOption(shareOptionsLayout);
        addClipboardOption(shareOptionsLayout);

        /*final PackageManager packageManager = getActivity().getPackageManager();
        String shareText = getShareString();
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(
                ShareUtils.newShareIntent(shareText), 0);
        for (final ResolveInfo resolveInfo : resolveInfoList) {
            switch (resolveInfo.activityInfo.packageName){
                case "com.facebook.katana":
                case "com.facebook.lite":
                case "com.whatsapp":
                case "org.tta.mobile":
                case "org.edx.mobile":
                    ImageView shareImage = addShareOption(shareOptionsLayout);
                    shareImage.setImageDrawable(resolveInfo.loadIcon(packageManager));
                    shareImage.setOnClickListener(v -> {
                        final ComponentName componentName = new ComponentName(
                                resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                        ShareUtils.ShareType shareType = ShareUtils.getShareTypeFromComponentName(componentName);
                        listener.onMenuItemClick(componentName, shareType);

                        if (!shareType.equals(ShareUtils.ShareType.TTA)) {
                            final Intent intent = ShareUtils.newShareIntent(shareText);
                            intent.setComponent(componentName);
                            getActivity().startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Content shared on TheTeacherApp", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
            }
        }*/

        dialog.setContentView(contentView);

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    private String getShareString(){
        Map<String, CharSequence> map = new HashMap<>();
        map.put("source_title", feed.getMeta_data().getSource_title());
        map.put("platform_name", getActivity().getString(R.string.platform_name));
        map.put("content_name", feed.getMeta_data().getText());
        return ResourceUtil.getFormattedString(
                getActivity().getResources(),
                R.string.share_message,
                map).toString() + "\n" + feed.getMeta_data().getShare_url();
    }

    private void addTTAOption(LinearLayout optionsLayout){

        ImageView imageView = addShareOption(optionsLayout);
        imageView.setImageResource(R.drawable.tta_launcher_icon);
        imageView.setOnClickListener(v -> {
            if (listener != null){
                listener.onMenuItemClick(null, ShareUtils.ShareType.TTA);
            }
            Toast.makeText(getActivity(), "Content shared on TheTeacherApp", Toast.LENGTH_LONG).show();
        });

    }

    private void addFbOption(LinearLayout optionsLayout){
        if (AppUtil.appInstalledOrNot("com.facebook.katana", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.facebook.katana"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.FACEBOOK);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.facebook.katana");
                getActivity().startActivity(intent);
            });
        } else if (AppUtil.appInstalledOrNot("com.facebook.lite", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.facebook.lite"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.FACEBOOK);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.facebook.lite");
                getActivity().startActivity(intent);
            });
        }
    }

    private void addWhatsappOption(LinearLayout optionsLayout){
        if (AppUtil.appInstalledOrNot("com.whatsapp", getActivity().getPackageManager())){
            ImageView imageView = addShareOption(optionsLayout);
            Drawable d = null;
            try {
                d = getActivity().getPackageManager().getActivityIcon(
                        new Intent().setAction(Intent.ACTION_SEND).setType("text/plain")
                                .setPackage("com.whatsapp"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            imageView.setImageDrawable(d);
            imageView.setOnClickListener(v -> {
                if (listener != null){
                    listener.onMenuItemClick(null, ShareUtils.ShareType.WHATSAPP);
                }

                Intent intent = ShareUtils.newShareIntent(getShareString());
                intent.setPackage("com.whatsapp");
                getActivity().startActivity(intent);
            });
        }
    }

    private void addClipboardOption(LinearLayout optionsLayout){
        ImageView imageView = addShareOption(optionsLayout);
        imageView.setImageResource(R.drawable.t_icon_link);
        imageView.setOnClickListener(v -> {
            if (listener != null){
                listener.onMenuItemClick(null, ShareUtils.ShareType.TTA);
            }

            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("TTA share", feed.getMeta_data().getShare_url());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getActivity(), "Share URL copied to clipboard", Toast.LENGTH_LONG).show();
        });
    }

    private ImageView addShareOption(LinearLayout layout){
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Tools.dp2px(getActivity(), 24), 1);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setBackgroundResource(R.drawable.light_selectable_box_overlay);
        imageView.setClickable(true);
        imageView.setFocusable(true);
        layout.addView(imageView);
        return imageView;
    }

    private String getUserClasses(String tagLabel){
        StringBuilder builder = new StringBuilder("कक्षाएँ - ");

        if (tagLabel == null || tagLabel.length() == 0) {
            return builder.append("N/A").toString();
        }

        String[] section_tag_list = tagLabel.split(" ");
        boolean classesAdded = false;

        for (String section_tag : section_tag_list) {
            String[] duet = section_tag.split("_");
            if (duet[0].contains("कक्षा")){
                builder.append(duet[1]).append(", ");
                classesAdded = true;
            }
        }

        if (classesAdded){
            return builder.substring(0, builder.length() - 2);
        } else {
            return builder.append("N/A").toString();
        }

    }
}
