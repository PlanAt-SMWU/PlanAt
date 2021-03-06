package com.example.planat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.Utility;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private ISessionCallback mSessionCallback;
    //define view objects
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonSignin, share_button;
    TextView textviewSingin;
    TextView textviewMessage;
    TextView textviewFindPassword;
    ProgressDialog progressDialog;
    //define firebase object
    FirebaseAuth firebaseAuth;
    DocumentReference docs;
    String userName; //?????? ????????? SignupActivity????????? ????????????

    //define firebase cloud store object
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSessionCallback = new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                //???????????????
                UserManagement.getInstance().me(new MeV2ResponseCallback() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        //????????? ?????????
                        Toast.makeText(LoginActivity.this, "????????? ????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        //????????? ??????
                        Toast.makeText(LoginActivity.this, "????????? ???????????????. ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        //????????? ??????
                        Intent intent = new Intent(LoginActivity.this, KakoResultActivity.class);
                        intent.putExtra("name", result.getKakaoAccount().getProfile().getNickname());
                        intent.putExtra("profileImg", result.getKakaoAccount().getProfile().getProfileImageUrl());
                        intent.putExtra("email", result.getKakaoAccount().getEmail());

                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "?????? ?????????!", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                Toast.makeText(LoginActivity.this, "onSessionOpenFailed", Toast.LENGTH_SHORT).show();
            }

        };
        Session.getCurrentSession().addCallback(mSessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        //initializig firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

//        if (firebaseAuth.getCurrentUser() != null) {
//            Log.d("?????? ?????????",firebaseAuth.getCurrentUser().toString());
//            //?????? ????????? ???????????? ??? ??????????????? ?????????
//            finish();
//            //????????? profile ??????????????? ??????.
//            startActivity(new Intent(LoginActivity.this, ProfileActivity.class)); //????????? ??? ProfileActivity
//        }

        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textviewSingin = (TextView) findViewById(R.id.textViewSignin);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        textviewFindPassword = (TextView) findViewById(R.id.textViewFindpassword);
        buttonSignin = (Button) findViewById(R.id.buttonSignup);
        progressDialog = new ProgressDialog(this);
        //final String phoneNumber = ((EditText) findViewById(R.id.Imageiv_profile)).getText().toString();
        //button click event
        buttonSignin.setOnClickListener(this);
        textviewSingin.setOnClickListener(this);
        textviewFindPassword.setOnClickListener(this);
    }
    //firebase userLogin method
    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "??????????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("?????????????????????. ?????? ????????? ?????????...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "\"????????? ?????? ??????\\n - password??? ?????? ????????????.\\n -????????????\"", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //?????? ????????? ??????????????? ????????? ?????? ????????????
        docs = db.collection("users").document(email);
        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Object docData = document.getData().get("nickname");
                    userName = docData.toString();
                    Log.d("?????????????????????????????????",userName);
                }else{
                    Log.d("??????","firebase ?????? ??????");
                }
            }
        });
        Intent intent2 = new Intent(getApplicationContext(),ScheduleActivity.class);
        intent2.putExtra("userName",userName);
        intent2.putExtra("userEmail",email);
        startActivity(intent2);
    }
    @Override
    public void onClick(View view){
        if(view == buttonSignin)
            userLogin();
        if(view == textviewSingin) {
            startActivity(new Intent(this, SignupActivity.class));

        }
        if(view == textviewFindPassword) {
            startActivity(new Intent(this, FindActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(mSessionCallback);
    }
}
