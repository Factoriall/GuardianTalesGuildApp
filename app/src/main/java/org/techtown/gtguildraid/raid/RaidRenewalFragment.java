package org.techtown.gtguildraid.raid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.common.HeroBottomSheetDialog;
import org.techtown.gtguildraid.models.entities.Boss;
import org.techtown.gtguildraid.models.entities.Hero;
import org.techtown.gtguildraid.models.entities.Raid;
import org.techtown.gtguildraid.repository.RoomDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class RaidRenewalFragment
        extends Fragment
        implements RaidBossRecyclerAdapter.RecyclerViewListener,
        HeroBottomSheetDialog.BottomSheetListener {
    private static final long DAY_13 = 60*60*24*13*1000;
    final String dateFormat = "yy/MM/dd";
    ViewGroup view;
    RoomDB database;
    Raid currentRaid;
    TextView isNotFound;
    CardView currentRaidCard;
    RecyclerView bossRecyclerView;
    RaidBossRecyclerAdapter adapter;
    ConstraintLayout bossTab;
    ImageView raidThumbnailDialog;
    ImageView raidThumbnail;

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
        raidThumbnail = view.findViewById(R.id.raidThumbnail);
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

    private void updateRaid(){
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.dialog_raid_renewal);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        EditText raidNameDialog = dialog.findViewById(R.id.raidName);
        EditText raidTermDialog = dialog.findViewById(R.id.raidTerm);
        raidThumbnailDialog = dialog.findViewById(R.id.raidThumbnail);

        Date defaultDate = today;
        if (isCurrentExist) {
            raidNameDialog.setText(currentRaid.getName());
            raidThumbnailDialog.setImageResource(
                    getResources().getIdentifier(
                            "character_" + currentRaid.getThumbnail(),
                            "drawable",
                            requireContext().getPackageName())
            );
            raidThumbnailDialog.setTag(0xffffffff, currentRaid.getThumbnail());
            defaultDate = currentRaid.getStartDay();
        }
        else {
            raidThumbnailDialog.setImageResource(
                    getResources().getIdentifier(
                            "character_normal",
                            "drawable",
                            requireContext().getPackageName())
            );
            raidThumbnailDialog.setTag(0xffffffff,"normal");
        }
        raidTermDialog.setText(setRaidTerm(defaultDate));

        Calendar myCalendar = Calendar.getInstance();
        myCalendar.setTime(defaultDate);
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            myCalendar.set(Calendar.HOUR_OF_DAY, 0);
            myCalendar.set(Calendar.MINUTE, 0);
            myCalendar.set(Calendar.SECOND, 0);

            raidTermDialog.setText(setRaidTerm(myCalendar.getTime()));
        };

        raidTermDialog.setOnClickListener(v -> {
            if(isCurrentExist &&
                    database.raidDao().getCurrentRaid(today).getStartDay().compareTo(new Date()) <= 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("현재 날짜 기준 레이드가 이미 시작한 상태입니다.\n- 변경 시 예상 부작용\n" +
                                "* 현 레이드 기록이 일찍 종료할 수 있습니다.\n" +
                                "* N일차 기록이 어긋날 수 있습니다.\n" +
                                "* 통계 기록이 정확히 표시되지 않을 수 있습니다.\n\n" +
                                "그래도 변경하시겠습니까?")
                                .setPositiveButton("네", (dialog1, id) -> {
                                    dialog1.dismiss();
                                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                                            getActivity(),
                                            date,
                                            myCalendar.get(Calendar.YEAR),
                                            myCalendar.get(Calendar.MONTH),
                                            myCalendar.get(Calendar.DAY_OF_MONTH));

                                    datePickerDialog.getDatePicker().setMinDate(
                                            System.currentTimeMillis() - DAY_13);
                                    datePickerDialog.getDatePicker().setMaxDate(
                                            System.currentTimeMillis() + DAY_13);
                                    datePickerDialog.setMessage("시작 날짜를 입력해주세요.");
                                    datePickerDialog.show();
                                })
                                .setNegativeButton("아니오", (dialog1, id) -> {
                                    dialog1.dismiss();
                                });
                AlertDialog alert = builder.create();
                alert.setTitle("날짜 이전 경고");
                alert.show();
            }
            else if(isCurrentExist){
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(),
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.getDatePicker().setMinDate(
                        System.currentTimeMillis() - DAY_13);
                datePickerDialog.getDatePicker().setMaxDate(
                        System.currentTimeMillis() + DAY_13);
                datePickerDialog.setMessage("시작 날짜를 입력해주세요.");
                datePickerDialog.show();
            }
        });

        raidThumbnailDialog.setOnClickListener(view1 -> {
            HeroBottomSheetDialog bottomDialog = new HeroBottomSheetDialog(this);
            bottomDialog.show(requireActivity().getSupportFragmentManager(), "bottomSheetDialog");
        });

        Button updateButton = dialog.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(view -> {
            String sName = raidNameDialog.getText().toString().trim();
            Date sDate = myCalendar.getTime();
            Date eDate = getEndDate(sDate);
            String sThumbnail = (String) raidThumbnailDialog.getTag(0xffffffff);
            if (!sName.equals("")) {
                dialog.dismiss();

                if (!isCurrentExist) {//새로 생성
                    Raid raid = new Raid();
                    raid.setName(sName);
                    raid.setStartDay(sDate);
                    raid.setEndDay(eDate);
                    raid.setThumbnail(sThumbnail);
                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
                    editor.putInt("currentRound", 0);
                    editor.putString("recentWrite", "없음");
                    editor.apply();

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
                    isCurrentExist = true;
                } else {//업데이트
                    database.raidDao().updateRaid(currentRaid.getRaidId(), sName, sDate, eDate, sThumbnail);
                }
                setCurrentRaidView();
            } else Toast.makeText(getContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String setRaidTerm(Date date) {
        return getString(R.string.raid_term,
                new SimpleDateFormat(dateFormat).format(date),
                new SimpleDateFormat(dateFormat).format(getDate(date, 13)));
    }

    private Date getDate(Date sDate, int addDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        cal.add(Calendar.DATE, addDays);
        return cal.getTime();
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
        raidThumbnail.setImageResource(getResources().getIdentifier(
                "character_" + currentRaid.getThumbnail(),
                "drawable",
                requireContext().getPackageName()));

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

    @Override
    public void onImageClicked(Hero hero) {
        Log.d("raidThumbnail", hero.getEnglishName());

        raidThumbnailDialog.setTag(0xffffffff, hero.getEnglishName());
        Log.d("raidThumbnail", (String) raidThumbnailDialog.getTag(0xffffffff));

        raidThumbnailDialog.setImageResource(
                getResources().getIdentifier("character_" + hero.getEnglishName() , "drawable", requireContext().getPackageName()));
    }
}
