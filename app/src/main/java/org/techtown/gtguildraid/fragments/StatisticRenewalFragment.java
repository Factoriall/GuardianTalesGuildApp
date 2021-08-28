package org.techtown.gtguildraid.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.activities.StatisticRaidActivity;
import org.techtown.gtguildraid.adapters.StatisticRaidCardAdapter;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticRenewalFragment
        extends Fragment
        implements StatisticRaidCardAdapter.RecyclerViewListener {
    View view;
    RoomDB database;
    List<Raid> pastRaids;
    StatisticRaidCardAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistic_renewal, container, false);
        database = RoomDB.getInstance(getContext());

        setCurrentRaidView();
        setPastRaidsView();

        return view;
    }

    private void setCurrentRaidView() {
        final String dateFormat = "yy/MM/dd";
        TextView currentName = view.findViewById(R.id.raidName);
        TextView currentTerm = view.findViewById(R.id.raidTerm);
        ImageView currentThumbnail = view.findViewById(R.id.raidThumbnail);
        TextView isNotFound = view.findViewById(R.id.isNotFound);
        Raid currentRaid = database.raidDao().getCurrentRaid(new Date());
        if(currentRaid != null)
            Log.d("currentRaid", new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()));
        if(currentRaid == null || currentRaid.getStartDay().getTime() - System.currentTimeMillis() >= 0){
            isNotFound.setVisibility(View.VISIBLE);
            currentName.setVisibility(View.GONE);
            currentTerm.setVisibility(View.GONE);
            currentThumbnail.setVisibility(View.GONE);
            return;
        }
        isNotFound.setVisibility(View.GONE);
        currentName.setVisibility(View.VISIBLE);
        currentTerm.setVisibility(View.VISIBLE);
        currentThumbnail.setVisibility(View.VISIBLE);
        currentName.setText(currentRaid.getName());
        currentTerm.setText(new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()) + "~" +
                new SimpleDateFormat(dateFormat).format(getEndTime(currentRaid.getStartDay())));
        currentThumbnail.setImageResource(getResources().getIdentifier(
                "character_" + currentRaid.getThumbnail(),
                "drawable",
                getContext().getPackageName()
        ));

        CardView raidCard = view.findViewById(R.id.raidCard);
        raidCard.setOnClickListener(v1 ->{
            startStatisticActivity(currentRaid.getRaidId());
        });
    }

    private void setPastRaidsView() {
        pastRaids = database.raidDao().getAllRaidsExceptRecent(new Date());
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(requireActivity());
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        recyclerView.setLayoutManager(lm);

        adapter = new StatisticRaidCardAdapter(this, pastRaids);
        recyclerView.setAdapter(adapter);
    }

    private void startStatisticActivity(int raidId) {
        Intent intent = new Intent(getContext(), StatisticRaidActivity.class);
        intent.putExtra("raidId", raidId);
        startActivity(intent);
    }

    private Date getEndTime(Date day) {
        Calendar end = Calendar.getInstance();
        end.setTime(day);
        end.add(Calendar.DATE, 13);

        return end.getTime();
    }

    @Override
    public void onDeleteClicked(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("삭제 진행 시 관련 데이터도 같이 삭제되며 복구가 불가능합니다. 그래도 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("네", (dialog, id) -> {
                    database.raidDao().delete(pastRaids.get(position));
                    pastRaids.remove(position);
                    adapter.removeRaid(position);
                })
                .setNegativeButton("아니오", (dialog, id) -> {
                    dialog.dismiss();
                });
        AlertDialog alert = builder.create();
        alert.setTitle("길드 레이드 삭제");
        alert.show();
    }
}
