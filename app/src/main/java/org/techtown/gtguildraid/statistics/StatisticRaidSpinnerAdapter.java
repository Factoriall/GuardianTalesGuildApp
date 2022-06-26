package org.techtown.gtguildraid.statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.techtown.gtguildraid.R;

import java.util.List;

public class StatisticRaidSpinnerAdapter extends ArrayAdapter<String> {
    private final Context ctx;
    private final List<String> raidArray;
    private final List<Integer> imageArray;

    public StatisticRaidSpinnerAdapter(Context context, List<String> objects,
                                     List<Integer> imageArray) {
        super(context, R.layout.spinner_statistic_raid, R.id.spinnerTextView, objects);
        this.ctx = context;
        this.raidArray = objects;
        this.imageArray = imageArray;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    public View getCustomView(int position, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.spinner_statistic_raid, parent, false);

        TextView raidName = view.findViewById(R.id.raidName);
        TextView raidTerm = view.findViewById(R.id.raidTerm);
        ImageView raidThumbnail = view.findViewById(R.id.raidThumbnail);

        String[] raidInfo = raidArray.get(position).split("_");
        String raidTermText = raidInfo[raidInfo.length - 1];
        String raidNameText = "";
        for(int i = 0; i < raidInfo.length - 1; i++){
            raidNameText += raidInfo[i];
            if(i != raidInfo.length - 2) raidNameText += "_";
        }

        raidName.setText(raidNameText);
        raidTerm.setText(raidTermText);

        int imageId = imageArray.get(position);
        if(imageId != 0)
            raidThumbnail.setImageResource(imageId);

        return view;
    }
}
