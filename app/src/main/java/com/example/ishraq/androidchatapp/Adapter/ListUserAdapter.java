package com.example.ishraq.androidchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by Ishraq on 6/9/2017.
 */

public class ListUserAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<QBUser> qbUsers;

    public ListUserAdapter(Context context, ArrayList<QBUser> qbUsers) {
        this.context = context;
        this.qbUsers = qbUsers;
    }


    @Override
    public int getCount() {
        return qbUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return qbUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(qbUsers.get(position).getLogin());
        }

        return view;
    }
}
