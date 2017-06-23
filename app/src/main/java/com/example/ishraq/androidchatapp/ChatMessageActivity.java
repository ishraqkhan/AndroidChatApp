package com.example.ishraq.androidchatapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.ishraq.androidchatapp.Adapter.ChatMessageAdapter;
import com.example.ishraq.androidchatapp.Adapter.ListUserAdapter;
import com.example.ishraq.androidchatapp.Common.Common;
import com.example.ishraq.androidchatapp.Holder.QBChatMessagesHolder;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView list_chat_msgs;
    ImageButton submit_btn, smile_btn;
    EditText editContent;

    ChatMessageAdapter adapter;
    Toolbar chat_message_toolbar;

    //Count onine users
    ImageView img_online_count;
    TextView txt_online_count;

    //Variables for editing/deleting message
    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP){
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.chat_group_edit_name:
                editGroupName();
                break;
            case R.id.chat_group_add_user:
                addUser();
                break;
            case R.id.chat_group_remove_user:
                removeUser();
                break;
        }
        
        return true;
    }

    private void removeUser() {
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUser() {
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editGroupName() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(view);
        final EditText newName = (EditText) view.findViewById(R.id.edit_new_group_name);

        //Set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                qbChatDialog.setName(newName.getText().toString()); //Set new name of chat message in server

                QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                        .performAsync(new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                Toast.makeText(ChatMessageActivity.this, "Group chat name updated", Toast.LENGTH_SHORT).show();
                                chat_message_toolbar.setTitle(newName.getText().toString());
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        //Set alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Get index context meu click
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId()){
            case R.id.chat_message_update_message:
                updateMessage();
                break;
            case R.id.chat_message_delete_message:
                deleteMessage();
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog deleteDialog = new ProgressDialog(ChatMessageActivity.this);
        deleteDialog.setMessage("Please wait...");
        deleteDialog.show();

        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        QBRestChatService.deleteMessage(editMessage.getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                //Update message dialog
                retrieveMessage();
                deleteDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateMessage() {
        //Set menu for edit text to update message
        editMessage = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        editContent.setText(editMessage.getBody());
        isEditMode = true; //Allow editable
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu, menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        //Initialize views
        list_chat_msgs = (ListView) findViewById(R.id.list_of_messages);
        submit_btn = (ImageButton) findViewById(R.id.send_btn);
        smile_btn = (ImageButton) findViewById(R.id.smile_button);
        editContent = (EditText) findViewById(R.id.edit_content);
        img_online_count = (ImageView) findViewById(R.id.img_online_count);
        txt_online_count = (TextView) findViewById(R.id.txt_online_count);

        //Add context meu
        registerForContextMenu(list_chat_msgs);

        //Initialize toolbar
        chat_message_toolbar = (Toolbar) findViewById(R.id.chat_message_toolbar);

        initChatDialogs();

        retrieveMessage();

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editContent.getText().toString().isEmpty()) {
                    if (!isEditMode) {
                        QBChatMessage message = new QBChatMessage();
                        message.setBody(editContent.getText().toString());
                        message.setSenderId(QBChatService.getInstance().getUser().getId());
                        message.setSaveToHistory(true);

                        try {
                            qbChatDialog.sendMessage(message);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        //Show group message when message is directed to someone that isn't user
                        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                            //Store message
                            QBChatMessagesHolder.getInstance().putMessage(qbChatDialog.getDialogId(), message);
                            ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(message.getDialogId());
                            adapter = new ChatMessageAdapter(getBaseContext(), messages);
                            list_chat_msgs.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        //Remove content from edit text
                        editContent.setText("");
                        editContent.setFocusable(true);
                    } else {
                        final ProgressDialog updateDialog = new ProgressDialog(ChatMessageActivity.this);
                        updateDialog.setMessage("Please wait");
                        updateDialog.show();

                        QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
                        messageUpdateBuilder.updateText(editContent.getText().toString()).markDelivered().markRead();

                        QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder)
                                .performAsync(new QBEntityCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                        //Refresh data
                                        retrieveMessage();
                                        isEditMode = false;
                                        updateDialog.dismiss();

                                        //Reset edit text
                                        editContent.setText("");
                                        editContent.setFocusable(true);
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
            }
        });
    }

    private void initChatDialogs(){
        //Initialize chat dialog
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());

        //Register listeners incoming message
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        //Enable ability to join a group chat
        if(qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP){
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", e.getMessage());
                }
            });

            //Set title for toolbar
            chat_message_toolbar.setTitle(qbChatDialog.getName());
            setSupportActionBar(chat_message_toolbar);
        }

        final QBChatDialogParticipantListener qbChatDialogParticipantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String s, QBPresence qbPresence) {
                //s reperesents the dialog ID
                if(s == qbChatDialog.getDialogId()){
                    QBRestChatService.getChatDialogById(s).performAsync(new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                            //Add online user to count
                            try {
                                Collection<Integer> onlineList = qbChatDialog.getOnlineUsers();
                                TextDrawable.IBuilder builder = TextDrawable.builder()
                                        .beginConfig()
                                        .withBorder(4)
                                        .endConfig()
                                        .round();
                                TextDrawable online = builder.build("", Color.RED);
                                img_online_count.setImageDrawable(online);

                                txt_online_count.setText(String.format("%d/%d online users", onlineList.size(), qbChatDialog.getOccupants().size()));
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        qbChatDialog.addParticipantListener(qbChatDialogParticipantListener);

        qbChatDialog.addMessageListener(this);
    }

    private void retrieveMessage() {
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(500); //Retrieve only up to 500 messages

        if(qbChatDialog != null){
            QBRestChatService.getDialogMessages(qbChatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    //Store messages to cache in order to recycle and reuse them
                    QBChatMessagesHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);

                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    list_chat_msgs.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("ERROR", e.getMessage());
                }
            });
        }
    }


    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        //Store message
        QBChatMessagesHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessagesHolder.getInstance().getChatMessagesByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        list_chat_msgs.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("ERROR", e.getMessage());
    }
}

