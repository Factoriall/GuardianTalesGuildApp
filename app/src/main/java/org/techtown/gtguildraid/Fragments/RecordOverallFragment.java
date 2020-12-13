package org.techtown.gtguildraid.Fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordOverallFragment extends Fragment {
    final int MAX_DURATION = 14;
    RoomDB database;

    Raid raid;
    int memberId;
    int raidId;
    boolean isAdjustMode;

    public RecordOverallFragment() {
        // Required empty public constructor
    }

    public static RecordOverallFragment newInstance(int memberId, int raidId, boolean isChecked) {
        RecordOverallFragment fragment = new RecordOverallFragment();
        Bundle args = new Bundle();
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_overall, container, false);
        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            isAdjustMode = getArguments().getBoolean("isChecked");
        }

        database = RoomDB.getInstance(getActivity());
        raid = database.raidDao().getRaid(raidId);

        List<Record> memberRecords = database.recordDao().getCertainMemberRecordsWithBoss(memberId, raidId);
        int recordCount = memberRecords.size();
        int raidCount = getRaidCountFromToday();
        TextView hitCount = view.findViewById(R.id.hitCount);
        hitCount.setText(recordCount + "/" + raidCount);

        int totalDamageInt = 0;
        for(Record record : memberRecords) {
            Log.d("record", Integer.toString(record.getBossId()));
            if (isAdjustMode)
                totalDamageInt += (int) (record.getDamage() * record.getBoss().getHardness());
            else
                totalDamageInt += record.getDamage();
        }
        TextView totalDamage = view.findViewById(R.id.totalDamage);
        totalDamage.setText(NumberFormat.getNumberInstance(Locale.US).format(totalDamageInt));

        List<Boss> bosses = database.raidDao().getBossesList(raidId);

        TextView[] bossHitNumList = new TextView[4];
        ImageView[] bossImageList = new ImageView[4];
        TextView[] vsBossList = new TextView[4];
        TextView[] bossDamageList = new TextView[4];

        int idx = 1;
        for(Boss boss : bosses){
            Resources res = getResources();
            int hitId = res.getIdentifier("boss" + idx + "HitNum", "id", getContext().getPackageName());
            int imageId = res.getIdentifier("boss" + idx + "Image", "id", getContext().getPackageName());
            int textId = res.getIdentifier("vsBoss" + idx, "id", getContext().getPackageName());
            int damageId = res.getIdentifier("boss" + idx + "Damage", "id", getContext().getPackageName());

            bossHitNumList[idx-1] = view.findViewById(hitId);
            bossImageList[idx-1] = view.findViewById(imageId);
            vsBossList[idx-1] = view.findViewById(textId);
            bossDamageList[idx-1] = view.findViewById(damageId);

            List<Record> bossRecords = database.recordDao()
                    .getCertainBossRecordsWithBoss(memberId, raidId, boss.getBossId());
            int bossHitNum = bossRecords.size();
            bossHitNumList[idx-1].setText(Integer.toString(bossHitNum));
            bossImageList[idx-1].setImageResource(boss.getImageId());
            vsBossList[idx-1].setText("vs " + boss.getName());

            int bossDamageInt = 0;
            for(Record record : bossRecords) {
                if(isAdjustMode)
                    bossDamageInt += (int) (record.getDamage() * record.getBoss().getHardness());
                else
                    bossDamageInt += record.getDamage();
            }
            bossDamageList[idx - 1].setText(NumberFormat.getNumberInstance(Locale.US).format(bossDamageInt));

            idx++;
        }

        return view;
    }

    private int getRaidCountFromToday() {
        Date today = new Date();
        Date startDate = raid.getStartDay();

        int differentDays = (int) ((today.getTime() - startDate.getTime()) / (1000 * 3600 * 24));

        if (differentDays < 0)
            return 0;
        if (differentDays >= MAX_DURATION)
            return MAX_DURATION * 3;

        return (differentDays + 1) * 3;
    }
}
