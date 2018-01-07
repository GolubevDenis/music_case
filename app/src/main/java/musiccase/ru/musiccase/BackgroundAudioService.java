package musiccase.ru.musiccase;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class BackgroundAudioService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnPreparedListener, Refreshable {

    static boolean isReady = false;
    static boolean isRunning = false;

    static MediaPlayer mMediaPlayer;
    static MediaSessionCompat mMediaSessionCompat;
    private static BackgroundAudioService it;
    private Handler updateMetadataHandler;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
            }
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state){
                    case 1:
                        if(isReady) {
                            startMusic();
                            mMediaSessionCompat.setActive(true);
                            RefresherStatusMusic.fullRefresh();
                            isRunning = true;
                        }
                        break;
                    case 0:
                        if(isRunning){
                            stopMusic();
                            RefresherStatusMusic.fullRefresh();
                            isRunning = false;
                        }
                        break;
                }
            }
        }
    };

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if( !successfullyRetrievedAudioFocus() ) {
                return;
            }
            if(isReady){startMusic();}
        }

        @Override
        public void onPause() {
            super.onPause();

            if( mMediaPlayer.isPlaying() ) {
                stopMusic();
            }
        }
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }
    };

    private void startMusic(){
        if(!mMediaPlayer.isPlaying())mMediaPlayer.start();
        mMediaPlayer.setVolume(1,1);
        mMediaSessionCompat.setActive(true);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        showPlayingNotification();
    }
    private void stopMusic(){
        mMediaPlayer.setVolume(0,0);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        showPausedNotification();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateMetadataHandler  = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                updateNotification();
            }
        };
        RefresherStatusMusic.registerUpdateMetadata(this);
        it = this;
        create();
    }

    private void create(){
        MetadataLoader.start();
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    private void initNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(mNoisyReceiver, filter);
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    private void destroy(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(1);
        onCompletion(mMediaPlayer);
        isReady = false;
        isRunning = false;
    }

    private void initMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(Const.URL_RADIO);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void showPlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(it, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(it, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.icon_nota);
        NotificationManagerCompat.from(it).notify(1, builder.build());
    }

    static void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(it, mMediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(it, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mMediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.drawable.icon_nota);
        NotificationManagerCompat.from(it).notify(1, builder.build());
    }

    static void updateNotification(){
        if(HomePage.homePage != null && !HomePage.homePage.isFinishing())
            if(isRunning) showPlayingNotification();
            else showPausedNotification();
    }


    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {}

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if( mMediaPlayer != null ) {
            mediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isReady = true;
    }

    @Override
    public void refresh() {}

    @Override
    public void updateMetadata() {
        updateMetadataHandler.sendEmptyMessage(0);
    }


    static class MetadataLoader{
        private static boolean isMetadataLoaderRunning = false;
        private static Timer timer = new Timer();
        private static FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        private static TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    mmr.setDataSource(Const.URL_RADIO);
                    String metadataASCII = mmr.extractMetadata("icy_metadata");
                    try {
                        metadataASCII = metadataASCII.substring(metadataASCII.indexOf("'")+1, metadataASCII.indexOf(";StreamUrl='';"));
                    } catch (Exception e) {}
                    String title = metadataASCII.substring(metadataASCII.indexOf("title=")+6, metadataASCII.indexOf("&album="));
                    title = URLDecoder.decode(title, "UTF-8");
                    if(!Metadata.title.equals(title)){
                        String artist = metadataASCII.substring(metadataASCII.indexOf("artist=")+7, metadataASCII.lastIndexOf("'"));
                        String album = "";
                        try {
                            album = metadataASCII.substring(metadataASCII.indexOf("album=")+6, metadataASCII.indexOf("&artist"));
                        } catch (Exception e) {
                            Log.e(Const.LOG_TAG, e.getMessage());
                        }

                        Metadata.title = title;
                        try {
                            Metadata.artist = URLDecoder.decode(artist, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            Metadata.artist = " ";
                        }
                        Metadata.album = URLDecoder.decode(album, "UTF-8");

                        Metadata.loadBitmap();
                    }
                 }catch (Exception e){Log.e(Const.LOG_TAG, "Ошибка при извлечении методанных!  " + e.getMessage()); e.printStackTrace();}
            }
        };

        static void start(){
            if(!isMetadataLoaderRunning) {
                timer.schedule(timerTask, 200, 7000);
                isMetadataLoaderRunning = true;
            }
        }
        static void stop(){
            try {
                timer.cancel();
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, e.getMessage());
            }
        }
    }
}
