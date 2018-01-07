package musiccase.ru.musiccase;

import android.media.AudioManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class ParentActivity extends AppCompatActivity {

    AudioManager audioManager;
    SlidingUpPanelLayout mLayout;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(audioManager != null){
            if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN || keyCode==KeyEvent.KEYCODE_VOLUME_UP){
                PlayerFragment.value.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
    void initSlidingUpPanel(int id){
        mLayout = (SlidingUpPanelLayout) findViewById(id);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                PlayerFragment.updateIconPlay();
                if(slideOffset<=0.20) {
                    PlayerFragment.showTopFragment();
                    PlayMusicFragment.updateMetadataHere();
                }
                else{
                    PlayerFragment.hideTopFragment();
                    PlayerFragment.updateMetadataHere();
                }
            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {}
        });
    }

    //этот метод вешает на ImageDow в PlayerFragment слушатель.
    // Вызывать в onCreate Activity где есть PlayerFragment
    //id - id фрагмента в Activity
    void setForImageDowListener(int id){
        findViewById(id).
                getRootView().findViewById(R.id.imageDow).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                });
    }

    //id - id ToolBar в активити
    void createActionBar(String titleActionBar, int id){
        setSupportActionBar((Toolbar) findViewById(id));
        ActionBar abar = getSupportActionBar();
        if(abar!=null) {
            View viewActionBar = getLayoutInflater().inflate(R.layout.action_bar_center, null);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);

            TextView textviewTitle = (TextView) viewActionBar.findViewById(R.id.actionbar_textview);
            textviewTitle.setText(titleActionBar);
            abar.setCustomView(viewActionBar, params);
            abar.setDisplayShowCustomEnabled(true);
            abar.setDisplayShowTitleEnabled(false);
            abar.setHomeButtonEnabled(true);
            abar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
