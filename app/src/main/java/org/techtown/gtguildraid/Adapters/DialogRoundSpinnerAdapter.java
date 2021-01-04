package org.techtown.gtguildraid.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.techtown.gtguildraid.R;

import java.util.List;

public class DialogRoundSpinnerAdapter extends ArrayAdapter<String> {
    private Context ctx;
    private List<String> contentArray;

    public DialogRoundSpinnerAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, R.id.spinnerTextView, objects);
        this.ctx = context;
        this.contentArray = objects;
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
        View row = inflater.inflate(R.layout.spinner_value_layout2, parent, false);

        TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
        textView.setText(contentArray.get(position));

        return row;
    }
}