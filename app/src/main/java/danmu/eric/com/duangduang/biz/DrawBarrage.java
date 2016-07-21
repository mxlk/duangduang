package danmu.eric.com.duangduang.biz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import danmu.eric.com.duangduang.beans.BarrageItem;

/**
 * Created by Administrator on 2016/1/21.
 */
public class DrawBarrage extends View{

    Paint paintText;
    private List<BarrageItem> barrageItems = new ArrayList<BarrageItem>();
    private Random random = new Random();

    private static final String[] COLORS= new String[]{
            "#ee339900",
            "#ee990000",
            "#eeFF3366",
            "#ee3366FF",
            "#ee00cc33",
            "#eeF9F9F9",
            "#eeF9F9F9",
            "#eeF9F9F9",
            "#eeCCCCCC",
            "#eeFF0099",
            "#eeFF3030",
            "#eeffcc00",
            "#eeFF9900",
            "#ee00bbdd",
            "#ee00bbdd",
            "#eeFF3333",
            "#ee33FF33",
            "#ee3333FF"
    };

    private Rect rect;
    private int textSize;
    private int line_height;
    private int lines;
    private int[] line_book = new int[100];
    private AddRemoveCallBack addRemoveCallBack;

    public interface AddRemoveCallBack{
        void removeView();
    }

    public void setAddRemoveCallBack(AddRemoveCallBack addRemoveCallBack){
        this.addRemoveCallBack = addRemoveCallBack;
    }

    public DrawBarrage(Context context) {
        this(context, null);
    }

    public DrawBarrage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawBarrage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setAlpha(50);
        paintText.setShadowLayer(2, 2, 2, Color.GRAY);

        rect = new Rect();
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 28, getResources().getDisplayMetrics());
        paintText.setTextSize(textSize);
        paintText.getTextBounds("ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()", 0, 33, rect);

        line_height = rect.height();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        BarrageItem barrageItem;
        int loopCount = barrageItems.size() > 35 ? 35 : barrageItems.size();
        for (int i = 0; i < loopCount; i++){
            barrageItem = barrageItems.get(i);
            paintText.setColor(barrageItem.getColor());
            canvas.drawText(barrageItem.getItemText(), barrageItem.getX(), barrageItem.getY(), paintText);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lines = getMeasuredHeight() / line_height;
        Log.d("Lines", String.valueOf(lines));
    }

    private int getBestLine(){
        int line = 1 + random.nextInt(lines);
        if(line_book[line] == 0){
            line_book[line] += 1;
            return line;
        }else{
            int min = 999;
            int min_index = 1;
            for(int i = line + 1; i < lines; i++){
                if(line_book[i] == 0){
                    line_book[i] += 1;
                    return i;
                }
                else if(line_book[i] < min){
                    min = line_book[i];
                    min_index = i;
                }
            }
            for(int i=line;i>=1;i--){
                if(line_book[i] ==0){
                    line_book[i] +=1;
                    return i;
                }else if(line_book[i]<min){
                    min = line_book[i];
                    min_index = i;
                }
            }
            line_book[min_index] +=1;
            return min_index;
        }
//        return 0;
    }

    public void addBarrage(final BarrageItem barrageItem){
        barrageItem.setX(getMeasuredWidth());

        int bestLine = getBestLine();
        barrageItem.setLine(bestLine);

        barrageItem.setY(bestLine * line_height);
        barrageItem.setColor(Color.parseColor(COLORS[random.nextInt(COLORS.length)]));

        paintText.getTextBounds(barrageItem.getItemText(), 0, barrageItem.getItemText().length(), rect);
        barrageItem.setTextLength(rect.width());
        barrageItems.add(barrageItem);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(barrageItem.getX(), -barrageItem.getTextLength());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int x = (int) valueAnimator.getAnimatedValue();
                barrageItem.setX(x);
                postInvalidate();
            }
        });
        valueAnimator.setDuration(2500 + barrageItem.getItemText().length() * 70 + random.nextInt(1500));
        valueAnimator.setInterpolator(new LinearInterpolator());

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                barrageItems.remove(barrageItem);
                line_book[barrageItem.getLine()]-=1;
                if(barrageItems.size() == 0){
                    if(addRemoveCallBack != null){
                        addRemoveCallBack.removeView();
                    }
                }
            }
        });
        valueAnimator.start();

    }
}
