package com.example.planat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    //define view objects
    ImageView back;
    EditText editTextEmail;
    EditText editTextPassword;
    EditText join_name;
    Button buttonSignup;
    Button buttonDelete;
    TextView textviewSingin;
    TextView textviewMessage;
    ProgressDialog progressDialog;
    //define firebase object
    FirebaseAuth firebaseAuth;

    //define firebase cloud store object
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();
    String nickname,email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //initializing views
        back = findViewById(R.id.back_button);
        editTextEmail = (EditText) findViewById(R.id.join_email);
        editTextPassword = (EditText) findViewById(R.id.join_password);
        join_name = findViewById(R.id.join_name);
        textviewSingin = (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        buttonSignup = (Button) findViewById(R.id.join_button);
        buttonDelete = (Button) findViewById(R.id.cancel_button);
        progressDialog = new ProgressDialog(this);

        //button click event
        buttonSignup.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        textviewSingin.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    //Firebse creating a new user
    private void registerUser() {
        //???????????? ???????????? email, password??? ????????????.
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        nickname = join_name.getText().toString().trim();


        //email??? password??? ???????????? ???????????? ?????? ??????.
        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
        }

        //email??? password??? ????????? ???????????? ????????? ?????? ????????????.
        progressDialog.setMessage("??????????????????. ????????? ?????????...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finish();
                            user.put("nickname",nickname);
                            db.collection("users").document(email)
                                    .set(user, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), email+"??? ?????????????????????.", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("docs error", "Error adding document", e);
                                        }
                                    });
                            Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignupActivity.this, "?????????????????????! ????????? ?????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
                        } else {
                            //???????????????
                            textviewMessage.setText("????????????\n - ?????? ????????? ?????????  \n -?????? ?????? 6?????? ?????? \n - ????????????");
                            Toast.makeText(SignupActivity.this, "?????? ??????!", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    //button click event
    @Override
    public void onClick(View view) {
        if (view == buttonSignup) {
            //TODO
            registerUser();
        }

        if (view == textviewSingin) {
            //TODO
            startActivity(new Intent(this, LoginActivity.class)); //????????? ??? ????????? ????????????
        }

        if (view == buttonDelete) {
            onBackPressed();
        }

        if (view == back) {
            finish();
        }
    }


}
