package com.jareddlc.turquoisebicuspid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class ColorPickerView extends View {
	Paint paint;
    float rSize;
    float sWidth;

	public ColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		 float density = context.getResources().getDisplayMetrics().density;
         rSize = FloatMath.floor(24.f * density + 0.5f);
         sWidth = FloatMath.floor(1.f * density + 0.5f);

         paint = new Paint();
         paint.setColor(0xffffffff);
         paint.setStyle(Style.STROKE);
         paint.setStrokeWidth(sWidth);
	}
	
	@Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(sWidth, sWidth, rSize - sWidth, rSize - sWidth, paint);
	}
}
