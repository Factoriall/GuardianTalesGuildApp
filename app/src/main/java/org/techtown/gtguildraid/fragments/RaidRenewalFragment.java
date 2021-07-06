package org.techtown.gtguildraid.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.activities.BossEditActivity;
import org.techtown.gtguildraid.adapters.RaidBossRecyclerAdapter;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class RaidRenewalFragment extends Fragment implements RaidBossRecyclerAdapter.RecyclerViewListener {
    final String dateFormat = "yy/MM/dd";
    ViewGroup view;
    RoomDB database;
    Raid currentRaid;
    TextView isNotFound;
    CardView currentRaidCard;
    RecyclerView bossRecyclerView;
    RaidBossRecyclerAdapter adapter;
    ConstraintLayout bossTab;

    TextView raidName;
    TextView raidTerm;

    Date today;
    Boolean isCurrentExist;
    Button raidButton;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", -1);
                Boss updated = database.raidDao().getCurrentRaidWithBosses(today)
                        .getBossList().get(position);
                Log.d("BossInfo", updated.getName() + " " + updated.getElementId());
                adapter.updateItem(updated, position);
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_raid_renewal, container, false);
        isNotFound = view.findViewById(R.id.notFound);
        raidName = view.findViewById(R.id.raidName);
        raidTerm = view.findViewById(R.id.raidTerm);
        currentRaidCard = view.findViewById(R.id.currentRaidCard);
        bossRecyclerView = view.findViewById(R.id.recyclerView);
        bossTab = view.findViewById(R.id.bossTab);

        database = RoomDB.getInstance(getActivity());
        today = new Date();
        isCurrentExist = database.raidDao().isCurrentRaidExist(today);

        raidButton = view.findViewById(R.id.raidButton);
        raidButton.setOnClickListener(view -> updateRaid());

        if (isCurrentExist) {
            setCurrentRaidView();
        } else {
            isNotFound.setVisibility(View.VISIBLE);
            currentRaidCard.setVisibility(View.GONE);
            bossRecyclerView.setVisibility(View.GONE);
            bossTab.setVisibility(View.GONE);
            raidButton.setText("생성");
        }
        return view;
    }

    @SuppressLint("SimpleDateFormat")
    private void updateRaid() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.dialog_raid);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        EditText raidName = dialog.findViewById(R.id.raidName);
        ImageView raidThumbnail = dialog.findViewById(R.id.raidThumbnail);
        EditText startDate = dialog.findViewById(R.id.startDate);
        Date defaultDate = today;
        if (isCurrentExist) {
            raidName.setText(currentRaid.getName());
            //raidThumbnail.setImageResource();
            defaultDate = currentRaid.getStartDay();
        }
        startDate.setText(new SimpleDateFormat(dateFormat).format(defaultDate));

        Calendar myCalendar = Calendar.getInstance();
        myCalendar.setTime(defaultDate);
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            startDate.setText(new SimpleDateFormat(dateFormat).format(myCalendar.getTime()));
        };

        startDate.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            final int DAY_13 = (13 * 1000 * 60 * 60 * 24);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    date,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - DAY_13);
            datePickerDialog.show();
        });

        Button updateButton = dialog.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(view -> {
            String sName = raidName.getText().toString().trim();
            Date sDate = myCalendar.getTime();
            Date eDate = getEndDate(sDate);
            if (!sName.equals("")) {
                dialog.dismiss();

                if (!isCurrentExist) {//새로 생성
                    Raid raid = new Raid();
                    raid.setName(sName);
                    raid.setStartDay(sDate);
                    raid.setEndDay(eDate);

                    List<Boss> bosses = new ArrayList<>();
                    for (int i = 1; i <= 4; i++) {
                        Boss boss = new Boss();
                        boss.setName("보스" + i);
                        boss.setHardness(1.0f);
                        String imageName = Integer.toString(i);
                        boss.setImgName(imageName);
                        boss.setFurious(false);

                        bosses.add(boss);
                    }
                    database.raidDao().insertRaidWithBosses(raid, bosses);
                } else {//업데이트
                    database.raidDao().updateRaid(currentRaid.getRaidId(), sName, sDate, eDate);
                }
                setCurrentRaidView();
            } else {
                //showToast("이름을 입력하세요");
            }

        });
    }

    private Date getEndDate(Date sDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        cal.add(Calendar.DATE, 15);
        return cal.getTime();
    }

    private void setCurrentRaidView() {
        raidButton.setText("수정");
        currentRaid = database.raidDao().getCurrentRaidWithBosses(today);
        isNotFound.setVisibility(View.GONE);
        currentRaidCard.setVisibility(View.VISIBLE);
        bossRecyclerView.setVisibility(View.VISIBLE);
        bossTab.setVisibility(View.VISIBLE);

        raidName.setText(currentRaid.getName());
        Date aEnd = adjustEndTime(currentRaid.getEndDay());
        raidTerm.setText(getString(R.string.raid_term,
                new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()),
                new SimpleDateFormat(dateFormat).format(aEnd)));
        raidButton.setText("수정");

        List<Boss> bosses = currentRaid.getBossList();
        bossRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RaidBossRecyclerAdapter(bosses, this);
        bossRecyclerView.setAdapter(adapter);
    }

    private Date adjustEndTime(Date eDate) {
        Calendar end = Calendar.getInstance();
        end.setTime(eDate);
        end.add(Calendar.DATE, -2);

        return end.getTime();
    }

    @Override
    public void onEditButtonClicked(int bossId, int position) {
        Intent intent = new Intent(getContext(), BossEditActivity.class);
        intent.putExtra("bossId", bossId);
        intent.putExtra("position", position);
        startActivityForResult(intent, 101);
    }
}
