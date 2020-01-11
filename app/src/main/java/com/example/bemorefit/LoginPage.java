package com.example.bemorefit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Calendar;

public class LoginPage extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private LinearLayout status;
    private Button btn_logout, btnCreateUser, btnGoBack,btnLetsGoBack;
    private Switch switchImperial;
    private SignInButton sign_in_button;
    private TextView name, email, txtSelectDate, textViewWeight, textViewHeight;
    private EditText editTextWeight, editTextHeight, txtName;
    private ImageView loginCharicter, logo;
    private ImageButton btnGender;
    private GoogleApiClient googleApiClient;
    private GoogleSignInClient googleSignInClient;
    private LinearLayout linearLayoutCreateUser, linearLayoutLogin;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private static final int REQ_CODE = 9001;
    private boolean isMale = true;
    private boolean isImperial = false;
    private String Birthdate;
    private  SQLManager sql;
   private GoogleSignInAccount account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        status = (LinearLayout) findViewById(R.id.Status);
        btn_logout = (Button) findViewById(R.id.Btn_logout);
        btnGender = (ImageButton) findViewById(R.id.BtnGender);
        btnCreateUser = (Button) findViewById(R.id.BtnCreateUser);
        btnGoBack = (Button) findViewById(R.id.BtnGoBack);
        btnLetsGoBack = (Button) findViewById(R.id.BtnLetsGoBack);
        switchImperial = (Switch)findViewById(R.id.SwitchImperial);
        sign_in_button = (SignInButton) findViewById(R.id.Sign_in_button);

        name = (TextView) findViewById(R.id.Name);
        email = (TextView) findViewById(R.id.Email);
        txtSelectDate = (TextView) findViewById(R.id.TxtSelectDate);
        textViewWeight = (TextView) findViewById(R.id.TextViewWeight);
        textViewHeight = (TextView) findViewById(R.id.TextViewHeight);
        loginCharicter = (ImageView) findViewById(R.id.LoginCharicter);
        logo = (ImageView) findViewById(R.id.Logo);
        linearLayoutCreateUser = (LinearLayout) findViewById(R.id.LinearLayoutCreateUser);
        linearLayoutLogin = (LinearLayout) findViewById(R.id.LinearLayoutLogin);
        sql = new SQLManager(this);
        //Application is waiting for these buttons to be pressed
        switchImperial.setOnClickListener(this);
        btnGender.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
        btnCreateUser.setOnClickListener(this);
        sign_in_button.setOnClickListener(this);
        btnGoBack.setOnClickListener(this);
        btnLetsGoBack.setOnClickListener(this);
        status.setVisibility(View.GONE); //Hides the user status
        GoogleSignInOptions Options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, Options);

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, Options).build();
        //Tries to log the user in if they have previously used the application

        editTextWeight = (EditText)findViewById(R.id.EditTextWeight);
        editTextHeight = (EditText)findViewById(R.id.EditTextHeight);
        txtName = (EditText)findViewById(R.id.TxtName);

        checkLastLoginGoogle(GoogleSignIn.getLastSignedInAccount(this));

        //                                                                  --------------------Change some code here https://www.youtube.com/watch?v=hwe1abDO2Ag
        txtSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        LoginPage.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        onDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Birthdate = day+"-"+month+"-"+year;
                txtSelectDate.setText(Birthdate);
            }
        };

        linearLayoutLogin.setVisibility(View.VISIBLE);
        linearLayoutCreateUser.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.Sign_in_button){
        signIn();
        }
        else if(v.getId() == R.id.Btn_logout){
        signOut();
        }else  if(v.getId() == R.id.BtnGender){
            if (isMale == true){
                isMale = false;
                btnGender.setImageResource(R.drawable.female);
            }else{
                isMale = true;
                btnGender.setImageResource(R.drawable.male);
            }
        }else if(v.getId() == R.id.SwitchImperial) { //If the user changes the program to imperial or metric
            if (isImperial == false) {
                isImperial = true;
                textViewWeight.setText("Weight: Lbs");
                textViewHeight.setText("Height: Feet");

            }else{
                isImperial = false;
                textViewWeight.setText("Weight: Kg");
                textViewHeight.setText("Height: Meters");

            }

        }else if(v.getId() == R.id.BtnGoBack){
                signIn();

        }else if(v.getId() == R.id.BtnCreateUser){ //Save the user to the database

           if (checkCredentials() == true ){
               createUser();
           }

        }else if(v.getId() == R.id.BtnLetsGoBack){ //If they want to use a different account
            editTextWeight.setText("");
            editTextHeight.setText("");
            txtSelectDate.setText("Tap to select ");
            linearLayoutLogin.setVisibility(View.VISIBLE);
            linearLayoutCreateUser.setVisibility(View.GONE);
        }
    }

    public void createUser(){


        if (isImperial == true){
            if (txtName.getText().toString().length() == 0){
                sql.addUser(account.getDisplayName(),account.getEmail(),Double.parseDouble(editTextWeight.getText().toString())/2.20462,Double.parseDouble(editTextHeight.getText().toString())/3.28084 ,isMale,isImperial,Birthdate);
            }else{
                sql.addUser(txtName.getText().toString(),account.getEmail(),Double.parseDouble(editTextWeight.getText().toString())/2.20462,Double.parseDouble(editTextHeight.getText().toString())/3.28084 ,isMale,isImperial,Birthdate);
            }

        }else{
            if (txtName.getText().toString().length() == 0){
                sql.addUser(account.getDisplayName(),account.getEmail(),Double.parseDouble(editTextWeight.getText().toString()),Double.parseDouble(editTextHeight.getText().toString()) ,isMale,isImperial,Birthdate);
            }else{
                sql.addUser(txtName.getText().toString(),account.getEmail(),Double.parseDouble(editTextWeight.getText().toString()),Double.parseDouble(editTextHeight.getText().toString()) ,isMale,isImperial,Birthdate);
            }

        }
        showMainPage();
    }

    public void showMainPage(){
        //Opens the main activity page
        //------------------------------------------------------------------
        Intent HomePage = new Intent(this,Home.class);
        HomePage.putExtra("GoogleSignInAccount",account);
        linearLayoutLogin.setVisibility(View.VISIBLE);
        linearLayoutCreateUser.setVisibility(View.GONE);
        finish();
        startActivity(HomePage);
        //------------------------------------------------------------------
    }

//editTextWeight, editTextHeight;
    public boolean checkCredentials(){
        if (editTextWeight.getText().toString().length() !=0 && editTextHeight.getText().toString().length() != 0 && Birthdate != null) {
        return true;
        }else if(Birthdate == null){

            txtSelectDate.setTextColor(Color.parseColor("#FF0000"));
        }

    if (editTextWeight.getText().toString().length() !=0 ){

    }else{
        editTextWeight.setHint("Please Enter Here");
        editTextWeight.setHintTextColor(Color.parseColor("#FF0000"));

    }

    if ( editTextHeight.getText().toString().length() != 0){

    }else{
        editTextHeight.setHint("Please Enter Here");
        editTextHeight.setHintTextColor(Color.parseColor("#FF0000"));

    }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void signIn(){

    Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

    //Stops from auto Sign In if the user wants to use different account
        try {
            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }catch (Throwable e){

        }
    startActivityForResult(intent,REQ_CODE);
    }

    public void signOut(){

         googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
            public void onComplete(@NonNull Task<Void> task) {
            updateUIGoogleLogin();
            }
        });





    }

    private  void checkLastLoginGoogle(GoogleSignInAccount account){

        if (account != null && sql.checkUserExits(account.getEmail()) == true) {

            this.account = account;
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
            updateUIGoogleLogin(account);
        }
    }

    //Makes sure that google sends the users information back to the application
    private Void handleResult(GoogleSignInResult result){
        if(result.isSuccess()){
            account = result.getSignInAccount();

            updateUIGoogleLogin(account);

        }else{
            updateUIGoogleLogin();
        }
        //Check issues                                                          ---------------------------------------
        return null;
    }

    //Display Login Screen
    private void updateUIGoogleLogin() {

        status.setVisibility(View.GONE);
        sign_in_button.setVisibility(View.VISIBLE);
        logo.setVisibility(View.VISIBLE);

    }


    private void updateUIGoogleLogin(GoogleSignInAccount account){

        if (sql.userExists(account.getEmail()) == false){
            linearLayoutLogin.setVisibility(View.GONE);
            linearLayoutCreateUser.setVisibility(View.VISIBLE);


            if (account.getDisplayName() != null){
                txtName.setHint(account.getDisplayName());
            }


        }else {
            linearLayoutLogin.setVisibility(View.VISIBLE);
            linearLayoutCreateUser.setVisibility(View.GONE);

            status.setVisibility(View.VISIBLE);
            sign_in_button.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);

            String name = account.getDisplayName();
            String email = account.getEmail();

            this.name.setText(name);
            this.email.setText(email);
            showMainPage();



        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }
}
