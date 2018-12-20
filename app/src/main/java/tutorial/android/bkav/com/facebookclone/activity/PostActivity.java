package tutorial.android.bkav.com.facebookclone.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import tutorial.android.bkav.com.facebookclone.R;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;

    private static final int GALLARY_PICK = 1;
    private String description = "";
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUri, current_user_id;
    private Uri imageUri = null;

    private StorageReference postsImageRef;
    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        postsImageRef = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        loadingBar = new ProgressDialog(this);

        mapping();


        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });


    }

    private void validatePostInfo() {
        description = postDescription.getText().toString().trim();
        if (imageUri == null) {
            Toast.makeText(this, "Please select post image ..", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please say something about your image ..", Toast.LENGTH_SHORT).show();

        } else {

            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait , while we are updating your new post ..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storingImageToFirebaseStorage();
        }
    }

    private void storingImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentdate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat curentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = curentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        StorageReference fielPath = postsImageRef.child("Post Images").child(imageUri.getLastPathSegment()
                + postRandomName + ".jpg");

        fielPath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(getApplicationContext(), "Image uploaded successfully to storge ..", Toast.LENGTH_SHORT).show();
                    savingPostInformationToDatabase();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error occured :" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void savingPostInformationToDatabase() {


        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child("user_name").getValue().toString();
                    String userProfileImage = dataSnapshot.child("user_image").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("postimage", downloadUri);
                    postMap.put("user_image", userProfileImage);
                    postMap.put("user_name", userFullName);


                    postRef.child(current_user_id + postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        sendUserToMainActivity();
                                        Toast.makeText(getApplicationContext(), "New Post is updated successfully ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error occured while update your post", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    private void openGallery() {
        Intent gallaryIntent = new Intent();
        gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        gallaryIntent.setType("image/*");
        startActivityForResult(gallaryIntent, GALLARY_PICK);
    }

    private void mapping() {
        selectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postDescription = (EditText) findViewById(R.id.click_post_description);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(PostActivity.this, MainHomeActivity.class);
        startActivity(intent);
    }
}
