package com.example.ishraq.androidchatapp.Common;

import com.example.ishraq.androidchatapp.Holder.QBUsersHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by Ishraq on 6/9/2017.
 */

public class Common {
    //Created class for setting up name of chat dialog, doesn't matter if chat is group chat or private chat now
    public static final String DIALOG_EXTRA = "Dialogs";

    public static String createDialogName(List<Integer> qbUsers){
        List<QBUser> qbUsers1 = QBUsersHolder.getInstance().getUsersByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        for(QBUser user:qbUsers1){
            name.append(user.getFullName()).append(" ");
        }
        if(name.length() > 25){
            name = name.replace(25, name.length() - 1, "...");
        }

        return name.toString();
    }
}