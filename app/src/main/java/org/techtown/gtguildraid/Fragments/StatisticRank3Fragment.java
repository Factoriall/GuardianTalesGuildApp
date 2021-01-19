package org.techtown.gtguildraid.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.Adapters.StatisticRankCardAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.GuildMember;
import org.techtown.gtguildraid.Models.RankInfo;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticRank3Fragment extends Fragment {
    RoomDB database;
    private int raidId;
    private int bossPosition;
    private int levelPosition;
    private boolean isAverageMode;
    private boolean isAdjustMode;
    StatisticRankCardAdapter adapter;

    public static Fragment newInstance(int raidId, int bossPosition, int levelPosition, boolean isAverageMode, boolean isAdjustMode) {
        StatisticRank3Fragment fragment = new StatisticRank3Fragment();
        Bundle args = new Bundle();
        args.putInt("raidId", raidId);
        args.putInt("bossPosition", bossPosition);
        args.putInt("levelPosition", levelPosition);
        args.putBoolean("isAverageMode", isAverageMode);
        args.putBoolean("isAdjustMode", isAdjustMode);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic_rank_3, container, false);

        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            bossPosition = getArguments().getInt("bossPosition");
            levelPosition = getArguments().getInt("levelPosition");
            isAverageMode = getArguments().getBoolean("isAverageMode");
            isAdjustMode =  getArguments().getBoolean("isAdjustMode", false);
        }

        database = RoomDB.getInstance(getActivity());

        int[] rounds = {1, 7, 12, 17, 22};

        List<RankInfo> rankInfos = new ArrayList<>();
        for(GuildMember m : database.memberDao().getAllMembers()) {
            if(database.recordDao().getCertainMemberRecords(m.getID(), raidId).size() == 0)
                continue;

            List<Record> recordList;
            if(bossPosition != 0) {
                recordList = database.recordDao().getMemberRoundRecordsWithOneBossAndLeader(
                        m.getID(),
                        raidId,
                        database.raidDao().getBossesList(raidId).get(bossPosition-1).getBossId(),
                        rounds[levelPosition]);
            }
            else{
                recordList = database.recordDao().getCertainMemberRoundRecordsWithOneBossAndLeader(
                        m.getID(),
                        raidId,
                        rounds[levelPosition]);
            }

            RankInfo ri = new RankInfo(m.getName(), recordList.size());

            for(Record r : recordList) {
                if(isAdjustMode) {
                    ri.addDamage((int) (r.getDamage() * r.getBoss().getHardness()));
                }
                else
                    ri.addDamage(r.getDamage());
            }

            ri.setFinalDamage(isAverageMode);
            rankInfos.add(ri);
        }

        Collections.sort(rankInfos);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new StatisticRankCardAdapter();
        adapter.setItems(rankInfos);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
