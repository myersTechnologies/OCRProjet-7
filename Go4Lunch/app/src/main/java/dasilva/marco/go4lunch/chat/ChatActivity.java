package dasilva.marco.go4lunch.chat;

import android.graphics.drawable.GradientDrawable;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class ChatActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;
    private RecyclerView chatRecyclerView;
    private List<ChatMessage>  chatMessages;
    private Go4LunchService service = DI.getService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar chatToolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(chatToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.chat_toolbar_title));

        chatRecyclerView = findViewById(R.id.list_of_messages);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(mLayoutManager);

        displayChatMessages();

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText message = (EditText)findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference("chat")
                        .push()
                        .setValue(new ChatMessage(message.getText().toString(),
                                service.getUser().getId())
                        );

                // Clear the input
                message.setText("");
            }
        });


    }


    private void displayChatMessages() {
        chatMessages = new ArrayList<>();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("chat");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatMessages.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    ChatMessage message = childSnapshot.getValue(ChatMessage.class);
                    chatMessages.add(message);
                }
                chatAdapter = new ChatAdapter(chatMessages);
                chatRecyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
