package com.example.ishraq.androidchatapp.Holder;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ishraq on 6/9/2017.
 */
//Class to store users
public class QBUsersHolder {
    private static QBUsersHolder instance;

    private SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QBUsersHolder getInstance(){
        if(instance == null){
            instance = new QBUsersHolder();
        }
        return instance;
    }

    private QBUsersHolder(){
        qbUserSparseArray = new SparseArray<QBUser>();
    }

    public void putUsers(List<QBUser> users){
        for(QBUser user:users){
            putUser(user);
        }
    }

    private void putUser(QBUser user) {
        qbUserSparseArray.put(user.getId(), user);
    }

    public QBUser getUserById(int id){
        return qbUserSparseArray.get(id);
    }

    public List<QBUser> getUsersByIds(List<Integer> ids){
        List<QBUser> qbUsers = new ArrayList<QBUser>();
        for(Integer id:ids){
            QBUser user = getUserById(id);
            if(user != null){
                qbUsers.add(user);
            }
        }

        return qbUsers;
    }

    public ArrayList<QBUser> getAllUsers(){
        ArrayList<QBUser> users = new ArrayList<QBUser>();
        for(int i = 0; i < qbUserSparseArray.size(); i++){
            users.add(qbUserSparseArray.valueAt(i));
        }

        return users;
    }
}
