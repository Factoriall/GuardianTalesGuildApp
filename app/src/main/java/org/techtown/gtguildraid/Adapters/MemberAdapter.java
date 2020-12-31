package org.techtown.gtguildraid.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.R;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<GuildMember> memberList;

    public MemberAdapter(List<GuildMember> memberList){
        this.memberList = memberList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_member, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        GuildMember member = memberList.get(position);
        holder.setItem(member);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nickname;
        TextView remark;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.nickname);
            remark = itemView.findViewById(R.id.remark);
        }

        public void setItem(GuildMember member){
            nickname.setText(member.getName());
            remark.setText(member.getRemark());
        }
    }
}
