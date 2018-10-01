package tutorial.android.bkav.com.facebookclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private Toolbar mToolbar;
    private Button loginButton;
    private EditText loginEmail;
    private EditText loginPassword;

    private final static String PREFNAME = "my_data";

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginButton = (Button) findViewById(R.id.login_button);
        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);

        restoringPreferences();

        loadingBar = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                loginUserAccount(email, password);
            }
        });
    }

    private void loginUserAccount(final String email, final String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please write your email ???", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please write your password ??", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait . while we are verifying your credertials .");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String online_user_id = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                userRef.child(online_user_id).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                savingAccount(email, password);
                                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        });


                            } else {

                                Toast.makeText(getApplicationContext(), "Wrong email and password . Please check your vaild email or password ??",
                                        Toast.LENGTH_SHORT).show();
                            }

                            loadingBar.dismiss();
                        }
                    });
        }

    }

    public void restoringPreferences() {
        SharedPreferences pre = getSharedPreferences(PREFNAME, MODE_PRIVATE);
        String username = pre.getString("username", "");
        String password = pre.getString("password", "");

        loginEmail.setText(username);
        loginPassword.setText(password);
    }

    private void savingAccount(String username, String password) {
        SharedPreferences pre = getSharedPreferences(PREFNAME, MODE_PRIVATE);

        SharedPreferences.Editor editor = pre.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

    }
}
