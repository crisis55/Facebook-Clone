package tutorial.android.bkav.com.facebookclone.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import tutorial.android.bkav.com.facebookclone.R;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView settingDisplayProfileImage;
    private TextView settingDisplayName;
    private TextView settingDisplayStatus;
    private Button settingChangeProfileImage;
    private Button settingChangeStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference getUserDataRef;
    private StorageReference storeProfileImageRef;
    private StorageReference thumbImageRef;

    private static final int GALLARY_PICK = 1;

    private Bitmap thumb_bitmap = null;


    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataRef.keepSynced(true);

        storeProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageRef = FirebaseStorage.getInstance().getReference().child("Thumb_Image");

        settingDisplayProfileImage = (CircleImageView) findViewById(R.id.setting_profile_image);
        settingDisplayName = (TextView) findViewById(R.id.setting_username);
        settingDisplayStatus = (TextView) findViewById(R.id.setting_user_status);
        settingChangeProfileImage = (Button) findViewById(R.id.setting_change_profile_image);
        settingChangeStatus = (Button) findViewById(R.id.setting_change_status);


        loadingBar = new ProgressDialog(this);

        getUserDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String name = dataSnapshot.child("user_name").getValue().toString();
                    String status = dataSnapshot.child("user_status").getValue().toString();
                    final String image = dataSnapshot.child("user_image").getValue().toString();
                    String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                    settingDisplayName.setText(name);
                    settingDisplayStatus.setText(status);
                    if (image.equals("Default Profile")) {

                    } else {
                        Picasso.with(getApplicationContext()).load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.profile_default).
                                into(settingDisplayProfileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getApplicationContext()).load(image)
                                                .placeholder(R.drawable.profile_default)
                                                .into(settingDisplayProfileImage);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLARY_PICK);
            }
        });


        settingChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String old_status = settingDisplayStatus.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("user_status", old_status);
                statusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(statusIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Updating profie image .");
                loadingBar.setMessage("Please wait , while we are updting profile image .");
                loadingBar.show();

                Uri resultUri = result.getUri();
                File thumbFilePath = new File(resultUri.getPath());

                String user_id = mAuth.getCurrentUser().getUid();

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(50)
                            .compressToBitmap(thumbFilePath);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();


                StorageReference filePath = storeProfileImageRef.child(user_id + ".jpg");

                final StorageReference thum_filePath = thumbImageRef.child(user_id + ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Saving your profile image to firebase .", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thum_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thum_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (task.isSuccessful()) {
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image", downloadUrl);
                                        update_user_data.put("user_thumb_image", thum_downloadUrl);


                                        getUserDataRef.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getApplicationContext(),
                                                                "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                });
                                    }
                                }
                            });


                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(getApplicationContext(),
                        "Error occured , while uploading your profile pick .", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
}
