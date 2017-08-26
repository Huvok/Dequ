package sunfire.dequ;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReportInfoActivity
        extends
        AppCompatActivity
    implements
        View.OnClickListener
{
    //Bitmap
    byte[] decodedString;
    Bitmap bitmap;
    //Create objects
    Button btnCreateEvent;
    Button btnCancelEvent;
    ImageView imgReport;
    TextView txtName;
    TextView txtType;
    TextView txtLevel;
    TextView txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_info);
        //Get instance info
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //Debería checar que el intent no sea NULL?
        //Asignar al objeto
        txtName = (TextView) findViewById(R.id.txtViewReportInfoName);
        txtDescription = (TextView) findViewById(R.id.txtViewReportInfoDescription);
        txtType = (TextView) findViewById(R.id.txtViewReportInfoType);
        txtLevel = (TextView) findViewById(R.id.txtViewReportInfoLevel);
        btnCancelEvent = (Button) findViewById(R.id.btnCancelFbEvent);
        btnCreateEvent = (Button) findViewById(R.id.btnCreateFbEvent);
        imgReport = (ImageView) findViewById(R.id.imgViewReport);
        //Asignar valor del intent, no estoy seguro del textView
        txtName.setText((String)bundle.get("title"));
        txtType.setText((String)bundle.get("type"));
        txtLevel.setText((String)bundle.get("level"));
        txtDescription.setText((String)bundle.get("description"));
        decodedString = Base64.decode( (String) bundle.get("image"), Base64.NO_WRAP);
        InputStream inputStream = new ByteArrayInputStream(decodedString);
        bitmap = BitmapFactory.decodeStream(inputStream);
        imgReport.setImageBitmap(bitmap);
        //Botones
        btnCreateEvent.setOnClickListener(this);
        btnCancelEvent.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        //Si se va a salir, mover al main activity
        if(view.getId() == R.id.btnCancelFbEvent){
            finish();
        }
        //Crear evento de fb
        else if(view.getId() == R.id.btnCreateFbEvent){
            final CharSequence[] items = {"Create event from app", "Create event on Facebook",
                "Link to a Facebook event", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(ReportInfoActivity.this);
            builder.setTitle("How will you create the event?");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();

                    if (items[i].equals("Create event from app"))
                    {

                    }
                    else if (items[i].equals("Create event on Facebook"))
                    {

                    }
                    else if (items[i].equals("Link to a Facebook event"))
                    {

                    }
                }
            });

            builder.show();


        }
    }
}
