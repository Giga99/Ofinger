package com.example.ofinger.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.google.android.material.textview.MaterialTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class HelpActivity extends AppCompatActivity {
    Button btnFeedback, btnSupport;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pomoc");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        btnFeedback = findViewById(R.id.btnFeedback);
        btnSupport = findViewById(R.id.btnSupport);

        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(HelpActivity.this).inflate(R.layout.dialog_support_feedback, null);
                MaterialEditText etText = view.findViewById(R.id.etText);
                Button btnSubmit = view.findViewById(R.id.btnSubmit);

                final String text = etText.getText().toString();
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.setMessage("Slanje utiska...");
                        progressDialog.show();

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ofingerdeveloperteam@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                        i.putExtra(Intent.EXTRA_TEXT   , text);
                        try {
                            startActivity(Intent.createChooser(i, "Slanje mejla:"));
                            progressDialog.dismiss();
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(HelpActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(HelpActivity.this).inflate(R.layout.dialog_support_feedback, null);
                MaterialTextView header = view.findViewById(R.id.header);
                MaterialEditText etText = view.findViewById(R.id.etText);
                Button btnSubmit = view.findViewById(R.id.btnSubmit);

                header.setText("Kako mozemo da pomognemo?");

                final String text = etText.getText().toString();
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.setMessage("Slanje pitanja...");
                        progressDialog.show();

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ofingerdeveloperteam@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Support");
                        i.putExtra(Intent.EXTRA_TEXT   , text);
                        try {
                            startActivity(Intent.createChooser(i, "Slanje mejla:"));
                            progressDialog.dismiss();
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(HelpActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkTypingStatus (String typing){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        status("online");
        checkTypingStatus("noOne");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }
}
