package com.example.ishraq.androidchatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ishraq.androidchatapp.Adapter.ListUserAdapter;
import com.example.ishraq.androidchatapp.Common.Common;
import com.example.ishraq.androidchatapp.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IntrospectionProvider;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {
    ListView listUsers;
    Button btn_create_chat;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<QBUser>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);


        listUsers = (ListView) findViewById(R.id.list_users);
        listUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btn_create_chat = (Button) findViewById(R.id.create_chat);
        btn_create_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mode == null || mode.isEmpty()) {
                    if (listUsers.getCheckedItemPositions().size() == 1) {
                        makePrivateChat(listUsers.getCheckedItemPositions());
                    } else if (listUsers.getCheckedItemPositions().size() > 1) {
                        makeGroupChat(listUsers.getCheckedItemPositions());
                    } else {
                        Toast.makeText(ListUsersActivity.this, "Please select user(s) to start chat with", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null){
                    if(userAdd.size() > 0){
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int countChoice = listUsers.getCount();
                        SparseBooleanArray checkItemPositions = listUsers.getCheckedItemPositions();
                        for(int i = 0; i < countChoice; i++){
                            if(checkItemPositions.get(i)){
                                QBUser user = (QBUser)listUsers.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }

                        //Call rest chat service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(getBaseContext(), "Add user success", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null){
                    if(userAdd.size() > 0){
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int countChoice = listUsers.getCount();
                        SparseBooleanArray checkItemPositions = listUsers.getCheckedItemPositions();
                        for(int i = 0; i < countChoice; i++){
                            if(checkItemPositions.get(i)){
                                QBUser user = (QBUser)listUsers.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }

                        //Call rest chat service
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(getBaseContext(), "Remove user success", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });

        if((mode == null || mode.isEmpty()) && qbChatDialog == null){
            retrieveAllUsers();
        }
        else{
            if(mode.equals(Common.UPDATE_ADD_MODE)){
                loadListAvailableUsers();
            }
            else if (mode.equals(Common.UPDATE_REMOVE_MODE)){
                loadListUsersInGroup();
            }
        }
    }

    private void loadListUsersInGroup() {
        btn_create_chat.setText("Remove users");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                List<Integer> occupantIds = qbChatDialog.getOccupants();
                List<QBUser> usersAlreadyInGroup = QBUsersHolder.getInstance().getUsersByIds(occupantIds);
                ArrayList<QBUser> users = new ArrayList<QBUser>();
                users.addAll(usersAlreadyInGroup);

                ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), users);
                listUsers.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                userAdd = users;
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void loadListAvailableUsers() {
        btn_create_chat.setText("Add users");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ArrayList<QBUser> lstUsers = QBUsersHolder.getInstance().getAllUsers();
                List<Integer> occupantIds = qbChatDialog.getOccupants();
                List<QBUser> usersAlreadyInGroup = QBUsersHolder.getInstance().getUsersByIds(occupantIds);

                //Remove users already in chat group
                for(QBUser user:usersAlreadyInGroup){
                    lstUsers.remove(user);
                }

                if(lstUsers.size() > 0){
                    ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), lstUsers);
                    listUsers.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    userAdd = lstUsers;
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListUsersActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeGroupChat(SparseBooleanArray checkItemPositions){
        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int num_count = listUsers.getCount();
        ArrayList<Integer> occupantIds = new ArrayList<Integer>();

        //Gets all the checked users
        for (int i = 0; i < num_count; i++){
            if(checkItemPositions.get(i)){
                QBUser user = (QBUser) listUsers.getItemAtPosition(i);
                occupantIds.add(user.getId());
            }
        }

        //Make a chat dialog
        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createDialogName(occupantIds));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIds);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialog.dismiss();
                Toast.makeText(getBaseContext(), "Chat Dialog created successfully!", Toast.LENGTH_SHORT).show();

                //Send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for(int i = 0; i < qbChatDialog.getOccupants().size(); i++){
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }

                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });

    }

    private void makePrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int num_count = listUsers.getCount();

        for(int i = 0; i < num_count; i++){
            if(checkedItemPositions.get(i)){
                final QBUser user = (QBUser) listUsers.getItemAtPosition(i);

                //Quick Blox has built in methods for creating one on one private chat dialogs!
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Private chat dialog made successfully!", Toast.LENGTH_SHORT).show();

                        //Send system message to recipient id user
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", e.getMessage());
                    }
                });
            }
        }
    }

    private void retrieveAllUsers() {
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle){
                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> otherUsers = new ArrayList<QBUser>();
                for(QBUser user : qbUsers){
                    if(!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin())){
                        otherUsers.add(user);
                    }

                    ListUserAdapter adapter = new ListUserAdapter(getBaseContext(), qbUsers);
                    listUsers.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }
}
