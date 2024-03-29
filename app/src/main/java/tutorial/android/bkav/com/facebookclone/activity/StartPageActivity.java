package tutorial.android.bkav.com.facebookclone.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import tutorial.android.bkav.com.facebookclone.R;

public class StartPageActivity extends AppCompatActivity {

    private Button alreadyHaveAccountButton;
    private Button needNewAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        mapping();
    }

    private void mapping() {
        alreadyHaveAccountButton = (Button) findViewById(R.id.already_have_an_account_button);
        needNewAccountButton = (Button) findViewById(R.id.need_account_button);


        needNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });


        alreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
