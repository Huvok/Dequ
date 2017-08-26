/* TASK - Login Activity */
package sunfire.dequ;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

//                                                          //AUTHOR: Hugo García
//                                                          //CO-AUTHOR:
//                                                          //Date: 6/19/2017
//                                                          //PURPOSE: FB Login activity..

//======================================================================================================================
public class LoginActivity extends AppCompatActivity
{
    //------------------------------------------------------------------------------------------------------------------
    //                                                      //PROPERTIES
    private LoginButton loginbtnFB;
    private CallbackManager callbackManagerFB;

    //------------------------------------------------------------------------------------------------------------------
    //                                                      //METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //                                                  //Register the button and manager for login.
        callbackManagerFB = CallbackManager.Factory.create();
        loginbtnFB = (LoginButton) findViewById(R.id.loginButton);
        loginbtnFB.registerCallback(callbackManagerFB, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                subGoToMainScreen();
            }

            @Override
            public void onCancel()
            {
                Toast.makeText(getApplicationContext(), R.string.login_cancel, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error)
            {
                Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private void subGoToMainScreen()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
            Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManagerFB.onActivityResult(requestCode, resultCode, data);
    }
}
//======================================================================================================================
/* END-TASK */
