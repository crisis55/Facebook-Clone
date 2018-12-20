package tutorial.android.bkav.com.facebookclone.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tutorial.android.bkav.com.facebookclone.R;

public class StatusActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference changeStatusRef;

    private Toolbar mToolbar;
    private EditText statusInput;
    private Button saveChangeButton;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        statusInput = (EditText) findViewById(R.id.status_input);
        saveChangeButton = (Button) findViewById(R.id.save_status_change_button);

        loadingBar = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();
        statusInput.setText(old_status);


        saveChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_status = statusInput.getText().toString().trim();
                changeProfileStatus(new_status);
            }
        });
    }

    private void changeProfileStatus(String new_status) {
        if (TextUtils.isEmpty(new_status)) {
            Toast.makeText(getApplicationContext(), "Please write your new status ???", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Change profile status ");
            loadingBar.setMessage("Please wait , while we are updating your profile status ...");
            loadingBar.show();

            changeStatusRef.child("user_status").setValue(new_status)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                loadingBar.dismiss();
                                Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                                settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(settingIntent);
                                Toast.makeText(getApplicationContext(), "Profile status updated successfully ..", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "Error Occured : ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
