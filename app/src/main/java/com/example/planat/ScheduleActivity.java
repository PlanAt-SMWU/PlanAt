package com.example.planat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.google.firebase.storage.FirebaseStorage;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialCalendarView materialcalendarView;
    private Button add_button;
    private TextView tv_title_info,tv_time_info,tv_location_info;
    private EditText et_title,et_time,et_location,et_title_inEtDlg,et_time_inEtDlg,et_location_inEtDlg;
    private Dialog dialog; //?????? ?????? ???????????????
    private Button cancel_button,done_button,done_button_inEtDlg,cancel_button_inEtDlg;

    private ImageButton info_button,map_button,location_button_inEtDlg,edit_button_info,close_button_info,delete_button_info;
    private ImageView iv_share, iv_photo; //????????? ????????? ????????????
    private TextView tv_name;//????????? ????????? ????????????
    UserModel userModel;


    private FirebaseFirestore db;

    private DocumentReference docs;
    private Map<String, Object> contents = new HashMap<>(); //??????,??????,?????? ???????????? Map
    private Map<String,Object> contentsTitle = new HashMap<>(); //????????? ????????? ???????????? contents??? ????????? Map

    private CalendarDay m_cDay;

    //???????????? ????????? ????????? ??? result ???????????? ?????? ?????? ??????
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    et_location.setText(intent.getStringExtra("location"));
                }
            }
    );

    //???????????? ????????? ????????? ??? result ???????????? ?????? ?????? ?????? => in ?????? ???????????????
    ActivityResultLauncher<Intent> mStartForResultInEditDialog = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    et_location_inEtDlg.setText(intent.getStringExtra("location"));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("userEmail");
        docs = db.collection("users").document(userEmail);

        materialcalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);//??????

        //?????? ???,???,?????? m_cDay ????????? ??? ?????? ?????? ?????? decorate
        this.handleDecorate(CalendarDay.today());

        add_button = findViewById(R.id.add_button); //?????? ????????????

        iv_share = findViewById(R.id.share_lit);
        iv_share.setOnClickListener(this);

        info_button = findViewById(R.id.info_button); //?????? ???????????? ?????? ??????


        //????????? ??????????????? ??????
        dialog = new Dialog(ScheduleActivity.this);//???????????? ?????????????????????

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_schedule);

        //?????? ??????????????? ?????? ?????? ?????????
        cancel_button = dialog.findViewById(R.id.cancel_button);
        done_button = dialog.findViewById(R.id.done_button);
        map_button = dialog.findViewById(R.id.map_button);
        et_title = dialog.findViewById(R.id.et_title);
        et_time = dialog.findViewById(R.id.et_time);
        et_location = dialog.findViewById(R.id.et_location);

        iv_photo = findViewById(R.id.iv_photo);
        getUserInfoFromServer(); //????????? ????????? ?????? DB?????? ????????????
        tv_name = findViewById(R.id.tv_name);


        iv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ScheduleActivity.this,MyPageActivity.class));
            }
        });

        materialcalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay cDay, boolean selected) {
                m_cDay = cDay;//??????????????? ????????? cDay??? ??????

                info_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //?????? ???????????????
                        final Dialog info_dialog = new Dialog(ScheduleActivity.this);
                        info_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        info_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        info_dialog.setContentView(R.layout.dialog_schedule_info);

                        //??? ?????? ??????
                        tv_title_info = info_dialog.findViewById(R.id.tv_title);
                        tv_time_info = info_dialog.findViewById(R.id.tv_time);
                        tv_location_info = info_dialog.findViewById(R.id.tv_location);
                        edit_button_info = info_dialog.findViewById(R.id.edit_button);
                        close_button_info = info_dialog.findViewById(R.id.close_button);
                        delete_button_info = info_dialog.findViewById(R.id.delete_button);

                        //?????? ?????? ????????? DB?????? ????????????
                        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    //?????? ????????? ?????? ????????? ???????????? ??????
                                    String key = cDay.getYear() + "-" + (cDay.getMonth() + 1) + "-" + cDay.getDay();
                                    Object docData = document.getData().get(key);

                                    if(docData == null){
                                        Toast.makeText(ScheduleActivity.this, "?????? ????????? ????????? ????????? ????????????!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    //????????????????????? ????????? ????????? ????????? ?????? ?????????????????? ?????????.
                                    info_dialog.show();

                                    String data = docData.toString();
                                    
                                    String[] dateInfoArray = data.split(",");

                                    String str_location = dateInfoArray[0].substring(10);
                                    String str_time = dateInfoArray[1].substring(6);
                                    String str_title = dateInfoArray[2].substring(7);

                                    //?????? ??? ?????? ???????????? setText ???????????????
                                    tv_location_info.setText(str_location);
                                    tv_time_info.setText(str_time);
                                    tv_title_info.setText(str_title);
                                }
                            }
                        });
                        //Map ?????????
                        contents.clear();
                        contentsTitle.clear();

                        //?????? ?????? - ?????? ??????????????? ??????
                        close_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                info_dialog.dismiss();
                            }
                        });

                        //?????? ??????(?????? ?????????) ????????? ?????? ??????????????? ?????????
                        edit_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                info_dialog.dismiss(); //?????? ??????????????? ??????
                                Dialog editDialog = new Dialog(ScheduleActivity.this);

                                editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                editDialog.setContentView(R.layout.dialog_schedule);

                                editDialog.show();

                                done_button_inEtDlg = editDialog.findViewById(R.id.done_button);
                                cancel_button_inEtDlg = editDialog.findViewById(R.id.cancel_button);
                                location_button_inEtDlg = editDialog.findViewById(R.id.map_button);
                                et_title_inEtDlg = editDialog.findViewById(R.id.et_title);
                                et_time_inEtDlg = editDialog.findViewById(R.id.et_time);
                                et_location_inEtDlg = editDialog.findViewById(R.id.et_location);

                                //?????? db??? ????????? ????????? ????????? ??????
                                et_title_inEtDlg.setText(tv_title_info.getText());
                                et_time_inEtDlg.setText(tv_time_info.getText());
                                et_location_inEtDlg.setText(tv_location_info.getText());

                                //?????? ?????? ????????? ????????? ???
                                done_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+m_cDay.getDay();
                                        contents.put("day",key);
                                        contents.put("title",et_title_inEtDlg.getText().toString());
                                        contents.put("time",et_time_inEtDlg.getText().toString());
                                        contents.put("location",et_location_inEtDlg.getText().toString());

                                        contentsTitle.put(key,contents);
                                        docs.update(contentsTitle);

                                        //????????? ???????????? ????????? ??????
                                        tv_title_info.setText(et_title_inEtDlg.getText());
                                        tv_time_info.setText(et_time_inEtDlg.getText());
                                        tv_location_info.setText(et_location_inEtDlg.getText());

                                        //?????????
                                        contents.clear();
                                        contentsTitle.clear();
                                        editDialog.dismiss();
                                        info_dialog.show();
                                    }
                                });
                                cancel_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        editDialog.dismiss();
                                    }
                                });
                                //?????? ?????? ?????? ?????? ??? ?????? => ?????? ??????...
                                location_button_inEtDlg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ScheduleActivity.this,MiddlePlaceActivity.class);
                                        //intent.putExtra("userEmail",userEmail);
                                        mStartForResultInEditDialog.launch(intent);
                                    }
                                });
                            }
                        });

                        //?????? ?????? - ?????? ?????? ?????? ?????? ?????? ??????????????? ????????? ???/???????????? ??????
                        delete_button_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Dialog delete_dialog = new Dialog(ScheduleActivity.this);
                                delete_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                delete_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                                delete_dialog.setContentView(R.layout.dialog_check_delete);
                                delete_dialog.show();

                                Button noBtn = delete_dialog.findViewById(R.id.noBtn);
                                Button yesBtn = delete_dialog.findViewById(R.id.yesBtn);

                                //?????? ??????
                                noBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        delete_dialog.dismiss();
                                    }
                                });
                                //?????? ????????? ??????
                                yesBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //?????? ?????????
                                        Map<String,Object> dataFordelete = new HashMap<>();

                                        String key = cDay.getYear()+"-"+(cDay.getMonth()+1)+"-"+cDay.getDay();
                                        dataFordelete.put(key, FieldValue.delete());
                                        docs.update(dataFordelete);

                                        //?????? ??????????????? ??? ??????
                                        delete_dialog.dismiss();
                                        dialog.dismiss();

                                        //decorate ??????
                                        Intent intent1 = getIntent();
                                        finish();
                                        startActivity(intent1);
                                    }
                                });
                            }
                        });
                    }
                });
                add_button.setOnClickListener(ScheduleActivity.this);
                done_button.setOnClickListener(ScheduleActivity.this);
                cancel_button.setOnClickListener(ScheduleActivity.this);
                map_button.setOnClickListener(ScheduleActivity.this);
            }
        });
        //?????????????????? month??? ?????? ????????? db?????? ?????? ???????????? ??????
        materialcalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                handleDecorate(date);
            }
        });
    }
    public void handleDecorate(CalendarDay cDay){
        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                contents.clear();
                contentsTitle.clear();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.getData() != null && !document.getData().isEmpty()) {// ???????????? ?????? ?????? ?????? ???????????? ?????? ??????
                        /*???????????? ???????????? contentsTitle??? ????????????
                        ???,???,?????? ????????? ?????? ?????? CalendarDay??? ????????? decorate?????????.*/
                        Calendar calendar = Calendar.getInstance();
                        m_cDay = cDay; //?????????
                        calendar.set(m_cDay.getYear(),m_cDay.getMonth(),1);
                        //???????????? ??? ?????? ?????????.
                        int idx =calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                        for(int i=1;i<=idx;i++){
                            String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+i;
                            //1????????? ???????????? ????????? ????????? DB??? ?????? ?????? ????????? ????????? ??????
                            if(document.getData().get(key) != null){
                                String[] dateInfoArray = key.split("-");//-???????????? ????????? ?????????
                                CalendarDay cDay = CalendarDay.from(Integer.parseInt(dateInfoArray[0]),Integer.parseInt(dateInfoArray[1])-1,Integer.parseInt(dateInfoArray[2]));
                                //DB??? ????????? ?????? day ????????? ????????? ?????? ????????????.

                                materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(cDay)));
                            }
                        }
                    } else {
                        //date field??? ?????? ?????? ?????? ????????????.
                        contentsTitle = new HashMap<>();
                        docs.set(contentsTitle);
                        //?????????
                        contents.clear();
                        contentsTitle.clear();
                    }
                }
            }
        });
    }

    //?????? ??????????????? ???????????? ??????????????? ?????? ??????
    void getUserInfoFromServer() {
        docs.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Object userName = document.getData().get("nickname");
                    //????????? ????????? ??????
                    if(userName != null){
                        tv_name.setText(userName.toString());
                    }
                }
            }
        });
        try{
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d("FireSever", "onEvent");
                    userModel = documentSnapshot.toObject(UserModel.class);
                    if (userModel.getUserPhoto() != null && !userModel.getUserPhoto().equals("null") && !"".equals(userModel.getUserPhoto())) {
                        FirebaseStorage.getInstance().getReference("userPhoto/" + userModel.getUserPhoto()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Glide.with(ScheduleActivity.this)
                                        .load(task.getResult())
                                        .circleCrop()
                                        .into(iv_photo);
                            }
                        });

                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view){
        if(view == add_button){
            dialog.show();
        }
        if(view == map_button){ //???????????? ???????????? ??????
            Intent intent = new Intent(this,MiddlePlaceActivity.class);
            mStartForResult.launch(intent);
        }

        if(view == iv_share){
            FeedTemplate params = FeedTemplate
                    .newBuilder(ContentObject.newBuilder("PlanAt-???????????? ??????????????????",
                            "https://image.genie.co.kr/Y/IMAGE/IMG_ALBUM/081/191/791/81191791_1555664874860_1_600x600.JPG",
                            LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
                                    .setMobileWebUrl("https://developers.kakao.com").build())
                            .setDescrption("?????? 7??? ????????? ?????????????????? ??????!")
                            .build())
                    //.addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder().setWebUrl("https://developers.kakao.com").setMobileWebUrl("https://developers.kakao.com").build()))
                    .addButton(new ButtonObject("????????? ??????", LinkObject.newBuilder()
                            .setWebUrl("https://developers.kakao.com")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .build();

            Map<String, String> serverCallbackArgs = new HashMap<String, String>();
            serverCallbackArgs.put("user_id", "${current_user_id}");
            serverCallbackArgs.put("product_id", "${shared_product_id}");


            KakaoLinkService.getInstance().sendDefault(this, params, new ResponseCallback<KakaoLinkResponse>() {
                @Override
                public void onFailure(ErrorResult errorResult) {}

                @Override
                public void onSuccess(KakaoLinkResponse result) {
                }
            });
        }

        if(view == done_button){
            //??????????????? ????????? ?????? String?????? ??????
            String key = m_cDay.getYear()+"-"+(m_cDay.getMonth()+1)+"-"+m_cDay.getDay();
            contents.put("day",key);
            contents.put("title",et_title.getText().toString());
            contents.put("time",et_time.getText().toString());
            contents.put("location",et_location.getText().toString());

            contentsTitle.put(key,contents);
            docs.update(contentsTitle);

            //?????????
            contents.clear();
            contentsTitle.clear();
            et_time.setText(null);
            et_title.setText(null);
            et_location.setText(null);

            materialcalendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(m_cDay)));

            dialog.dismiss();
        }
        else if(view == cancel_button){
            dialog.dismiss();
        }
    }
}
