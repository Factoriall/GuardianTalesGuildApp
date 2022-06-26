package org.techtown.gtguildraid.raid;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.statistics.BossImageRecyclerAdapter;
import org.techtown.gtguildraid.models.BossImage;

import java.util.Arrays;

public class BossBottomSheetDialog
        extends BottomSheetDialogFragment
        implements BossImageRecyclerAdapter.BottomSheetListener{

    private final BottomSheetListener mListener;
    private BossImage[] mImgIds;


    public interface BottomSheetListener{
        void onImageClicked(BossImage boss);
    }

    public BossBottomSheetDialog(Activity activity){
        this.mListener = (BottomSheetListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gallery, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 3));

        initData();
        BossImageRecyclerAdapter adapter = new BossImageRecyclerAdapter(Arrays.asList(mImgIds), this);
        recyclerView.setAdapter(adapter);
        return view;
    }


    private void initData() {
        mImgIds = new BossImage[] {
                new BossImage("1", R.drawable.boss_1, "보스1"), new BossImage("2", R.drawable.boss_2, "보스2"),
                new BossImage("3", R.drawable.boss_3, "보스3"), new BossImage("4", R.drawable.boss_4, "보스4"),
                new BossImage("bug", R.drawable.boss_bug, "황소벌레"), new BossImage("cyborg", R.drawable.boss_cyborg, "에리나"),
                new BossImage("devil", R.drawable.boss_devil, "고대악마"), new BossImage("fairy", R.drawable.boss_fairy, "요정"),
                new BossImage("gast", R.drawable.boss_gast, "가스트"), new BossImage("harvester", R.drawable.boss_harvester, "하베스터"),
                new BossImage("invader", R.drawable.boss_invader, "인베이더"), new BossImage("madpanda", R.drawable.boss_madpanda, "매드팬더"),
                new BossImage("mino", R.drawable.boss_mino,"미노타"), new BossImage("monster", R.drawable.boss_monster,"유사괴물"),
                new BossImage("sapa", R.drawable.boss_sapa, "사파두령"), new BossImage("shadow", R.drawable.boss_shadow,"그림자마수"),
                new BossImage("slime", R.drawable.boss_slime, "용암슬라임"), new BossImage("elpaba", R.drawable.boss_elpaba, "엘파바"),
                new BossImage("chief", R.drawable.boss_chief, "고블린족장"), new BossImage("marina", R.drawable.boss_marina, "마리나"),
                new BossImage("ninetail", R.drawable.boss_ninetail, "구미호"), new BossImage("duncan", R.drawable.boss_duncan, "던컨")
        };
    }

    @Override
    public void onImageClicked(BossImage boss) {
        mListener.onImageClicked(boss);
        dismiss();
    }
}
