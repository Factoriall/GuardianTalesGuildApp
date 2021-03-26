package org.techtown.gtguildraid.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.BossHitCardAdapter;
import org.techtown.gtguildraid.adapters.RecordCardAdapter;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Record;
import org.techtown.gtguildraid.utils.AppExecutor;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StatisticMemberBasic2Fragment extends Fragment {
    private RoomDB database;
    private TextView totalDamage;
    private View view;

    private List<Record> recordList = new ArrayList<>();
    private int raidId;
    private int day;
    private int memberId;


    public static StatisticMemberBasic2Fragment newInstance(int counter, int raidId, int memberId) {
        StatisticMemberBasic2Fragment fragment = new StatisticMemberBasic2Fragment();
        Bundle args = new Bundle();
        args.putInt("day", counter);
        args.putInt("raidId", raidId);
        args.putInt("memberId", memberId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
            memberId = getArguments().getInt("memberId");
        }
        database = RoomDB.getInstance(getActivity());

        if(day != 0) {
            view = inflater.inflate(R.layout.fragment_statistic_member_basic_recycler, container, false);

            RecyclerView recyclerView = view.findViewById(R.id.recordRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            RecordCardAdapter adapter = new RecordCardAdapter();
            recyclerView.setAdapter(adapter);

            recordList = getReverseList();

            adapter.setItems(recordList);
            adapter.notifyDataSetChanged();

            TextView damageText = view.findViewById(R.id.damageText);
            damageText.setText(day + "일차 총 데미지");
            totalDamage = view.findViewById(R.id.totalDamage);
            setTotalDamage();
            return view;
        }
        else{
            view = inflater.inflate(R.layout.fragment_statistic_member_basic_all, container, false);

            TextView damage = view.findViewById(R.id.damage);
            TextView contribution = view.findViewById(R.id.contribution);
            TextView average = view.findViewById(R.id.average);
            TextView hitNum = view.findViewById(R.id.hitNum);

            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            BossHitCardAdapter adapter = new BossHitCardAdapter();
            recyclerView.setAdapter(adapter);

            ProgressDialog mProgressDialog = ProgressDialog.show(getContext(), "잠시 대기","데이터베이스 접속 중", true);

            AppExecutor.getInstance().diskIO().execute(() -> {
                List<Record> allRecords = database.recordDao().getAllRecordsWithExtra(raidId);
                List<Record> memberRecords = database.recordDao()
                        .get1MemberRecordsWithExtra(memberId, raidId);
                HashMap<Integer, Integer> bossCount = new HashMap<>();
                List<Boss> bosses = database.raidDao().getBossesList(raidId);
                for(Boss boss : bosses)
                    bossCount.put(boss.getBossId(), 0);

                int lastHitCount = 0;
                for(Record r : memberRecords){
                    int bossId = r.getBoss().getBossId();
                    bossCount.put(bossId, bossCount.get(bossId) + 1);

                    if(r.isLastHit())
                        lastHitCount++;
                }
                long allDamage = getDamageFromList(allRecords, false);
                long memberDamage = getDamageFromList(memberRecords, false);
                long averageDamage = getAverageFromList(memberRecords);

                int finalLastHitCount = lastHitCount;
                getActivity().runOnUiThread(() -> {
                    mProgressDialog.dismiss();

                    damage.setText(getNumberFormat(memberDamage));
                    contribution.setText(getPercentage(memberDamage, allDamage));
                    hitNum.setText(memberRecords.size() + " / " + finalLastHitCount);
                    average.setText(getNumberFormat(averageDamage));

                    adapter.setItems(bossCount);
                    adapter.notifyDataSetChanged();
                });
            });
            return view;
        }
    }

    private String getPercentage(long memberDamage, long allDamage) {
        if(allDamage == 0)
            return "0.00";
        return String.format("%.2f", memberDamage/(double)allDamage * 100);
    }

    private String getNumberFormat(long num) {
        return NumberFormat.getNumberInstance(Locale.US).format(num);
    }

    private long getDamageFromList(List<Record> records, boolean excludeLastHit) {
        long damage = 0;
        for(Record r: records) {
            if(excludeLastHit && r.isLastHit())
                continue;
            damage += r.getDamage();
        }

        return damage;
    }

    private long getAverageFromList(List<Record> records) {
        long damage = 0;
        int cnt = 0;
        for(Record r: records) {
            if(r.isLastHit())
                continue;
            damage += r.getDamage();
            cnt++;
        }

        return cnt == 0 ? 0 : damage / cnt;
    }

    private List<Record> getReverseList() {
        List<Record> list = database.recordDao().get1DayRecordsWithExtra(
                memberId, raidId, day);
        Collections.reverse(list);//ui에 맞게
        return list;
    }

    private void setTotalDamage() {
        long total = 0;
        for (Record record : recordList)
            total += record.getDamage();

        totalDamage.setText(NumberFormat.getNumberInstance(Locale.US).format(total));
    }
}
