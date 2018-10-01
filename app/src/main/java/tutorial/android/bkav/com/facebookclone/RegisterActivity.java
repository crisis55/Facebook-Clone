package tutorial.android.bkav.com.facebookclone;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText register_username;
    private EditText register_password;
    private EditText register_email;
    private Button createAccountButton;


    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDeafultDataRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        register_username = (EditText) findViewById(R.id.register_name);
        register_email = (EditText) findViewById(R.id.register_email);
        register_password = (EditText) findViewById(R.id.register_password);
        createAccountButton = (Button) findViewById(R.id.create_account_button);

        loadingBar = new ProgressDialog(this);


        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = register_username.getText().toString().trim();
                String email = register_email.getText().toString().trim();
                String password = register_password.getText().toString().trim();

                registerAccount(name, email, password);
            }
        });

    }

    private void registerAccount(final String name, final String email, String password) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), "Please write your name ???", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please write your email ..", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please write your password ??", Toast.LENGTH_SHORT);
        } else {
            loadingBar.setTitle("Creating new account ");
            loadingBar.setMessage("Please wait , while we are creating account for you ...");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();


                                String current_user_id = mAuth.getCurrentUser().getUid();
                                storeUserDeafultDataRef = FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child(current_user_id);

                                storeUserDeafultDataRef.child("user_name").setValue(name);
                                storeUserDeafultDataRef.child("user_status").setValue("Hello . Nice to know you .");
                                storeUserDeafultDataRef.child("user_image").setValue("Default Profile");
                                storeUserDeafultDataRef.child("device_token").setValue(deviceToken);
                                storeUserDeafultDataRef.child("user_thumb_image").setValue("Deafault_Image")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        });

                            } else {
                                Toast.makeText(getApplicationContext(), "Error Occured . Try again ???", Toast.LENGTH_SHORT).show();
                            }
                            ;
                            loadingBar.dismiss();
                        }
                    });
        }
    }
}
