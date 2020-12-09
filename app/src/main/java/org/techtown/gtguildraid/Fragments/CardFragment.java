package org.techtown.gtguildraid.Fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class CardFragment extends Fragment{
    private final int MAX_SIZE = 3;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    List<Record> recordList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    RecordAdapter adapter;
    FloatingActionButton fab;

    final String[] elementKoreanArray = new String[]{"화", "수", "지", "광", "암", "무"};
    final String[] elementEnglishArray = new String[]{"fire", "water", "earth", "light", "dark", "basic"};

    private int memberId;
    private int raidId;
    private int day;
    ArrayList<ArrayList<Integer>> heroIds;
    SpinnerAdapter elementAdapter;
    int[] selectedHeroId = new int[4];

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = RoomDB.getInstance(getActivity());

        List<Hero> heroList = database.heroDao().getAllHeroes();
        //heroId 정보 생성
        heroIds = new ArrayList<>();
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

        List<String> elementList = Arrays.asList(elementKoreanArray);
        elementAdapter = new SpinnerAdapter(getContext(), R.layout.spinner_value_layout, elementList, elementImageList);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume", "onResume");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.recordRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recordList = database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day);

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordAdapter();
        adapter.setItems(recordList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshList();
            }
        });

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCard(false, null);
            }
        });

        if(recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    private void refreshList() {
        recordList.clear();
        recordList.addAll(database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day));
        adapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    private void updateCard(Boolean isEditing, Record record) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        Spinner bossSpinner = dialog.findViewById(R.id.bossSpinner);
        EditText damage = dialog.findViewById(R.id.damage);
        EditText level = dialog.findViewById(R.id.level);

        Raid raid = database.raidDao().getCurrentRaid(new Date());

        //보스 스피너 생성
        List<Boss> bosses = database.raidDao().getBossesList(raid.getRaidId());
        List<String> bossNames = new ArrayList<>();
        List<Integer> bossIds = new ArrayList<>();
        for(Boss boss : bosses){
            bossNames.add(boss.getName());
            bossIds.add(boss.getBossId());
        }
        final int[] selectedBossId = {0};
        int[] selectedHeroElement = new int[4];


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

        for(int i=1; i<=4; i++){
            int idx = i;
            Resources res = getResources();
            int elementId = res.getIdentifier("elementSpinner" + idx, "id", getContext().getPackageName());
            int heroNameId = res.getIdentifier("heroNameSpinner" + idx, "id", getContext().getPackageName());

            elements[idx-1] = dialog.findViewById(elementId);
            heroNames[idx-1] = dialog.findViewById(heroNameId);

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

                    if(isEditing){
                        for(int j=0; j<elementHeroes.size(); j++) {
                            if(selectedHeroId[idx-1] == elementHeroes.get(j).getHeroId()) {
                                heroNames[idx - 1].setSelection(j);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
        }

        if(isEditing){
            damage.setText(Integer.toString(record.getDamage()));
            String bossName = record.getBoss().getName();

            Hero hero1 = record.getHero1();
            setHeroSpinner(0, hero1, elements[0]);
            Hero hero2 = record.getHero2();
            setHeroSpinner(1, hero2, elements[1]);
            Hero hero3 = record.getHero3();
            setHeroSpinner(2, hero3, elements[2]);
            Hero hero4 = record.getHero4();
            setHeroSpinner(3, hero4, elements[3]);

            for(int i=0; i<4; i++){
                if(bossName.equals(bossSpinner.getItemAtPosition(i).toString())) {
                    bossSpinner.setSelection(i);
                    break;
                }
            }
        }

        Button createButton = dialog.findViewById(R.id.createButton);
        if(isEditing)
            createButton.setText("수정");
        else
            createButton.setText("생성");

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sDamage = damage.getText().toString().trim();
                String sLevel = level.getText().toString().trim();
                Integer[] iHeroIds = new Integer[4];
                for(int i=0; i<4; i++){
                    iHeroIds[i] = heroIds.get(elements[i].getSelectedItemPosition() + 1)
                            .get(heroNames[i].getSelectedItemPosition());
                }

                if(!sDamage.equals("") && !sLevel.equals("")) {
                    dialog.dismiss();
                    if(isEditing){//수정 중이면 업데이트
                        database.recordDao().updateRecord(record.getRecordID(),
                                Integer.parseInt(sDamage), selectedBossId[0], Integer.parseInt(sLevel),
                                iHeroIds[0], iHeroIds[1], iHeroIds[2], iHeroIds[3]);
                    }
                    else {//새로운 데이터 생성
                        Record record = new Record(memberId, raidId, day);
                        record.setDamage(Integer.parseInt(sDamage));
                        record.setBossId(selectedBossId[0]);
                        record.setHero1Id(iHeroIds[0]);
                        record.setHero2Id(iHeroIds[1]);
                        record.setHero3Id(iHeroIds[2]);
                        record.setHero4Id(iHeroIds[3]);
                        record.setLevel(Integer.parseInt(sLevel));

                        //recordList 갱신
                        database.recordDao().insertRecord(record);
                    }
                    recordList.clear();
                    recordList.addAll(database.recordDao().getCertainDayRecordsWithHeroes(memberId, raidId, day));

                    setFabVisibility();

                    adapter.notifyDataSetChanged();
                }
                else{
                    showToast("데미지 및 보스 레벨을 입력하세요!");
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

    private void setHeroSpinner(int idx, Hero hero, Spinner element) {
        int ele = hero.getElement();
        element.setSelection(ele - 1);

        selectedHeroId[idx] = hero.getHeroId();
    }

    private void setFabVisibility() {
        if(recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);
        else
            fab.setVisibility(View.VISIBLE);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT |
            ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            switch(direction){

                case ItemTouchHelper.LEFT:
                    //String deletedInfo = "Day " + recordList.get(position).getDay() + ", #" + (position + 1) + " 삭제";
                    Record selected = recordList.get(position);
                    database.recordDao().deleteRecord(selected);
                    recordList.remove(position);
                    adapter.notifyItemRemoved(position);
                    setFabVisibility();
                    break;
                case ItemTouchHelper.RIGHT:
                    Record editRecord = recordList.get(position);
                    updateCard(true, editRecord);
                    adapter.notifyItemChanged(position);
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.RED)
                    .addSwipeLeftActionIcon(R.drawable.icon_delete)
                    .addSwipeRightBackgroundColor(Color.GREEN)
                    .addSwipeRightActionIcon(R.drawable.icon_create)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    private void showToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
