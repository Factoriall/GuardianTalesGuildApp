package org.techtown.gtguildraid.Fragments;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.techtown.gtguildraid.Adapters.DialogSpinnerAdapter;
import org.techtown.gtguildraid.Adapters.RecordAdapter;
import org.techtown.gtguildraid.Models.Boss;
import org.techtown.gtguildraid.Models.Hero;
import org.techtown.gtguildraid.Models.Raid;
import org.techtown.gtguildraid.Models.Record;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.Utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecordCardFragment extends Fragment {
    private final int MAX_SIZE = 3;
    final String[] elementKoreanArray = new String[]{"1성", "화", "수", "지", "광", "암", "무"};
    final String[] elementEnglishArray = new String[]{"normal", "fire", "water", "earth", "light", "dark", "basic"};

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    DialogSpinnerAdapter elementAdapter;
    LinearLayoutManager linearLayoutManager;
    RoomDB database;
    RecordAdapter adapter;

    TextView totalDamage;
    FloatingActionButton fab;

    List<Record> recordList = new ArrayList<>();
    private int memberId;
    private int raidId;
    private int day;
    private boolean isChecked;
    private ArrayList<ArrayList<Integer>> heroIds;

    int selectedHeroId;
    int selectedHeroElement;

    public static RecordCardFragment newInstance(int counter, int memberId, int raidId, boolean isChecked) {
        RecordCardFragment fragment = new RecordCardFragment();
        Bundle args = new Bundle();
        args.putInt("day", counter + 1);
        args.putInt("memberId", memberId);
        args.putInt("raidId", raidId);
        args.putBoolean("isChecked", isChecked);

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
        for (int i = 0; i < 7; i++)
            heroIds.add(new ArrayList<>());

        for (Hero hero : heroList)
            heroIds.get(hero.getElement()).add(hero.getHeroId());

        //elementSpinner 정보 생성
        List<String> elementEnglishList = Arrays.asList(elementEnglishArray);
        List<Integer> elementImageList = new ArrayList<>();
        for (String elementName : elementEnglishList) {
            int imageId = getIdentifierFromResource("element_" + elementName, "drawable");
            elementImageList.add(imageId);
        }

        List<String> elementList = Arrays.asList(elementKoreanArray);
        elementAdapter = new DialogSpinnerAdapter(getContext(), R.layout.spinner_value_layout, elementList, elementImageList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            memberId = getArguments().getInt("memberId");
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
            isChecked = getArguments().getBoolean("isChecked");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);
        Log.d("RecordMemberInfo", database.memberDao().getMember(memberId).getName());

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = view.findViewById(R.id.recordRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recordList = database.recordDao().getCertainDayRecordsWithBossAndLeader(memberId, raidId, day);

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordAdapter(isChecked);
        adapter.setItems(recordList);
        recyclerView.setAdapter(adapter);

        TextView damageText = view.findViewById(R.id.damageText);
        damageText.setText(day + "일차 총 데미지");
        totalDamage = view.findViewById(R.id.totalDamage);
        setTotalDamage(isChecked);

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

        if (recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    private void setTotalDamage(Boolean isChecked) {
        int total = 0;
        for (Record record : recordList) {
            if (isChecked)
                total += (int) (record.getDamage() * record.getBoss().getHardness());
            else
                total += record.getDamage();
        }

        totalDamage.setText(NumberFormat.getNumberInstance(Locale.US).format(total));
    }

    private void refreshList() {
        recordList.clear();
        recordList.addAll(database.recordDao().getCertainDayRecordsWithBossAndLeader(memberId, raidId, day));
        setTotalDamage(isChecked);
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
        List<Integer> bossImages = new ArrayList<>();
        for (Boss boss : bosses) {
            bossNames.add(boss.getName());
            bossImages.add(getIdentifierFromResource("boss_" + boss.getImgName(), "drawable"));
            bossIds.add(boss.getBossId());
        }
        final int[] selectedBossId = {0};

        DialogSpinnerAdapter bossDialogSpinnerAdapter = new DialogSpinnerAdapter(getContext(), R.layout.spinner_value_layout, bossNames, bossImages);

        bossSpinner.setAdapter(bossDialogSpinnerAdapter);
        bossSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBossId[0] = bossIds.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //원소 및 영웅 스피너 생성
        Spinner elements;
        Spinner heroNames;

        int elementId = getIdentifierFromResource("elementSpinner", "id");
        int heroNameId = getIdentifierFromResource("heroNameSpinner", "id");

        elements = dialog.findViewById(elementId);
        heroNames = dialog.findViewById(heroNameId);

        heroNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHeroId = heroIds.get(selectedHeroElement).get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        elements.setAdapter(elementAdapter);
        elements.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHeroElement = i;
                List<Hero> elementHeroes = database.heroDao().getHeroesWithElement(i);
                List<String> heroList = new ArrayList<>();
                List<Integer> imageList = new ArrayList<>();

                for (Hero hero : elementHeroes) {
                    int imageId = getIdentifierFromResource("character_" + hero.getEnglishName(), "drawable");
                    heroList.add(hero.getKoreanName());
                    imageList.add(imageId);
                }
                SpinnerAdapter adapter = new DialogSpinnerAdapter(getContext(), R.layout.spinner_value_layout, heroList, imageList);
                heroNames.setAdapter(adapter);

                if (isEditing) {
                    for (int j = 0; j < elementHeroes.size(); j++) {
                        if (selectedHeroId == elementHeroes.get(j).getHeroId()) {
                            heroNames.setSelection(j);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if (isEditing) {
            damage.setText(Integer.toString(record.getDamage()));
            level.setText(Integer.toString(record.getLevel()));
            String bossName = record.getBoss().getName();

            Hero leader = record.getLeader();
            elements.setSelection(leader.getElement());
            selectedHeroId = leader.getHeroId();

            for (int i = 0; i < 4; i++) {
                if (bossName.equals(bossSpinner.getItemAtPosition(i).toString())) {
                    bossSpinner.setSelection(i);
                    break;
                }
            }
        }

        Button createButton = dialog.findViewById(R.id.createButton);
        if (isEditing)
            createButton.setText("수정");
        else
            createButton.setText("생성");

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sDamage = damage.getText().toString().trim();
                String sLevel = level.getText().toString().trim();

                Integer iHeroId;
                iHeroId = heroIds.get(elements.getSelectedItemPosition())
                            .get(heroNames.getSelectedItemPosition());

                if (!sDamage.equals("") && !sLevel.equals("")) {
                    dialog.dismiss();
                    if (isEditing) {//수정 중이면 업데이트
                        database.recordDao().updateRecord(record.getRecordId(),
                                Integer.parseInt(sDamage), selectedBossId[0], Integer.parseInt(sLevel), iHeroId);
                    } else {//새로운 데이터 생성
                        Record record = new Record(memberId, raidId, day);
                        record.setDamage(Integer.parseInt(sDamage));
                        record.setBossId(selectedBossId[0]);
                        record.setLeaderId(iHeroId);
                        record.setLevel(Integer.parseInt(sLevel));

                        //recordList 갱신
                        database.recordDao().insertRecord(record);
                    }
                    recordList.clear();
                    recordList.addAll(database.recordDao().getCertainDayRecordsWithBossAndLeader(memberId, raidId, day));

                    setFabVisibility();

                    adapter.notifyDataSetChanged();
                } else {
                    showToast("데미지 및 보스 레벨을 입력하세요!");
                }
            }
        });

        Button exitButton = dialog.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
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
            switch (direction) {
                case ItemTouchHelper.LEFT:
                    String deletedInfo = "Day " + recordList.get(position).getDay() + ", #" + (position + 1) + " 삭제";
                    Record selected = recordList.get(position);
                    database.recordDao().deleteRecord(selected);
                    recordList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Snackbar.make(recyclerView, deletedInfo, 2000)
                            .setAction("취소", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    database.recordDao().insertRecord(selected);
                                    recordList.add(position, selected);
                                    adapter.notifyItemInserted(position);
                                }
                            }).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setFabVisibility();
                        }
                    }, 2500);
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

    int getIdentifierFromResource(String name, String defType){
        return getResources().getIdentifier(
                name, defType, getContext().getPackageName());
    }

    private void setFabVisibility() {
        if (recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);
        else
            fab.setVisibility(View.VISIBLE);
    }

    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
