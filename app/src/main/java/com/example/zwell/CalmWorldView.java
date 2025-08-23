package com.example.zwell;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class CalmWorldView extends View {
    private int colorTop = Color.parseColor("#0b1b2b");
    private int colorBottom = Color.parseColor("#1b2b3b");
    private float fog = 0.3f;
    private int particleCount = 100;
    private float brightness = 1.0f;
    private final Random rnd = new Random();
    private final float[] px = new float[400], py = new float[400], ps = new float[400];

    public CalmWorldView(Context c, AttributeSet a) { super(c, a); init(); }
    public CalmWorldView(Context c) { super(c); init(); }

    private void init() {
        for (int i=0;i<px.length;i++) { px[i]=rnd.nextFloat(); py[i]=rnd.nextFloat(); ps[i]=0.3f+rnd.nextFloat()*1.5f; }
        post(anim);
    }

    private final Runnable anim = new Runnable() {
        @Override public void run() {
            for (int i=0;i<px.length;i++) { py[i]+=0.001f*ps[i]; if (py[i]>1) py[i]=0; }
            invalidate();
            postDelayed(this, 16);
        }
    };

    public void configure(int ct, int cb, float fogAmt, int particles) {
        colorTop = ct; colorBottom = cb; fog = fogAmt; particleCount = Math.min(particles, px.length);
        invalidate();
    }

    public void setBrightness(float b) { brightness = Math.max(0.6f, Math.min(2.0f, b)); invalidate(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w=getWidth(), h=getHeight();
        Paint p = new Paint();
        Shader sh = new LinearGradient(0,0,0,h, applyBrightness(colorTop), applyBrightness(colorBottom), Shader.TileMode.CLAMP);
        p.setShader(sh);
        canvas.drawRect(0,0,w,h,p);

        Paint fogPaint = new Paint();
        fogPaint.setColor(Color.argb((int)(fog*120), 200, 200, 220));
        canvas.drawRect(0,0,w,h,fogPaint);

        Paint star = new Paint();
        star.setColor(Color.argb(180,255,255,255));
        for (int i=0;i<particleCount;i++) {
            float x = px[i]*w, y = py[i]*h;
            canvas.drawCircle(x, y, ps[i]*2, star);
        }
    }

    private int applyBrightness(int c) {
        float r = Math.min(255, Color.red(c)*brightness);
        float g = Math.min(255, Color.green(c)*brightness);
        float b = Math.min(255, Color.blue(c)*brightness);
        return Color.rgb((int)r,(int)g,(int)b);
    }
}
