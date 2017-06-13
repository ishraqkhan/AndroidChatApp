package com.example.ishraq.androidchatapp.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ishraq on 6/10/2017.
 */
//Class helpful for storing messages
public class QBChatMessagesHolder {
    //Instance of this class
    private static QBChatMessagesHolder instance;

    //Hashmap to store the messages per user in chat
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray; //Id of the dialog, messages in that dialog

    //synchronized method in order to deal with many users sending messages in different dialogs at teh same time
    public static synchronized QBChatMessagesHolder getInstance(){
        QBChatMessagesHolder qbChatMessagesHolder;
        synchronized (QBChatMessagesHolder.class){
            if(instance == null){
                instance = new QBChatMessagesHolder();
            }
            qbChatMessagesHolder = instance;
        }
        return qbChatMessagesHolder;
    }

    private QBChatMessagesHolder(){
        this.qbChatMessageArray = new HashMap<String, ArrayList<QBChatMessage>>();
    }

    public void putMessages(String dialogId, ArrayList<QBChatMessage> qbChatMessages){
        this.qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMessage(String dialogId, QBChatMessage qbChatMessage){
        List<QBChatMessage> list_messages = (List)this.qbChatMessageArray.get(dialogId);
        list_messages.add(qbChatMessage);
        ArrayList<QBChatMessage> list_added = new ArrayList(list_messages.size());
        list_added.addAll(list_messages);
        putMessages(dialogId, list_added);
    }

    public ArrayList<QBChatMessage> getChatMessagesByDialogId(String dialogId){
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogId);
    }


}
