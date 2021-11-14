package org.techtown.gtguildraid.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gtguildraid.interfaces.MyButtonClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class MySwipeHelper extends ItemTouchHelper.SimpleCallback {
    //ItemTouchHelper의 simpleCallback을 통해 swipe 구현
    int buttonWidth;
    private RecyclerView recyclerView;
    private List<MyButton> buttonList;
    private GestureDetector gestureDetector;
    private int swipePosition = -1;
    private float swipeThreshold = 0.5f;
    private Map<Integer, List<MyButton>> buttonBuffer;
    private Queue<Integer> removerQueue;

    private void recoverSwipedItem() {//removerQueue의 것을 원래대로 돌려놓기
        while(!removerQueue.isEmpty()){
            int pos = removerQueue.poll();
            if(pos > -1)
                recyclerView.getAdapter().notifyItemChanged(pos);
        }
    }

    //swipeHelper 구현
    public MySwipeHelper(Context context, RecyclerView recyclerView, int buttonWidth) {
        super(0, ItemTouchHelper.LEFT);//left 방향 swipe 구현
        this.recyclerView = recyclerView;
        this.buttonList = new ArrayList<>();
        //제스쳐를 쉽게 구분, 중복 코드를 줄여주는 gesturedetector 객체
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {//어디가 클릭되었는지 확인
                for (MyButton button : buttonList) {
                    if (button.onClick(e.getX(), e.getY()))
                        break;
                }
                return true;
            }
        };//버튼 클릭 listener
        this.gestureDetector = new GestureDetector(context, gestureListener);

        //swipePosition 따라 확인
        @SuppressLint("ClickableViewAccessibility")
        View.OnTouchListener onTouchListener = (view, motionEvent) -> {
            if (swipePosition < 0) return false;//swipe 안됨
            Point point = new Point((int) motionEvent.getRawX(), (int) motionEvent.getRawY());

            RecyclerView.ViewHolder swipeViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePosition);
            View swipedItem = swipeViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            //swipe 클릭 및 up, move시 그 탭에 있으면 onTouchEvent 실시
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                    motionEvent.getAction() == MotionEvent.ACTION_UP ||
                    motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y) {
                    gestureDetector.onTouchEvent(motionEvent);
                } else {
                    removerQueue.add(swipePosition);
                    swipePosition = -1;
                    recoverSwipedItem();
                }
            }
            return false;
        };
        this.recyclerView.setOnTouchListener(onTouchListener);
        this.buttonBuffer = new HashMap<>();
        this.buttonWidth = buttonWidth;


        removerQueue = new LinkedList<Integer>(){
            @Override
            public boolean add(Integer i) {
                if(contains(i))
                    return false;
                else
                    return super.add(i);
            }
        };

        attachSwipe();
    }

    private void attachSwipe() {//recyclerView에 swipeListener 부착
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    //버튼 구현
    public class MyButton {
        private String text;
        private int imageResId, textSize, color, pos;
        private RectF clickRegion;
        private MyButtonClickListener listener;
        private Context context;

        public MyButton(Context context, String text, int textSize, int imageResId, int color, MyButtonClickListener listener) {
            this.text = text;
            this.imageResId = imageResId;
            this.textSize = textSize;
            this.color = color;
            this.listener = listener;
            this.context = context;
        }

        //버튼 클릭 시 listener 통해 pos 클릭 정보를 보내줌
        public boolean onClick(float x, float y){
            if(clickRegion != null && clickRegion.contains(x,y)){
                listener.onClick(pos);
                return true;
            }
            return false;
        }

        //canvas를 활용해서 클릭 버튼을 구현해줌
        public void onDraw(Canvas c, RectF rectF, int pos){
            Paint p = new Paint();
            p.setColor(color);
            c.drawRect(rectF, p);

            p.setColor(Color.BLACK);
            p.setTextSize(textSize);

            Rect r = new Rect();
            float cHeight = rectF.height();
            float cWidth = rectF.width();
            p.setTextAlign(Paint.Align.LEFT);
            p.getTextBounds(text, 0, text.length(), r);

            //이미지 정보 없음 text 정보 적어줌
            if(imageResId == 0) {
                float x = cWidth / 2f - r.width() / 2f - r.left;
                float y = cHeight / 2f + r.height() / 2f - r.bottom;
                c.drawText(text, rectF.left + x, rectF.top + y, p);
            }
            else{//아니면 bitmap 기록
                Drawable d = ContextCompat.getDrawable(context, imageResId);
                Bitmap bitmap = drawableToBitmap(d);

                c.drawBitmap(bitmap, (rectF.left + rectF.right)/2, (rectF.top + rectF.bottom)/2, p);
            }
            clickRegion = rectF;
            this.pos = pos;
        }
    }

    private Bitmap drawableToBitmap(Drawable d) {
        if(d instanceof BitmapDrawable)
            return ((BitmapDrawable) d).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),
                d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0,0,canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bitmap;
    }

    //위아래로 움직일 때
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    //양옆으로 움직일 때
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        if(swipePosition != pos)//다르면 removerQueue에 저장해서 되돌림
            removerQueue.add(swipePosition);
        swipePosition = pos;
        if(buttonBuffer.containsKey(swipePosition))
            buttonList = buttonBuffer.get(swipePosition);
        else
            buttonList.clear();
        buttonBuffer.clear();
        swipeThreshold = 0.5f * buttonList.size() * buttonWidth;
    }

    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder){
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    //itemTouchListener의 onChildDraw를 override를 해서 스와이프 시 버튼이 나타나게 구현
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;
        if(pos < 0){//음수면 끝
            swipePosition = pos;
            return;
        }
        //dx가 0보다 작으면 buffer.size()에 따라서 버튼의 크기를 조절
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(dX < 0){
                List<MyButton> buffer = new ArrayList<>();//버튼 개수 넣은 부분
                if(!buttonBuffer.containsKey(pos)){
                    instantiateMyButton(buffer);
                    buttonBuffer.put(pos, buffer);
                }
                else{
                    buffer = buttonBuffer.get(pos);
                }
                translationX = dX * buffer.size() * buttonWidth / itemView.getWidth();
                drawButton(c, itemView, buffer, pos, translationX);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    //onChildDraw따라서 버튼 그리기, 한족으로 붙어서 진행
    public void drawButton(Canvas c, View itemView, List<MyButton> buffer, int pos, float translationX){
        float right = itemView.getRight();
        float dButtonWidth = -1 * translationX / buffer.size();
        for(MyButton button : buffer){
            float left = right - dButtonWidth;
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), pos);
            right = left;
        }
    }

    public abstract void instantiateMyButton(List<MyButton> buffer);
}
