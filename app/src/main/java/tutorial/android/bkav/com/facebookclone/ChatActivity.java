package tutorial.android.bkav.com.facebookclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;
    private String messageSenderId;

    private Toolbar chatToolbar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userProfileImage;

    private ImageButton sendMessageButton;
    private ImageButton selectImageButton;
    private EditText inputMessgaeText;
    private RecyclerView userMessageList;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private StorageReference messageImageStoreRef;

    private final List<Messages> messagesList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        messageSenderId = mAuth.getCurrentUser().getUid();
        messageImageStoreRef = FirebaseStorage.getInstance().getReference().child("Message_Picture");


        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();



        chatToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chatToolbar);

        loadingBar = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        userNameTitle = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message);
        selectImageButton = (ImageButton) findViewById(R.id.select_image);
        inputMessgaeText = (EditText) findViewById(R.id.input_message);


        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messgae_list_of_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

        fetchMessages();


        userNameTitle.setText(messageReceiverName);

        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile_default)
                        .into(userProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.profile_default).into(userProfileImage);
                            }
                        });
                if (online.equals("true")) {
                    userLastSeen.setText("Online");
                } else {
                    LastSeenTime getTime = new LastSeenTime();
                    long last_seen = Long.parseLong(online);

                    String lastSeenDisplayTime = getTime.getTimeAgo(last_seen, getApplicationContext());

                    userLastSeen.setText(lastSeenDisplayTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {

            loadingBar.setTitle("Sending Chat Image");
            loadingBar.setMessage("Please wait , while your message is sending ...");
            loadingBar.show();
            Uri imageUri = data.getData();

            final String message_sender_ref = "Message/" + messageSenderId + "/" + messageReceiverId;
            final String message_receiver_ref = "Message/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();

            final String message_push_id = user_message_key.getKey();

            StorageReference filePath = messageImageStoreRef.child(message_push_id + ".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messageBody = new HashMap();
                        messageBody.put("message", downloadUrl);
                        messageBody.put("seen", false);
                        messageBody.put("type", "image");
                        messageBody.put("time", ServerValue.TIMESTAMP);
                        messageBody.put("from", messageSenderId);

                        Map messageBodyDetail = new HashMap();
                        messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageBody);
                        messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageBody);

                        rootRef.updateChildren(messageBodyDetail, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.d("Chat_Log", databaseError.getMessage().toString());
                                }
                                inputMessgaeText.setText("");
                                loadingBar.dismiss();
                            }
                        });
                        Toast.makeText(getApplicationContext(), "Picture sent successfully ", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Picture not sent .", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void fetchMessages() {
        rootRef.child("Message").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String messageText = inputMessgaeText.getText().toString();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getApplicationContext(), "Please write your message ...", Toast.LENGTH_SHORT).show();
        } else {
            // Đường dẫn
            String message_sender_ref = "Message/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Message/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();

            String message_push_id = user_message_key.getKey();

            Map messageBody = new HashMap();
            messageBody.put("message", messageText);
            messageBody.put("seen", false);
            messageBody.put("type", "text");
            messageBody.put("time", ServerValue.TIMESTAMP);
            messageBody.put("from", messageSenderId);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageBody);
            messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageBody);

            rootRef.updateChildren(messageBodyDetail, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("check_log", databaseError.getMessage().toString());
                    }
                    inputMessgaeText.setText("");
                }
            });
        }
    }
}
