package com.andreydymko.spoaudiocalls.Ui.UsersListRecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Utils.CollectionsCompat;

import java.util.ArrayList;
import java.util.List;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListViewHolder> {
    private final List<UserModel> dataSet;
    private ItemClickListener itemClickListener;

    public UsersListAdapter(ItemClickListener listener) {
        this.dataSet = new ArrayList<>();
        this.itemClickListener = listener;
    }

    public UsersListAdapter() {
        this.dataSet = new ArrayList<>();
        this.itemClickListener = null;
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void removeOnItemClickListener() {
        this.itemClickListener = null;
    }

    @NonNull
    @Override
    public UsersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_call_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersListViewHolder holder, int position) {
        final UserModel userModel = dataSet.get(position);
        holder.initView(userModel, itemClickListener);
    }

    public void addUser(UserModel userModel) {
        int idx = CollectionsCompat.addUniqueUpdating(dataSet, userModel);

        if (idx == dataSet.size() - 1) {
            notifyItemInserted(idx);
        } else {
            notifyItemChanged(idx);
        }
    }

    public void removeUser(UserModel userModel) {
        int userIdx = dataSet.indexOf(userModel);
        if (userIdx >= 0) {
            dataSet.remove(userIdx);
            notifyItemRemoved(userIdx);
        }
    }

    public void clear() {
        dataSet.clear();
        notifyItemRangeRemoved(0, dataSet.size() - 1);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public interface ItemClickListener {
        void onItemClicked(UserModel userModel);
    }
}
