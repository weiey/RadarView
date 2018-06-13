package com.example.administrator.radarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {
    private int count = 8;
    private int mLayerCount = 6;
    private float angle = (float) (Math.PI * 2 / count);
    private float radius;  //外接圆半径
    private int centerX;
    private int centerY;
    private String[] titles = {"A", "B", "C", "D", "E", "F","G","H"};
    private double[] data = {1, 0.30, 0.6, 0.5, 0.8, 0.2,0.0,0.0};


    private int connLineColor;
    private int connLineSize;

    private int overlayColor;
    private int overlayalpha;
    private int tagColor;
    private int tagSize;

    private Paint netPaint;
    private Paint valuePaint;
    private Paint textPaint;


    public RadarView(Context context) {
        this(context, null);
        init(context);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init(context);
    }
    private void initAttrs(AttributeSet attrs) {
        TypedArray style = getContext().obtainStyledAttributes(attrs, R.styleable.RadarView);
        int rvTitleColor = style.getColor(R.styleable.RadarView_rvTitleColor, Color.BLACK);
        int rvTitleSize = style.getInt(R.styleable.RadarView_rvTitleSize, 20);

        int rvConnLineColor = style.getColor(R.styleable.RadarView_rvConnLineColor, Color.GRAY);
        int rvConnLineSize = style.getInt(R.styleable.RadarView_rvConnLineSize, 2);

        int rvOverlayColor = style.getColor(R.styleable.RadarView_rvOverlayColor, Color.GRAY);
        int rvOverlayAlpha = style.getInt(R.styleable.RadarView_rvOverlayAlpha, 150);

        int dotColor = style.getColor(R.styleable.RadarView_rvDotColor, Color.RED);
        int dotSize = style.getInt(R.styleable.RadarView_rvDotSize, 5);
        style.recycle();    //回收实例

        setTagColor(rvTitleColor);
        setTagSize(rvTitleSize);
        setConnLineColor(rvConnLineColor);
        setConnLineSize(rvConnLineSize);
        setOverlayColor(rvOverlayColor);
        setOverlayalpha(rvOverlayAlpha);
    }

    private void init(Context context) {
        count = Math.min(data.length, titles.length);


        netPaint = new Paint();
        netPaint.setAntiAlias(true);
        netPaint.setColor(connLineColor);
        netPaint.setStrokeWidth(connLineSize);
        netPaint.setStyle(Paint.Style.STROKE);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(overlayColor);
        valuePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(tagSize);
        textPaint.setColor(tagColor);
        textPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(w, h) / 2 * 0.7f;
        centerX = w / 2;
        centerY = h / 2;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heighSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heighSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode== MeasureSpec.AT_MOST&&heighSpecMode== MeasureSpec.AT_MOST){
            setMeasuredDimension(200,200);
        }else if(widthSpecMode== MeasureSpec.AT_MOST){
            setMeasuredDimension(200,heighSpecSize);
        }else if(heighSpecMode== MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize,200);

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNet(canvas);
        drawText(canvas);
        drawRegion(canvas);
    }

    private void drawNet(Canvas canvas) {
        //绘制六边形
        Path path = new Path();
        float r = radius / (mLayerCount - 1); //每次递增的高度
        for (int i = 0; i < mLayerCount; i++) {
            float currentRadius = r * i;
            path.reset();
            for (int j = 0; j < count; j++) {
                if (j == 0) {
                    path.moveTo(centerX + currentRadius, centerY);
                } else {
                    float x = (float) (centerX + currentRadius * Math.cos(angle * j));
                    float y = (float) (centerY + currentRadius * Math.sin(angle * j));
                    path.lineTo(x, y);
                }
            }

            path.close();
            canvas.drawPath(path, netPaint);
        }


        //绘制轴线
        for (int i = 1; i < count + 1; i++) {
            path.reset();
            path.moveTo(centerX, centerY);
            float x = (float) (centerX + radius * Math.cos(angle * i));
            float y = (float) (centerY + radius * Math.sin(angle * i));
            path.lineTo(x, y);
            canvas.drawPath(path, netPaint);
        }

    }

    private void drawText(Canvas canvas) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent; //文字的高度

        //修正标题

        for (int i = 0; i < count; i++) {
            float x = (float) (centerX + (radius + fontHeight / 2) * Math.cos(angle * i));
            float y = (float) (centerY + (radius + fontHeight / 2) * Math.sin(angle * i));
            float dis = textPaint.measureText(titles[i]);//获取文本长度

            if (angle * i > 0 && angle * i < Math.PI) {
                canvas.drawText(titles[i], x-dis/2, y+fontHeight , textPaint);
            } else if (angle * i >= Math.PI && angle * i < 3 * Math.PI / 2) {
                canvas.drawText(titles[i], x - dis, y, textPaint);
            } else {
                canvas.drawText(titles[i], x, y, textPaint);
            }

        }

    }


    private void drawRegion(Canvas canvas) {
        Path path = new Path();
        for (int i = 0; i < count; i++) {
            float x = (float) (centerX + radius * Math.cos(angle * i) * data[i]);
            float y = (float) (centerY + radius * Math.sin(angle * i) * data[i]);
            if (i == 0) {
                path.moveTo(x, centerY);
            } else {
                path.lineTo(x, y);
            }
            canvas.drawCircle(x, y, 5, valuePaint);
        }
        path.close();
        valuePaint.setAlpha(overlayalpha);
        canvas.drawPath(path, valuePaint);


    }


    /**
     * 设置网状线角标
     *
     * @param titles
     */
    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    /**
     * 设置绘制区域的占比
     *
     * @param data
     */
    public void setPercent(double[] data) {
        this.data = data;
    }

    public void setLayerCount(int count) {
        this.mLayerCount = count > 0 ? count : 5;
    }

    public int getConnLineColor() {
        return connLineColor;
    }

    public void setConnLineColor(int connLineColor) {
        this.connLineColor = connLineColor;
    }

    public int getConnLineSize() {
        return connLineSize;
    }

    public void setConnLineSize(int connLineSize) {
        this.connLineSize = connLineSize;
    }

    public int getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColor(int overlayColor) {
        this.overlayColor = overlayColor;
    }

    public int getOverlayalpha() {
        return overlayalpha;
    }

    public void setOverlayalpha(int overlayalpha) {
        this.overlayalpha = overlayalpha;
    }

    public int getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = tagColor;
    }

    public int getTagSize() {
        return tagSize;
    }

    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }
}
