package org.techtown.gtguildraid.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<GuildMember> memberList;
    private Activity context;
    private RoomDB database;


    public MemberAdapter(Activity context, List<GuildMember> memberList){
        this.context = context;
        this.memberList = memberList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_member, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        GuildMember member = memberList.get(position);
        database = RoomDB.getInstance(context);
        holder.setItem(member);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nickname;
        TextView remark;
        ImageView editButton, deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.raidName);
            remark = itemView.findViewById(R.id.remark);
            //editButton = itemView.findViewById(R.id.editButton);
            //deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void setItem(GuildMember member){
            nickname.setText(member.getName());
            remark.setText(member.getRemark());
        }
    }
}
