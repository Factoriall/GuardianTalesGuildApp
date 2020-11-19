package org.techtown.gtguildraid;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<GuildMember> memberList = new ArrayList<>();
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
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuildMember m = memberList.get(holder.getAdapterPosition());
                final int sID = m.getID();
                String sName = m.getName();
                String sRemark = m.getRemark();

                final Dialog dialog = new Dialog(context);

                dialog.setContentView(R.layout.dialog_update);

                int width = WindowManager.LayoutParams.MATCH_PARENT;
                int height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.show();

                final EditText name = dialog.findViewById(R.id.name);
                final EditText remark = dialog.findViewById(R.id.remark);
                Button updateButton = dialog.findViewById(R.id.updateButton);

                name.setText(sName);
                remark.setText(sRemark);

                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        String uName = name.getText().toString().trim();
                        String uRemark = remark.getText().toString().trim();

                        database.memberDao().update(sID, uName, uRemark);

                        memberList.clear();
                        memberList.addAll(database.memberDao().getAll());
                        notifyDataSetChanged();
                    }
                });

            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuildMember m = memberList.get(holder.getAdapterPosition());

                database.memberDao().delete(m);

                int position = holder.getAdapterPosition();
                memberList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, memberList.size());
            }
        });

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

            nickname = itemView.findViewById(R.id.myName);
            remark = itemView.findViewById(R.id.remark);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void setItem(GuildMember member){
            nickname.setText(member.getName());
            remark.setText(member.getRemark());
        }
    }
}
