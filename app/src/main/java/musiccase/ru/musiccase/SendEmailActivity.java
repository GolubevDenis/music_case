package musiccase.ru.musiccase;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class SendEmailActivity extends ParentActivity {

    private Button buttonSend;
    private EditText phone;
    private EditText textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createActionBar("Сообщить о проблеме", R.id.send_email_toolbar);
        initSlidingUpPanel(R.id.activity_send_email);
        setForImageDowListener(R.id.setd_email_fragment_player);
        if(!BackgroundAudioService.isRunning){
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

        textMessage = (EditText) findViewById(R.id.editTextMessage);
        phone = (EditText) findViewById(R.id.editTextPhone);
        buttonSend = (Button) findViewById(R.id.buttonSendEmail);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sendEmail();
            }
        });
    }
    void sendEmail(){
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[] {getString(R.string._email) });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                phone.getText().toString());
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                textMessage.getText().toString());
        SendEmailActivity.this.startActivity(Intent.createChooser(emailIntent,"Отправка Email"));
    }
}
