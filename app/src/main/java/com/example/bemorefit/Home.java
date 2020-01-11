package com.example.bemorefit;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class Home extends AppCompatActivity{

    //Has the Users Information

    private GoogleSignInAccount account;
    private ViewPager Pager = null;
    private LinearLayout status;
    private SlideAdapter myadapter;
    //private LinearLayout linear;
    private Button btnAccept, btnCancel;
    //private TextView pageNumber, test;
    private GoogleSignInClient googleSignInClient;
    private GoogleApiClient googleApiClient;
    private BottomNavigationView bottomNavigationView;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         dialog = new Dialog(Home.this);

        setContentView(R.layout.activity_home);
        Pager = (ViewPager) findViewById(R.id.viewPager); //Creates Fragment

        //linear = (LinearLayout) findViewById(R.id.navigation); //Shows User what page they are on
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.NavigationBar);
        Intent intent = getIntent();
        account = intent.getParcelableExtra("GoogleSignInAccount");


        myadapter = new SlideAdapter(this, account);


        Pager.setAdapter(myadapter);

        Pager.setCurrentItem(3);
        bottomNavigationView.setSelectedItemId(R.id.goalDisplay);



        Pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0){
                    bottomNavigationView.setSelectedItemId(R.id.stepsDisplay);
                }else if (i == 1){
                    bottomNavigationView.setSelectedItemId(R.id.weightDisplay);
                }else if (i == 2){
                    bottomNavigationView.setSelectedItemId(R.id.cameraDisplay);
                }else if (i == 3){
                    bottomNavigationView.setSelectedItemId(R.id.goalDisplay);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener (new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.stepsDisplay:
                        Pager.setCurrentItem(0);
                        break;
                    case R.id.weightDisplay:
                        Pager.setCurrentItem(1);
                        break;
                    case R.id.cameraDisplay:
                        Pager.setCurrentItem(2);
                        break;
                    case R.id.goalDisplay:
                        Pager.setCurrentItem(3);
                        break;

                }
                return true;
            }
        });


        myadapter.setCustomObjectListener(new SlideAdapter.MyCustomObjectListener() {  //-------------Custom Listener created URL https://guides.codepath.com/android/Creating-Custom-Listeners#3-implement-listener-callback
            @Override
            public void onObjectReady(String title) {
                status = findViewById(R.id.LinearLayoutDialog); //Prepares the dialog and prevent redundant code
                dialog.setContentView(R.layout.activity_popupdialog);//popup view is the layout you created
                btnAccept = (Button) dialog.findViewById(R.id.BtnAccept);
                btnCancel = (Button) dialog.findViewById(R.id.BtnCancel);
                TextView txtDisplaytoUser = (TextView) dialog.findViewById(R.id.TxtDisplayToUser);

                if (title == "btn_logout") {
                    txtDisplaytoUser.setText("Are you sure you want to log out?");
                    btnAccept.setText("LogOut");
                    dialog.show();

                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GoogleSignInOptions Options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                            googleSignInClient = GoogleSignIn.getClient(Home.this, Options);
                            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    finish();

                                    Intent HomePage = new Intent(Home.this,LoginPage.class);
                                    finish();
                                    startActivity(HomePage);

                                }
                            });

                        }
                    });

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                }else if(title == "btnDeleteAcount"){
                    btnAccept.setText("Bye");
                    txtDisplaytoUser.setText("Are you sure you want to Leave Forever?");
                    dialog.show();


                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                    public void onClick(View v) {
                        GoogleSignInOptions Options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                        googleSignInClient = GoogleSignIn.getClient(Home.this, Options);
                        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();

                                Intent HomePage = new Intent(Home.this,LoginPage.class);
                                SQLManager sql = new SQLManager(Home.this);
                                sql.getUserID(account.getEmail());
                                sql.deleteUser();
                                finish();
                                startActivity(HomePage);

                            }
                        });

                    }
                    });

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });


                }
            }
        });
    }







    //Onclick must call this method and reopen LoginPage to successfully be logged out




    @Override
    protected void onResume() {
        super.onResume();

    }


}
