package tutorial.android.bkav.com.facebookclone;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Ref;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView postList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;

    private CircleImageView navProfileImage;
    private TextView nameProfileUserImage;
    private ImageButton addNewPostButton;

    private String current_user_id = "";


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mapping();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View nav_view = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) nav_view.findViewById(R.id.nav_profile_image);
        nameProfileUserImage = (TextView) nav_view.findViewById(R.id.nav_user_full_name);

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String image = dataSnapshot.child("user_image").getValue().toString();
                    if (image.equals("Default Profile")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                            navProfileImage.setImageDrawable(
                                    getResources().getDrawable(
                                            R.drawable.profile,
                                            getApplicationContext().getTheme()));
                        } else {
                            navProfileImage.setImageDrawable(
                                    getResources().getDrawable(
                                            R.drawable.profile));
                        }
                    } else {
                        Picasso.with(MainHomeActivity.this).load(image).placeholder(R.drawable.profile_default).into(navProfileImage);
                    }

                    String name = dataSnapshot.child("user_name").getValue().toString();
                    nameProfileUserImage.setText(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                userMenuSelector(item);

                return false;
            }
        });


        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPostActivity();
            }
        });


        displayAllUsersPost();
    }

    private void displayAllUsersPost() {
        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostViewHolder>(
                        Posts.class,
                        R.layout.all_post_layout,
                        PostViewHolder.class,
                        PostRef
                ) {

                    @Override
                    protected void populateViewHolder(PostViewHolder viewHolder, Posts model, int position) {

                        final String postKey = getRef(position).getKey();

                        viewHolder.setUser_name(model.getUser_name());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setUser_image(getApplicationContext(), model.getUser_image());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainHomeActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", postKey);
                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    private static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUser_name(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_profile_name);
            username.setText(fullname);
        }

        public void setUser_image(Context context, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.postProfileImage);
            if (profileimage.equals("Default Profile")) {
                image.setImageResource(R.drawable.profile_default);

            } else {
                Picasso.with(context).load(profileimage).placeholder(R.drawable.profile)
                        .into(image);
            }


        }

        public void setTime(String time) {
            TextView post_time = (TextView) mView.findViewById(R.id.post_time);
            post_time.setText(time + "");
        }

        public void setDate(String date) {
            TextView postdate = (TextView) mView.findViewById(R.id.post_date);
            postdate.setText(date + "");
        }

        public void setDescription(String description) {
            TextView postDescription = (TextView) mView.findViewById(R.id.post_décription);
            postDescription.setText(description);
        }

        public void setPostimage(Context context, String postimage) {
            ImageView postImage = (ImageView) mView.findViewById(R.id.click_post_image);
            Picasso.with(context).load(postimage).placeholder(R.drawable.add_post)
                    .into(postImage);
        }

    }

    private void sendUserToPostActivity() {
        Intent intent = new Intent(MainHomeActivity.this, PostActivity.class);
        startActivity(intent);
    }


    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainHomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void userMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post: {
                sendUserToPostActivity();
                break;
            }
            case R.id.nav_profile: {
                Intent intent = new Intent(MainHomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_home: {
                Intent intent = new Intent(MainHomeActivity.this, MainHomeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_friends: {

                Intent intent = new Intent(MainHomeActivity.this, MainActivity.class);
                intent.putExtra("flag", 3);
                startActivity(intent);

                break;
            }
            case R.id.nav_find_frieds: {
                Intent intent = new Intent(MainHomeActivity.this, AllUsersActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_message: {
                Intent intent = new Intent(MainHomeActivity.this, MainActivity.class);
                intent.putExtra("flag", 2);
                startActivity(intent);
                break;
            }

            case R.id.nav_setting: {
                Intent intent = new Intent(MainHomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_logout: {
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mapping() {

        mToolbar = (Toolbar) findViewById(R.id.main_page_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        addNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainHomeActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);


    }
}
