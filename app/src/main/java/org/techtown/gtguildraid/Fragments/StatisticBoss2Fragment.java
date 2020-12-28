package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.List;

public class StatisticBoss2Fragment extends Fragment {
    final int BOSS_1 = 0;
    final int BOSS_2 = 1;
    final int BOSS_3 = 2;
    final int BOSS_4 = 3;
    final int DAY_IN_SECONDS = 1000 * 3600 * 24;
    final int MAX_DURATION = 14;

    RoomDB database;
    int raidId;
    int position;
    boolean isAdjustMode;

    public static StatisticBoss2Fragment newInstance(int position, int raidId, boolean isChecked) {
        StatisticBoss2Fragment fragment = new StatisticBoss2Fragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_boss_2, container, false);
        database = RoomDB.getInstance(getActivity());
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            isAdjustMode = getArguments().getBoolean("isChecked");
            position = getArguments().getInt("position");
        }

        //setView(position);
        
        return view;
    }

    private void setView(int position) {
        List<Boss> bosses = database.raidDao().getBossesList(raidId);
        switch(position){
            case BOSS_1:
                setBossData(bosses.get(0));
                break;
            case BOSS_2:
                setBossData(bosses.get(1));
                break;
            case BOSS_3:
                setBossData(bosses.get(2));
                break;
            case BOSS_4:
                setBossData(bosses.get(3));
                break;
        }
    }

    private void setBossData(Boss boss) {
    }
}
