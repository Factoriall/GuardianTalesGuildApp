package org.techtown.gtguildraid.etc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.techtown.gtguildraid.R;

public class BossBottomSheetDialog extends BottomSheetDialogFragment {
    private final BottomSheetListener mListener;
    private LayoutInflater mInflater;

    private static class imageInfo{
        final String imgName;
        final int imgId;

        public imageInfo(String imgName, int imgId) {
            this.imgName = imgName;
            this.imgId = imgId;
        }

        public String getImgName() {
            return imgName;
        }

        public int getImgId() {
            return imgId;
        }
    }

    private imageInfo[] mImgIds;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gallery_boss, container, false);
        mInflater = LayoutInflater.from(getContext());
        initData();
        initView(view);
        return view;
    }

    public interface BottomSheetListener{
        void onImageClicked(String imgId);
    }

    public BossBottomSheetDialog(Fragment fragment){
        this.mListener = (BottomSheetListener) fragment;
    }

    private void initData() {
        mImgIds = new imageInfo[] {
                new imageInfo("1", R.drawable.boss_1), new imageInfo("2", R.drawable.boss_2),
                new imageInfo("3", R.drawable.boss_3), new imageInfo("4", R.drawable.boss_4),
                new imageInfo("bug", R.drawable.boss_bug), new imageInfo("cyborg", R.drawable.boss_cyborg),
                new imageInfo("devil", R.drawable.boss_devil), new imageInfo("fairy", R.drawable.boss_fairy),
                new imageInfo("gast", R.drawable.boss_gast), new imageInfo("harvester", R.drawable.boss_harvester),
                new imageInfo("invader", R.drawable.boss_invader), new imageInfo("madpanda", R.drawable.boss_madpanda),
                new imageInfo("mino", R.drawable.boss_mino), new imageInfo("monster", R.drawable.boss_monster),
                new imageInfo("sapa", R.drawable.boss_sapa), new imageInfo("shadow", R.drawable.boss_shadow),
                new imageInfo("slime", R.drawable.boss_slime), new imageInfo("elpaba", R.drawable.boss_elpaba),
                new imageInfo("chief", R.drawable.boss_chief), new imageInfo("marina", R.drawable.boss_marina)
        };
    }

    private void initView(View view) {
        LinearLayout mGallery = view.findViewById(R.id.id_gallery);

        for (int i = 0; i < mImgIds.length; i++) {
            View mView = mInflater.inflate(R.layout.dialog_gallery_item, mGallery, false);
            final ImageView img = mView.findViewById(R.id.id_index_gallery_item_image);
            img.setImageResource(mImgIds[i].getImgId());

            mGallery.addView(mView);

            final int finalI = i;
            img.setOnClickListener(view1 -> {
                mListener.onImageClicked(mImgIds[finalI].getImgName());
                dismiss();
            });
        }
    }
}
