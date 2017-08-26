package sunfire.dequ;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import static android.app.PendingIntent.getActivity;

public class ReportInfoActivity
        extends
        AppCompatActivity
    implements
        View.OnClickListener,
        TimePickerDialog.OnTimeSetListener
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
    //Dialogo crear evento
    AlertDialog.Builder dialogPlaceEvent;
    AlertDialog alertDialog;
    View viewEventDialog;
    EditText edtxtEventTitle;
    EditText edtxtEventDescription;
    Button btnSelectDate;
    Button btnSelectHour;
    Button btnCancelEventOnApp;
    Button btnCreateEventOnApp;
    //Fecha y hora del evento
    int yearR, monthR, dayR, hourR, minuteR;


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

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_events"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("rsvp_event"));
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
                        //Saltar al otro layout
                        dialogPlaceEvent = new AlertDialog.Builder(ReportInfoActivity.this);

                        viewEventDialog = getLayoutInflater().inflate(R.layout.report_event, null);
                        dialogPlaceEvent.setView(viewEventDialog).create();
                        dialogPlaceEvent.setTitle(R.string.event_dialog_title);
                        alertDialog = dialogPlaceEvent.show();

                        btnCancelEventOnApp = (Button) viewEventDialog.findViewById(R.id.btnCancelOnAppEvent);
                        btnCreateEventOnApp = (Button) viewEventDialog.findViewById(R.id.btnCreateOnAppEvent);
                        btnSelectDate = (Button) viewEventDialog.findViewById(R.id.btnDate);
                        btnSelectHour = (Button) viewEventDialog.findViewById(R.id.btnHour);
                        edtxtEventDescription = (EditText) viewEventDialog.findViewById(R.id.edTxtDescription);
                        edtxtEventTitle = (EditText) viewEventDialog.findViewById(R.id.edTxtReportEventTitle);

                        btnCancelEventOnApp.setOnClickListener(ReportInfoActivity.this);
                        btnCreateEventOnApp.setOnClickListener(ReportInfoActivity.this);
                        btnSelectHour.setOnClickListener(ReportInfoActivity.this);
                        btnSelectDate.setOnClickListener(ReportInfoActivity.this);


                    }
                    else if (items[i].equals("Create event on Facebook"))
                    {
                        startActivity(newFacebookIntent(getPackageManager(),
                            "https://www.facebook.com/events/upcoming?ref=46&action_history=null"));
                    }
                    else if (items[i].equals("Link to a Facebook event"))
                    {
                        new GraphRequest(
                                AccessToken.getCurrentAccessToken(),
                                "/me/events",
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        JSONObject jsonObject = response.getJSONObject();
                                        try {
                                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                                            final CharSequence[] items = {jsonArray.getJSONObject(0).getString("name"),
                                                "Cancel"};
                                            final String name = jsonArray.getJSONObject(0).getString("name");
                                            final String date = jsonArray.getJSONObject(0).getString("start_time");
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ReportInfoActivity.this);
                                            builder.setTitle("Link to Facebook event");
                                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i)
                                                {
                                                    dialogInterface.dismiss();

                                                    if (items[i].equals(items[0]))
                                                    {
                                                        HashMap<String, String> mapHeaders = new HashMap<String, String>();
                                                        mapHeaders.put("Content-Type", "application/json");

                                                        JSONObject jsonObjectNewReport = new JSONObject();
                                                        try {
                                                            jsonObjectNewReport.put("report", getIntent().getExtras().get("title"));;
                                                            jsonObjectNewReport.put("user_id", Profile.getCurrentProfile().getId());
                                                            jsonObjectNewReport.put("title", name);
                                                            jsonObjectNewReport.put("people_needed", 3);
                                                            jsonObjectNewReport.put("people_count", 1);
                                                            jsonObjectNewReport.put("due_date", date);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                        new ReportInfoActivity.RESTPostTask("http://" + getString(R.string.server_url) + "/api/event", mapHeaders, jsonObjectNewReport, "Event").execute();
                                                    }
                                                }
                                            });

                                            builder.show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        ).executeAsync();
                    }
                }
            });

            builder.show();


        }
        else if(view.getId() == R.id.btnCreateOnAppEvent){
            //Subir la info al servidor
        }
        else if(view.getId() == R.id.btnCancelOnAppEvent){
            alertDialog.dismiss();
        }
        else if(view.getId() == R.id.btnHour){
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getFragmentManager(), "timePicker");
        }
        else if(view.getId() == R.id.btnDate){
            //Seleccionar fecha
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }

    //==================================================================================================================
    public static Intent newFacebookIntent(PackageManager pm, String url) {
        Uri uri = Uri.parse(url);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + url);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    //==================================================================================================================
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = 0;
            int minute = 0;

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }

    //==================================================================================================================
    class RESTPostTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        private String strURL;
        private HashMap<String, String> mapHeaders;
        private JSONObject jsonObject;
        private String strTaskCode;

        public RESTPostTask(
                String strURL,
                HashMap<String, String> mapHeaders,
                JSONObject jsonObject,
                String strTaskCode
        )
        {
            this.strURL = strURL;
            this.mapHeaders = mapHeaders;
            this.jsonObject = jsonObject;
            this.strTaskCode = strTaskCode;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ReportInfoActivity.this);
            progressDialog.setMessage("Inserting data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return postData();
            }
            catch (IOException ex)
            {
                return "Network error.";
            }
            catch (JSONException ex)
            {
                return "Invalid data.";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }

        private String postData() throws IOException, JSONException
        {
            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Initialize and configure request, then connect to server.
                URL url = new URL(this.strURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true); //Enables output (body data)
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                //Write data into server.
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(this.jsonObject.toString());
                bufferedWriter.flush();

                //Read data response from server.
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            finally
            {
                if (bufferedReader != null)
                {
                    bufferedReader.close();
                }

                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }


            return result.toString();
        }
    }
}
