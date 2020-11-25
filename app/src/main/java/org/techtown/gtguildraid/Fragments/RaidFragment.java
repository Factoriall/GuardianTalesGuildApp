package org.techtown.gtguildraid.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RaidFragment extends Fragment {
    final String dateFormat = "yyyy-MM-dd";

    List<Raid> raidList = new ArrayList<>();
    RoomDB database;
    Raid currentRaid;
    TextView isNotFound;
    TextView raidName;
    TextView raidTerm;
    LinearLayout raidInfo;
    Date today;
    Boolean isCurrentExist;
    Button raidButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_raid, container, false);
        isNotFound = view.findViewById(R.id.notFound);
        raidInfo = view.findViewById(R.id.raidInfo);
        raidName = view.findViewById(R.id.raidName);
        raidTerm = view.findViewById(R.id.raidTerm);

        database = RoomDB.getInstance(getActivity());
        today = new Date();
        isCurrentExist = database.raidDao().isCurrentRaidExist(today);

        raidButton = view.findViewById(R.id.raidButton);

        if(isCurrentExist) {
            currentRaid = database.raidDao().getCurrentRaidWithBosses(today);
            isNotFound.setVisibility(View.GONE);
            raidInfo.setVisibility(View.VISIBLE);

            raidName.setText(currentRaid.getName());
            Date aEnd = adjustEndTime(currentRaid.getEndDay());

            List<Boss> bosses = currentRaid.getBossList();
            for(int i=0; i<bosses.size(); i++){
                Log.d("bossName", bosses.get(i).getName());
            }
            for(int i=1; i<=4; i++){
                Resources res = getResources();
                int nameId = res.getIdentifier("boss" + i, "id", getContext().getPackageName());
                int barId = res.getIdentifier("progressBar" + i, "id", getContext().getPackageName());
                int hardnessId = res.getIdentifier("hardness" + i, "id", getContext().getPackageName());

                TextView bossName = view.findViewById(nameId);
                TextView hardness = view.findViewById(hardnessId);
                ProgressBar progressBar = view.findViewById(barId);

                bossName.setText(bosses.get(i-1).getName());
                hardness.setText("배율: " + bosses.get(i-1).getHardness());
                progressBar.setProgress((int)bosses.get(i-1).getHardness() * 10);
            }


            raidTerm.setText(new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()) +"~" +
                    new SimpleDateFormat(dateFormat).format(aEnd));
            raidButton.setText("수정");
        }
        else{
            isNotFound.setVisibility(View.VISIBLE);
            raidInfo.setVisibility(View.GONE);
            raidButton.setText("생성");
        }

        raidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRaid();
            }
        });

        return view;
    }

    private void updateRaid() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.dialog_raid);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.raidName);
        final EditText startDate = dialog.findViewById(R.id.startDate);

        if(isCurrentExist){
            name.setText(currentRaid.getName());
            startDate.setText(new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()));
        }
        else{
            startDate.setText(new SimpleDateFormat(dateFormat).format(today));
        }

        String[] sDate = startDate.getText().toString().trim().split("-");
        final Calendar myCalendar = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, Integer.parseInt(sDate[0]));
        myCalendar.set(Calendar.MONTH, Integer.parseInt(sDate[1])-1);
        myCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sDate[2]));

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                startDate.setText(sdf.format(myCalendar.getTime()));
            }
        };

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final int DAY_13 = (13 * 1000 * 60 * 60 * 24);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - DAY_13);
                dialog.show();
            }
        });



        Button updateButton = dialog.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = name.getText().toString().trim();
                Date sDate;
                Date eDate;
                try {
                    sDate = new SimpleDateFormat(dateFormat).parse(startDate.getText().toString().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sDate);
                    cal.add(Calendar.DATE, 14);
                    eDate = cal.getTime();
                    Date aEnd = adjustEndTime(eDate);

                    if(!sName.equals("")){
                        dialog.dismiss();

                        if(!isCurrentExist){//새로 생성
                            Raid raid = new Raid();
                            raid.setName(sName);
                            raid.setStartDay(sDate);
                            raid.setEndDay(eDate);

                            List<Boss> bosses = new ArrayList<>();
                            Log.d("currentExist", "새로 생성");
                            for(int i=1; i<=4; i++){
                                Boss boss = new Boss();
                                boss.setName("보스" + i);
                                boss.setHardness(1.0f);

                                bosses.add(boss);
                            }
                            database.raidDao().insertRaidWithBosses(raid, bosses);

                            isNotFound.setVisibility(View.GONE);
                            raidInfo.setVisibility(View.VISIBLE);
                            raidButton.setText("수정");
                            raidName.setText(sName);
                            raidTerm.setText(new SimpleDateFormat(dateFormat).format(sDate) +"~" +
                                    new SimpleDateFormat(dateFormat).format(aEnd));
                        }
                        else{//업데이트
                            database.raidDao().update(currentRaid.getRaidId(), sName, sDate, eDate);
                        }
                        raidName.setText(sName);
                        raidTerm.setText(new SimpleDateFormat(dateFormat).format(sDate) +"~" +
                                new SimpleDateFormat(dateFormat).format(aEnd));
                        //새로고침
                        currentRaid = database.raidDao().getCurrentRaidWithBosses(today);

                        raidList.clear();
                        raidList.addAll(database.raidDao().getAllRaids());
                    }
                    else{
                        showToast("이름을 입력하세요");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Date adjustEndTime(Date eDate) {
        Calendar end = Calendar.getInstance();
        end.setTime(eDate);
        end.add(Calendar.DATE, -1);

        return end.getTime();
    }

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
