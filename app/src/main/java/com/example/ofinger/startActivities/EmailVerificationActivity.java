package com.example.ofinger.startActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ofinger.R;

public class EmailVerificationActivity extends AppCompatActivity {
    ImageView ivGoToLogin;
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verfication);

        ivGoToLogin = findViewById(R.id.ivGoToLogin);
        tvInfo = findViewById(R.id.tvInfo);

        String id = getIntent().getStringExtra("id");

        if(id.equals("register")){
            tvInfo.setText("Uspesno ste se registrovali, molim vas proverite mejl sa daljim instrukcijama");
        } else if (id.equals("update")){
            tvInfo.setText("Uspesno ste se promenili mejl, molim vas proverite mejl sa daljim instrukcijama");
        }

        ivGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
