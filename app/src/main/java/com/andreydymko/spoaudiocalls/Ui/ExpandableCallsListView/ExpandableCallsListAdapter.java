package com.andreydymko.spoaudiocalls.Ui.ExpandableCallsListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.Compat.DrawableCompat;
import com.andreydymko.spoaudiocalls.Utils.CollectionsCompat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ExpandableCallsListAdapter extends BaseExpandableListAdapter {

    private final List<CallViewModel> callsList;
    private final LayoutInflater inflater;
    private OnUserClickJoinCallback onJoinCallback;

    public ExpandableCallsListAdapter(Context context) {
        this.callsList = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnJoinCallback(OnUserClickJoinCallback onJoinCallback) {
        this.onJoinCallback = onJoinCallback;
    }

    public void removeOnJoinCallback() {
        this.onJoinCallback = null;
    }

    public void addUser(UUID callUuid, UserModel userModel) {
        int callIdx = callsList.indexOf(new CallViewModel(callUuid));
        if (callIdx >= 0) {
            if (addUser(callIdx, userModel)) {
                notifyDataSetChanged();
            }
        }
    }

    public void addUsers(UUID callUuid, Collection<UserModel> userModels) {
        int callIdx = callsList.indexOf(new CallViewModel(callUuid));
        if (callIdx >= 0) {
            for (UserModel userModel : userModels) {
                addUser(callIdx, userModel);
            }
            notifyDataSetChanged();
        } else {
            addCall(callUuid);
            addUsers(callUuid, userModels);
        }
    }

    private boolean addUser(int callIdx, UserModel userModel) {
        return CollectionsCompat.addUniqueUpdating(callsList.get(callIdx).getUsersList(), userModel) >= 0;
    }

    public void updateUser(UUID callUuid, UserModel userModel) {
        int callIdx = callsList.indexOf(new CallViewModel(callUuid));
        if (callIdx < 0) {
            addCall(callUuid);
        }

        addUser(callUuid, userModel);
    }

    public void removeUser(UUID callUuid, UserModel userModel) {
        int callIdx = callsList.indexOf(new CallViewModel(callUuid));
        if (callIdx >= 0) {
            if (callsList.get(callIdx).getUsersList().remove(userModel)) {
                notifyDataSetChanged();
            }
        }
    }

    public void addCall(UUID callUuid) {
        if (CollectionsCompat.addUnique(callsList, new CallViewModel(callUuid))) {
            notifyDataSetChanged();
        }
    }

    public void addCalls(Collection<UUID> callUuids) {
        for (UUID uuid : callUuids) {
            CollectionsCompat.addUnique(callsList, new CallViewModel(uuid));
        }
        notifyDataSetChanged();
    }

    public void removeCall(UUID callUuid) {
        if (callsList.remove(new CallViewModel(callUuid))) {
            notifyDataSetChanged();
        }
    }

    public void clear() {
        callsList.clear();
        notifyDataSetInvalidated();
    }

    @Override
    public int getGroupCount() {
        return callsList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return callsList.get(groupPosition).getUsersList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return callsList.get(groupPosition).getUsersList();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return callsList.get(groupPosition).getUsersList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_call_users_list, parent, false);
        }

        ((ExpandableListView) parent).expandGroup(groupPosition);

        TextView textView = convertView.findViewById(R.id.textViewCallId);
        textView.setText(callsList.get(groupPosition).getCallId().toString());

        Button button = convertView.findViewById(R.id.buttonJoinCall);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onJoinCallback != null) {
                    onJoinCallback.onUserClickJoin(callsList.get(groupPosition).getCallId());
                }
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_call_user, parent, false);
        }
        TextView textUserName = convertView.findViewById(R.id.textViewUserName);
        ImageView imageUserStatus = convertView.findViewById(R.id.imageViewUserStatus);

        UserModel userModel = callsList.get(groupPosition).getUsersList().get(childPosition);

        textUserName.setText(userModel.getUserUuid().toString());
        imageUserStatus.setImageResource(DrawableCompat.getStatusDrawable(userModel.isOnline(), userModel.isConnected()));

        return convertView;
    }



    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return callsList.isEmpty();
    }
}
