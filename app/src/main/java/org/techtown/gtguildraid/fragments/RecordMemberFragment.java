package org.techtown.gtguildraid.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kyleduo.switchbutton.SwitchButton;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

import org.angmarch.views.NiceSpinner;
import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.DialogImageSpinnerAdapter;
import org.techtown.gtguildraid.adapters.RecordCardAdapter;
import org.techtown.gtguildraid.etc.MySpinner;
import org.techtown.gtguildraid.models.daos.Boss;
import org.techtown.gtguildraid.models.daos.Favorites;
import org.techtown.gtguildraid.models.daos.GuildMember;
import org.techtown.gtguildraid.models.daos.Hero;
import org.techtown.gtguildraid.models.daos.Raid;
import org.techtown.gtguildraid.models.daos.Record;
import org.techtown.gtguildraid.utils.RoomDB;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecordMemberFragment extends Fragment {
    private final int FAVORITE_MAX = 10;
    private final int MAX_SIZE = 3;

    final String[] elementKoreanArray = new String[]{"1성", "화", "수", "지", "광", "암", "무"};
    final String[] elementEnglishArray = new String[]{"normal", "fire", "water", "earth", "light", "dark", "basic"};
    final int[] hpPerRound = {1080000, 1080000,
            1237500, 1237500,
            1500000, 1500000,
            2025000, 2640000, 3440000, 4500000, 5765625,
            7500000, 9750000, 12000000, 16650000, 24000000,
            35000000, 50000000, 72000000,
            100000000, 140000000, 200000000};

    private DialogImageSpinnerAdapter elementAdapter;
    private RoomDB database;
    private RecordCardAdapter adapter;
    private TextView totalDamage;
    private FloatingActionButton fab;
    private NiceSpinner memberSpinner;
    private SwitchButton manualButton;
    ImageView addButton;
    ImageView deleteButton;

    private List<Record> recordList = new ArrayList<>();
    private List<Boss> bosses = new ArrayList<>();
    private int raidId;
    private int day;
    private ArrayList<ArrayList<Integer>> heroIds;
    private List<GuildMember> members;
    private int selectedBossId;
    private int selectedHeroId;
    private int selectedHeroElement;
    private boolean isCreateMode = true;
    private int sMemberIdx = 0;
    boolean isSetByRecord;
    private Toast myToast;

    private SharedPreferences pref;

    private static class MemberForSpinner implements Comparable<MemberForSpinner>{
        private final String name;
        private final int id;
        private int todayRemain;

        public MemberForSpinner(String name, int id, int todayRemain){
            this.name = name;
            this.id = id;
            this.todayRemain = todayRemain;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public int getTodayRemain() {
            return todayRemain;
        }

        public void setTodayRemain(int todayRemain) {
            this.todayRemain = todayRemain;
        }

        @Override
        public int compareTo(MemberForSpinner member) {
            if(todayRemain == 0){
                if(member.todayRemain == 0)
                    return name.compareTo(member.name);
                else
                    return 1;
            }
            else{
                if(member.todayRemain == 0)
                    return -1;
                else
                    return name.compareTo(member.name);
            }
        }
    }

    List<MemberForSpinner> memberList = new ArrayList<>();

    public static RecordMemberFragment newInstance(int counter, int raidId) {
        RecordMemberFragment fragment = new RecordMemberFragment();
        Bundle args = new Bundle();
        args.putInt("day", counter + 1);
        args.putInt("raidId", raidId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
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
        elementAdapter = new DialogImageSpinnerAdapter(getContext(), R.layout.spinner_value_layout, elementList, elementImageList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            raidId = getArguments().getInt("raidId");
            day = getArguments().getInt("day");
        }

        View view = inflater.inflate(R.layout.fragment_record_recycler, container, false);
        memberSpinner = view.findViewById(R.id.nickname);
        members = database.memberDao().getCurrentMembers();

        setMemberSpinner();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = view.findViewById(R.id.recordRecyclerView);
        recordList = getReverseList();

        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordCardAdapter();
        adapter.setItems(recordList);
        recyclerView.setAdapter(adapter);

        memberSpinner.setOnSpinnerItemSelectedListener((parent, view12, position, id) -> {
            Log.d("newSelected", "position: " + position);
            sMemberIdx = position;
            recordList = getReverseList();
            adapter.setItems(recordList);
            setTotalDamage();
            adapter.notifyDataSetChanged();

            setFabVisibility();
        });

        TextView damageText = view.findViewById(R.id.damageText);
        damageText.setText(day + "일차 총 데미지");
        totalDamage = view.findViewById(R.id.totalDamage);
        setTotalDamage();

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> updateCard(false, null));

        if (recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    private List<Record> getReverseList() {
        List<Record> list = database.recordDao().get1DayRecordsWithExtra(
                memberList.get(sMemberIdx).getId(), raidId, day);
        Collections.reverse(list);//ui에 맞게
        return list;
    }

    private void setMemberSpinner() {
        memberList.clear();
        for (GuildMember m : members) {
            memberList.add(new MemberForSpinner(m.getName(), m.getID(),
                    getRemainedRecord(m.getID(), raidId)));
        }

        Collections.sort(memberList);

        List<String> memberNameList = new ArrayList<>();
        for(MemberForSpinner m : memberList){
            memberNameList.add(m.getName() + " - " + m.getTodayRemain());
        }

        memberSpinner.attachDataSource(memberNameList);

        if(memberNameList.size() == 1)
            memberSpinner.setText(memberNameList.get(0));
    }

    private void refreshSpinnerItem(int rIdx) {
        int prevMemberId = memberList.get(rIdx).getId();
        int before = memberList.get(rIdx).todayRemain;
        int after = getRemainedRecord(prevMemberId, raidId);
        memberList.get(rIdx).setTodayRemain(after);

        boolean isOrderChange = before == 0 || after == 0;

        if(isOrderChange)
            Collections.sort(memberList);

        List<String> memberNameList = new ArrayList<>();

        int newSelectedIdx = 0;
        int idx = 0;
        for (MemberForSpinner m : memberList) {
            if (m.getId() == prevMemberId){
                newSelectedIdx = idx;
            }

            memberNameList.add(m.getName() + " - " + m.getTodayRemain());
            idx++;
        }
        memberSpinner.attachDataSource(memberNameList);


        if(isOrderChange)
            sMemberIdx = newSelectedIdx;

        memberSpinner.setSelectedIndex(sMemberIdx);
    }

    private void setTotalDamage() {
        long total = 0;
        for (Record record : recordList)
            total += record.getDamage();

        totalDamage.setText(NumberFormat.getNumberInstance(Locale.US).format(total));
    }

    private void updateCard(Boolean isEditing, Record record) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record);
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        TextView dialogInfo = dialog.findViewById(R.id.dialogInfo);
        ToggleSwitch bossSwitch = dialog.findViewById(R.id.bossSwitch);
        EditText damage = dialog.findViewById(R.id.damage);
        CheckBox isLastHit = dialog.findViewById(R.id.isLastHit);
        addButton = dialog.findViewById(R.id.addButton);
        deleteButton = dialog.findViewById(R.id.deleteButton);
        Button oneCutButton = dialog.findViewById(R.id.oneCutButton);
        HorizontalScrollView hsv = dialog.findViewById(R.id.favoriteScrollView);
        LinearLayout favoritesList = dialog.findViewById(R.id.favoriteList);
        SwitchButton switchButton = dialog.findViewById(R.id.viewSwitch);
        manualButton = dialog.findViewById(R.id.manualSwitch);
        TextView lhNotUse = dialog.findViewById(R.id.lhNotUse);

        MySpinner elements;
        Spinner heroNames;

        Raid raid = database.raidDao().getCurrentRaid(new Date());

        dialogInfo.setText(memberList.get(sMemberIdx).getName() + " / "
                + day + "일차 / " + (isEditing ? "수정\n리더: " + record.getLeader().getKoreanName() : "생성"));

        switchButton.setChecked(day > 3);

        isSetByRecord = switchButton.isChecked();

        //bossSwitch 생성
        bosses = database.raidDao().getBossesList(raid.getRaidId());
        int curRound = pref.getInt("currentRound", 0);
        int idx = (curRound >= hpPerRound.length) ? hpPerRound.length - 1 : curRound;

        bossSwitch.setView(
                R.layout.dialog_record_boss_select,
                bosses.size(),
                (toggleSwitchButton, view, position) -> {
                    TextView textView = view.findViewById(R.id.bossName);
                    String name = bosses.get(position).getName();
                    if (name.length() > 5)
                        name = name.substring(0, 5) + "..";
                    textView.setText(name);

                    ImageView imageView = view.findViewById(R.id.bossImage);
                    imageView.setImageResource(
                            getIdentifierFromResource("boss_" + bosses.get(position).getImgName(), "drawable"));
                    long d = database.recordDao().get1Boss1RoundSum(raidId, bosses.get(position).getBossId(), curRound + 1);
                    long r = hpPerRound[idx] - d;
                    if(r <= 0){
                        setGrayFilterOnImage(imageView);
                        textView.setPaintFlags(textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    },
                (view, position) -> {
                    TextView textView = view.findViewById(R.id.bossName);
                    textView.setTextColor(ContextCompat.getColor(requireActivity(), android.R.color.white));
                    long d = database.recordDao().get1Boss1RoundSum(raidId, bosses.get(position).getBossId(), curRound + 1);
                    long r = hpPerRound[idx] - d;
                    if(r <= 0) {
                        if (r == 0) showToast("이미 피가 0인 보스입니다.");
                        else showToast("피가 음수인 보스입니다.");
                    }
                    Record lh = database.recordDao().get1Boss1RoundLastHit(raidId, bosses.get(position).getBossId(), curRound + 1);
                    if(lh != null){
                        isLastHit.setChecked(false);
                        isLastHit.setVisibility(View.GONE);
                        lhNotUse.setVisibility(View.VISIBLE);
                    }
                    else {
                        isLastHit.setVisibility(View.VISIBLE);
                        lhNotUse.setVisibility(View.GONE);
                    }
                    },
                (view, position) -> {
                    TextView textView = view.findViewById(R.id.bossName);
                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray));
                });

        selectedBossId = -1;
        selectedHeroId = -1;

        //회차 넘버피커 생성
        List<String> rounds = new ArrayList<>();
        int[] levelPerRound = {50, 50, 55, 55, 60, 60};
        for (int i = 1; i <= 40; i++) {
            final int START_NUM = 65;
            final int START_IDX = 7;
            final int MAX_LEVEL = 80;
            int level = (i <= levelPerRound.length) ? levelPerRound[i - 1] : START_NUM + (i - START_IDX);
            if (level > MAX_LEVEL) level = MAX_LEVEL;
            rounds.add(level + "(" + i + ")");
        }
        Button decrement = dialog.findViewById(R.id.decrement);
        Button increment = dialog.findViewById(R.id.increment);
        TextView roundDisplay = dialog.findViewById(R.id.display);

        manualButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(isChecked){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("\n좌하단 체크 버튼을 이용해 기록 내의 보스들의 피를 확인이 가능하며, " +
                        "이번 회차 모든 보스 피가 0이 된 상태로 데이터 갱신 시 회차는 자동 갱신됩니다.\n" +
                        "가급적이면 순서대로 기록하고, 불가피할 때만 수동 조정해주시길 바랍니다.\n\n" +
                        "그래도 수동 수정을 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", (dialog1, id) -> {
                            increment.setVisibility(View.VISIBLE);
                            decrement.setVisibility(View.VISIBLE);
                            dialog1.dismiss();
                        })
                        .setNegativeButton("아니오",(dialog1, id) -> {
                            manualButton.setChecked(false);
                            dialog1.dismiss();
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("수동 조정 전 안내 사항");
                alert.show();
            }
            else{
                increment.setVisibility(View.GONE);
                decrement.setVisibility(View.GONE);
            }
        });

        final int[] pickerIdx = {pref.getInt("currentRound", 0)};
        roundDisplay.setText(rounds.get(pickerIdx[0]));
        increment.setOnClickListener(view -> {
            if (pickerIdx[0] >= rounds.size() - 1)
                return;
            pickerIdx[0]++;
            roundDisplay.setText(rounds.get(pickerIdx[0]));
        });
        decrement.setOnClickListener(view -> {
            if (pickerIdx[0] <= 0)
                return;
            pickerIdx[0]--;
            roundDisplay.setText(rounds.get(pickerIdx[0]));
        });


        //원소 및 영웅 스피너 생성
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

                SpinnerAdapter adapter = new DialogImageSpinnerAdapter(getContext(), R.layout.spinner_value_layout, heroList, imageList);
                heroNames.setAdapter(adapter);
                for (int j = 0; j < elementHeroes.size(); j++) {
                    if (selectedHeroId == elementHeroes.get(j).getHeroId()) {
                        heroNames.setSelection(j);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if(day == 1)
            oneCutButton.setVisibility(View.VISIBLE);
        else
            oneCutButton.setVisibility(View.GONE);

        oneCutButton.setOnClickListener(view -> {
            isLastHit.setChecked(true);
            damage.setText(Integer.toString(hpPerRound[idx]));
        });

        //스위치 누를 때 listener 생성
        bossSwitch.setOnChangeListener(position -> {
            selectedBossId = bosses.get(position).getBossId();
            setFavoriteView(elements, favoritesList);
        });

        if (isEditing) {
            damage.setText(Long.toString(record.getDamage()));
            pickerIdx[0] = record.getRound() - 1;
            roundDisplay.setText(rounds.get(pickerIdx[0]));

            int bossId = record.getBoss().getBossId();
            isLastHit.setChecked(record.isLastHit());

            Hero leader = record.getLeader();
            selectedHeroId = leader.getHeroId();

            elements.setSelection(leader.getElement());

            for (int i = 0; i < bosses.size(); i++) {
                if (bossId == bosses.get(i).getBossId()) {
                    bossSwitch.setCheckedPosition(i);
                    selectedBossId = bossId;
                    break;
                }
            }
        }

        switchButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            isSetByRecord = isChecked;
            setFavoriteView(elements, favoritesList);
        });

        setFavoriteView(elements, favoritesList);

        addButton.setOnClickListener(view -> {
            if(!isCreateMode) return;

            List<Favorites> favs = database.favoritesDao().getAllFavorites();
            if (favs.size() >= FAVORITE_MAX) {
                showToast("최대 " + FAVORITE_MAX + "개까지 저장이 가능합니다");
                return;
            }
            for (Favorites fav : favs) {
                if (fav.getHeroId() == selectedHeroId) {
                    showToast("이미 즐겨찾기에 추가되어 있습니다");
                    return;
                }
            }

            database.favoritesDao().insert(new Favorites(selectedHeroId));
            refreshManualFavorites(elements, favoritesList);
        });

        deleteButton.setOnClickListener(view -> {
            if (isCreateMode) {
                hsv.setBackgroundResource(R.color.delete_color);
                addButton.setBackgroundResource(R.color.gray);
                deleteButton.setImageResource(R.drawable.icon_undo);
                deleteButton.setBackgroundResource(R.color.bg_icon);
            } else {
                hsv.setBackgroundResource(R.color.bg_icon);
                addButton.setBackgroundResource(R.color.create_color);
                deleteButton.setImageResource(R.drawable.icon_delete);
                deleteButton.setBackgroundResource(R.color.delete_color);
            }
            isCreateMode = !isCreateMode;
        });

        Button createButton = dialog.findViewById(R.id.createButton);
        if (isEditing)
            createButton.setText("수정");
        else
            createButton.setText("생성");


        createButton.setOnClickListener(view -> {
            String sDamage = damage.getText().toString().trim();

            Integer iHeroId;
            iHeroId = heroIds.get(elements.getSelectedItemPosition())
                    .get(heroNames.getSelectedItemPosition());

            int rId = isEditing ? record.getRecordId() : -1;


            if (!sDamage.equals("") && selectedBossId != -1) {
                if (elements.getSelectedItemPosition() == 0) {//normal 선택 상태
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("1성이 리더로 선택되어있습니다. 맞는 선택입니까?")
                            .setCancelable(false)
                            .setPositiveButton("네", (dialog1, id) -> {
                                dialog1.dismiss();
                                saveDataInDatabase(isEditing, rId, sDamage, pickerIdx[0], iHeroId, isLastHit.isChecked());

                                checkDamage(pickerIdx[0]);
                                dialog.dismiss();
                            })
                            .setNegativeButton("아니오", (dialog1, id) -> dialog1.dismiss());
                    AlertDialog alert = builder.create();
                    alert.setTitle("부자연스러운 데이터 발견");
                    alert.show();
                } else {
                    saveDataInDatabase(isEditing, rId, sDamage, pickerIdx[0], iHeroId, isLastHit.isChecked());
                    checkDamage(pickerIdx[0]);
                    dialog.dismiss();
                }
            } else {
                showToast("상대 보스 및 데미지를 입력하세요!");
            }

        });

        Button exitButton = dialog.findViewById(R.id.exitButton);
        exitButton.setOnClickListener(view -> {
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });
    }

    private void checkDamage(int curRound) {
        Log.d("checkDamage", "current:" + pref.getInt("currentRound", -1));
        if(curRound < pref.getInt("currentRound", 0)){
            showToast("과거 기록 수정");
            return;
        }
        if(curRound > pref.getInt("currentRound", 0)){
            showToast("전 회차가 마무리되지 않았습니다.");
            return;
        }

        int idx = (curRound >= hpPerRound.length) ? hpPerRound.length - 1 : curRound;
        long damage = database.recordDao().get1Boss1RoundSum(raidId, selectedBossId, curRound + 1);
        long remain = hpPerRound[idx] - damage;
        Record lhr = database.recordDao().get1Boss1RoundLastHit(raidId, selectedBossId, curRound + 1);
        Log.d("checkDamage", "damage:" + damage + ",remain: " + remain);

        StringBuilder toastStr = new StringBuilder();

        if(remain == 0 && lhr != null){
            List<String> notEliminated = new ArrayList<>();
            List<String> lhNotCorrect = new ArrayList<>();
            for(int i = 0; i < bosses.size(); i++){
                long d = database.recordDao().get1Boss1RoundSum(raidId, bosses.get(i).getBossId(), curRound + 1);
                long r = hpPerRound[idx] - d;
                Record lh = database.recordDao().get1Boss1RoundLastHit(raidId, bosses.get(i).getBossId(), curRound + 1);

                if(r != 0) notEliminated.add(bosses.get(i).getName());
                if(lh != null) lhNotCorrect.add(bosses.get(i).getName());
            }

            if(notEliminated.isEmpty() && lhNotCorrect.isEmpty()){
                SharedPreferences.Editor editor = pref.edit();
                toastStr.append(curRound + 1).append("회차 보스가 모두 처리됐습니다. 다음 회차로 자동 조정됍니다.");
                editor.putInt("currentRound", curRound + 1);
                editor.apply();
            }
            else{
                toastStr.append("보스가 처리되었습니다.\n[남은 체크리스트]\n");
                toastStr.append("피 체크:");
                for(String ne : notEliminated){
                    toastStr.append(ne).append("/");
                }
                toastStr.append("\n막타 체크:");
                for(String lnc : lhNotCorrect){
                    toastStr.append(lnc).append("/");
                }
            }

        }
        else {
            if(remain == 0) toastStr.append("보스 피가 0이 되었으나 막타 체크가 이뤄지지 않았습니다.");//showToast("보스 피가 0이 되었으나 막타 체크가 이뤄지지 않았습니다.");
            else if (remain > 0) {
                if (lhr != null) toastStr.append("막타를 쳤지만 아직 보스의 피가 0이 아닙니다.");
            } else {
                toastStr.append("보스 피가 음수가 되었습니다!");
            }
        }

        if(!toastStr.toString().equals("")) showToast(toastStr.toString());
    }

    private void saveDataInDatabase(boolean isEditing, int recordId, String damage, int pIdx, int heroId, boolean isChecked) {
        if (isEditing) {//수정 중이면 업데이트
            database.recordDao().updateRecord(recordId,
                    Integer.parseInt(damage), selectedBossId,
                    pIdx + 1,
                    heroId, isChecked);
        } else {//새로운 데이터 생성
            //새로 생성할 때에만 preference에 저장
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("currentRound", pIdx);
            editor.putString("recentWrite",
                    "닉네임-" + memberList.get(sMemberIdx).getName() +
                            "/리더-" + database.heroDao().getHero(heroId).getKoreanName());
            editor.apply();

            //새로운 데이터 저장
            Record record1 = new Record(memberList.get(sMemberIdx).getId(), raidId, day);
            record1.setDamage(Long.parseLong(damage));
            record1.setBossId(selectedBossId);
            record1.setLeaderId(heroId);
            record1.setRound(pIdx + 1);
            record1.setLastHit(isChecked);

            //recordList 갱신
            database.recordDao().insertRecord(record1);
        }
        recordList.clear();
        recordList.addAll(getReverseList());
        adapter.notifyDataSetChanged();

        setFabVisibility();
        setTotalDamage();
        refreshSpinnerItem(sMemberIdx);
    }

    private void setFavoriteView(MySpinner elements, LinearLayout favoritesList) {
        if(!isSetByRecord){
            addButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            refreshManualFavorites(elements, favoritesList);
        }
        else{
            addButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            refreshRecordFavorites(elements, favoritesList);
        }
    }

    private void refreshRecordFavorites(Spinner elements, LinearLayout favoritesList){
        favoritesList.removeAllViews();
        List<Integer> leaderIds = database.recordDao()
                .getLeaderIdsDesc(memberList.get(sMemberIdx).getId(), raidId, selectedBossId);
        boolean isFirst = true;
        for(int id: leaderIds){
            LayoutInflater vi = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.card_favorites, null);

            ImageView heroImage = v.findViewById(R.id.heroImage);
            Hero hero = database.heroDao().getHero(id);
            heroImage.setImageResource(
                    getIdentifierFromResource("character_" + hero.getEnglishName(), "drawable"));
            boolean isAlreadyUsed = false;
            for(Record r : recordList){
                if(r.getLeaderId() == id && r.getLeaderId() != selectedHeroId) {
                    isAlreadyUsed = true;
                    break;
                }
            }

            if(!isAlreadyUsed) {
                if(isFirst){
                    isFirst = false;
                    selectedHeroId = hero.getHeroId();
                    elements.setSelection(hero.getElement());
                }
                heroImage.setOnClickListener(view -> {
                    selectedHeroId = hero.getHeroId();
                    elements.setSelection(hero.getElement());
                });
            }
            else{
                setGrayFilterOnImage(heroImage);
                heroImage.setOnClickListener(view -> showToast("이미 사용된 리더입니다!"));
            }

            favoritesList.addView(v);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
            params.setMargins(5, 0, 5, 0);
            v.setLayoutParams(params);
        }
    }

    private void refreshManualFavorites(Spinner elements, LinearLayout favoritesList) {
        favoritesList.removeAllViews();
        List<Favorites> favs = database.favoritesDao().getAllFavoritesAndHero();

        for(Favorites fav : favs) {
            LayoutInflater vi = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View v = vi.inflate(R.layout.card_favorites, null);

            ImageView heroImage = v.findViewById(R.id.heroImage);
            Hero hero = fav.getHero();
            heroImage.setImageResource(
                    getIdentifierFromResource("character_" + hero.getEnglishName(), "drawable"));

            boolean isAlreadyUsed = false;
            for(Record r : recordList){
                if(r.getLeaderId() == hero.getHeroId() && r.getLeaderId() != selectedHeroId) {
                    isAlreadyUsed = true;
                    break;
                }
            }
            if(!isAlreadyUsed) {
                deleteFilterOnImage(heroImage);
                heroImage.setOnClickListener(view -> {
                    if (isCreateMode) {
                        selectedHeroId = hero.getHeroId();
                        elements.setSelection(hero.getElement());
                    } else {
                        database.favoritesDao().delete(fav);
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                });
            }
            else{
                setGrayFilterOnImage(heroImage);
                heroImage.setOnClickListener(view -> {
                    if (isCreateMode) {
                        showToast("이미 사용된 리더입니다!");
                    } else {
                        deleteFilterOnImage(heroImage);
                        database.favoritesDao().delete(fav);
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                });

            }

            if (isCreateMode) {
                favoritesList.addView(v);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                params.setMargins(5, 0, 5, 0);
                v.setLayoutParams(params);
            }
        }
    }

    private void setGrayFilterOnImage(ImageView image) {
        Log.d("RecordMemberFragment", "setGrayFilterOnImage");
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        // make color filter
        ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
        // set filter to ImageView
        image.setColorFilter(colorFilter);
        image.setImageAlpha(128);
    }

    private void deleteFilterOnImage(ImageView image) {
        image.setColorFilter(null);
        image.setImageAlpha(255);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage("정말로 삭제하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("네", (dialog1, id) -> {
                                Record selected = recordList.get(position);
                                database.recordDao().deleteRecord(selected);
                                recordList.remove(position);
                                adapter.notifyItemRemoved(position);

                                for (int i = 0; i < 4; i++) {
                                    if (selectedBossId == selected.getBoss().getBossId()) {
                                        break;
                                    }
                                }
                                checkDamage(selected.getRound() - 1);

                                setTotalDamage();
                                refreshSpinnerItem(sMemberIdx);
                                setFabVisibility();
                                dialog1.dismiss();
                            })
                            .setNegativeButton("아니오", (dialog1, id) -> {
                                dialog1.dismiss();
                                adapter.notifyDataSetChanged();
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("데이터 삭제");
                    alert.show();


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

    private int getRemainedRecord(int id, int raidId) {
        List<Record> recordList = database.recordDao()
                .get1DayRecords(id, raidId, day);

        return MAX_SIZE - recordList.size();
    }

    int getIdentifierFromResource(String name, String defType){
        return getResources().getIdentifier(
                name, defType, requireContext().getPackageName());
    }

    private void setFabVisibility() {
        if (recordList.size() >= MAX_SIZE)
            fab.setVisibility(View.GONE);
        else
            fab.setVisibility(View.VISIBLE);
    }

    private void showToast(String msg) {
        if(myToast != null) myToast.cancel();
        myToast = Toast.makeText(getActivity(), null, Toast.LENGTH_SHORT);
        myToast.setText(msg);
        myToast.show();
    }
}
