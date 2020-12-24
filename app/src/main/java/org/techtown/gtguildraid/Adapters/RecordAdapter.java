package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;
    private static boolean isChecked;
    
    public RecordAdapter(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @NonNull
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_record, parent, false);

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
        TextView damage;
        TextView level;
        TextView hardness;
        ImageView bossImage;
        //ImageView[] heroes;
        LinearLayout adjustLayout;
        TextView bossName;
        RoomDB database;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            database = RoomDB.getInstance(itemView.getContext());

            //heroes = new ImageView[4];
            damage = itemView.findViewById(R.id.damage);
            level = itemView.findViewById(R.id.level);
            hardness = itemView.findViewById(R.id.hardness);
            bossName = itemView.findViewById(R.id.bossName);
            bossImage = itemView.findViewById(R.id.bossImage);
            adjustLayout = itemView.findViewById(R.id.adjustLayout);
            /*
            for (int i = 1; i <= 4; i++) {
                int heroId = context.getResources()
                        .getIdentifier("hero" + i, "id", context.getPackageName());

                heroes[i - 1] = itemView.findViewById(heroId);
            }*/
        }

        public void setItem(Record record) {
            hardness.setText(new DecimalFormat("#.#").format(record.getBoss().getHardness()));
            level.setText(Integer.toString(record.getLevel()));
            bossName.setText(record.getBoss().getName());
            bossImage.setImageResource(record.getBoss().getImageId());
            
            if(isChecked){
                adjustLayout.setVisibility(View.VISIBLE);
                hardness.setText(new DecimalFormat("#.#").format(record.getBoss().getHardness()));
                damage.setText(NumberFormat.getNumberInstance(Locale.US).format((int)(record.getDamage() * record.getBoss().getHardness())));
            }
            else{
                adjustLayout.setVisibility(View.INVISIBLE);
                hardness.setText(new DecimalFormat("#.#").format(record.getBoss().getHardness()));
                damage.setText(NumberFormat.getNumberInstance(Locale.US).format(record.getDamage()));
            }

            /*
            List<Hero> heroList = new ArrayList<>();
            heroList.add(record.getHero1());
            heroList.add(record.getHero2());
            heroList.add(record.getHero3());
            heroList.add(record.getHero4());

            for (int i = 0; i < 4; i++) {
                String name = heroList.get(i).getEnglishName();
                Log.d("heroName", name);

                int imageId = context.getResources()
                        .getIdentifier("character_" + name, "drawable", context.getPackageName());
                heroes[i].setImageResource(imageId);
            }*/
        }
    }
}
