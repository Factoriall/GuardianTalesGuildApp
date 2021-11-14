package org.techtown.gtguildraid.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.StatisticPagerAdapter;
import org.techtown.gtguildraid.models.entities.Raid;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatisticRaidActivity extends AppCompatActivity {
    int raidId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_raid);
        getSupportActionBar().hide();
        final String dateFormat = "yy/MM/dd";

        RoomDB database = RoomDB.getInstance(this);

        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        raidId = getIntent().getIntExtra("raidId", 0);
        Raid raid = database.raidDao().getRaid(raidId);

        TextView raidName = findViewById(R.id.raidName);
        TextView raidTerm = findViewById(R.id.raidTerm);
        ImageView raidThumbnail = findViewById(R.id.raidThumbnail);
        raidName.setText(raid.getName());
        raidTerm.setText((new SimpleDateFormat(dateFormat).format(raid.getStartDay()) + "~" +
                        new SimpleDateFormat(dateFormat).format(getEndTime(raid.getStartDay()))));
        raidThumbnail.setImageResource(getResources().getIdentifier(
                "character_" + raid.getThumbnail(),
                "drawable",
                getPackageName()));

        //pager 관련 어댑터
        StatisticPagerAdapter adapter = new StatisticPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.setData(raidId);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);//양옆 로드할 page 리미트 생성

        //path 정보를 담는 info 버튼
        ImageButton pathInfo = findViewById(R.id.pathInfo);
        pathInfo.setOnClickListener(view -> {
            Dialog di = new Dialog(this);
            di.setContentView(R.layout.dialog_path);
            di.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT
                    , WindowManager.LayoutParams.WRAP_CONTENT);
            di.show();

            Button okButton = di.findViewById(R.id.okbutton);
            okButton.setOnClickListener( v -> di.dismiss());
        });

        ImageView exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(view -> {
            finish();
        });

        //ViewPager2와 TabLayout을 연결 및 tab 내용 설정
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position == 0)
                tab.setText("순위표");
            else if(position == 1)
                tab.setText("개인별 기록");
            else if(position == 2)
                tab.setText("보스별 기록");
        }).attach();
        viewPager.setUserInputEnabled(false);
    }

    private Date getEndTime(Date day) {
        Calendar end = Calendar.getInstance();
        end.setTime(day);
        end.add(Calendar.DATE, 13);

        return end.getTime();
    }
}
