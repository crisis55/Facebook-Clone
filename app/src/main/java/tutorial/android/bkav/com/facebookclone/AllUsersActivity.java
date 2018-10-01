package tutorial.android.bkav.com.facebookclone;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    // Khai báo biến

    private Toolbar mToolbar;
    private RecyclerView allUserList;
    private EditText searchInputText;
    private ImageButton search_button;

    // Tham chiếu đến cơ sở dữ liệu
    private DatabaseReference allUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Tham chiểu đến bảng Users trong csdl
        allUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allUserRef.keepSynced(true);

        // Ánh xạ
        mToolbar = (Toolbar) findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUserList = (RecyclerView) findViewById(R.id.all_user_list);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager(new LinearLayoutManager(this));

        searchInputText = (EditText) findViewById(R.id.search_input_text);
        search_button = (ImageButton) findViewById(R.id.search_button);

        // Bắt sự kiện khi nhấn vào nút tìm kiếm
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchUserName = searchInputText.getText().toString();
                if (TextUtils.isEmpty(searchUserName)) {
                    Toast.makeText(getApplicationContext(), "Please write a user name search .", Toast.LENGTH_SHORT).show();

                }
                searchForPeopleAndFriend(searchUserName);
            }
        });

    }

    // Hiển thị danh sách tất cả các user lên
    @Override
    protected void onStart() {
        super.onStart();
        searchForPeopleAndFriend("");
    }

    // Tìm kiếm tất cả user theo yêu cầu
    private void searchForPeopleAndFriend(String searchUserName) {

        // Câu lệnh tìm kiếm
        Query serachPeopleAndFriends = allUserRef.orderByChild("user_name")
                .startAt(searchUserName).endAt(searchUserName + "\uf8ff");

        // Adapter của RecyclerView

        FirebaseRecyclerAdapter<AllUser, AllUserViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<AllUser, AllUserViewHolder>(
                        // Truyền vào 4 tham số
                        AllUser.class,
                        R.layout.all_user_display_layout,
                        AllUserViewHolder.class,
                        serachPeopleAndFriends

                ) {
                    @Override
                    protected void populateViewHolder(AllUserViewHolder viewHolder, AllUser model, final int position) {
                        viewHolder.setUser_name(model.getUser_name());
                        viewHolder.setUser_status(model.getUser_status());
                        viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());

                        // Bắt dự kiện khi nhấn vào 1 item trên recyclerview
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();
                                Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }
                };

        // Set adapter cho recyclerview
        allUserList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class AllUserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public AllUserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUser_name(String user_name) {
            TextView name = (TextView) mView.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }

        public void setUser_status(String user_status) {
            TextView status = (TextView) mView.findViewById(R.id.all_user_status);
            status.setText(user_status);
        }

        public void setUser_thumb_image(final Context context, final String user_thumb_image) {

            final CircleImageView thum_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            Picasso.with(context).load(user_thumb_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile_default)
                    .into(thum_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(context).load(user_thumb_image).placeholder(R.drawable.profile_default).into(thum_image);
                        }
                    });
        }
    }
}






