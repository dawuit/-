package chn.wu.jianhui.speed_detection.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

/*
* @author W.J.H 
* @email jianhui.wu.chn@hotmail.com
* @create at 2018/10/15
*/
public class SpeedView extends View {
    private String color_outcircle = "#ffffff";
    private String color_incircle = "#DEDEDE";
    private String color_bg_outcircle = "#DEDEDE";
    private String color_bg_incircle = "#58ADE4";
    private String color_progress = "#87CEEB";
    private String color_smart_circle = "#C2B9B0";
    private String color_indicator_left = "#E1DCD6";
    private String color_indicator_right = "#F4EFE9";
    //动画实现
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(accelerated > 0)
            {
                if(progress >= progress_o)
                    progress = progress_o;
                else
                    progress += accelerated;
            }
            else
            {
                if(progress <= progress_o)
                    progress = progress_o;
                else
                    progress += accelerated;
            }

            invalidate();
        }
    };
    //目标进度
    private double progress_o = 0;
    /**
     * 当前进度
     */
    private double progress = 150;

    //数据更新周期(秒)
    private double period = 1.0;

    //加速计算
    private double accelerated = (progress_o - progress) / (period * 25);

    private double direction = 0;
    /**
     * 最大进度
     */
    private int maxProgress = 240;
    /**
     * 要画的内容的实际宽度
     */
    private int contentWidth;
    /**
     * view的实际宽度
     */
    private int viewWidth;
    /**
     * view的实际高度
     */
    private int viewHeight;
    /**
     * 外环线的宽度
     */
    private int outCircleWidth = 1;
    /**
     * 外环的半径
     */
    private int outCircleRadius = 0;
    /**
     * 内环的半径
     */
    private int inCircleRedius = 0;
    /**
     * 内环与外环的距离
     */
    private int outAndInDistance = 0;
    /**
     * 内环的宽度
     */
    private int inCircleWidth = 0;
    /**
     * 刻度盘距离它外面的圆的距离
     */
    private int dialOutCircleDistance = 0;
    /**
     * 内容中心的坐标
     */
    private int[] centerPoint = new int[2];


    /**
     * 刻度线的数量
     */
    private int dialCount = 0;
    /**
     * 每隔几次出现一个长线
     */
    private int dialPer = 0;
    /**
     * 长线的长度
     */
    private int dialLongLength = 0;
    /**
     * 短线的长度
     */
    private int dialShortLength = 0;
    /**
     * 刻度线距离圆心最远的距离
     */
    private int dialRadius = 0;


    /**
     * 圆弧开始的角度
     */
    private int startAngle = 0;
    /**
     * 圆弧划过的角度
     */
    private int allAngle = 0;

    private Paint mPaint;
    /**
     * 刻度盘上数字的数量
     */
    private int figureCount = 13;


    public SpeedView(Context context) {
        this(context, null);
    }

    public SpeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        //开启定时器
        timer.schedule(timerTask, Math.round(period*1000/25), Math.round(period*1000/25));
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initValues();
    }

    /**
     * 初始化尺寸
     */
    private void initValues() {
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        contentWidth = viewWidth > viewHeight ? viewHeight : viewWidth;
        outCircleWidth = 6;
        outCircleRadius = contentWidth / 2 - outCircleWidth;
        outAndInDistance = (int) (contentWidth / 26.5);
        inCircleWidth = (int) (contentWidth / 18.7);
        centerPoint[0] = viewWidth / 2;
        centerPoint[1] = viewHeight / 2;
        inCircleRedius = outCircleRadius - outAndInDistance - inCircleWidth / 2;
        startAngle = 150;
        allAngle = 240;
        dialOutCircleDistance = inCircleWidth;

        dialCount = 120;
        dialPer = 10;
        dialLongLength = (int) (dialOutCircleDistance / 1.2);
        dialShortLength = (int) (dialLongLength / 1.8);
        dialRadius = inCircleRedius - dialOutCircleDistance;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawStatic(canvas);
        drawDynamic(canvas);
    }

    /**
     * 绘制静态的部分
     *in
     * @param canvas
     */
    private void drawStatic(Canvas canvas) {
        drawOutCircle(canvas);
        drawCircleWithRound(startAngle, allAngle, inCircleWidth, inCircleRedius, Color.parseColor(color_incircle), canvas);
        drawDial(startAngle, allAngle, dialCount, dialPer, dialLongLength, dialShortLength, dialRadius, canvas);
        drawBackGround(canvas);
        drawFigure(canvas, figureCount);
    }

    private void drawFigure(Canvas canvas, int count) {
        int figure = 0;
        int angle;
        for (int i = 0; i < count; i++) {
            figure = (int) (240 / (1f * count-1) * i);
            angle = (int) ((allAngle) / ((count-1) * 1f) * i) + startAngle;
            int[] pointFromAngleAndRadius = getPointFromAngleAndRadius(angle, dialRadius - dialLongLength * 2 );
            mPaint.setTextSize(30);
            mPaint.setColor(Color.parseColor("#ffffff"));
            mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.save();
            canvas.rotate(angle+90,pointFromAngleAndRadius[0],pointFromAngleAndRadius[1]);
            canvas.drawText(figure+"",pointFromAngleAndRadius[0],pointFromAngleAndRadius[1],mPaint);
            canvas.restore();
        }
    }

    /**
     * 画内层背景
     *
     * @param canvas
     */
    private void drawBackGround(Canvas canvas) {
        mPaint.setColor(Color.parseColor(color_bg_outcircle));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(outCircleRadius / 3 / 2.5f);
        canvas.drawCircle(centerPoint[0], centerPoint[1], outCircleRadius / 3.2f, mPaint);
        mPaint.setColor(Color.parseColor(color_bg_incircle));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerPoint[0], centerPoint[1], (outCircleRadius / 3f / 2), mPaint);
    }

    /**
     * 画刻度盘
     *
     * @param startAngle  开始画的角度
     * @param allAngle    总共划过的角度
     * @param dialCount   总共的线的数量
     * @param per         每隔几个出现一次长线
     * @param longLength  长线的长度
     * @param shortLength 短线的长度
     * @param radius      距离圆心最远的地方的半径
     */
    private void drawDial(int startAngle, int allAngle, int dialCount, int per, int longLength, int shortLength, int radius, Canvas canvas) {
        int length;
        int angle;
        for (int i = 0; i <= dialCount; i++)
        {
            angle = (int) ((allAngle) / (dialCount * 1f) * i) + startAngle;
            mPaint.setColor(getColor(i / (float)dialCount * 120));

            if (i % 5 == 0)
            {
                length = longLength;
            }
            else
            {
                length = shortLength;
            }
            drawSingleDial(angle, length, radius, canvas);
        }
    }

    /**
     * 画刻度中的一条线
     *
     * @param angle  所处的角度
     * @param length 线的长度
     * @param radius 距离圆心最远的地方的半径
     */
    private void drawSingleDial(int angle, int length, int radius, Canvas canvas) {
        mPaint.setStrokeWidth(3);
        int[] startP = getPointFromAngleAndRadius(angle, radius);
        int[] endP = getPointFromAngleAndRadius(angle, radius - length);
        canvas.drawLine(startP[0], startP[1], endP[0], endP[1], mPaint);
    }

    /**
     * 画最外层的圆
     *
     * @param canvas
     */
    private void drawOutCircle(Canvas canvas) {
        mPaint.setStrokeWidth(outCircleWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor(color_outcircle));
        canvas.drawCircle(centerPoint[0], centerPoint[1], outCircleRadius, mPaint);
    }

    /**
     * 绘制动态的部分
     *
     * @param canvas
     */
    private void drawDynamic(Canvas canvas) {

        drawProgress(progress, canvas);
        drawIndicator(progress, canvas);
        drawCurrentProgressTv(progress, direction, canvas);
    }

    /**
     * 绘制当前进度是文字
     *
     * @param progress
     * @param canvas
     */
    private void drawCurrentProgressTv(double progress, double direction, Canvas canvas) {
        mPaint.setTextSize(60);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setColor(Color.parseColor(color_progress));
        mPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float baseLine1 = centerPoint[1] + (outCircleRadius / 20f * 11f - fontMetrics.top - fontMetrics.bottom);
        mPaint.setColor(Color.parseColor("#ffffff"));
        canvas.drawText(new DecimalFormat("##0.00").format(progress)+" KM/H", centerPoint[0], baseLine1, mPaint);
        mPaint.setTextSize(45);
        canvas.drawText(new DecimalFormat("##0.00").format(direction) +"°", centerPoint[0], baseLine1 + 50, mPaint);
    }

    /**
     * 画指针以及他的背景
     *
     * @param progress
     * @param canvas
     */
    private void drawIndicator(double progress, Canvas canvas) {
        drawPointer(canvas);
        drawIndicatorBg(canvas);
    }

    /**
     * 指针的最远处的半径和刻度线的一样
     */
    private void drawPointer(Canvas canvas) {
        RectF rectF = new RectF(centerPoint[0] - (int) (outCircleRadius / 3f / 2 / 2),
                centerPoint[1] - (int) (outCircleRadius / 3f / 2 / 2), centerPoint[0] + (int) (outCircleRadius / 3f / 2 / 2), centerPoint[1] + (int) (outCircleRadius / 3f / 2 / 2));
        int angle = (int) ((allAngle) / (maxProgress * 1f) * progress) + startAngle;
        //指针的定点坐标
        int[] peakPoint = getPointFromAngleAndRadius(angle, dialRadius);
        //顶点朝上，左侧的底部点的坐标
        int[] bottomLeft = getPointFromAngleAndRadius(angle - 90, (int) (outCircleRadius / 3f / 2 / 2));
        //顶点朝上，右侧的底部点的坐标
        int[] bottomRight = getPointFromAngleAndRadius(angle + 90, (int) (outCircleRadius / 3f / 2 / 2));
        Path path = new Path();

        mPaint.setColor(Color.parseColor(color_indicator_left));
        path.moveTo(centerPoint[0], centerPoint[1]);
        path.lineTo(peakPoint[0], peakPoint[1]);
        path.lineTo(bottomLeft[0], bottomLeft[1]);
        path.close();
        canvas.drawPath(path, mPaint);
        canvas.drawArc(rectF, angle - 181, 91, true, mPaint);
        Log.e("InstrumentView", "drawPointer" + angle);


        mPaint.setColor(Color.parseColor(color_indicator_right));
        path.reset();
        path.moveTo(centerPoint[0], centerPoint[1]);
        path.lineTo(peakPoint[0], peakPoint[1]);
        path.lineTo(bottomRight[0], bottomRight[1]);
        path.close();
        canvas.drawPath(path, mPaint);

        canvas.drawArc(rectF, angle + 80, 100, true, mPaint);

}

    private void drawIndicatorBg(Canvas canvas)
    {
        mPaint.setColor(Color.parseColor(color_smart_circle));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerPoint[0], centerPoint[1], (outCircleRadius / 3f / 2 / 4), mPaint);
    }

    /**
     * 根据进度画进度条
     *
     * @param progress
     */
    private void drawProgress(double progress, Canvas canvas)
    {
        mPaint.setStrokeWidth(inCircleWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        RectF rectF = new RectF(centerPoint[0] - inCircleRedius, centerPoint[1] - inCircleRedius, centerPoint[0] + inCircleRedius, centerPoint[1] + inCircleRedius);
        for(int i = 0; i < progress; i+=1)
        {
            mPaint.setColor(getColor(i * 1f / maxProgress * 120));
           canvas.drawArc(rectF, startAngle + i, 2, false, mPaint);
        }
        mPaint.setStrokeWidth(0);
        mPaint.setColor(getColor(0));
        drawArcRoune(inCircleRedius, startAngle, inCircleWidth, canvas);
        mPaint.setColor(getColor((float)progress / maxProgress * 120));
        drawArcRoune(inCircleRedius, (int)(startAngle + progress * 1f / maxProgress * allAngle) + 1, inCircleWidth, canvas);
    }

    public double getProgress()
    {
        return progress;
    }

    public double getDirection()
    {
        return direction;
    }

    /**
     * 进度条动画，默认每秒15帧，66ms一帧
     * @param progress
     * @param direction
     *
     */
    public void setStatues(double progress, double direction)
    {
        //避免累计误差
        this.progress = this.progress_o;
        this.progress_o = progress;
        this.direction = direction;
        this.accelerated = (this.progress_o - this.progress) / (25 * period);
    }

    /**
     * 画一个两端为圆弧的圆形曲线
     *
     * @param startAngle 曲线开始的角度
     * @param allAngle   曲线走过的角度
     * @param radius     曲线的半径
     * @param width      曲线的厚度
     */
    private void drawCircleWithRound(int startAngle, int allAngle, int width, int radius, int color, Canvas canvas) {
        mPaint.setStrokeWidth(width);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(color);
        RectF rectF = new RectF(centerPoint[0] - radius, centerPoint[1] - radius, centerPoint[0] + radius, centerPoint[1] + radius);
        canvas.drawArc(rectF, startAngle, allAngle, false, mPaint);
        drawArcRoune(radius, startAngle, width, canvas);
        drawArcRoune(radius, startAngle + allAngle, width, canvas);
    }

    /**
     * 绘制圆弧两端的圆
     *
     * @param radius 圆弧的半径
     * @param angle  所处于圆弧的多少度的位置
     * @param width  圆弧的宽度
     */
    private void drawArcRoune(int radius, int angle, int width, Canvas canvas) {
        int[] point = getPointFromAngleAndRadius(angle, radius);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(point[0], point[1], width / 2, mPaint);
    }

    /**
     * 根据角度和半径，求一个点的坐标
     *
     * @param angle
     * @param radius
     * @return
     */
    private int[] getPointFromAngleAndRadius(int angle, int radius) {
        double x = radius * Math.cos(angle * Math.PI / 180) + centerPoint[0];
        double y = radius * Math.sin(angle * Math.PI / 180) + centerPoint[1];
        return new int[]{(int) x, (int) y};
    }

    /**
     * 渐变色 绿色到黄到红
     * @param val
     * @return
     */
    public int getColor(float val)
    {
        float one = (255 + 255) / 80;//（255+255）除以最大取值的三分之二
        int r=0,g=0,b=0;
        if (val <= 40)//第一个三等分
        {
            r = (int)(one * val);
            g = 255;
        }
        else if (val > 40 && val <= 80)//第二个三等分
        {
            r = 255;
            g = 255 - (int)((val - 40) * one);//val减最大取值的三分之一
        }
        else
         {
             r = 255;
         }
        return Color.rgb(r, g, b);
    }

}

