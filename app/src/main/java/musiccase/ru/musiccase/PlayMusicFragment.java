package musiccase.ru.musiccase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class PlayMusicFragment extends android.support.v4.app.Fragment implements Refreshable {

    private static TextView infoMusic;
    private static ImageView albumImage;
    private Handler updateMetadataHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_play_music, container, false);
        infoMusic = (TextView) view.findViewById(R.id._fragmentAuthorNameMusic);
        albumImage = (ImageView) view.findViewById(R.id.image_albom);
        updateMetadataHere();
        RefresherStatusMusic.registerUpdateMetadata(this);
        updateMetadataHandler  = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                updateMetadataHere();
            }
        };
        return  view;
    }
    static void updateMetadataHere(){
        try {
            if(Metadata.bitmap==null) albumImage.setImageResource(R.drawable.logo);
            else albumImage.setImageBitmap(Metadata.bitmap);

            infoMusic.setText(Metadata.artist + " - " + Metadata.title);
            infoMusic.setSelected(true);
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, "Ошибка обновления PlayMusicFragment!" + "\n\n" + e.getMessage());}
    }

    @Override
    public void refresh() {}

    @Override
    public void updateMetadata() {
        updateMetadataHandler.sendEmptyMessage(0);
    }
}

