package objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import objectdetection.ai.things.android.masato.ka.androidthingsobjectdetectionsample.objectdetection.model.Recognition;

import java.util.ArrayList;

public class CanvasView extends View {

    private Bitmap mBitmap;
    private ArrayList<Recognition> recognitions;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBitmap = null;
        recognitions = null;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvasDraw(canvas);
    }

    private void canvasDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(1);

        canvas.drawColor(Color.BLACK);

        if (mBitmap != null) {
            float scale = (float)getWidth() / mBitmap.getWidth();
            canvas.scale(scale, scale);
            canvas.drawBitmap(mBitmap, 0, 0, paint);
        }

        if(recognitions!=null){
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GREEN);
            for(int i = 0; i < recognitions.size(); i++){
                Recognition mRecognition = recognitions.get(i);
                if (mRecognition.getConfidence() < 0.5) {
                    continue;
                }
                canvas.drawText(mRecognition.getTitle(),
                        mRecognition.getLocation().left,
                        mRecognition.getLocation().top,
                        paint);
                canvas.drawRect(mRecognition.getLocation(), paint);
            }

        }

    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public void setRecognitions(ArrayList<Recognition> recognitions) {
        this.recognitions = recognitions;
    }

}
