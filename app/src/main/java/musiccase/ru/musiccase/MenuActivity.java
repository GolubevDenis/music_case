package musiccase.ru.musiccase;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MenuActivity extends ParentActivity implements View.OnClickListener {

    private LinearLayout field_logo;
    private LinearLayout field_vk;
    private LinearLayout field_telegram;
    private LinearLayout field_instagram;
    private LinearLayout field_facebook;
    private LinearLayout field_contacs;
    private LinearLayout field_share;
    private LinearLayout field_problem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        createActionBar("Ещё", R.id.menu_toolbar);

        initSlidingUpPanel(R.id.activity_menu);

        setForImageDowListener(R.id.menu_fragment_player);
        if(!BackgroundAudioService.isRunning){
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

        field_logo = (LinearLayout) findViewById(R.id.field_logo);
        field_vk = (LinearLayout) findViewById(R.id.field_vk);
        field_telegram = (LinearLayout) findViewById(R.id.field_telegram);
        field_instagram = (LinearLayout) findViewById(R.id.field_instagram);
        field_facebook = (LinearLayout) findViewById(R.id.field_facebook);
        field_contacs = (LinearLayout) findViewById(R.id.field_contacs);
        field_share = (LinearLayout) findViewById(R.id.field_share);
        field_problem = (LinearLayout) findViewById(R.id.field_problem);

        field_logo.setOnClickListener(this);
        field_vk.setOnClickListener(this);
        field_telegram.setOnClickListener(this);
        field_instagram.setOnClickListener(this);
        field_facebook.setOnClickListener(this);
        field_contacs.setOnClickListener(this);
        field_share.setOnClickListener(this);
        field_problem.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.field_logo:
                toSite(getString(R.string.musiccase_ru));
                break;
            case R.id.field_vk:
                toSite(getString(R.string.vk_com_music_case));
                break;
            case R.id.field_telegram:
                toSite(getString(R.string.t_me_musiccase));
                break;
            case R.id.field_instagram:
                toSite(getString(R.string.instagram_com_music_case));
                break;
            case R.id.field_facebook:
                toSite(getString(R.string.facebook_com_musiccase));
                break;
            case R.id.field_contacs:
                toActiviry(ContactActivity.class);
                break;
            case R.id.field_share:
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String textToSend= getString(R.string._urlApplication);
                intent.putExtra(Intent.EXTRA_TEXT, textToSend);
                try {
                    startActivity(Intent.createChooser(intent, "Описание действия"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.field_problem:
                startActivity(new Intent(this, SendEmailActivity.class));
                break;
        }
    }
    private void toSite(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://"+url));
        startActivity(i);
    }
    private void toActiviry(Class activity){
        Intent i = new Intent(this, activity);
        startActivity(i);
    }
}
