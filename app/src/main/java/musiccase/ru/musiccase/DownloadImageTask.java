package musiccase.ru.musiccase;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.HashMap;

//класс для скачивания и кеширования картинок
class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    static final HashMap<String, Bitmap> list = new HashMap();

    static int curcorColor = 0;
    static final int[] listColor = {
            R.drawable.blue,
            R.drawable.red,
            R.drawable.yellow,
            R.drawable.green,
            R.drawable.orange};


    DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
        bmImage.setImageResource(getNextColor());
    }

    int getNextColor(){
        if(curcorColor + 1 > listColor.length)
            curcorColor = 0;
        return listColor[curcorColor++];
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        try{
            if(list.get(urldisplay)!=null){
                return list.get(urldisplay);
            }
        }catch (Exception e){Log.d(Const.LOG_TAG, "[DownloadImageTask.doInBackground] ошибка извлечвения из карты изображений");}

        Bitmap mIcon1 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon1 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, e.getMessage());
        }

        try{
            list.put(urldisplay, mIcon1);
        }catch (Exception e){Log.d(Const.LOG_TAG, "[DownloadImageTask.doInBackground] ошибка добавления в карту изображений");}

        return mIcon1;
    }

    protected void onPostExecute(Bitmap result) {
        if(result!=null)
            bmImage.setImageBitmap(result);
        else
            bmImage.setImageResource(getNextColor());
    }
}
