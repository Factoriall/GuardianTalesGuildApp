package org.techtown.gtguildraid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.DialogImageSpinnerAdapter;
import org.techtown.gtguildraid.etc.BossBottomSheetDialog;
import org.techtown.gtguildraid.models.Boss;
import org.techtown.gtguildraid.utils.RoomDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BossEditActivity extends AppCompatActivity
        implements BossBottomSheetDialog.BottomSheetListener {
    ImageView bossImage;
    Boss boss;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_edit);

        RoomDB database = RoomDB.getInstance(this);
        int position = getIntent().getIntExtra("position", 0);
        int bossId = getIntent().getIntExtra("bossId", 0 );
        boss = database.bossDao().getBoss(bossId);

        bossImage = findViewById(R.id.bossImage);
        TextView bossName = findViewById(R.id.bossName);
        Spinner elementSpinner = findViewById(R.id.elementSpinner);
        Switch isFuriousCheck = findViewById(R.id.isFurious);
        SeekBar hardness = findViewById(R.id.hardness);
        Button button = findViewById(R.id.button);
        TextView hardnessValue = findViewById(R.id.hardnessValue);

                // spinner 설정
        setElementSpinner(elementSpinner);
        elementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boss.setElementId(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });


        bossImage.setImageResource(getIdentifierFromResource("boss_" + boss.getImgName()));
        bossName.setText(boss.getName());
        elementSpinner.setSelection(boss.getElementId());
        isFuriousCheck.setChecked(boss.isFurious());
        hardness.setProgress((int)(boss.getHardness() * 10));
        hardnessValue.setText("x " + boss.getHardness());

        isFuriousCheck.setOnClickListener(view ->{
            boss.setFurious(isFuriousCheck.isChecked());
        });

        //boss 이미지 창 띄우고, 누른 것에 따라 이미지 및 이름 설정
        bossImage.setOnClickListener(view -> {
            BossBottomSheetDialog bottomSheetDialog = new BossBottomSheetDialog(this);
            bottomSheetDialog.show(getSupportFragmentManager(), "bottomSheetDialog");
        });

        hardness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(progress < 1) {
                    hardness.setProgress(1);
                    hardnessValue.setText("x " + 0.1);
                }
                else
                    hardnessValue.setText("x " + (progress / 10.0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        //업데이트
        button.setOnClickListener (view ->{
            String sName = bossName.getText().toString().trim();
            double dHardness = hardness.getProgress() / 10.0;
            String imgName = (String) boss.getImgName();
            int elementIdx = elementSpinner.getSelectedItemPosition();
            boolean isFurious = isFuriousCheck.isChecked();
            if(elementIdx == 0 || sName.equals("")){
                Toast.makeText(this, "입력 부족, 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                return;
            }
            database.raidDao().updateBoss(bossId, sName, imgName, dHardness, elementIdx, isFurious);
            Intent retIntent = new Intent();
            retIntent.putExtra("position", position);

            setResult(RESULT_OK, retIntent);
            finish();
        });
    }


    private void setElementSpinner(Spinner elementSpinner) {
        List<String> elementKoreanArray = Arrays.asList("선택", "화", "수", "지", "광", "암", "무");
        String[] elementEnglishArray = {"", "fire", "water", "earth", "light", "dark", "basic"};
        List<Integer> elementImageList = new ArrayList<>();

        for(String element : elementEnglishArray){
            int imageId = getResources().getIdentifier(
                    "element_" + element, "drawable", getPackageName());
            elementImageList.add(imageId);
        }

        elementSpinner.setAdapter(new DialogImageSpinnerAdapter(
                getApplicationContext(),
                R.layout.spinner_value_layout,
                elementKoreanArray,
                elementImageList
        ));
    }

    private int getIdentifierFromResource(String name) {
        return getResources().getIdentifier(
                name, "drawable", getPackageName());
    }

    @Override
    public void onImageClicked(String imgId) {
        Log.d("onImageClicked", imgId);
        bossImage.setImageResource(getIdentifierFromResource("boss_" + imgId));
        boss.setImgName(imgId);
    }
}
