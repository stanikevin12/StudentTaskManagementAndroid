package com.example.studenttaskmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.auth.SessionManager;
import com.example.studenttaskmanagement.database.dao.UserDao;
import com.example.studenttaskmanagement.model.User;
import com.example.studenttaskmanagement.utils.PasswordUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textGoRegister;

    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);

        bindViews();
        setupActions();
    }

    private void bindViews() {
        editTextEmail = findViewById(R.id.editTextLoginEmail);
        editTextPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textGoRegister = findViewById(R.id.textGoRegister);
    }

    private void setupActions() {
        buttonLogin.setOnClickListener(v -> login());
        textGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void login() {
        String email = textOf(editTextEmail);
        String password = textOf(editTextPassword);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        String inputHash = PasswordUtils.sha256(password);
        if (!inputHash.equals(user.getPasswordHash())) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveLoggedInUserId(user.getId());
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
