package tutorial.android.bkav.com.facebookclone.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import tutorial.android.bkav.com.facebookclone.R;

public class ClickPostActivity extends AppCompatActivity {

    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private ImageView postImage;
    private TextView postDescription;
    private Button editPostButton;
    private Button deletePostButton;

    private String postKey = "", currentUserId = "", databaseUserId = "";
    String description = "", image = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postKey = getIntent().getExtras().get("PostKey").toString();

        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);


        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        editPostButton = (Button) findViewById(R.id.click_edit_post_button);
        deletePostButton = (Button) findViewById(R.id.click_delete_post_button);


        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    postDescription.setText(description);

                    Picasso.with(ClickPostActivity.this).load(image).placeholder(R.drawable.profile_default)
                            .into(postImage);

                    if (currentUserId.equals(databaseUserId)) {
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
                    }

                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editCurrentPost(description);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentPost();
            }
        });
    }

    private void editCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputFiled = new EditText(ClickPostActivity.this);
        inputFiled.setText(description);
        builder.setView(inputFiled);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickPostRef.child("description").setValue(inputFiled.getText().toString().trim());
                Toast.makeText(getApplicationContext(), "Post update successfully ???", Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void deleteCurrentPost() {
        clickPostRef.removeValue();
        sendUserToMainActivity();

        Toast.makeText(this, "Post has  been deleted .", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(ClickPostActivity.this, MainHomeActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}









