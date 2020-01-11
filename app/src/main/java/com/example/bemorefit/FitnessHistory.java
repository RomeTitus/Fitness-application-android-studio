package com.example.bemorefit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FitnessHistory extends AppCompatActivity {
private SQLManager sql;
private  LinearLayout linearLayoutHistory;
private TextView txtDate,txtSteps,txtWeight;
private ImageView imgUser;
private Button txtTakeMeBack;
private boolean isImperial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_history);
        sql = new SQLManager(FitnessHistory.this);
        Bundle bundle = getIntent().getExtras();
        String Email = bundle.getString ("Account");

        sql.getUserID(Email);
        isImperial = sql.getUserImperial();
        linearLayoutHistory = findViewById(R.id.LinearLayoutHistory);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        Cursor cursor = sql.getUserHistory();
        int test = cursor.getCount();
        for (int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            View view = layoutInflater.inflate(R.layout.activity_image_detail,linearLayoutHistory, false);
            txtDate = view.findViewById(R.id.TxtDate);
            txtSteps = view.findViewById(R.id.TxtSteps);
            txtWeight = view.findViewById(R.id.TxtWeight);
            imgUser = view.findViewById(R.id.ImgUser);


            txtDate.setText(cursor.getString(5));
            txtSteps.setText(cursor.getString(1));

            if(cursor.getString(2) != null){

                if (isImperial == false){
                    txtWeight.setText((cursor.getString(2)) + " Kg");
                }else{
                    int imperial = (int) (Integer.parseInt(cursor.getString(2))*2.205);
                    txtWeight.setText(imperial + " Lbs");
                }
            }else{
                txtWeight.setText("No weight recorded");
            }


            byte[] byteArray = cursor.getBlob(4); //gets the Bytes that the database holds
            if(byteArray != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); //Converts the Bytes to BitMap
                imgUser.setImageBitmap(bitmap);
            }
            linearLayoutHistory.addView(view);
        }

        txtTakeMeBack = findViewById(R.id.TxtTakeMeBack);

        txtTakeMeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
