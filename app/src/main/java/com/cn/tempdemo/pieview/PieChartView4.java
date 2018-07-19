package com.cn.tempdemo.pieview;

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
import android.view.View;

import com.cn.tempdemo.R;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Created by yunzhao.liu on 2018/7/13
 */

public class PieChartView4 extends View {

    public PieChartView4(Context context) {
        this(context, null);
    }

    public PieChartView4(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView4(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private int centerX;
    private int centerY;
    private float radius;
    private float radius1;
    private RectF rectF;
    private Rect centerTextBound = new Rect();
    private Rect dataTextBound = new Rect();
    private Paint mArcPaint;
    private Paint centerTextPaint;
    private Paint dataPaint;
    private float[] numbers;
    private String[] names;
    private float sum;
    private int[] colors;
    private Random random = new Random();

    private float centerTextSize = 70;//中间字体大小
    private float dataTextSize = 24;
    private int centerTextColor = Color.BLACK;
    private int dataTextColor = Color.BLACK;
    private float circleWidth = 200;
    private int DEFAULT_SIZE = 800;
    private Paint linePaint;
    private boolean isFill = false;

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieView);
        centerTextSize = typedArray.getDimension(R.styleable.PieView_centerTextSize, centerTextSize);
        dataTextSize = typedArray.getDimension(R.styleable.PieView_dataTextSize, dataTextSize);
        circleWidth = typedArray.getDimension(R.styleable.PieView_circleWidth, circleWidth);
        centerTextColor = typedArray.getColor(R.styleable.PieView_centerTextColor, centerTextColor);
        dataTextColor = typedArray.getColor(R.styleable.PieView_dataTextColor, dataTextColor);
        typedArray.recycle();
        initPaint();
    }

    /**
     * 初始化
     */
    private void initPaint() {
        Log.d("liuyz-", "init");
        mArcPaint = new Paint();
        mArcPaint.setStrokeWidth(circleWidth);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);

        centerTextPaint = new Paint();
        centerTextPaint.setTextSize(centerTextSize);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setColor(centerTextColor);

        dataPaint = new Paint();
        dataPaint.setStrokeWidth(2);
        dataPaint.setTextSize(dataTextSize);
        dataPaint.setAntiAlias(true);
        dataPaint.setColor(dataTextColor);

        linePaint = new Paint();
        linePaint.setStrokeWidth(10);
        linePaint.setColor(Color.WHITE);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("liuyz-", "onMeasure");

        int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (measureWidthMode == MeasureSpec.AT_MOST
                && measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE);
        } else if (measureWidthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, measureHeightSize);
        } else if (measureHeightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measureWidthSize, DEFAULT_SIZE);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setRectChange();
    }

    private void setRectChange() {
        Log.d("liuyz", "---onSizeChanged");
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
        //设置半径为宽高最小值的1/4
        if (isFill) {
            mArcPaint.setStyle(Paint.Style.FILL);
            radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 4 - 20;
            radius1 = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 4 + circleWidth / 2;
            rectF = new RectF(centerX - radius1,
                    centerY - radius1,
                    centerX + radius1,
                    centerY + radius1);
        } else {
            mArcPaint.setStyle(Paint.Style.STROKE);
            radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 4;
            rectF = new RectF(centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateAndDraw(canvas);
    }

    /**
     * 计算比例并且绘制扇形和数据
     */
    private void calculateAndDraw(Canvas canvas) {
        Log.d("liuyz-", "onDarw");
        if (numbers == null || numbers.length == 0) {
            return;
        }
        //扇形开始度数
        int startAngle = 0;
        //所占百分比
        float percent;
        //所占度数
        float angle;

        for (int i = 0; i < numbers.length; i++) {
            percent = numbers[i] / (float) sum;
            //获取百分比在360中所占度数
            if (i == numbers.length - 1) {//保证所有度数加起来等于360
                angle = 360 - startAngle;
            } else {
                //ceil：向上取整数，如5.1=6，5.5 =6，5.9=6，-5.1=-5，-5.5=5，-5.9=-5
                angle = (float) Math.ceil(percent * 360);
            }
            //绘制第i段扇形
            drawArc(canvas, startAngle, angle, colors[i]);
            startAngle += angle;

            //绘制数据
            if (numbers[i] <= 0) {
                continue;
            }
            //当前弧线中心点相对于纵轴的夹角度数,由于扇形的绘制是从三点钟方向开始，所以加90
            float arcCenterDegree = 90 + startAngle - angle / 2;
            drawData(canvas, arcCenterDegree, i, percent);
        }
        //绘制中心数字总和
        canvas.drawText(sum + "", centerX - centerTextBound.width() / 2, centerY + centerTextBound.height() / 2, centerTextPaint);
    }


    /**
     * 计算每段弧度的中心坐标
     */
    private float[] calculatePosition(float degree) {
        //由于Math.sin(double a)中参数a不是度数而是弧度，所以需要将度数转化为弧度
        //而Math.toRadians(degree)的作用就是将度数转化为弧度
        //sin 一二正，三四负 sin（180-a）=sin(a)
        float x = 0f;
        float y = 0f;
        //扇形弧线中心点距离圆心的x坐标
        if (isFill) {
            x = (float) (Math.sin(Math.toRadians(degree)) * radius1) / 5 * 3;
        } else {
            x = (float) (Math.sin(Math.toRadians(degree)) * radius);
        }
        //cos 一四正，二三负
        //扇形弧线中心点距离圆心的y坐标
        if (isFill) {
            y = (float) (Math.cos(Math.toRadians(degree)) * radius1) / 5 * 3;
        } else {
            y = (float) (Math.cos(Math.toRadians(degree)) * radius);
        }

        //每段弧度的中心坐标(扇形弧线中心点相对于view的坐标)
        float startX = centerX + x;
        float startY = centerY - y;

        float[] position = new float[2];
        position[0] = startX;
        position[1] = startY;
        return position;
    }

    /**
     * 绘制数据
     */
    private void drawData(Canvas canvas, float degree, int i, float percent) {
        //弧度中心坐标
        float startX = calculatePosition(degree)[0];
        float startY = calculatePosition(degree)[1];

        //获取名称文本大小
        dataPaint.getTextBounds(names[i], 0, names[i].length(), dataTextBound);
        //绘制名称数据，10为纵坐标偏移量
        canvas.drawText(names[i],
                startX - dataTextBound.width() / 2,
                startY - dataTextBound.height() / 2 + 10,
                dataPaint);

        //拼接百分比并获取文本大小
        DecimalFormat df = new DecimalFormat("0.0");
        String percentString = df.format(percent * 100) + "%";
        dataPaint.getTextBounds(percentString, 0, percentString.length(), dataTextBound);

        //绘制百分比数据，10为纵坐标偏移量,5为两段文字的间隙
        canvas.drawText(percentString,
                startX - dataTextBound.width() / 2,
                startY + dataTextBound.height() / 2 + 15,
                dataPaint);
    }

    /**
     * 绘制扇形
     */
    private void drawArc(Canvas canvas, float startAngle, float angle, int color) {
        mArcPaint.setColor(color);
        //+2是为了让每个扇形之间没有间隙
        if (angle != 0) {
            angle += 2f;
        }
        //useCenter:这个参数的作用是设置我们的圆弧在绘画的时候，是否经过圆形
        //当paint的style设置成storke模式时，是看不出效果的，需要设置成fill
        if (isFill) {
            canvas.drawArc(rectF, startAngle, angle, true, mArcPaint);
        } else {
            canvas.drawArc(rectF, startAngle, angle, false, mArcPaint);
        }
    }

    /**
     * 生成随机颜色
     */
    private int randomColor() {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    /**
     * 设置数据(使用随机颜色)
     */
    public void setData(float[] numbers, String[] names) {
        Log.d("liuyz-", "setData--");

        if (numbers == null || numbers.length == 0
                || names == null || names.length == 0) {
            return;
        }
        if (numbers.length != names.length) {
            return;
        }
        this.numbers = numbers;
        this.names = names;
        colors = new int[numbers.length];
        sum = 0;
        for (int i = 0; i < this.numbers.length; i++) {
            //计算总和
            sum += numbers[i];
            //随机颜色
            colors[i] = randomColor();
        }
        //计算总和数字的宽高
        centerTextPaint.getTextBounds(sum + "", 0, (sum + "").length(), centerTextBound);
        invalidate();
    }

    public void updateView(boolean fill) {
        isFill = fill;
        setRectChange();
        invalidate();
    }

}
