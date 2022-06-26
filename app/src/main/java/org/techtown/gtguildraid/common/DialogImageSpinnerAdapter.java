package org.techtown.gtguildraid.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.techtown.gtguildraid.R;

import java.util.List;

public class DialogImageSpinnerAdapter extends ArrayAdapter<String> {
    private final Context ctx;
    private final List<String> contentArray;
    private final List<Integer> imageArray;

    public DialogImageSpinnerAdapter(Context context, int resource, List<String> objects,
                                     List<Integer> imageArray) {
        super(context, resource, R.id.spinnerTextView, objects);
        this.ctx = context;
        this.contentArray = objects;
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
        View row = inflater.inflate(R.layout.spinner_value_layout, parent, false);

        TextView textView = row.findViewById(R.id.spinnerTextView);
        textView.setText(contentArray.get(position));

        ImageView imageView = row.findViewById(R.id.spinnerImages);
        int imageId = imageArray.get(position);
        if(imageId != 0)
            imageView.setImageResource(imageId);

        return row;
    }
}
