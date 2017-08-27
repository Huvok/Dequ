/* TASK - Main Activity */
package sunfire.dequ;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//                                                          //AUTHOR: Hugo García
//                                                          //CO-AUTHOR:
//                                                          //Date: 6/19/2017
//                                                          //PURPOSE: Main Dequ activity.

//======================================================================================================================
public class MainActivity
    extends
        AppCompatActivity
    implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener,
        PopupMenu.OnMenuItemClickListener,
        GoogleMap.OnInfoWindowClickListener
{
    //------------------------------------------------------------------------------------------------------------------
    //                                                      //VARIABLES AND PROPERTIES
    private GoogleMap googleMap;

    //                                                      //The entry point to Google Play services, used by the
    //                                                      //      Fused Location Provider.
    private GoogleApiClient googleApiClient;

    //                                                      //A default location (Sydney, Australia) and default zoom to
    //                                                      //      use when location permission is not granted.
    private final LatLng latLngDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean boolLocationPermissionGranted;

    //                                                      //The geographical location where the device is currently
    //                                                      //      located. That is, the last-known location retrieved
    //                                                      //      by the Fused Location Provider.
    private Location locationLastKnownLocation;

    private CameraPosition cameraPosition;

    //                                                      //Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private ProfileTracker profileTracker;
    private Profile profile;

    //                                                      //Used for the heatmap.
    private HeatmapTileProvider heatmapTileProvider;
    private TileOverlay tileOverlay;

    public static final int REQUEST_LOCATION_CODE = 99;

    private ProgressBar progressBarExperience;
    private int intCurrentExperience = 0;
    ImageView imgViewPin;

    ImageButton imgBtnSettings;
    Button btnReport;
    Button btnPlaceReport;
    Button btnCancelReport;
    Button btnChooseImage;
    Button btnOnOffMarkers;
    boolean boolAreMarkersVisible;
    AlertDialog.Builder dialogPlaceReport;
    AlertDialog alertDialog;

    String encodedImage;
    ArrayList<WeightedLatLng> lstHeatMap;
    View viewReportDialog;

    //                                                      //Report dialog elements
    EditText edTxtReportTitle;
    Spinner spinnerReportType;
    Spinner spinnerReportLevel;
    EditText edTxtDescription;

    ArrayList<Marker> lstMarkers;

    private static final int ALT_HEATMAP_RADIUS = 50;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.7;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 0),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 0),
            Color.rgb(170, 255, 0),
            Color.rgb(255, 170, 0),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.80f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //                                                  //Retrieve location and camera position from saved instance
        //                                                  //      state.
        if (savedInstanceState != null)
        {
            locationLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //                                                  //Render the activity.
        setContentView(R.layout.activity_main);

        //                                                  //If not logged in, go to login screen.
        if (AccessToken.getCurrentAccessToken() == null)
        {
            subGoToLoginScreen();
        }
        else
        {
            //                                              //Get the facebook profile, because we will need some
            //                                              //      information later.
            if (Profile.getCurrentProfile() == null)
            {
                profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, Profile profile2)
                    {
                        profileTracker.stopTracking();
                    }
                };
            }
            else
            {
                profile = Profile.getCurrentProfile();
            }

            //                                              //We just have to actively check permissions since
            //                                              //      Marshmallow.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                subCheckLocationPermission();
            }

            //                                              //Build the Play services client for use by the Fused
            //                                              //      Location Provider and the Places API. Use the
            //                                              //      addApi() method to request the Google APIs and
            //                                              //      the Fused Location Provider.
            googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
            googleApiClient.connect();

            imgBtnSettings = (ImageButton) findViewById(R.id.imgBtnSettings);

            imgBtnSettings.setOnClickListener(this);
            TextView txtViewUserName = (TextView) findViewById(R.id.txtViewUserName);
            TextView txtViewUserLevelAndExp = (TextView) findViewById(R.id.txtViewUserLevelAndExp);
            imgViewPin = (ImageView) findViewById(R.id.imgViewPin);
            imgViewPin.setVisibility(View.INVISIBLE);
            btnReport = (Button) findViewById(R.id.btnReport);
            btnPlaceReport = (Button) findViewById(R.id.btnPlaceReport);
            btnPlaceReport.setOnClickListener(this);
            btnReport.setOnClickListener(this);
            btnPlaceReport.setVisibility(View.INVISIBLE);
            btnOnOffMarkers = (Button) findViewById(R.id.btnOnOffMarkers);
            btnOnOffMarkers.setOnClickListener(this);

            ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(profile.getId());

            txtViewUserName.setText(profile.getFirstName() + " " + profile.getLastName());
            txtViewUserLevelAndExp.setText(txtViewUserLevelAndExp.getText() + ": 0"+ "    " + "0/100");
            boolAreMarkersVisible = false;
        }
    }

    private void SelectImage()
    {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();

                if (items[i].equals("Camera"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 1);
                }
                else if (items[i].equals("Gallery"))
                {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select Picture"), 0);
                }
            }
        });

        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == 1)
            {
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            }
            else if (requestCode == 0)
            {
                Uri selectedImageUri = data.getData();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap bmp = null;
                try {
                    bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            }

            HashMap<String, String> mapHeaders = new HashMap<String, String>();
            mapHeaders.put("Content-Type", "application/json");

            LatLng latLngCurrentLocation = googleMap.getCameraPosition().target;

            JSONObject jsonObjectNewReport = new JSONObject();
            try {
                jsonObjectNewReport.put("user_id", profile.getId().toString());
                jsonObjectNewReport.put("title", edTxtReportTitle.getText().toString());
                jsonObjectNewReport.put("type", spinnerReportType.getSelectedItem().toString());
                jsonObjectNewReport.put("level", spinnerReportLevel.getSelectedItemPosition());
                jsonObjectNewReport.put("description", edTxtDescription.getText().toString());
                jsonObjectNewReport.put("latitude", latLngCurrentLocation.latitude);
                jsonObjectNewReport.put("longitude", latLngCurrentLocation.longitude);
                jsonObjectNewReport.put("image", encodedImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new RESTPostTask("http://" + getString(R.string.server_url) + "/api/reports", mapHeaders, jsonObjectNewReport, "Report").execute();

            lstHeatMap.add(new WeightedLatLng(latLngCurrentLocation));
            heatmapTileProvider.setWeightedData(lstHeatMap);
            tileOverlay.clearTileCache();
            alertDialog.dismiss();
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void subCheckLocationPermission()
    {
        //                                                  //The only "dangerous" permission is the location one.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED)
        {
            //                                              //If not granted, check if it has been asked before.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                //                                          //If true, user must activate it manually.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You have to allow the location services to use Dequ.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Toast.makeText(MainActivity.this,
                                "You cannot change the world if you do not let us help :(", Toast.LENGTH_LONG).show();
                        }
                    })
                    .create();
            }
            else
            {
                //                                          //If not, just ask for it.
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_LOCATION_CODE);
            }
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private void subGoToLoginScreen()
    {
        //                                                  //Start login activity making sure that the app sees it like
        //                                                  //      the first started activity.
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
            Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //Saves the state of the map when the activity is paused.
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, locationLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //Builds the map when the Google Play services client is
    //                                                      //      successfully connected and requests for location
    //                                                      //      updates.
    @Override
    public void onConnected(Bundle connectionHint)
    {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationRequest locationRequest;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //Handles failure to connect to the Google Play services.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result)
    {
        Log.d("MainActivity", "Play services connection failed: ConnectionResult.getErrorCode() = "
            + result.getErrorCode());
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //Handles suspension of the connection to the Google Play
    //                                                      //      services client.
    @Override
    public void onConnectionSuspended(int cause)
    {
        Log.d("MainActivity", "Play services connection suspended");
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.imgBtnSettings)
        {
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(MainActivity.this, imgBtnSettings);
            //Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.settings_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }
        else if (view.getId() == R.id.btnReport)
        {
            imgViewPin.setVisibility(View.VISIBLE);
            btnPlaceReport.setVisibility(View.VISIBLE);
            btnReport.setEnabled(false);
        }
        else if (view.getId() == R.id.btnPlaceReport)
        {
            imgViewPin.setVisibility(View.INVISIBLE);
            btnReport.setEnabled(true);
            btnPlaceReport.setVisibility(View.INVISIBLE);
            dialogPlaceReport = new AlertDialog.Builder(this);

            viewReportDialog = getLayoutInflater().inflate(R.layout.report_dialog_layout, null);
            dialogPlaceReport.setView(viewReportDialog).create();
            dialogPlaceReport.setTitle("Report Information");
            alertDialog = dialogPlaceReport.show();

            btnCancelReport = (Button) viewReportDialog.findViewById(R.id.btnCancelReport);
            btnChooseImage = (Button) viewReportDialog.findViewById(R.id.btnChooseImage);
            btnCancelReport.setOnClickListener(this);
            btnChooseImage.setOnClickListener(this);
        }
        else if (view.getId() == R.id.btnCancelReport)
        {
            alertDialog.dismiss();
        }
        else if (view.getId() == R.id.btnChooseImage)
        {
            edTxtReportTitle = (EditText) viewReportDialog.findViewById(R.id.edTxtReportTitle);
            spinnerReportType = (Spinner) viewReportDialog.findViewById(R.id.spinnerReportType);
            spinnerReportLevel = (Spinner) viewReportDialog.findViewById(R.id.spinnerReportLevel);;
            edTxtDescription = (EditText) viewReportDialog.findViewById(R.id.edTxtDescription);

            if (edTxtReportTitle.getText().toString().matches(""))
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Please provide a title for your report.",
                    Toast.LENGTH_SHORT);
                toast.show();
            }
            else if (edTxtDescription.getText().toString().matches(""))
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Please provide a description for your report.",
                    Toast.LENGTH_SHORT);
                toast.show();
            }
            else
            {
                SelectImage();
            }
        }
        else if (view.getId() == R.id.btnOnOffMarkers)
        {
            for (Marker marker : lstMarkers)
            {
                if (boolAreMarkersVisible)
                {
                    marker.setVisible(false);
                }
                else
                {
                    marker.setVisible(true);
                }
            }

            boolAreMarkersVisible = !boolAreMarkersVisible;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        if (item.getItemId() == R.id.menuBtnLanguage)
        {
            Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.menuBtnLogout)
        {
            subLogout(findViewById(R.id.viewMainActivity));
        }
        return true;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void subLogout(View view)
    {
        LoginManager.getInstance().logOut();
        subGoToLoginScreen();
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap map)
    {
        googleMap = map;

        //                                                  //Check if location provider is turned on.
        subPromptLocationProvider();

        //                                                  //Turn on the My Location layer and the related control on
        //                                                  //      the map.
        subUpdateLocationUI();

        //                                                  //Get the current location of the device and set the
        //                                                  //      position of the map.
        subGetDeviceLocation();

        addHeatMap();
    }

    private void addHeatMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");
        new RESTGetTask("Reports", "http://" + getString(R.string.server_url) + "/api/reports", null, map).execute();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //                                                      //If GPS provider is turned off, prompts an alert to change
    //                                                      //      it.
    public void subPromptLocationProvider()
    {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //                                                      //Updates the map's UI settings based on whether the user
    //                                                      //      has granted location permission.
    private void subUpdateLocationUI()
    {
        if (googleMap == null)
        {
            return;
        }

        //                                                  //Request location permission (even if we already asked it,
        //                                                  //      the compiler forces us to ask for it again.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            boolLocationPermissionGranted = true;
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (boolLocationPermissionGranted)
        {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        else
        {
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            locationLastKnownLocation = null;
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //                                                      //Gets the current location of the device, and positions the
    //                                                      //      map's camera.
    private void subGetDeviceLocation()
    {
        //                                                  //Request location permission, so that we can get the
        //                                                  //      location of the device. Again, we need to check it
        //                                                  //      even if we had before because of the compiler.
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            boolLocationPermissionGranted = true;
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        //                                                  //Get the best and most recent location of the device.
        if (boolLocationPermissionGranted)
        {
            locationLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        //                                                  //Set the map's camera position to the current location of
        //                                                  //      the device.
        if (cameraPosition != null)
        {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else if (locationLastKnownLocation != null)
        {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLastKnownLocation.getLatitude(),
                locationLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
        else
        {
            Log.d("MainActivity", "Current location is null. Using defaults.");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngDefaultLocation, DEFAULT_ZOOM));
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void onLocationChanged(Location location)
    {
        locationLastKnownLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        if (googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //Handles the result of the request for permissions.
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
        )
    {
        boolLocationPermissionGranted = false;
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    boolLocationPermissionGranted = true;
                }
            }
        }

        subUpdateLocationUI();
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");

        new RESTGetTask("Report", "http://" + getString(R.string.server_url) + "/api/report?id=" +
            marker.getTag(), null, map).execute();
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

            progressDialog = new ProgressDialog(MainActivity.this);
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

            if (this.strTaskCode.equals("Reports"))
            {
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                lstHeatMap = new ArrayList<WeightedLatLng>();
                JSONArray jsonarray = null;
                lstMarkers = new ArrayList<Marker>();
                int count = 1;
                try {
                    jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        Double Lat = Double.parseDouble(jsonobject.getString("latitude"));
                        Double Lng = Double.parseDouble(jsonobject.getString("longitude"));
                        lstHeatMap.add(new WeightedLatLng(new LatLng(Lat, Lng), count));
                        count++;
                        Marker marker;
                        if (jsonobject.getString("has_event").equals("true"))
                        {
                            marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Lat, Lng)).
                                    title(jsonobject.getString("title")).snippet(jsonobject.getString("type") +
                                    " / " + jsonobject.getString("level")).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                        else
                        {
                            marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Lat, Lng)).
                                    title(jsonobject.getString("title")).snippet(jsonobject.getString("type") +
                                    " / " + jsonobject.getString("level")).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }

                        marker.setVisible(false);
                        marker.setTag(jsonobject.getString("_id"));
                        lstMarkers.add(marker);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                googleMap.setOnInfoWindowClickListener(MainActivity.this);

                if (lstHeatMap.size() <= 0)
                {
                    lstHeatMap.add(new WeightedLatLng(new LatLng(0, 0)));
                }

                heatmapTileProvider = new HeatmapTileProvider.Builder().weightedData(lstHeatMap).build();
                heatmapTileProvider.setRadius(ALT_HEATMAP_RADIUS);
                heatmapTileProvider.setGradient(ALT_HEATMAP_GRADIENT);
                heatmapTileProvider.setOpacity(ALT_HEATMAP_OPACITY);

                // Add a tile overlay to the map, using the heat map tile provider.
                tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
            }
            else if (this.strTaskCode.equals("Report"))
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Intent intent = new Intent(getBaseContext(), ReportInfoActivity.class);
                    intent.putExtra("report", jsonObject.getString("_id"));
                    intent.putExtra("title", jsonObject.getString("title"));
                    intent.putExtra("type", jsonObject.getString("type"));
                    intent.putExtra("level", jsonObject.getString("level"));
                    intent.putExtra("image", jsonObject.getString("image"));
                    intent.putExtra("description", jsonObject.getString("description"));

                    startActivity(intent);
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
                String id = profile.getId();
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

            progressDialog = new ProgressDialog(MainActivity.this);
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

    //==================================================================================================================
    class PutDataTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Updating data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return putData(params[0]);
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

        private String putData(String urlPath) throws IOException, JSONException
        {
            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            try {
                //Create data to update.
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("name", "RemotePUT");
                dataToSend.put("lastname", "SheetPUT");

                //Initialize and configure request, then connect to server.
                URL url = new URL(urlPath);
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
                bufferedWriter.write(dataToSend.toString());
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
                if (bufferedReader != null)
                {
                    bufferedReader.close();
                }

                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }

        }
    }

    //==================================================================================================================
    class DeleteDataTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Deleting data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                return deleteData(params[0]);
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

            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }

        private String deleteData(String urlPath) throws IOException
        {
            String result = null;

            //Initialize and configure request, then connect to server.
            URL url = new URL(urlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            //Check if delete was successful.
            if (urlConnection.getResponseCode() == 200)
            {
                result = "Deletion successful.";
            }
            else
            {
                result = "Deletion failure.";
            }

            return result;
        }
    }
}
//======================================================================================================================
/* END-TASK */
