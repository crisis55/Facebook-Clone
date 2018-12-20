package tutorial.android.bkav.com.facebookclone.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import tutorial.android.bkav.com.facebookclone.R;
import tutorial.android.bkav.com.facebookclone.activity.ChatActivity;
import tutorial.android.bkav.com.facebookclone.model.Chats;


public class ChatsFragment extends Fragment {

    private DatabaseReference friendRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private View mView;
    private RecyclerView myChatList;

    private String online_user_id;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myChatList = (RecyclerView) mView.findViewById(R.id.chat_list);
        myChatList.setHasFixedSize(true); // Tránh llamf thay đổi bố cục  . Khi bạn thêm sủa xóa
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatList.setLayoutManager(linearLayoutManager);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(
                Chats.class,
                R.layout.all_user_display_layout,
                ChatsViewHolder.class,
                friendRef
        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Chats model, int position) {
                final String list_user_id = getRef(position).getKey();
                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }
                        viewHolder.setUserName(userName);
                        viewHolder.setUserThumbImage(getContext(), thumbImage);
                        viewHolder.setUserStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (dataSnapshot.child("online").exists()) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);
                                } else {
                                    usersRef.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id", list_user_id);
                                                    chatIntent.putExtra("user_name", userName);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        myChatList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String username) {
            TextView userNameDispplay = (TextView) mView.findViewById(R.id.all_users_username);
            userNameDispplay.setText(username);
        }

        public void setUserThumbImage(final Context context, final String user_thumb_image) {
            final CircleImageView thum_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            Picasso.with(context).load(user_thumb_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_default)
                    .into(thum_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(user_thumb_image).placeholder(R.drawable.profile_default).into(thum_image);
                        }
                    });
        }
        // check user online if online display image status and visiable
        public void setUserOnline(String online_status) {
            ImageView onlineStatus = (ImageView) mView.findViewById(R.id.online_status);
            if (online_status.equals("true")) {
                onlineStatus.setVisibility(View.VISIBLE);
            } else {
                onlineStatus.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus) {
            TextView user_status = (TextView) mView.findViewById(R.id.all_user_status);
            user_status.setText(userStatus);
        }
    }
}
