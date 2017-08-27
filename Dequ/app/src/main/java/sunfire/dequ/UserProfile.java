package sunfire.dequ;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.Profile;

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
    ScrollView scrollEvents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        btnCancel = (Button) findViewById(R.id.btnCancelUserProfile);
        btnLogout = (Button) findViewById(R.id.btnLogOutUserProfile);
        txtUserName = (TextView) findViewById(R.id.txtViewUserName);
        txtUserLevel = (TextView) findViewById(R.id.txtViewUserLevel);
        txtUserExp = (TextView) findViewById(R.id.txtViewUserExp);
        scrollEvents = (ScrollView) findViewById(R.id.scrollViewEvents);

        //Profile.getCurrentProfile()


        btnCancel.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnCancelUserProfile){
            finish();
        }
        else{
            
        }
    }
}
