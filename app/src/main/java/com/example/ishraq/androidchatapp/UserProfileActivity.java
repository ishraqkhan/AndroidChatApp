package com.example.ishraq.androidchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ishraq.androidchatapp.Common.Common;
import com.example.ishraq.androidchatapp.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfileActivity extends AppCompatActivity {
    EditText edit_old_password, edit_new_password, edit_full_name, edit_email, edit_phone_num;
    Button btn_update, btn_cancel;
    Toolbar toolbar_profile;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.user_update_logout:
                logout();
                break;
            default:
                break;
        }
        return true;
    }

    private void logout() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "You have logged out", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Remove all previous activity
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Toolbar
        toolbar_profile = (Toolbar) findViewById(R.id.user_update_toolbar);
        toolbar_profile.setTitle("Android Pro Chat App");
        setSupportActionBar(toolbar_profile);

        //Initialize layout
        initViews();
        loadUserProfile();

        //Set onclick listeners
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = edit_old_password.getText().toString();
                String newPassword = edit_new_password.getText().toString();
                String fullName = edit_full_name.getText().toString();
                String email = edit_email.getText().toString();
                String phoneNumber = edit_phone_num.getText().toString();

                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId()); //Ensure that user's info is being updated instead of creating new user in system

                //Update user's info
                if(!Common.isNullOrEmptyString(oldPassword)){
                    user.setOldPassword(oldPassword);
                }
                if(!Common.isNullOrEmptyString(newPassword)){
                    user.setPassword(newPassword);
                }
                if(!Common.isNullOrEmptyString(fullName)){
                    user.setFullName(fullName);
                }
                if(!Common.isNullOrEmptyString(email)){
                    user.setEmail(email);
                }
                if(!Common.isNullOrEmptyString(phoneNumber)){
                    user.setPhone(phoneNumber);
                }

                //Show that update is in process
                final ProgressDialog mDialog = new ProgressDialog(UserProfileActivity.this);
                mDialog.setMessage("Please wait...");
                mDialog.show();
                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "User "+qbUser.getLogin()+"updated", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfileActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void loadUserProfile() {
        QBUser currentUser = QBChatService.getInstance().getUser();

        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        edit_full_name.setText(fullName);
        edit_email.setText(email);
        edit_phone_num.setText(phone);
    }

    private void initViews() {
        btn_cancel = (Button) findViewById(R.id.cancel_update_btn);
        btn_update = (Button) findViewById(R.id.user_update_btn);

        edit_old_password = (EditText) findViewById(R.id.update_edit_old_password);
        edit_new_password = (EditText) findViewById(R.id.update_edit_new_password);
        edit_full_name = (EditText) findViewById(R.id.update_edit_full_name);
        edit_email = (EditText) findViewById(R.id.update_edit_email);
        edit_phone_num = (EditText) findViewById(R.id.update_edit_phone_num);
    }
}
