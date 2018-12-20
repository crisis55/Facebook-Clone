package tutorial.android.bkav.com.facebookclone.activity;


import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import tutorial.android.bkav.com.facebookclone.R;
import tutorial.android.bkav.com.facebookclone.adapter.TabsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser curentUser;
    private DatabaseReference userRef;

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    public static TabLayout myTabLayout;

    private TabsPagerAdapter myTabsPageAdapter;

    private int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        curentUser = mAuth.getCurrentUser();
        if (curentUser != null) {

            String online_user_id = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(online_user_id);
        }


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        mToolbar.setTitle("Messages");
        setSupportActionBar(mToolbar);

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        myTabsPageAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        try {
            flag = (int) getIntent().getExtras().getInt("flag", 0);
        } catch (Exception ex) {

        }

        myViewPager.setAdapter(myTabsPageAdapter);
        if (flag == 2) {
            myViewPager.setCurrentItem(1);
        } else if (flag == 3) {
            myViewPager.setCurrentItem(2);
        } else {
            myViewPager.setCurrentItem(0);
        }

        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        curentUser = mAuth.getCurrentUser();

        if (curentUser == null) {
            logOutUser();
        } else if (curentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (curentUser != null) {
            userRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void logOutUser() {
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        fileList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_button) {

            if (curentUser != null) {
                userRef.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            logOutUser();
        }

        if (item.getItemId() == (R.id.main_account_setting_button)) {
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }

        if (item.getItemId() == (R.id.main_all_user__button)) {
            Intent allUserIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUserIntent);
        }

        if (item.getItemId() == (R.id.main_home)) {
            Intent homeIntent = new Intent(MainActivity.this, MainHomeActivity.class);
            startActivity(homeIntent);
        }
        return true;
    }
}
