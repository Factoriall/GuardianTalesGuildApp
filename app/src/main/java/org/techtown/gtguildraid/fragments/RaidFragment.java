package org.techtown.gtguildraid.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.DialogImageSpinnerAdapter;
import org.techtown.gtguildraid.adapters.RaidCardAdapter;
import org.techtown.gtguildraid.etc.BossBottomSheetDialog;
import org.techtown.gtguildraid.etc.MySpinner;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.models.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RaidFragment extends Fragment implements BossBottomSheetDialog.BottomSheetListener {
    final String dateFormat = "yy/MM/dd";
    final String[] elementsEnglish = new String[]{"fire", "water", "earth", "light", "dark", "basic"};

    ViewGroup view;
    RoomDB database;
    Raid currentRaid;
    TextView isNotFound;

    TextView raidName;
    TextView raidTerm;
    LinearLayout raidInfo;

    ImageView[] bossBtnList;
    LinearLayout bossInfo;

    Date today;
    Boolean isCurrentExist;
    Button raidButton;
    ImageView bossImageInDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_raid, container, false);
        isNotFound = view.findViewById(R.id.notFound);
        raidInfo = view.findViewById(R.id.raidInfo);
        bossInfo = view.findViewById(R.id.bossInfo);
        raidName = view.findViewById(R.id.raidName);
        raidTerm = view.findViewById(R.id.raidTerm);

        database = RoomDB.getInstance(getActivity());
        today = new Date();
        isCurrentExist = database.raidDao().isCurrentRaidExist(today);

        raidButton = view.findViewById(R.id.raidButton);

        if(isCurrentExist) {
            refreshView();
        }
        else{
            isNotFound.setVisibility(View.VISIBLE);
            raidInfo.setVisibility(View.GONE);
            raidButton.setText("생성");
        }

        raidButton.setOnClickListener(view -> updateRaid());

        final ImageView arrow = view.findViewById(R.id.currentArrow);

        arrow.setOnClickListener(view -> {
            if(bossInfo.getVisibility() == View.VISIBLE){
                bossInfo.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down);
            }
            else{
                bossInfo.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.icon_arrow_up);
            }
        });

        bossBtnList = new ImageView[4];

        for(int i=1; i<=4; i++){
            int buttonId = getIdentifierFromResource("editButton" + i, "id");

            bossBtnList[i-1] = view.findViewById(buttonId);

            final int finalI = i;
            bossBtnList[i-1].setOnClickListener(view -> updateBoss(finalI -1));
        }

        return view;
    }

    private void updateBoss(int idx){
        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.dialog_boss);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        final EditText name = dialog.findViewById(R.id.bossName);
        final SeekBar bar = dialog.findViewById(R.id.seekBar);
        final TextView hardness = dialog.findViewById(R.id.hardness);

        MySpinner elements = dialog.findViewById(R.id.elementSpinner);
        bossImageInDialog = dialog.findViewById(R.id.bossImage);

        List<Integer> elementImageList = new ArrayList<>();
        elementImageList.add(0);
        for (String elementName : elementsEnglish) {
            int imageId = getIdentifierFromResource("element_" + elementName, "drawable");
            elementImageList.add(imageId);
        }

        List<String> elementList = Arrays.asList("선택", "화", "수", "지", "광", "암", "무");
        DialogImageSpinnerAdapter elementAdapter
                = new DialogImageSpinnerAdapter(getContext(), R.layout.spinner_value_layout, elementList, elementImageList);

        elements.setAdapter(elementAdapter);

        final Boss boss = database.raidDao().getBossesList(currentRaid.getRaidId()).get(idx);
        final int[] selectedElement = {boss.getElementId()};

        name.setText(boss.getName());
        hardness.setText(Double.toString(boss.getHardness()));
        bar.setProgress((int)(boss.getHardness() * 10));

        bossImageInDialog.setImageResource(getIdentifierFromResource("boss_" + boss.getImgName(),"drawable"));
        bossImageInDialog.setTag(boss.getImgName());

        elements.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedElement[0] = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        elements.setSelection(selectedElement[0]);

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(progress < 1) {
                    bar.setProgress(1);
                    hardness.setText((Double.toString(0.1)));
                }
                else
                    hardness.setText(Double.toString(progress / 10.0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        bossImageInDialog.setOnClickListener(view -> {
            BossBottomSheetDialog bottomSheetDialog = new BossBottomSheetDialog(RaidFragment.this);
            bottomSheetDialog.show(getFragmentManager(), "bottomSheetDialog");
        });

        Button updateButton = dialog.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(view -> {
            String sName = name.getText().toString().trim();
            double dHardness = bar.getProgress() / 10.0;
            String imgName = (String) bossImageInDialog.getTag();
            if(!sName.equals("")) {
                database.raidDao().updateBoss(boss.getBossId(), sName, imgName, dHardness, selectedElement[0]);
                refreshView();
                dialog.dismiss();
            }
            else
                showToast("이름을 입력하세요.");
        });
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
        Date defaultDate;

        if(isCurrentExist){
            name.setText(currentRaid.getName());
            defaultDate = currentRaid.getStartDay();
        }
        else{
            defaultDate = today;
        }
        startDate.setText(new SimpleDateFormat(dateFormat).format(defaultDate));

        Calendar defaultCal = Calendar.getInstance();
        defaultCal.setTime(defaultDate);

        final Calendar myCalendar = Calendar.getInstance();
        myCalendar.set(Calendar.YEAR, defaultCal.get(Calendar.YEAR));
        myCalendar.set(Calendar.MONTH, defaultCal.get(Calendar.MONTH));
        myCalendar.set(Calendar.DAY_OF_MONTH, defaultCal.get(Calendar.DAY_OF_MONTH));

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
            DatePickerDialog dialog1 = new DatePickerDialog(getActivity(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            dialog1.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - DAY_13);
            dialog1.show();
        });

        Button updateButton = dialog.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(view -> {
            String sName = name.getText().toString().trim();
            Date sDate;
            Date eDate;
            try {
                sDate = new SimpleDateFormat(dateFormat).parse(startDate.getText().toString().trim());
                eDate = getEndDate(sDate);
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
                            String imageName = Integer.toString(i);
                            boss.setImgName(imageName);

                            bosses.add(boss);
                        }
                        database.raidDao().insertRaidWithBosses(raid, bosses);
                    }
                    else{//업데이트
                        database.raidDao().updateRaid(currentRaid.getRaidId(), sName, sDate, eDate);
                    }
                    refreshView();
                }
                else{
                    showToast("이름을 입력하세요");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    private Date getEndDate(Date sDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(sDate);
        cal.add(Calendar.DATE, 15);
        return cal.getTime();
    }

    private void refreshView() {
        currentRaid = database.raidDao().getCurrentRaidWithBosses(today);
        isNotFound.setVisibility(View.GONE);
        raidInfo.setVisibility(View.VISIBLE);

        raidName.setText(currentRaid.getName());
        Date aEnd = adjustEndTime(currentRaid.getEndDay());

        List<Boss> bosses = currentRaid.getBossList();

        for(int i=1; i<=4; i++){
            int nameId = getIdentifierFromResource("boss" + i, "id");
            int barId = getIdentifierFromResource("progressBar" + i, "id");
            int hardnessId = getIdentifierFromResource("hardness" + i, "id");
            int bossImageId = getIdentifierFromResource("boss" + i + "Image", "id");
            int elementId = getIdentifierFromResource("element" + i, "id");

            TextView bossName = view.findViewById(nameId);
            TextView hardness = view.findViewById(hardnessId);
            ProgressBar progressBar = view.findViewById(barId);
            ImageView bossImage = view.findViewById(bossImageId);
            ImageView elementImage = view.findViewById(elementId);

            bossName.setText(bosses.get(i-1).getName());
            hardness.setText(getString(R.string.boss_hardness, bosses.get(i-1).getHardness()));
            progressBar.setProgress((int)(bosses.get(i-1).getHardness() * 10));
            bossImage.setImageResource(getIdentifierFromResource("boss_" + bosses.get(i-1).getImgName(), "drawable"));
            int eId = bosses.get(i-1).getElementId();
            if(eId != 0) {
                elementImage.setVisibility(View.VISIBLE);
                elementImage.setImageResource(getIdentifierFromResource("element_" + elementsEnglish[eId - 1], "drawable"));
            }
            else
                elementImage.setVisibility(View.INVISIBLE);
        }

        raidTerm.setText(getString(R.string.raid_term,
                new SimpleDateFormat(dateFormat).format(currentRaid.getStartDay()),
                new SimpleDateFormat(dateFormat).format(aEnd)));

        raidButton.setText("수정");

    }

    private Date adjustEndTime(Date eDate) {
        Calendar end = Calendar.getInstance();
        end.setTime(eDate);
        end.add(Calendar.DATE, -2);

        return end.getTime();
    }

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageClicked(String imgName) {
        bossImageInDialog.setImageResource(getIdentifierFromResource("boss_" + imgName, "drawable"));
        bossImageInDialog.setTag(imgName);
    }

    int getIdentifierFromResource(String name, String defType){
        return getResources().getIdentifier(
                name, defType, Objects.requireNonNull(getContext()).getPackageName());
    }
}
