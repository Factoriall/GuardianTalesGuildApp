package org.techtown.gtguildraid.Fragments;

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
    private BottomSheetListener mListener;
    private LinearLayout mGallery;
    private LayoutInflater mInflater;
    private int[] mImgIds;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gallery, container, false);
        mInflater = LayoutInflater.from(getContext());
        initData();
        initView(view);
        return view;
    }

    public interface BottomSheetListener{
        void onImageClicked(int imgId);
    }

    BossBottomSheetDialog(Fragment fragment){
        this.mListener = (BottomSheetListener) fragment;
    }

    private void initData() {
        mImgIds = new int[] {
                R.drawable.boss_1, R.drawable.boss_2, R.drawable.boss_3, R.drawable.boss_4,
                R.drawable.boss_bug, R.drawable.boss_cyborg,
                R.drawable.boss_devil, R.drawable.boss_fairy, R.drawable.boss_gast,
                R.drawable.boss_harvester, R.drawable.boss_invader, R.drawable.boss_madpanda,
                R.drawable.boss_mino, R.drawable.boss_monster, R.drawable.boss_sapa,
                R.drawable.boss_shadow, R.drawable.boss_slime
        };
    }

    private void initView(View view) {
        mGallery = (LinearLayout) view.findViewById(R.id.id_gallery);

        for (int i = 0; i < mImgIds.length; i++) {
            View mView = mInflater.inflate(R.layout.dialog_gallery_item, mGallery, false);
            final ImageView img = (ImageView) mView
                    .findViewById(R.id.id_index_gallery_item_image);
            img.setImageResource(mImgIds[i]);

            mGallery.addView(mView);

            final int finalI = i;
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onImageClicked(mImgIds[finalI]);
                    dismiss();
                }
            });
        }
    }
}
