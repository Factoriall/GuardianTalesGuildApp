package org.techtown.gtguildraid.Fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.techtown.gtguildraid.Adapters.RecordAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CardFragment extends Fragment {
    private final int MAX_SIZE = 3;
    RecyclerView recyclerView;
    List<Record> recordList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    RecordAdapter adapter;

    FloatingActionButton fab;

    private static final String ARG_COUNT = "param1";
    private int counter;
    private int memberId;
    private int raidId;
    private int day;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance(int counter, int memberId, int raidId, int day) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COUNT, counter);
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);
        args.putInt("day", day);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            counter = getArguments().getInt(ARG_COUNT);
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);

        database = RoomDB.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.recordRecyclerView);
        recordList = database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day);

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordAdapter(getActivity(), recordList);
        recyclerView.setAdapter(adapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCard();
                if(recordList.size() == MAX_SIZE)
                    fab.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void createCard() {
        if(recordList.size() == MAX_SIZE){
            showToast("멤버 인원이 가득찼습니다.");
            return;
        }

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Spinner bossSpinner = dialog.findViewById(R.id.bossSpinner);
        EditText damage = dialog.findViewById(R.id.damage);

        Raid raid = database.raidDao().getCurrentRaid(new Date());
        List<Boss> bosses = database.raidDao().getBossesList(raid.getRaidId());
        List<String> bossNames = new ArrayList<>();
        List<Integer> bossIds = new ArrayList<>();
        for(Boss boss : bosses){
            Log.d("bossName",boss.getName());
            bossNames.add(boss.getName());
            bossIds.add(boss.getBossId());
        }
        final int[] selectedBossId = {0};

        ArrayAdapter<String> bossSpinnerAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, bossNames);
        bossSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        bossSpinner.setAdapter(bossSpinnerAdapter);
        bossSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBossId[0] = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner[] elements = new Spinner[4];
        Spinner[] heroNames = new Spinner[4];

        List<String> heroNameList = database.heroDao().getAllHeroesNames();
        for(int i=1; i<=4; i++){
            Resources res = getResources();
            int elementId = res.getIdentifier("elementSpinner" + i, "id", getContext().getPackageName());
            int heroNameId = res.getIdentifier("heroNameSpinner" + i, "id", getContext().getPackageName());

            elements[i-1] = dialog.findViewById(elementId);
            heroNames[i-1] = dialog.findViewById(heroNameId);

            ArrayAdapter<String> heroAdapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_spinner_item, heroNameList);
            heroAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);

            heroNames[i-1].setAdapter(heroAdapter);
        }

        Button createButton = dialog.findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button exitButton = dialog.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
