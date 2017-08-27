package sunfire.dequ;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
    TextView txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_report);
        //Get instance info
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //TODO Debería checar que el intent no sea NULL?
        //Asignar al objeto
        txtName = (TextView) findViewById(R.id.txtViewJoinEventName);
        txtDescription = (TextView) findViewById(R.id.txtViewJoinEventDescription);
        txtType = (TextView) findViewById(R.id.txtViewJoinEventType);
        txtLevel = (TextView) findViewById(R.id.txtViewJoinEventLevel);
        btnCancelJoin = (Button) findViewById(R.id.btnCancelJoinEvent);
        btnJoinEvent = (Button) findViewById(R.id.btnJoinEventJoin);
        imgJoinEvent = (ImageView) findViewById(R.id.imgViewJoinEvent);
        //Asignar valor del intent, no estoy seguro del textView
        txtName.setText((String)bundle.get("title"));
        txtType.setText((String)bundle.get("type"));
        txtLevel.setText((String)bundle.get("level"));
    }

    @Override
    public void onClick(View v) {

    }
}
