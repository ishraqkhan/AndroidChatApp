package com.example.ishraq.androidchatapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.ishraq.androidchatapp.Holder.QBUnreadMessageHolder;
import com.example.ishraq.androidchatapp.R;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;

/**
 * Created by Ishraq on 6/9/2017.
 */

public class ChatDialogAdapters extends BaseAdapter{
    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogAdapters(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }


    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        //Create new view when view is initially not there
        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_chat_dialog, null);

            TextView text_title, text_messsage;
            ImageView imageView, image_unread;

            text_title = (TextView) view.findViewById(R.id.list_chat_dialog_title);
            text_messsage = (TextView) view.findViewById(R.id.list_chat_dialog_message);

            imageView = (ImageView) view.findViewById(R.id.image_chatDialog);
            image_unread = (ImageView) view.findViewById(R.id.image_unread);

            text_title.setText(qbChatDialogs.get(position).getName());
            text_messsage.setText(qbChatDialogs.get(position).getLastMessage());

            ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
            int randColor = colorGenerator.getRandomColor();

            TextDrawable.IBuilder iBuilder = TextDrawable.builder().beginConfig().withBorder(4)
                    .endConfig()
                    .round();

            // Use the first character from chat dialog title to make chat dialog image
            TextDrawable textDrawable = iBuilder.build(text_title.getText().toString().substring(0,1).toUpperCase(),
                    randColor);
            imageView.setImageDrawable(textDrawable);

            //Set the number of unread messages
            TextDrawable.IBuilder unread_messages = TextDrawable.builder().beginConfig().withBorder(4).endConfig().round();
            int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if(unread_count > 0){
                TextDrawable unread_drawable = unread_messages.build(""+unread_count,
                        Color.BLUE);
                image_unread.setImageDrawable(unread_drawable);
            }

        }

        return view;
    }
}
