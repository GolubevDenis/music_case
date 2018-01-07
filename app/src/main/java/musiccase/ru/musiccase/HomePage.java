package musiccase.ru.musiccase;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class HomePage extends ParentActivity implements View.OnClickListener, Refreshable {

    private ImageView imagePlay;
    private ImageView imageMenu;

    private boolean isMediaBrowserCompat = false;

    private MediaBrowserCompat mMediaBrowserCompat;
    static MediaControllerCompat mMediaControllerCompat;
    static HomePage homePage;

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mMediaControllerCompat = new MediaControllerCompat(HomePage.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                setSupportMediaController(mMediaControllerCompat);
            } catch( RemoteException e ) {}
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {return;}
            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    setStatePlay();
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    setStatePaused();
                    break;
                }
            }
            PlayerFragment.updateIconPlay();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        homePage = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        if(savedInstanceState!=null){
            isMediaBrowserCompat = savedInstanceState.getBoolean("isMediaBrowserCompat");
        }
        initSlidingUpPanel(R.id.activity_home_page);
        setForImageDowListener(R.id.home_fragment_player);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        imagePlay = (ImageView) findViewById(R.id.home_button_play);
        imageMenu = (ImageView) findViewById(R.id.home_button_menu);
        imagePlay.setOnClickListener(this);
        imageMenu.setOnClickListener(this);
        checkState();
        RefresherStatusMusic.registerRefresh(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStateRunning();
        if(!mMediaBrowserCompat.isConnected()){
            try {
                mMediaBrowserCompat.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,BackgroundAudioService.class));
        mMediaBrowserCompat.disconnect();
        mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback);
        mMediaBrowserCompatConnectionCallback = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.home_button_play :
                onClickPlay();
                break;
            case R.id.home_button_menu :
                onClickMenu();
                break;
        }
    }
    private void onClickMenu(){
        try{
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }catch (Exception e){}
    }
    private void onClickPlay(){
        try{
            if (!BackgroundAudioService.isRunning) {
                if(BackgroundAudioService.isReady){
                    getSupportMediaController().getTransportControls().play();
                    setStatePlay();
                }else {
                    Toast.makeText(this, getString(R.string.music_no_prepare), Toast.LENGTH_SHORT ).show();
                }
            } else {
                getSupportMediaController().getTransportControls().pause();
                setStatePaused();
            }
        }catch (Exception e){}
    }

    private void setStatePlay(){
        imagePlay.setImageResource(R.drawable.stop_icon);
        BackgroundAudioService.isRunning = true;
        if(mLayout.getPanelState()==SlidingUpPanelLayout.PanelState.HIDDEN)
             mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void setStatePaused(){
        BackgroundAudioService.isRunning = false;
        imagePlay.setImageResource(R.drawable.play_icon);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isMediaBrowserCompat", isMediaBrowserCompat);
    }

    private void checkStateRunning(){
        if(BackgroundAudioService.isRunning){imagePlay.setImageResource(R.drawable.stop_icon);}
        else {imagePlay.setImageResource(R.drawable.play_icon);}
    }

    //этот метод проверяет что нужно обновить и обновляет в соответствии с состоянием приложения
    private void checkState(){
        if(BackgroundAudioService.isRunning){imagePlay.setImageResource(R.drawable.stop_icon);}
        if(!BackgroundAudioService.isRunning){
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        if(!isMediaBrowserCompat) {
            mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, BackgroundAudioService.class),
                    mMediaBrowserCompatConnectionCallback, getIntent().getExtras());
            isMediaBrowserCompat = true;
        }
    }

    @Override
    public void refresh() {
        checkStateRunning();
    }

    @Override
    public void updateMetadata() {}
}
