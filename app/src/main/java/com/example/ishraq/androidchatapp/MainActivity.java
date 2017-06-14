package com.example.ishraq.androidchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //Authentication information to send to Quick Blox cloud platform
    static final String APP_ID = "59012";
    static final String AUTH_KEY = "Bcj2n5a9zqteLcZ";
    static final String AUTH_SECRET = "yEf2ABQdO-pMFDw";
    static final String ACCOUNT_KEY = "et5F4m6rZ5G4jKN1Yz-w";

    private Button btn_login, btn_register;
    private EditText main_login, main_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFrameWork();

        btn_login = (Button) findViewById(R.id.main_btn_login);
        btn_register = (Button) findViewById(R.id.main_btn_signin);

        main_login = (EditText) findViewById(R.id.main_login_edit);
        main_password = (EditText) findViewById(R.id.main_password_edit);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = main_login.getText().toString();
                final String password = main_password.getText().toString();

                QBUser qbUser = new QBUser(user, password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, ChatDialogsActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish(); //close login activity after login goes through
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initializeFrameWork() {
        //Start this specific application based on the Id, key, etc
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
