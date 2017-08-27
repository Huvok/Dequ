package sunfire.dequ;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProfile
        extends
        AppCompatActivity
        implements
        View.OnClickListener
{
    Button btnCancel;
    Button btnLogout;
    TextView txtUserName;
    TextView txtUserLevel;
    TextView txtUserExp;
    ScrollView scrollMyEvents;
    ScrollView scrollOtherEvents;
    ArrayList<Event> lstCreatedEvents, lstEvents;
    ArrayList<TextView> lstViewCreatedEvents = new ArrayList<TextView>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        btnCancel = (Button) findViewById(R.id.btnCancelUserProfile);
        btnLogout = (Button) findViewById(R.id.btnLogOutUserProfile);
        txtUserName = (TextView) findViewById(R.id.txtViewUserName);
        txtUserLevel = (TextView) findViewById(R.id.txtViewUserLevel);
        txtUserExp = (TextView) findViewById(R.id.txtViewUserExp);
        scrollMyEvents = (ScrollView) findViewById(R.id.scrollViewMyEvents);
        scrollOtherEvents = (ScrollView) findViewById(R.id.scrollViewOtherEvents);

        ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        profilePictureView.setProfileId(Profile.getCurrentProfile().getId());
        txtUserName.setText(Profile.getCurrentProfile().getFirstName() + " " + Profile.getCurrentProfile().getLastName());
        btnCancel.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");
        new UserProfile.RESTGetTask("User", "http://" + getString(R.string.server_url) + "/api/user?id=" +
            Profile.getCurrentProfile().getId(), null, map).execute();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCancelUserProfile){
            finish();
        }
        else if(view.getId() == R.id.btnLogOutUserProfile){
            LoginManager.getInstance().logOut();
            subGoToLoginScreen();
        }
    }

    private void subGoToLoginScreen()
    {
        //                                                  //Start login activity making sure that the app sees it like
        //                                                  //      the first started activity.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

            progressDialog = new ProgressDialog(UserProfile.this);
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

            if (this.strTaskCode.equals("User"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    txtUserName.setText(jsonObject.getString("name") + " " + jsonObject.getString("lastname"));
                    txtUserLevel.setText(jsonObject.getString("level"));
                    txtUserExp.setText(jsonObject.getString("experience"));
                    JSONArray jsonArray = jsonObject.getJSONArray("created_events");
                    lstCreatedEvents = new ArrayList<Event>();
                    lstEvents = new ArrayList<Event>();
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("Content-Type", "application/json");
                        new UserProfile.RESTGetTask("Created Events", "http://" + getString(R.string.server_url) + "/api/event?id=" +
                            jsonArray.getString(i), null, map).execute();
                    }

                    jsonArray = jsonObject.getJSONArray("events");
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("Content-Type", "application/json");
                        new UserProfile.RESTGetTask("Events", "http://" + getString(R.string.server_url) + "/api/event?id=" +
                                jsonArray.getString(i), null, map).execute();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (this.strTaskCode.equals("Created Events"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Event e = new Event(jsonObject.getString("report"), Profile.getCurrentProfile().getId(),
                            jsonObject.getString("title"), jsonObject.getInt("people_needed"), jsonObject.getInt("people_count"),
                            jsonObject.getString("due_date"), jsonObject.getString("create_date"));
                    lstCreatedEvents.add(e);
                    TextView textView = new TextView(UserProfile.this);
                    lstViewCreatedEvents.add(textView);
                    int i = lstViewCreatedEvents.size() - 1;
                    lstViewCreatedEvents.get(i).setText((lstCreatedEvents.get(i).strTitle + "\r\n Created: " +
                            lstCreatedEvents.get(i).strCreateDate + "\r\n Scheduled: " + lstCreatedEvents.get(i).strDueDate
                            + "\r\n # People: " + String.valueOf(lstCreatedEvents.get(i).intPeopleCount) + "\r\n People needed: " +
                            String.valueOf(lstCreatedEvents.get(i).intPeopleNeeded) + "\r\n Description: " +
                            lstCreatedEvents.get(i).strReport));
                    scrollMyEvents.addView(lstViewCreatedEvents.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (this.strTaskCode.equals("Events"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Event e = new Event(jsonObject.getString("report"), Profile.getCurrentProfile().getId(),
                            jsonObject.getString("title"), jsonObject.getInt("people_needed"), jsonObject.getInt("people_count"),
                            jsonObject.getString("due_date"), jsonObject.getString("create_date"));
                    lstEvents.add(e);
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

    class Event
    {
        String strReport;
        String strUserId;
        String strTitle;
        int intPeopleNeeded;
        int intPeopleCount;
        String strDueDate;
        String strCreateDate;

        Event(
                String strReport,
                String strUserId,
                String strTitle,
                int intPeopleNeeded,
                int intPeopleCount,
                String strDueDate,
                String strCreateDate
            ) {
            this.strReport = strReport;
            this.strUserId = strUserId;
            this.strTitle = strTitle;
            this.intPeopleNeeded = intPeopleNeeded;
            this.intPeopleCount = intPeopleCount;
            this.strDueDate = strDueDate;
            this.strCreateDate = strCreateDate;
        }
    }
}
