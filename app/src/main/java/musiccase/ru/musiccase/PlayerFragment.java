package musiccase.ru.musiccase;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerFragment extends android.support.v4.app.Fragment implements Refreshable {

    static SeekBar value;
    private static AudioManager audioManager;
    private static ImageView imagePlay;
    private static View playMusicFragment;

    private static ImageView albomImage;
    private static TextView titleText;
    private static TextView artistText;

    private View view;
    private static ImageView minValueImage;
    private static ImageView maxValueImage;
    static Handler updateMetadataHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view =  inflater.inflate(R.layout.fragment_player, container, false);
        RefresherStatusMusic.registerRefresh(this);
        RefresherStatusMusic.registerUpdateMetadata(this);
        updateMetadataHere();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        updateMetadataHandler  = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                updateMetadataHere();
            }
        };

        playMusicFragment = view.findViewById(R.id.fragmentPlayMusic) ;

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        imagePlay = (ImageView) view.findViewById(R.id.player_play_image);

        albomImage = (ImageView) view.findViewById(R.id._player_AlbomMusic);
        titleText = (TextView) view.findViewById(R.id.player_musicNameText);
        artistText = (TextView) view.findViewById(R.id.player_authorText);

        updateIconPlay();

        imagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BackgroundAudioService.isRunning){
                    imagePlay.setImageResource(R.drawable.icon_play_player);
                    BackgroundAudioService.isRunning = false;
                    HomePage.mMediaControllerCompat.getTransportControls().pause();
                }else {
                    if(BackgroundAudioService.isReady) {
                        imagePlay.setImageResource(R.drawable.icon_pause_plauer);
                        BackgroundAudioService.isRunning = true;
                        HomePage.mMediaControllerCompat.getTransportControls().play();
                    }else {
                        Toast.makeText(getContext(), "Музыка еще не готова к воспроизведению, подождите чуть-чуть", Toast.LENGTH_SHORT ).show();
                    }
                }
            }
        });

        value = (SeekBar) view.findViewById(R.id.seekBarValue);
        value.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        try {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            value.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            value.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {}

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {}

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        minValueImage = (ImageView) view.findViewById(R.id.minValue);
        minValueImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(value.getProgress()>0){
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            value.getProgress()-1, 0);
                    value.setProgress(value.getProgress()-1);
                }
            }
        });
        maxValueImage = (ImageView) view.findViewById(R.id.maxValue);
        maxValueImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(value.getProgress()<value.getMax()){
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            value.getProgress()+1, 0);
                    value.setProgress(value.getProgress()+1);
                }
            }
        });
        updateMetadataHere();
    }

    static void updateIconPlay() {
        if(BackgroundAudioService.isRunning) imagePlay.setImageResource(R.drawable.icon_pause_plauer);
        else imagePlay.setImageResource(R.drawable.icon_play_player);
    }
    static void hideTopFragment(){playMusicFragment.setVisibility(View.GONE);}
    static void showTopFragment(){
        playMusicFragment.setVisibility(View.VISIBLE);
    }

    @Override
    public void refresh() {
        updateIconPlay();
    }

    @Override
    public void updateMetadata() {
        updateMetadataHandler.sendEmptyMessage(0);
    }

    static void updateMetadataHere(){
        try {
            if(Metadata.bitmap==null) albomImage.setImageResource(R.drawable.logo);
            else albomImage.setImageBitmap(Metadata.bitmap);

            titleText.setText(Metadata.title);
            titleText.setSelected(true);
            artistText.setText(Metadata.artist);

        } catch (Exception e) {
            Log.d(Const.LOG_TAG, "ОШИБКА Обновления " + "\n\n" + e.getMessage());
        }
    }
}
