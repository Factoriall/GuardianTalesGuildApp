package org.techtown.gtguildraid.etc;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.techtown.gtguildraid.R;
import org.techtown.gtguildraid.adapters.BossImageRecyclerAdapter;
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
                new BossImage("1", R.drawable.boss_1), new BossImage("2", R.drawable.boss_2),
                new BossImage("3", R.drawable.boss_3), new BossImage("4", R.drawable.boss_4),
                new BossImage("bug", R.drawable.boss_bug), new BossImage("cyborg", R.drawable.boss_cyborg),
                new BossImage("devil", R.drawable.boss_devil), new BossImage("fairy", R.drawable.boss_fairy),
                new BossImage("gast", R.drawable.boss_gast), new BossImage("harvester", R.drawable.boss_harvester),
                new BossImage("invader", R.drawable.boss_invader), new BossImage("madpanda", R.drawable.boss_madpanda),
                new BossImage("mino", R.drawable.boss_mino), new BossImage("monster", R.drawable.boss_monster),
                new BossImage("sapa", R.drawable.boss_sapa), new BossImage("shadow", R.drawable.boss_shadow),
                new BossImage("slime", R.drawable.boss_slime), new BossImage("elpaba", R.drawable.boss_elpaba),
                new BossImage("chief", R.drawable.boss_chief), new BossImage("marina", R.drawable.boss_marina)
        };
    }

    @Override
    public void onImageClicked(BossImage boss) {
        mListener.onImageClicked(boss);
        dismiss();
    }
}
