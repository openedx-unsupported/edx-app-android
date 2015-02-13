package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.comparator.SocialMemberComparator;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.util.SocialUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendListAdapter extends SimpleAdapter<SocialMember> implements Filterable {

    private OnSelectedItemCountChangeListener changeListener;
    private Set<Long> selectedList = new HashSet<Long>();
    private Set<Long> alreadyInGroupList = new HashSet<Long>();
    private List<SocialMember> originalList = new ArrayList<>();

    public FriendListAdapter(Context context) {
        super(context);
    }

    public void setOnSelectChangeListener(OnSelectedItemCountChangeListener listener){
        changeListener = listener;
    }

    public FriendListAdapter(Context context, List<SocialMember> data) {
        super(context, data);

        originalList = sortMembers(data, alreadyInGroupList);

    }

    public FriendListAdapter(Context context, List<SocialMember> data, Set<Long> alreadyInGroupList) {
        super(context, data);

        this.alreadyInGroupList = alreadyInGroupList;

        originalList = sortMembers(data, alreadyInGroupList);

    }

    public Set<Long> getSelectedList() {
        return this.selectedList;
    }

    public void setSelectedList(Set<Long> selectedList) {
        this.selectedList = selectedList;
    }

    public Set<Long> getAlreadyInGroupList() {
        return this.alreadyInGroupList;
    }

    public void setAlreadyInGroupList(Set<Long> alreadyInGroupList) {
        this.alreadyInGroupList = alreadyInGroupList;

        originalList = sortMembers(originalList, alreadyInGroupList);

        notifyDataSetChanged();
    }

    @Override
    public void setItems(List<SocialMember> data) {

        originalList = sortMembers(data, this.alreadyInGroupList);

        super.setItems(data);

    }

    private List<SocialMember> sortMembers(List<SocialMember> members, Set<Long> inList){

        if (members != null && members.size() > 0){

            //Clone array, sort alphabetically
            ArrayList<SocialMember> alphabetizedMembers = new ArrayList<>(members);
            Collections.sort(alphabetizedMembers, new SocialMemberComparator());

            //Clear the previous array (contents will be swapped out)
            members.clear();

            //If the previously selected list is populated sort based on its data.
            if (inList != null && inList.size() > 0){

                ArrayList<SocialMember> unselected = new ArrayList<>();
                ArrayList<SocialMember> selected = new ArrayList<>();

                for (SocialMember friend : alphabetizedMembers) {

                    if (inList.contains(friend.getId())){

                        unselected.add(friend);

                    } else {

                        selected.add(friend);

                    }

                }
                members.addAll(selected);
                members.addAll(unselected);

                return members;

            }
            members.addAll(alphabetizedMembers);

        }

        return members;

    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                final FilterResults oReturn = new FilterResults();
                final List<SocialMember> results = new ArrayList<SocialMember>();

                if(charSequence != null){
                    String search = charSequence.toString();
                    if (originalList != null && originalList.size() > 0) {
                        for (final SocialMember member : originalList) {
                            if (member.getFullName().toLowerCase().contains(search)){
                                results.add(member);
                            }
                        }

                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                data = (List<SocialMember>) filterResults.values;
                notifyDataSetChanged();
            }
        };

    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        return isExistingUserInGroup(position) ? 0 : 1;

    }

    private boolean isExistingUserInGroup(int position){

       SocialMember member = getItem(position);
       return  (alreadyInGroupList != null && alreadyInGroupList.contains(member.getId()));

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            int layoutID = isExistingUserInGroup(position) ? R.layout.friend_list_item_preselected : R.layout.friend_list_item ;
            convertView = inflate(layoutID, parent);
        }
        setUpView(convertView, getItem(position));

        return convertView;

    }

    @Override
    protected void setUpView(View view, final SocialMember item) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.friend_name);
            viewHolder.selectedBox = (CheckBox) view.findViewById(R.id.friend_selected_check);
            viewHolder.avatarContainer = (ViewGroup) view.findViewById(R.id.friend_avatar_container);
            view.setTag(viewHolder);
        }
        viewHolder.name.setText(item.getFullName());

        boolean hasAvatar = viewHolder.avatarContainer.getChildAt(0) != null;
        View avatarView = SocialUtils.getAvatarView(context, viewHolder.avatarContainer.getChildAt(0), String.valueOf(item.getId()));
        if (!hasAvatar) {
            int width = (int) context.getResources().getDimension(R.dimen.avatar_view_width);
            int height = (int) context.getResources().getDimension(R.dimen.avatar_view_height);
            avatarView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
            viewHolder.avatarContainer.addView(avatarView, viewHolder.avatarContainer.getChildCount());
        }

        if (viewHolder.selectedBox != null){

            viewHolder.selectedBox.setChecked(selectedList.contains(item.getId()));
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedList.contains(item.getId())) {
                        selectedList.remove(item.getId());
                    } else {
                        selectedList.add(item.getId());
                    }

                    if (changeListener != null) {
                        changeListener.onSelectionItemCountChange(selectedList.size());
                    }

                    notifyDataSetChanged();
                }

            };
            viewHolder.selectedBox.setOnClickListener(onClickListener);
            view.setOnClickListener(onClickListener);

            //Make sure that the view is in the enabled state if it's recycled.

        }
        view.setEnabled(true);

    }

    @Override
    protected int getRowLayout() {
        return R.layout.friend_list_item;
    }

    public List<SocialMember> getSelectedFriends(){

        List<SocialMember> results = new ArrayList<SocialMember>();
        if (data == null) {
            return results;
        }
        for (SocialMember member : data){
            for (Long id : selectedList){
                if (member.getId() == id){
                    results.add(member);
                }
            }
        }
        return results;

    }

    public interface OnSelectedItemCountChangeListener {
        public void onSelectionItemCountChange(int itemSelectCount);
    }

    private class ViewHolder {
        private TextView name;
        private CheckBox selectedBox;
        //Using a facebook centric view here as it vastly simplifies image loading
        private ViewGroup avatarContainer;
        private FrameLayout avatarScreen;
    }

}