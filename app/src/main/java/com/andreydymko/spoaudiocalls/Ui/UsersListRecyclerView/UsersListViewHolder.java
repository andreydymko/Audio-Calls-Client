package com.andreydymko.spoaudiocalls.Ui.UsersListRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andreydymko.spoaudiocalls.R;
import com.andreydymko.spoaudiocalls.SPONetworking.Users.UserModel;
import com.andreydymko.spoaudiocalls.Ui.Compat.DrawableCompat;

public class UsersListViewHolder extends RecyclerView.ViewHolder{

    public View view;
    public TextView textUserName;
    public ImageView imageUserStatus;

    public UsersListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
        this.textUserName = itemView.findViewById(R.id.textViewUserName);
        this.imageUserStatus = itemView.findViewById(R.id.imageViewUserStatus);
    }

    public void initView(final UserModel userModel, final UsersListAdapter.ItemClickListener clickListener) {
        textUserName.setText(userModel.getUserUuid().toString());
        imageUserStatus.setImageResource(DrawableCompat.getStatusDrawable(userModel.isOnline(), userModel.isConnected()));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClicked(userModel);
                }
            }
        });
    }
}
