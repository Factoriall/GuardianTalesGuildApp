package org.techtown.gtguildraid.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.gtguildraid.R;

public class FirstActivity extends AppCompatActivity {
    //splash 화면
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        getSupportActionBar().hide();

        //thread 실행을 통해 3초 동안 띄우게
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(3000);  //Delay of 3 seconds
                } catch (Exception e) { e.printStackTrace();}
                finally {
                    Intent i = new Intent(FirstActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }

    //back press 무력화, 렉 안 걸리게
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
