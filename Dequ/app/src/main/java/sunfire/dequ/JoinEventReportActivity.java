package sunfire.dequ;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JoinEventReportActivity
        extends
        AppCompatActivity
        implements
        View.OnClickListener
{
    //Create objects
    Button btnCancelJoin;
    Button btnJoinEvent;
    ImageView imgJoinEvent;
    TextView txtName;
    TextView txtType;
    TextView txtLevel;
    TextView txtPeople;
    TextView txtDescription;
    //Bitmap
    byte[] decodedString;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_report);
        //Get instance info
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //TODO Debería checar que el intent no sea NULL?
        //Asignar al objeto
        txtName = (TextView) findViewById(R.id.txtViewJoinEventTitle);
        txtDescription = (TextView) findViewById(R.id.txtViewJoinEventDescription);
        txtType = (TextView) findViewById(R.id.txtViewJoinEventType);
        txtLevel = (TextView) findViewById(R.id.txtViewJoinEventLevel);
        txtPeople = (TextView) findViewById(R.id.txtViewJoinEventPeople);
        btnCancelJoin = (Button) findViewById(R.id.btnCancelJoinEvent);
        btnJoinEvent = (Button) findViewById(R.id.btnJoinEventJoin);
        imgJoinEvent = (ImageView) findViewById(R.id.imgViewJoinEvent);
        //Asignar valor del intent, no estoy seguro del textView
        txtName.setText((String)bundle.get("title"));
        txtType.setText((String)bundle.get("type"));
        txtLevel.setText((String)bundle.get("level"));
        decodedString = Base64.decode( (String) bundle.get("image"), Base64.NO_WRAP);
        InputStream inputStream = new ByteArrayInputStream(decodedString);
        bitmap = BitmapFactory.decodeStream(inputStream);
        imgJoinEvent.setImageBitmap(bitmap);
        //TODO Recibir el numero de gente
        txtPeople.setText("5");
        //Botones
        btnCancelJoin.setOnClickListener(this);
        btnJoinEvent.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCancelJoinEvent){
            finish();
        }
        else if(view.getId() == R.id.btnJoinEventJoin){
            //Unirse al evento
        }
    }
}
