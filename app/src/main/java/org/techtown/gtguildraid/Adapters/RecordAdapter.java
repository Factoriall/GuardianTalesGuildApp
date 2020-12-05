package org.techtown.gtguildraid.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;

    public RecordAdapter(Activity context, List<Record> recordList) {
        this.recordList = recordList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.fragment_record_recycler, parent, false);

        return new RecordAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordAdapter.ViewHolder holder, int position) {
        Record record = recordList.get(position);
        holder.setItem(record);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void setItems(List<Record> recordList) {
        this.recordList = recordList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText damage;
        ImageView[] heroes;
        TextView bossName;
        RoomDB database;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            damage = itemView.findViewById(R.id.damage);
            context = itemView.getContext();
            database = RoomDB.getInstance(itemView.getContext());

            heroes = new ImageView[4];
            bossName = itemView.findViewById(R.id.bossName);

            for (int i = 1; i <= 4; i++) {
                Resources res = context.getResources();
                int heroId = res.getIdentifier("hero" + i, "id", context.getPackageName());

                heroes[i - 1] = itemView.findViewById(heroId);
            }

        }

        public void setItem(Record record) {
            damage.setText(Integer.toString(record.getDamage()));
            bossName.setText(record.getBoss().getName());

            List<Hero> heroList = new ArrayList<>();
            List<Integer> heroIds = record.getHeroIds();
            for(int heroId : heroIds){
                heroList.add(database.heroDao().getHero(heroId));
            }

            for (int i = 0; i < 4; i++) {
                String name = heroList.get(i).getEnglishName();

                int imageId = context.getResources().getIdentifier("character_" + name, "id", context.getPackageName());
                heroes[i].setImageResource(imageId);
            }
        }
    }
}
