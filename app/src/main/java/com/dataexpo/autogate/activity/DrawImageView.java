package com.dataexpo.autogate.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class DrawImageView extends ImageView {
    Canvas canvas = null;
    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    Paint paint = new Paint();
    {
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);//设置线宽
        paint.setAlpha(100);
    };

    @Override
    public void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        this.canvas = canvas;
        //canvas.drawRect(new Rect(100, 200, 400, 500), paint);//绘制矩形
        //draw(canvas);
    }

    public void test(RectF rectF, Paint paint) {

    }
}
