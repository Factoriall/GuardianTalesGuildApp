package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.gtguildraid.R;

public class RecordOverallFragment extends Fragment {
    public RecordOverallFragment() {
        // Required empty public constructor
    }

    public static RecordOverallFragment newInstance(int memberId, int raidId) {
        RecordOverallFragment fragment = new RecordOverallFragment();
        Bundle args = new Bundle();
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_overall, container, false);
        return view;
    }
}
