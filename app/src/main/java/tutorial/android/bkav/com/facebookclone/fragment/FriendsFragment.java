package tutorial.android.bkav.com.facebookclone.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import tutorial.android.bkav.com.facebookclone.activity.ProfileActivity;
import tutorial.android.bkav.com.facebookclone.R;
import tutorial.android.bkav.com.facebookclone.activity.ChatActivity;
import tutorial.android.bkav.com.facebookclone.model.Friends;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView myFriendsList;

    private FirebaseAuth mAuth;
    private DatabaseReference friendsRef;
    private DatabaseReference usersRef;

    private String online_user_id;

    private View myMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);


        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        friendsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        myFriendsList = (RecyclerView) myMainView.findViewById(R.id.friend_list);
        myFriendsList.setHasFixedSize(true);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(

                Friends.class,
                R.layout.all_user_display_layout,
                FriendsViewHolder.class,
                friendsRef
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();
                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }
                        viewHolder.setUserName(userName);
                        viewHolder.setUserThumbImage(getContext(), thumbImage);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{
                                        userName + "'s Profile",
                                        "Send Message"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {
                                        if (position == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if (position == 1) {
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
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        myFriendsList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {
            TextView sincefriendDate = (TextView) mView.findViewById(R.id.all_user_status);
            sincefriendDate.setText("Friends since :\n" + date);
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

        public void setUserOnline(String online_status) {
            ImageView onlineStatus = (ImageView) mView.findViewById(R.id.online_status);
            if (online_status.equals("true")) {
                onlineStatus.setVisibility(View.VISIBLE);
            } else {
                onlineStatus.setVisibility(View.INVISIBLE);
            }
        }
    }
}






