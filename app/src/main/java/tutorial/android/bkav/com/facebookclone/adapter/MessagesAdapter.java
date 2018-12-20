package tutorial.android.bkav.com.facebookclone.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import tutorial.android.bkav.com.facebookclone.R;
import tutorial.android.bkav.com.facebookclone.model.Messages;

/**
 * Created by PHONG on 3/31/2018.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> messagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    public MessagesAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_user, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = messagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("user_name").getValue().toString();
                String user_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                Picasso.with(holder.userProfileImage.getContext()).load(user_image)
                        .placeholder(R.drawable.profile_default).into(holder.userProfileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {
            holder.messagePicture.setVisibility(View.INVISIBLE);
            if (fromUserId.equals(message_sender_id)) {
                holder.messgaeText.setBackgroundResource(R.drawable.message_text_background_two);
                holder.messgaeText.setGravity(Gravity.RIGHT);
            } else {
                holder.messgaeText.setBackgroundResource(R.drawable.message_text_background);
                holder.messgaeText.setTextColor(Color.WHITE);
                holder.messgaeText.setGravity(Gravity.LEFT);
            }


            holder.messgaeText.setText(messages.getMessage());

        } else {
            holder.messgaeText.setVisibility(View.INVISIBLE);
            holder.messgaeText.setPadding(0, 0, 0, 0);


            Picasso.with(holder.userProfileImage.getContext()).load(messages.getMessage())
                    .placeholder(R.drawable.profile_default).into(holder.messagePicture);

        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messgaeText;
        public CircleImageView userProfileImage;
        public ImageView messagePicture;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messgaeText = (TextView) itemView.findViewById(R.id.message_text);
            userProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messagePicture = (ImageView) itemView.findViewById(R.id.message_image_view);
        }
    }

}


