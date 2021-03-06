package com.cn.tempdemo.pieview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.cn.tempdemo.R;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by yunzhao.liu on 2018/7/13
 */

public class PieChartView3 extends View {
    public PieChartView3(Context context) {
        this(context, null);
    }

    public PieChartView3(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private int centerSize;
    private int dataSize;
    private float cicleWidth;
    private int centerColor;
    private int dataColor;

    private Paint arcPaint;
    private Paint arcDataPaint;
    private Paint centerPaint;
    private Paint linePaint;

    private int measureWidth;
    private int measureHeight;
    private int centerX;
    private int centerY;

    private int radius;
    private float radius1;
    private RectF rectf;
    private RectF rectfTouch;

    private int ZOOM_SIZE = 20;//点击放大尺寸
    private int DEFAULT_SIZE = 800;//宽高的默认大小

    private float[] angles;//起始角度的集合
    private int position = -1;//点击的position
    private int comparePosition = -2;//用于比较的position
    private int count;//计数，用于扇形放大后缩小或缩小后放大
    private float[] data;
    private String[] name;
    private int[] colors;
    private float totalNum;

    private Rect centerTextBound = new Rect();//中间文本的大小
    private Rect dataTextBound = new Rect();//数据文本的大小

    private Random random = new Random();//用于生成随机颜色
    private float animationValue;

    private boolean isAnimatorEnd = false;//动画结束后才可以点击


    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView);
        centerSize = typedArray.getDimensionPixelSize(R.styleable.PieView_centerTextSize, 100);
        dataSize = typedArray.getDimensionPixelSize(R.styleable.PieView_dataTextSize, 200);
        cicleWidth = typedArray.getDimensionPixelSize(R.styleable.PieView_circleWidth, 200);
        centerColor = typedArray.getColor(R.styleable.PieView_centerTextColor, 20);
        dataColor = typedArray.getColor(R.styleable.PieView_dataTextColor, 20);
        typedArray.recycle();
        initPaint();
    }

    private void initPaint() {
        arcPaint = new Paint();
        arcPaint.setStrokeWidth(cicleWidth);
        arcPaint.setDither(true);//防抖
        arcPaint.setAntiAlias(true);//抗锯齿
        arcPaint.setStyle(Paint.Style.FILL);

        arcDataPaint = new Paint();
//        arcDataPaint.setStrokeWidth(dataSize);
        arcDataPaint.setTextSize(dataSize);
        arcDataPaint.setColor(dataColor);
        arcDataPaint.setAntiAlias(true);
        arcDataPaint.setDither(true);
        arcDataPaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint();
//        centerPaint.setStrokeWidth(centerSize);
        centerPaint.setTextSize(centerSize);

        centerPaint.setColor(centerColor);
        centerPaint.setAntiAlias(true);
        centerPaint.setDither(true);
        centerPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor(Color.WHITE);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int heighMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("liuyz", "onMeasure:" + measureWidth + "x" + measureHeight);

        //设置精准值，就是精准值，设置wrap-Content，match_parent获取都是match_parent
        //设置精确值、match_parent获取模式Exacly，设置wrap_content获取模式at_most
        if (widthMode == MeasureSpec.AT_MOST && heighMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, measureHeight);
        } else if (heighMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidth, DEFAULT_SIZE);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measureWidth = getMeasuredWidth();
        measureHeight = getMeasuredHeight();

        int min = Math.min(measureWidth, measureHeight);

        centerX = measureWidth / 2;
        centerY = measureHeight / 2;

        radius = min / 5 * 2;//最小边长的五分二当半径
        radius1 = radius / 2;

        rectf = new RectF(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        rectfTouch = new RectF(centerX - radius - ZOOM_SIZE,
                centerY - radius - ZOOM_SIZE,
                centerX + radius + ZOOM_SIZE,
                centerY + radius + ZOOM_SIZE);

        Log.d("liuyz", "onSizeChanged:" + measureWidth + "x" + measureHeight + "--" + rectf.toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        goDrawArc(canvas);
    }

    /**
     * 画弧
     */
    private void goDrawArc(Canvas canvas) {
        Log.d("liuyz", "onDraw:" + measureWidth + "x" + measureHeight);
        float startAngle = 0;
        count = 0;
        for (int i = 0; i < data.length; i++) {
            float percent = data[i] / totalNum;
            float sweepAngle = data[i] / totalNum * 360;//每个扇形的角度
            sweepAngle = sweepAngle * animationValue;
            angles[i] = startAngle;
            drawArc(canvas, startAngle, sweepAngle, colors[i], i);
            startAngle += sweepAngle;
            //当前弧线中心点相对于纵轴的夹角度数,由于扇形的绘制是从三点钟方向开始，所以加90
            float arcCenterDegree = 90 + startAngle - sweepAngle / 2;
            drawData(canvas, arcCenterDegree, i, percent);
        }
        setClickPosition();
        //绘制中心区域文字，
        //canvas.drawText(totalNum + "", centerX - centerTextBound.width() / 2, centerY + centerTextBound.height() / 2, centerPaint);
    }

    /**
     * 画数据
     */
    private void drawData(Canvas canvas, float degree, int i, float percent) {
        //弧度中心坐标
        float startX = calculatePosition(degree, radius1)[0];
        float startY = calculatePosition(degree, radius1)[1];
        arcDataPaint.getTextBounds(name[i], 0, name[i].length(), dataTextBound);
        ////绘制百分比数据，10为纵坐标偏移量
        canvas.drawText(name[i],
                startX - dataTextBound.width() / 2,
                startY - dataTextBound.height() / 2 + 10,
                arcDataPaint);

        DecimalFormat df = new DecimalFormat("0.00");
        String percentString = df.format(percent * 100) + "%";
        arcDataPaint.getTextBounds(percentString, 0, percentString.length(), dataTextBound);

        //绘制百分比数据，10为纵坐标偏移量,5为两段文字的间隙
        canvas.drawText(percentString,
                startX - dataTextBound.width() / 2,
                startY + dataTextBound.height() / 2 + 15,
                arcDataPaint);
    }

    /**
     * 计算点击的x、y坐标，相对于这个自定义控件的左上角0,0坐标，而不是弧形区域的中心坐标
     */
    private float[] calculatePosition(float degree, float radius) {
        //由于Math.sin(double a)中参数a不是度数而是弧度，所以需要将度数转化为弧度
        //而Math.toRadians(degree)的作用就是将度数转化为弧度
        float x = 0f;
        float y = 0f;
        //扇形弧线中心点距离圆心的x坐标
        //sin 一二正，三四负 sin（180-a）=sin(a)
        x = (float) (Math.sin(Math.toRadians(degree)) * radius);
        //扇形弧线中心点距离圆心的y坐标
        //cos 一四正，二三负
        y = (float) (Math.cos(Math.toRadians(degree)) * radius);

        //每段弧度的中心坐标(扇形弧线中心点相对于view的坐标)
        float startX = centerX + x;
        float startY = centerY - y;

        float[] position = new float[2];
        position[0] = startX;
        position[1] = startY;
        return position;
    }

    /**
     * 直接画狐
     */
    private void drawArc(Canvas canvas, float startAngle, float rotateAngle, int color, int i) {
        Log.d("liuyz", startAngle + "x" + rotateAngle);
        Log.d("actionDown", startAngle + "x" + rotateAngle);
        arcPaint.setColor(color);
        if (position - 1 == i && !(comparePosition == position)) {
            count += 1;
            canvas.drawArc(rectfTouch, startAngle, rotateAngle, true, arcPaint);
        } else {
            count += 0;
            canvas.drawArc(rectf, startAngle, rotateAngle, true, arcPaint);
        }
    }

    /**
     * 设置点击位置
     */
    private void setClickPosition() {
        if (count > 0) {
            comparePosition = position;
        } else {
            comparePosition = -2;
        }
    }

    /**
     * 设置数据
     */
    public void setData(float[] data, String[] name) {
        if (data == null || data.length == 0) return;
        if (name == null || name.length == 0) return;
        this.data = data;
        this.name = name;
        colors = new int[name.length];
        for (int i = 0; i < name.length; i++) {
            colors[i] = randomColor();
            totalNum += data[i];
        }
        angles = new float[name.length];

        //计算总和数字的宽高
        centerPaint.getTextBounds(totalNum + "", 0, (totalNum + "").length(), centerTextBound);
        invalidate();
    }

    /**
     * 设置随机颜色
     */
    private int randomColor() {
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }

    /**
     * 设置动画
     */
    public void startAnimation(int duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimatorEnd = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isAnimatorEnd) break;//动画进行时，不能点击
                float relative_centerX = centerX;
                float relative_centerY = -centerY;
                //坐标系  左上正，右下负
                float x = event.getX() - relative_centerX;
                float y = -event.getY() - relative_centerY;
                //angel=Math.atan2(y,x) => x 指定点的 x 坐标的数字，y 指定点的 y 坐标的数字，计算出来的结果angel是一个弧度值,也可以表示相对直角三角形对角的角，其中 x 是临边边长，而 y 是对边边长
                //Math.atan2(y,x)函数返回点(x,y)和原点(0,0)之间直线的倾斜角.那么如何计算任意两点间直线的倾斜角呢?只需要将两点x,y坐标分别相减得到一个新的点(x2-x1,y2-y1),转换可以实现计算出两点间连线的夹角Math.atan2(y2-y1,x2-x1)
                //函数atan2(y,x)中参数的顺序是倒置的，atan2(y,x)计算的值相当于点(x,y)的角度值
                //坐标系  左上正，右下负，结果为正表示从 X 轴逆时针旋转的角度，结果为负表示从 X 轴顺时针旋转的角度
                double v = Math.atan2(y, x);
                float touchAngle = (float) Math.toDegrees(v);//弧度转换为角度
                Log.d("actionDown:", v + "==" + touchAngle);

                //当前弧线 起始点 相对于 横轴 的夹角度数,由于扇形的绘制是从三点钟方向开始计为0度，所以需要下面的转换
                if (x > 0 && y > 0) {//1象限
                    touchAngle = 360 - touchAngle;
                } else if (x < 0 && y > 0) {//2象限
                    touchAngle = 360 - touchAngle;
                } else if (x < 0 && y < 0) {//3象限
                    touchAngle = Math.abs(touchAngle);
                } else if (x > 0 && y < 0) {//4象限
                    touchAngle = Math.abs(touchAngle);
                }

                //取点击半径
                float touchRadius = (float) Math.sqrt(x * x + y * y);//sqrt：对数值开根号
                if (touchRadius < radius) {
                    //如果找到关键字，则返回值为关键字在数组中的位置索引，且索引从0开始
                    //如果没有找到关键字，返回值为 负 的插入点值，所谓插入点值就是第一个比关键字大的元素在数组中的位置索引，
                    // 而且这个位置索引从1开始。
                    position = -Arrays.binarySearch(angles, touchAngle) - 1;
                    invalidate();
                }
                Log.d("actionDown:", "==" + position);
                break;
        }
        return super.onTouchEvent(event);
    }
}
