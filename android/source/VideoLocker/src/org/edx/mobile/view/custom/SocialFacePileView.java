package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.social.SocialMember;

import java.util.ArrayList;
import java.util.List;

public class SocialFacePileView extends LinearLayout {

    private float radius;
    private float avatarMaxSize;
    private float avatarGap;
    private float overflowBorderWidth;
    private float overflowTextSize;
    private int maxAvatarDisplay;
    private final List<RoundedProfilePictureView> profilePictureList = new ArrayList<RoundedProfilePictureView>();
    private TextView overflow;
    private List<SocialMember> memberAvatars;

    public SocialFacePileView(Context context, AttributeSet attrs) {

        super(context, attrs);

        setAttributes(context, attrs);
    }

    public SocialFacePileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setAttributes(context, attrs);
    }

    private void setAttributes(Context context, AttributeSet attrs) {

        if (isInEditMode()){return;}

        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.SocialFacePileView);
        this.maxAvatarDisplay = attrArray.getInt(R.styleable.SocialFacePileView_avatarCount, getResources().getInteger(R.integer.group_avatar_count));
        this.avatarMaxSize = attrArray.getDimension(R.styleable.SocialFacePileView_avatarMaxSize, 0);
        this.radius = attrArray.getDimension(R.styleable.SocialFacePileView_borderRadius, getResources().getDimension(R.dimen.group_avatar_radius));
        this.avatarGap = attrArray.getDimension(R.styleable.SocialFacePileView_avatarGap, getResources().getDimension(R.dimen.group_avatar_gap));
        this.overflowBorderWidth = attrArray.getDimension(R.styleable.SocialFacePileView_overflowBorderWidth, 0);
        this.overflowTextSize = attrArray.getDimension(R.styleable.SocialFacePileView_overflowTextSize, R.dimen.group_avatar_overflow_text);


        for (int i = 0; i < maxAvatarDisplay; i++){
            RoundedProfilePictureView profileView = new RoundedProfilePictureView(getContext());
            profilePictureList.add(profileView);
            profileView.setVisibility(View.GONE);
            LayoutParams params = new LayoutParams((int)avatarMaxSize, (int)avatarMaxSize);
            profileView.setLayoutParams(params);
            addView(profileView);
        }

        //Create the overflow view
        overflow = new TextView(getContext());

        GradientDrawable drawable = new GradientDrawable();
        //
        drawable.setColor(getResources().getColor(R.color.white));
        drawable.setCornerRadius(this.radius);
        drawable.setStroke(Math.round(this.overflowBorderWidth), getResources().getColor(R.color.grey_2));
        //
        overflow.setBackgroundDrawable(drawable);

        overflow.setGravity(Gravity.CENTER);
        overflow.setTextAppearance(getContext(), R.style.avatar_overflow_style);

        overflow.setTextSize(TypedValue.COMPLEX_UNIT_PX, this.overflowTextSize);
        overflow.setVisibility(View.GONE);
        addView(overflow);
    }

    private void resizeViews(int w){

        ViewParent viewParent = getParent();

        if (viewParent instanceof ViewGroup){
            int hPadding = this.getPaddingRight() + this.getPaddingLeft();
            w -= hPadding;
        }

        int avatarPadding = Math.round(this.avatarGap);
        int avatarSize = Math.round((w - (avatarPadding * (maxAvatarDisplay-1))) / maxAvatarDisplay);
        if (avatarMaxSize > 0 && avatarMaxSize < avatarSize) {
            avatarSize = Math.round(avatarMaxSize);
        }

        for (int i = 0; i < maxAvatarDisplay; i++){

            RoundedProfilePictureView profileView = profilePictureList.get(i);
            profileView.setRoundedCornerRadius(radius);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
            if (i < maxAvatarDisplay){
                params.setMargins(0, 0, avatarPadding, 0);
            }
            profileView.setLayoutParams(params);

        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        overflow.setLayoutParams(params);

    }

    public void clearAvatars() {
        setMemberList(new ArrayList<SocialMember>());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (isInEditMode()){return;}
        resizeViews(w);
    }

    public List<SocialMember> getMemberList() {
        return memberAvatars;
    }

    public void setMemberList(List<SocialMember> memberList) {
        this.memberAvatars = memberList;
        refreshList();
    }

    private void refreshList() {

        if (this.memberAvatars == null || profilePictureList == null) {
            return;
        }

        int totalAvatarCount = memberAvatars.size();
        int avatarImageCount = (totalAvatarCount == maxAvatarDisplay) ? maxAvatarDisplay : Math.min(totalAvatarCount, maxAvatarDisplay-1);

        int i = 0;
        while (i < avatarImageCount){

            Long profileID = memberAvatars.get(i).getId();

            RoundedProfilePictureView profileView = profilePictureList.get(i);
            profileView.setProfileId(profileID.toString());
            profileView.setVisibility(View.VISIBLE);

            i++;
        }

        while(i < maxAvatarDisplay){
            RoundedProfilePictureView profileView = profilePictureList.get(i);
            profileView.setVisibility(View.GONE);
            i++;
        }

        if (totalAvatarCount > maxAvatarDisplay) {
            int overflowCount = (totalAvatarCount - maxAvatarDisplay + 1);
            if (overflowCount > 999){
                overflow.setText("+" + Math.floor(overflowCount /1000));
            } else {
                overflow.setText("+" + overflowCount);
            }
            overflow.setVisibility(View.VISIBLE);
        }
        else{
            overflow.setVisibility(View.GONE);
        }

    }

}