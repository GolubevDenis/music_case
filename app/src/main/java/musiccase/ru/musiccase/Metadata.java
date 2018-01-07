package musiccase.ru.musiccase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

//этот класс хранит в себе все метаданные играющего трека
class Metadata {
    static volatile String title = "";
    static volatile String album = "";
    static volatile String artist = "";
    static volatile Bitmap bitmap = null;

    static void loadBitmap(){
        try {
            bitmap.recycle();
        } catch (Exception e) {Log.d(Const.LOG_TAG, "Ошибка загрузки альбома! \n" +  e.getMessage());}
        new DownloadBitmapTask(bitmap).execute(album);
    }

    static{
        loadBitmap();
    }

    private static class DownloadBitmapTask extends AsyncTask<String, Void, Bitmap> {
        Bitmap bit;

        DownloadBitmapTask(Bitmap bitmap) {
            this.bit = bitmap;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];

            Bitmap mIcon1 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon1 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, e.getMessage());
            }

            Metadata.bitmap = mIcon1;
            return mIcon1;
        }

        protected void onPostExecute(Bitmap result) {
            try {
                RefresherStatusMusic.fullUpdateMetadata();
            } catch (Exception e) {}
        }
    }
}
