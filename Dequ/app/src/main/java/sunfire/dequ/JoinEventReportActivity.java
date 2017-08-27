package sunfire.dequ;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

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
    Button btnSeeAttendance;
    AlertDialog alertDialog;
    Button btnCloseAttendance;
    ArrayList<String> lstAttendanceIds = new ArrayList<String>();

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
        btnSeeAttendance = (Button) findViewById(R.id.btnSeeAtendance);
        btnSeeAttendance.setOnClickListener(this);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");
        new JoinEventReportActivity.RESTGetTask("EventByReportForList", "http://" + getString(R.string.server_url) + "/api/event_by_report?id=" +
                getIntent().getExtras().getString("report"), null, map).execute();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCancelJoinEvent){
            finish();
        }
        else if(view.getId() == R.id.btnJoinEventJoin){
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Content-Type", "application/json");
            new JoinEventReportActivity.RESTGetTask("EventByReport", "http://" + getString(R.string.server_url) + "/api/event_by_report?id=" +
                getIntent().getExtras().getString("report"), null, map).execute();
        }
        else if (view.getId() == R.id.btnSeeAtendance)
        {
            AlertDialog.Builder dialogPlaceReport = new AlertDialog.Builder(this);

            View viewReportDialog = getLayoutInflater().inflate(R.layout.attendance_dialog, null);
            final LinearLayout layoutScrollView = (LinearLayout) viewReportDialog.findViewById(R.id.layoutScrollView);

            for (String str : lstAttendanceIds)
            {
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + str,
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                LinearLayout ly = new LinearLayout(JoinEventReportActivity.this);
                                ProfilePictureView profilePictureView = new ProfilePictureView(JoinEventReportActivity.this);
                                profilePictureView.setProfileId(Profile.getCurrentProfile().getId());
                                TextView textView = new TextView(JoinEventReportActivity.this);
                                try {
                                    textView.setText(response.getJSONObject().getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                textView.setPadding(50, 50, 0, 0);
                                profilePictureView.setPadding(20, 0, 0, 0);
                                ly.addView(profilePictureView);
                                ly.addView(textView);
                                layoutScrollView.addView(ly);
                            }
                        }
                ).executeAsync();
            }

            dialogPlaceReport.setView(viewReportDialog).create();
            dialogPlaceReport.setTitle("Attendance");
            alertDialog = dialogPlaceReport.show();

            btnCloseAttendance = (Button) viewReportDialog.findViewById(R.id.btnCloseAttendance);
            btnCloseAttendance.setOnClickListener(this);
        }
        else if (view.getId() == R.id.btnCloseAttendance)
        {
            alertDialog.dismiss();
        }
    }

    //==================================================================================================================
    class RESTGetTask extends AsyncTask<String, Void, String>
    {
        private String strURLPath;
        private HashMap<String, String> mapParamenters;
        private HashMap<String, String> mapHeaders;
        private String strTaskCode;

        public RESTGetTask(
                String strTaskCode,
                String strURLPath,
                HashMap<String, String> mapParameters,
                HashMap<String, String> mapHeaders
        )
        {
            this.strURLPath = strURLPath;
            this.mapParamenters = mapParameters;
            this.mapHeaders = mapHeaders;
            this.strTaskCode = strTaskCode;
        }

        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = new ProgressDialog(JoinEventReportActivity.this);
            progressDialog.setMessage("Preparing to change the world...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return getData();
            }
            catch (IOException ex)
            {
                return "Network error.";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if (this.strTaskCode.equals("EventByReport"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject jsonObjectToUpdate = new JSONObject();
                    jsonObjectToUpdate.put("event", jsonObject.getString("_id"));
                    new JoinEventReportActivity.RESTPutTask("http://" + getString(R.string.server_url) + "/api/user/event?id=" +
                            Profile.getCurrentProfile().getId(), mapHeaders, jsonObjectToUpdate, "Event").execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (this.strTaskCode.equals("EventByReportForList"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("attending");

                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        lstAttendanceIds.add(jsonArray.getString(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }

        private String getData() throws IOException
        {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;
            //Initialize and config request, then connect to server.
            try
            {
                URL url = new URL(this.strURLPath);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Authorization", AccessToken.getCurrentAccessToken().toString());
                urlConnection.connect();

                //Read data response from server
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    result.append(line).append("\n");
                }
            }
            catch(Exception ex)
            {
                String hue = ex.toString();
            }
            finally
            {
                if (bufferedReader != null)
                {
                    bufferedReader.close();
                }
            }

            return result.toString();
        }
    }

    //==================================================================================================================
    class RESTPutTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        private String strURL;
        private HashMap<String, String> mapHeaders;
        private JSONObject jsonObject;
        private String strTaskCode;

        public RESTPutTask (
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

            progressDialog = new ProgressDialog(JoinEventReportActivity.this);
            progressDialog.setMessage("Updating data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return putData();
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

            finish();
        }

        private String putData() throws IOException, JSONException
        {
            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;

            try {
                //Initialize and configure request, then connect to server.
                URL url = new URL(this.strURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true); //Enables output (body data)
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                //Write data into server.
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(this.jsonObject.toString());
                bufferedWriter.flush();

                if (urlConnection.getResponseCode() == 200)
                {
                    return "Update successfull.";
                }
                else
                {
                    return "Update failure.";
                }
            }
            finally
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }

        }
    }
}
