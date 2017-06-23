package com.example.ishraq.androidchatapp.Holder;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ishraq on 6/11/2017.
 */

public class QBChatDialogHolder {
    //Instance of the class
    private static QBChatDialogHolder instance;

    //Map of all the different chat dialogs
    private HashMap<String, QBChatDialog> qbChatDialogHashMap; //<Dialog's id, dialog>

    public static synchronized QBChatDialogHolder getInstance(){
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBChatDialogHolder.class){
            if(instance == null){
                instance = new QBChatDialogHolder();
            }
        }
        qbChatDialogHolder = instance;
        return qbChatDialogHolder;
    }

    public QBChatDialogHolder(){
        this.qbChatDialogHashMap = new HashMap<String, QBChatDialog>();
    }

    public void putDialogs(List<QBChatDialog> dialogs){
        for(QBChatDialog dialog:dialogs){
            putDialog(dialog);
        }
    }

    public void putDialog(QBChatDialog dialog) {
        this.qbChatDialogHashMap.put(dialog.getDialogId(), dialog);
    }

    public QBChatDialog getDialogById(String dialogID){
        return qbChatDialogHashMap.get(dialogID);
    }

    public List<QBChatDialog> getDialogsByIds(List<String> dialogIDs){
        List<QBChatDialog> qbChatDialogs = new ArrayList<QBChatDialog>();

        for(String id:dialogIDs){
            QBChatDialog chatDialog = getDialogById(id);
            if(chatDialog != null){
                qbChatDialogs.add(chatDialog);
            }
        }

        return qbChatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs(){
        ArrayList<QBChatDialog> allDialogs = new ArrayList<QBChatDialog>();
        for(String key:qbChatDialogHashMap.keySet()){
            allDialogs.add(qbChatDialogHashMap.get(key));
        }

        return allDialogs;
    }

    public void removeDialog(String id){
        qbChatDialogHashMap.remove(id);
    }

}
