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
import org.techtown.gtguildraid.Adapters.SpinnerAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.util.ArrayList;
import java.util.Arrays;
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

    final String[] elementArray = new String[]{"화", "수", "지", "광", "암", "무"};
    final String[] elementEnglishArray = new String[]{"fire", "water", "earth", "light", "dark", "basic"};

    private int memberId;
    private int raidId;
    private int day;

    public CardFragment() {
        // Required empty public constructor
    }

    public static CardFragment newInstance(int counter, int memberId, int raidId) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putInt("day", counter + 1);
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
        }
        Log.d("dayInfo", Integer.toString(day));
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);

        database = RoomDB.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.recordRecyclerView);
        recordList = database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day);


        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordAdapter();
        adapter.setItems(recordList);
        recyclerView.setAdapter(adapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCard();
            }
        });
        if(recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);

        return view;
    }

    private void createCard() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Spinner bossSpinner = dialog.findViewById(R.id.bossSpinner);
        EditText damage = dialog.findViewById(R.id.damage);

        Raid raid = database.raidDao().getCurrentRaid(new Date());

        //보스 스피너 생성
        List<Boss> bosses = database.raidDao().getBossesList(raid.getRaidId());
        List<String> bossNames = new ArrayList<>();
        List<Integer> bossIds = new ArrayList<>();
        for(Boss boss : bosses){
            Log.d("bossName",boss.getName());
            bossNames.add(boss.getName());
            bossIds.add(boss.getBossId());
        }
        final int[] selectedBossId = {0};
        int[] selectedHeroElement = new int[4];
        int[] selectedHeroId = new int[4];


        ArrayAdapter<String> bossSpinnerAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, bossNames);
        bossSpinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        bossSpinner.setAdapter(bossSpinnerAdapter);
        bossSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBossId[0] = bossIds.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        //원소 및 영웅 스피너 생성
        Spinner[] elements = new Spinner[4];
        Spinner[] heroNames = new Spinner[4];

        List<Hero> heroList = database.heroDao().getAllHeroes();

        //heroId 정보 생성
        ArrayList<ArrayList<Integer>> heroIds = new ArrayList<>();
        for(int i=0; i<7; i++)
            heroIds.add(new ArrayList<>());
        for(Hero hero : heroList)
            heroIds.get(hero.getElement()).add(hero.getHeroId());

        //elementSpinner 정보 생성
        List<String> elementEnglishList = Arrays.asList(elementEnglishArray);
        List<Integer> elementImageList = new ArrayList<>();
        for(String elementName : elementEnglishList){
            int imageId = getResources().getIdentifier("element_"+elementName, "drawable", getContext().getPackageName()); // or other resource class
            elementImageList.add(imageId);
        }

        List<String> elementList = Arrays.asList(elementArray);
        for(int i=1; i<=4; i++){
            int idx = i;
            Resources res = getResources();
            int elementId = res.getIdentifier("elementSpinner" + idx, "id", getContext().getPackageName());
            int heroNameId = res.getIdentifier("heroNameSpinner" + idx, "id", getContext().getPackageName());

            elements[idx-1] = dialog.findViewById(elementId);
            heroNames[idx-1] = dialog.findViewById(heroNameId);

            SpinnerAdapter elementAdapter = new SpinnerAdapter(getContext(), R.layout.spinner_value_layout, elementList, elementImageList);

            heroNames[idx-1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedHeroId[idx-1] = heroIds.get(selectedHeroElement[idx-1]).get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            elements[idx-1].setAdapter(elementAdapter);
            elements[idx-1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedHeroElement[idx-1] = i+1;
                    List<Hero> elementHeroes = database.heroDao().getHeroesWithElement(i+1);
                    List<String> heroList = new ArrayList<>();
                    List<Integer> imageList = new ArrayList<>();

                    for(Hero hero : elementHeroes){
                        int imageId = getResources().getIdentifier("character_"+hero.getEnglishName(), "drawable", getContext().getPackageName());
                        heroList.add(hero.getKoreanName());
                        imageList.add(imageId);
                    }
                    SpinnerAdapter adapter = new SpinnerAdapter(getContext(), R.layout.spinner_value_layout, heroList, imageList);
                    heroNames[idx-1].setAdapter(adapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
        }

        Button createButton = dialog.findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sDamage = damage.getText().toString().trim();
                String sBossName = bossSpinner.getSelectedItem().toString();
                Integer[] iHeroIds = new Integer[4];
                for(int i=0; i<4; i++){
                    iHeroIds[i] = heroIds.get(elements[i].getSelectedItemPosition() + 1)
                            .get(heroNames[i].getSelectedItemPosition());
                }

                if(!sDamage.equals("")) {
                    dialog.dismiss();
                    Record record = new Record(memberId, raidId, day);
                    record.setDamage(Integer.parseInt(sDamage));
                    record.setBossId(selectedBossId[0]);
                    record.setHero1Id(iHeroIds[0]);
                    record.setHero2Id(iHeroIds[1]);
                    record.setHero3Id(iHeroIds[2]);
                    record.setHero4Id(iHeroIds[3]);

                    //recordList 갱신
                    database.recordDao().insertRecord(record);
                    recordList.clear();
                    recordList.addAll(database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day));

                    Log.d("recordListSize", Integer.toString(recordList.size()));
                    if(recordList.size() >= MAX_SIZE)
                        fab.setVisibility(View.GONE);

                    adapter.notifyDataSetChanged();
                }
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
