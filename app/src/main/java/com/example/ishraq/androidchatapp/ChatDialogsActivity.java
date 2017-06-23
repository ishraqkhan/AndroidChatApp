package com.example.ishraq.androidchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ishraq.androidchatapp.Adapter.ChatDialogAdapters;
import com.example.ishraq.androidchatapp.Common.Common;
import com.example.ishraq.androidchatapp.R;
import com.example.ishraq.androidchatapp.Holder.QBChatDialogHolder;
import com.example.ishraq.androidchatapp.Holder.QBUnreadMessageHolder;
import com.example.ishraq.androidchatapp.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatDialogsActivity extends AppCompatActivity implements QBSystemMessageListener, QBChatDialogMessageListener{
    ListView listChatDialogs;
    FloatingActionButton fab;
    Toolbar toolbar;

    //Final varibales
    int contextMenuInfoPosition = -1;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuInfoPosition = info.position;

        switch(item.getItemId()){
            case R.id.context_delete_dialog:
                deleteDialog();
                break;
        }

        return true;
    }

    private void deleteDialog() {
        final QBChatDialog chatDialog = (QBChatDialog)listChatDialogs.getAdapter().getItem(contextMenuInfoPosition);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                listChatDialogs.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.chat_dialog_menu_user:
                showUserProfile();
                break;
            default:
                break;
        }

        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(ChatDialogsActivity.this, UserProfileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialogs);

        toolbar = (Toolbar) findViewById(R.id.chat_dialog_toolbar);
        toolbar.setTitle("Android Pro Chat");

        setSupportActionBar(toolbar);

        createSessionForChat();

        listChatDialogs = (ListView) findViewById(R.id.list_chat_dialogs);

        registerForContextMenu(listChatDialogs);

        listChatDialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)listChatDialogs.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialogsActivity.this, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                startActivity(intent);
            }
        });

        loadChatDialogs();

        fab = (FloatingActionButton) findViewById(R.id.chatdialog_add_user);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDialogsActivity.this, ListUsersActivity.class);
                startActivity(intent);
            }
        });

    }

    //Need to consistently update the chat dialogs in case someone makes a change during runtime
    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    private void loadChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                //store all dialogs in qbchatdialog holder
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                //Unread messages
                Set<String> setIds = new HashSet<String>();
                for(QBChatDialog chatDialog:qbChatDialogs){
                    setIds.add(chatDialog.getDialogId());
                }

                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {
                                //Save unread messages to unreadmessage holder class
                                QBUnreadMessageHolder.getInstance().setBundle(bundle);

                                //Refresh list dialogs
                                //Store all dialogs in QBChatDialogsHolder
                                ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(), QBChatDialogHolder.getInstance().getAllChatDialogs());
                                listChatDialogs.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {

                            }
                        });

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    private void createSessionForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialogsActivity.this);
        mDialog.setMessage("Please wait one moment...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        final String user = getIntent().getStringExtra("user");
        String password = getIntent().getStringExtra("password");

        //Load all the users and save into a cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.println(Log.INFO, "Error: ", ""+e.getMessage());
            }
        });

        final QBUser qbUser = new QBUser(user, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogsActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR", ""+e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        //Store dialog in QBChatDialog holder when we receive/send system message with a dialog ID that isn't null, Make life easier by being to get a dialog by a dialog id
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                //Store chat dialog in chaat dialog holder
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapters adapters = new ChatDialogAdapters(getBaseContext(), adapterSource);
                listChatDialogs.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR", e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
