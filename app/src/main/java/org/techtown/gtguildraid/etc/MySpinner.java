package org.techtown.gtguildraid.etc;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class MySpinner extends androidx.appcompat.widget.AppCompatSpinner {
    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position) {
        Log.d("setSelection", "" + position);
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            Log.d("setSelection", "sameSelected");
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}
