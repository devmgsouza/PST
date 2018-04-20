package meupet.android.soasd.com.br.meupet.httpservices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import br.com.soasd.meupet.GerarQRCode;
import meupet.android.soasd.com.br.meupet.R;

/**
 * Created by SOA - Development on 21/02/2018.
 */

public class WriteBitmap extends AsyncTask<Bitmap, Void, Bitmap> {
        private Bitmap b;

        private String textDown, textUp;
        Callback callback;
        private Context context;

        public WriteBitmap (Bitmap b, String textUp, String textDown, Callback callback, Context context){
            this.b = b;
            this.context = context;
            this.textDown = textDown;
            this.textUp = textUp;
            this.callback = callback;
        }

    @Override
    protected void onPostExecute(Bitmap resultado){
        callback.run(resultado);

    }





    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {

        Bitmap bm = writeTextTop(b, textUp, 35, 45);
        bm = writeTextTop(bm, "Leia o código", 55, 20);
        bm = writeTextDown(bm,"ou acesse: PetSmartTag.com",40, 20);
        bm = writeTextDown(bm,textDown,15, 30);

        return bm;
    }


    public Bitmap writeTextTop (Bitmap bitmap, String text, int height, int fsize) {

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(fsize);
        c.drawText(text, c.getWidth()/2, height, paint);

        return bitmap;
    }

    public Bitmap writeTextDown (Bitmap bitmap, String text, int height, int fsize) {

        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(fsize);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText( text, c.getWidth()/2, c.getHeight()-height, paint);

        return bitmap;
    }
/*
    public Bitmap overlay(Bitmap bmp1) {
        Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_patinha);

        Double newWitdh = b.getWidth() * 0.2;
        Double newHeight = b.getHeight() * 0.2;
        int w = newWitdh.intValue();
        int h = newHeight.intValue();

        Bitmap bmp2 = Bitmap.createScaledBitmap(b, w, h, false);



        Canvas c = new Canvas(bmp1);
        Paint paint = new Paint();

        paint.setAlpha(130);
        int w2 = c.getWidth()/2;
        int h2 = c.getHeight()/2;
        c.drawBitmap(bmp2, w2-(bmp2.getWidth()/2), h2-(bmp2.getHeight()/2), paint);

        return bmp1;
    }
*/

    public interface Callback {
        void run(Bitmap result);
    }

}
