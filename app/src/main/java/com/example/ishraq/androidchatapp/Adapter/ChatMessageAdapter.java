package com.example.ishraq.androidchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ishraq.androidchatapp.Holder.QBUsersHolder;
import com.example.ishraq.androidchatapp.R;
import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

/**
 * Created by Ishraq on 6/10/2017.
 */

public class ChatMessageAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    //Constructor
    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            BubbleTextView bubbleTextView;

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId())){
                view = layoutInflater.inflate(R.layout.list_send_message, null);
                bubbleTextView = (BubbleTextView) view.findViewById(R.id.message_content);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
            }
            else{
                view = layoutInflater.inflate(R.layout.list_receive_message, null);
                bubbleTextView = (BubbleTextView) view.findViewById(R.id.message_content_receive);
                bubbleTextView.setText(qbChatMessages.get(position).getBody());
                TextView text_name = (TextView) view.findViewById(R.id.message_user);
                text_name.setText(QBUsersHolder.getInstance().getUserById(qbChatMessages.get(position).getSenderId()).getFullName());

            }
        }

        return view;
    }
}
