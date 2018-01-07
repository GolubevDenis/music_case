package musiccase.ru.musiccase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class ContactActivity extends ParentActivity implements View.OnClickListener {

    private TextView emailReklama;
    private TextView emailShop;
    private TextView phone;
    AlertDialog.Builder adEmail;
    AlertDialog.Builder adPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createActionBar("Контакты", R.id.contact_toolbar);
        initSlidingUpPanel(R.id.activity_contact);
        setForImageDowListener(R.id.contact_fragment_player);
        if(!BackgroundAudioService.isRunning){
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        adEmail = new AlertDialog.Builder(this);
        adEmail.setTitle("Email");  // заголовок
        adEmail.setMessage("Написать письмо на "+getString(R.string.musiccaseradio_yandex_ru)+"?"); // сообщение
        adEmail.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                startActivity(new Intent(Intent.ACTION_SEND).setType("plain/text"));

            }
        });
        adEmail.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {}});

        adPhone = new AlertDialog.Builder(this);
        adPhone.setTitle("Звонок");  // заголовок
        adPhone.setMessage("Позвонить по номеру \n"+getString(R.string._7_923_3682222)); // сообщение
        adPhone.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+getString(R.string.phoneNum)));
                startActivity(intent);
            }
        });
        adPhone.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {}});

        emailReklama = (TextView) findViewById(R.id.textEmail);
        emailShop = (TextView) findViewById(R.id.textEmailShop);
        phone = (TextView) findViewById(R.id.textPhone);

        emailReklama.setOnClickListener(this);
        emailShop.setOnClickListener(this);
        phone.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.textEmail :
                adEmail.show();
                break;
            case R.id.textEmailShop :
                adEmail.show();
                break;
            case R.id.textPhone :
                adPhone.show();
                break;
        }
    }
}
