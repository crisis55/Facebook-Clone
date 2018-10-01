package tutorial.android.bkav.com.facebookclone;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button sendFriendRequest;
    private Button declineFriendRequest;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference friendRequestRef;
    private DatabaseReference friendRef;
    private DatabaseReference notificationRef;

    private String CURRENT_STATE = "";
    private String sender_user_id = "";
    private String receiver_user_id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        friendRequestRef.keepSynced(true);

        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRef.keepSynced(true);

        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        notificationRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        sendFriendRequest = (Button) findViewById(R.id.profile_visit_send_red_btn);
        declineFriendRequest = (Button) findViewById(R.id.profile_decline_friend_req_btn);
        profileName = (TextView) findViewById(R.id.profile_visit_username);
        profileStatus = (TextView) findViewById(R.id.profile_visit_user_status);
        profileImage = (ImageView) findViewById(R.id.profile_visit_user_image);

        CURRENT_STATE = "Not friend";

        usersRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile_default).into(profileImage);

                friendRequestRef.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild(receiver_user_id)) {
                                        String req_type = dataSnapshot.child(receiver_user_id)
                                                .child("request_type").getValue().toString();
                                        if (req_type.equals("sent")) {
                                            CURRENT_STATE = "request_sent";
                                            sendFriendRequest.setText("Cancel Friend Request");
                                            declineFriendRequest.setVisibility(View.INVISIBLE);
                                            declineFriendRequest.setEnabled(false);
                                        } else if (req_type.equals("received")) {
                                            CURRENT_STATE = "request_received";
                                            sendFriendRequest.setText("Accept Friend Request");

                                            declineFriendRequest.setVisibility(View.VISIBLE);
                                            declineFriendRequest.setEnabled(true);

                                            declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    declineFriendReq();
                                                }
                                            });
                                        }

                                    }
                                } else {
                                    friendRef.child(sender_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(receiver_user_id)) {
                                                        CURRENT_STATE = "Friend";
                                                        sendFriendRequest.setText("Unfriend this person");

                                                        declineFriendRequest.setVisibility(View.INVISIBLE);
                                                        declineFriendRequest.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        declineFriendRequest.setVisibility(View.INVISIBLE);
        declineFriendRequest.setEnabled(false);


        if (!sender_user_id.equals(receiver_user_id)) {
            sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequest.setEnabled(false);

                    if (CURRENT_STATE.equals("Not friend")) {
                        sendFriendRequestToAPerson();
                    }

                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelfriendRequest();
                    }

                    if (CURRENT_STATE.equals("request_received")) {
                        acceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("Friend")) {
                        unFriendAFriend();
                    }
                }
            });
        } else {
            declineFriendRequest.setVisibility(View.INVISIBLE);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void declineFriendReq() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "Not friend";
                                                sendFriendRequest.setText("Send Friend Request");
                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                declineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void unFriendAFriend() {
        friendRef.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "Not friend";
                                                sendFriendRequest.setText("Send Friend Request");
                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                declineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar calFordate = Calendar.getInstance();
        final SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(calFordate.getTime());

        friendRef.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendRef.child(receiver_user_id).child(sender_user_id).child("date")
                                .setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                sendFriendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "Friend";
                                                                                sendFriendRequest.setText("Unfriend this person");

                                                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                declineFriendRequest.setEnabled(false);

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void CancelfriendRequest() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "Not friend";
                                                sendFriendRequest.setText("Send Friend Request");
                                                declineFriendRequest.setVisibility(View.INVISIBLE);
                                                declineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequestToAPerson() {
        friendRequestRef.child(sender_user_id).child(receiver_user_id)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestRef.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", sender_user_id);
                                                notificationData.put("type", "request");

                                                notificationRef.child(receiver_user_id).push().setValue(notificationData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    sendFriendRequest.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    sendFriendRequest.setText("Cancel Friend Request");

                                                                    declineFriendRequest.setVisibility(View.INVISIBLE);
                                                                    declineFriendRequest.setEnabled(false);
                                                                }

                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}






