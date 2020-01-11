package com.example.bemorefit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Intent;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class SlideAdapter extends PagerAdapter implements SensorEventListener{


    public interface MyCustomObjectListener {
        // need to pass relevant arguments related to the event triggered
        public void onObjectReady(String title);

    }

Context context;
LayoutInflater inflater;

//List of Titles (Ignore for now)
    public String[] title = {
            "0oo",
            "o0o",
            "oo0",
            "o00"
    };
    //stores page Information
    View view;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int steps = 0, position;
    private TextView name, email, txtGoal, txtGoalAmount, txtUserAmount,textViewSetTstepGoal, textViewSetWeightGoal, txtStepsDisplay, txtViewBMI;
    private ImageView loginCharicter, logo, imgCurrentPhoto;
    private Button btn_logout, btnTakePhoto,btnDeleteAcount;
    private GoogleSignInAccount account;
    private GoogleApiClient googleApiClient;
    private Button Btn_logout, btnSubmitWight, btnViewHistory;
    private Switch switchImperial;
    private  LineChart lineChartDiagram;
    private TextView displayWeight, textViewDataCheck,txtWeNeedMoreDays;
    private SeekBar weightBar, seekBarStepGoal,seekBarWeightGoal;
    private SQLManager sql;
    private boolean imperial;
    private MyCustomObjectListener listener;

    List<PieEntry> weightPieList;
    PieChart weightPieLimit,stepPie;

    public SlideAdapter (Context context,  GoogleSignInAccount account){
        this.context = context;
        this.account = account;
        String email = account.getEmail();
        sql = new SQLManager(context);
        sql.getUserID(email);
        this.imperial = sql.getUserImperial();
        steps = sql.loadSteps();
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
        this.listener = null;



    }


@Override
    public int getCount(){
    return title.length;
}

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       container.removeView((LinearLayout)object);
    }


    //variables to be used in the other User Interfaces
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {

    inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.position = position;
        //different positions give different pages to show
        if (position == 0) {
            //                                                             ------------------------------------------------Stats Page
            view = inflater.inflate(R.layout.activity_stats, container, false);
            //stepPie = view.findViewById(R.id.StepPie);
            LinearLayout layoutslide = view.findViewById(R.id.slidelinearlayout);
            LinearLayout pielayout = view.findViewById(R.id.PieLayout);
            stepPie = view.findViewById(R.id.StepPie);
            txtWeNeedMoreDays = view.findViewById(R.id.TxtWeNeedMoreDays);
            lineChartDiagram = view.findViewById(R.id.LineChartDiagram);

            //When called, it generated the graph to be displayed to the user
            createPieChart();                     //Populates the Graph

            txtStepsDisplay = view.findViewById(R.id.TxtStepsDisplay);
            txtStepsDisplay.setText(steps + " ");

            createLineGraph();//-----------------------------------------------------End Of Start
        }else if(position == 1){
            view = inflater.inflate(R.layout.activity_weight, container, false);
            weightBar = (SeekBar) view.findViewById(R.id.seekBar);
            displayWeight = (TextView) view.findViewById(R.id.txtCalories);
            weightPieLimit = view.findViewById(R.id.WeightLimitPie);
            btnSubmitWight = view.findViewById(R.id.BtnSubmitWight);
            txtViewBMI = view.findViewById(R.id.TxtViewBMI);

            weightBar.setProgress(sql.loadWeight()*10);
            if (sql.getUserImperial() == false){
                displayWeight.setText((double)sql.loadWeight() + " Kg");
            }else{
                displayWeight.setText((int)((sql.loadWeight())*2.205) + " Lbs");
            }


            weightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (sql.getUserImperial() == false){
                    displayWeight.setText((double)progress/10 + " Kg");
                }else{
                    displayWeight.setText((int)((progress/10)*2.205) + " Lbs");
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

            createPieChartWeight();
            getBMI();
            btnSubmitWight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sql.saveWeight(weightBar.getProgress()/10);
                    createPieChartWeight();
                    getBMI();

                }
            });

            }else if(position == 2){            //----------------------------------------------------------------------------------------------------------------Start Camera Page
            view = inflater.inflate(R.layout.activity_takephoto, container, false);
            btnTakePhoto = view.findViewById(R.id.BtnTakePhoto);
            btnViewHistory = view.findViewById(R.id.BtnViewHistory);
            imgCurrentPhoto = view.findViewById(R.id.ImgCurrentPhoto);
            btnTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraPage = new Intent(view.getContext(), camera.class);
                    cameraPage.putExtra("Account",account.getEmail());
                    view.getContext().startActivity(cameraPage);
                    getTodayPhoto();
                }
            });

            btnViewHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent history = new Intent(view.getContext(), FitnessHistory.class);
                    history.putExtra("Account",account.getEmail());
                    history.putExtra("isImperial",sql.getUserImperial());
                    view.getContext().startActivity(history);
                }
            });

            getTodayPhoto();//loads the users photo in the image view
                                                //----------------------------------------------------------------------------------------------------------------Start Camera Page

        }else if(position == 3){ //-------------------------------------------------------------------------------------------------------------------------------Start Setting and user information Page


            view = inflater.inflate(R.layout.activity_settings,container,false);
            LinearLayout layoutslide = view.findViewById(R.id.SettingsLinearLayout);
            btn_logout = (Button) view.findViewById(R.id.Btn_logout);
            btnDeleteAcount = view.findViewById(R.id.BtnDeleteAcount);
            name = view.findViewById(R.id.Name);
            email = view.findViewById(R.id.Email);
            loginCharicter = view.findViewById(R.id.LoginCharicter);
            logo = view.findViewById(R.id.Logo);
            switchImperial = view.findViewById(R.id.SwitchImperial);
            textViewSetTstepGoal = view.findViewById(R.id.TextViewSetTstepGoal);
            textViewSetWeightGoal = view.findViewById(R.id.TextViewSetWeightGoal);
            seekBarWeightGoal = (SeekBar) view.findViewById(R.id.SeekBarWeightGoal);
            seekBarWeightGoal.setMax(8);
            seekBarStepGoal = (SeekBar) view.findViewById(R.id.SeekBarStepGoal);
            seekBarStepGoal.setMax(10);

            switchImperial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imperial = switchImperial.isChecked();
                    sql.setUserImperial(imperial);
                    if (sql.getUserImperial() == false){
                        textViewSetWeightGoal.setText("Current Weight Goal: " + (seekBarWeightGoal.getProgress()+4)*10 + "Kg");
                    }else{
                        textViewSetWeightGoal.setText("Current Weight Goal: " + (int)(((seekBarWeightGoal.getProgress()+4)*10)*2.205) + "Lbs");
                    }

                }
            });

            btnDeleteAcount.setOnClickListener(new View.OnClickListener()
            { @Override
            public void onClick(View v)
            {
                listener.onObjectReady("btnDeleteAcount");

            }});

            seekBarStepGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textViewSetTstepGoal.setText("Current Step Goal: " + (progress+5)*1000 + " steps");

                    if (sql.getUserImperial() == false){
                        textViewSetWeightGoal.setText("Current Weight Goal: " + (seekBarWeightGoal.getProgress()+4)*10 + "Kg");
                    }else{
                        textViewSetWeightGoal.setText("Current Weight Goal: " + (int)(((seekBarWeightGoal.getProgress()+4)*10)*2.205) + "Lbs");
                    }

                    sql.updateUserGoal((progress+5)*1000,(seekBarWeightGoal.getProgress()+4)*10);


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekBarWeightGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textViewSetTstepGoal.setText("Current Step Goal: " + (seekBarStepGoal.getProgress()+5)*1000 + " steps");

                    if (sql.getUserImperial() == false){
                        textViewSetWeightGoal.setText("Current Weight Goal: " + (progress+4)*10 + "Kg");
                    }else{
                       textViewSetWeightGoal.setText("Current Weight Goal: " + (int)(((progress+4)*10)*2.205) + "Lbs");
                    }


                    sql.updateUserGoal((seekBarStepGoal.getProgress()+5)*1000,(progress+4)*10);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            btn_logout.setOnClickListener(new View.OnClickListener()
           { @Override
                public void onClick(View v)
                {
                    listener.onObjectReady("btn_logout");

                }});
        getLoginDetails();

        }else{

            view = inflater.inflate(R.layout.activity_stats, container, false);

        }//------------------------------------------------------------------------------------------------------------------------------------------------End Settings and user information Page
    container.addView(view);
    return view; // returns the view and should return the button ID?
}

private void getBMI(){
        try{
          double BMI = 0;
            double userWeight = sql.loadWeight();
            double userHeight = sql.loadHeight();

            BMI = userWeight/(userHeight*userHeight);
            BMI = (int)(BMI*10); //used to get to 2 decimal places
            BMI = BMI/10;
            txtViewBMI.setText("BMI: "+BMI);
        }catch (Exception e){
            txtViewBMI.setText("Please Enter your weight today first!");
        }
}

private void createPieChartWeight(){ //creates a graph to visually see the users progress in there goal

    weightPieLimit.setUsePercentValues(true);
    List<PieEntry> weightPieList = new ArrayList<>();
    double userWeight = sql.loadWeight();
    double[] weight =sql.getUserGoal();
    int getweight = (int)weight[1];

if((int)userWeight< getweight){
    if (sql.getUserImperial() == false) {
        weightPieList.add(new PieEntry((int) userWeight, (int) userWeight + " KG"));
        weightPieList.add(new PieEntry(getweight - (int) userWeight, getweight - (int) userWeight + "KG"));
    }else{

        weightPieList.add(new PieEntry((int) userWeight, (int) (userWeight * 2.205) + " Lbs"));
        weightPieList.add(new PieEntry(getweight - (int) userWeight, (int) (getweight*2.205) - (int) (userWeight * 2.205) + " Lbs"));
    }

}else{

    if (sql.getUserImperial() == false) {
        weightPieList.add(new PieEntry((int) userWeight,(int)userWeight + " KG"));
        weightPieList.add(new PieEntry(((getweight-(int)userWeight)*-1),(int)((getweight-userWeight)*-1)+ " KG"));

    }else{

        weightPieList.add(new PieEntry((int) userWeight,(int)(userWeight*2.205) + " Lbs"));
        weightPieList.add(new PieEntry(((getweight-(int)userWeight)*-1),(int)(((getweight-userWeight)*-1)*2.205)+ " Lbs"));
    }

}
    PieDataSet weightPieDataSet;
    if((int)userWeight< getweight){
        weightPieDataSet = new PieDataSet(weightPieList,"Weight to gain");
    }else{
        weightPieDataSet = new PieDataSet(weightPieList,"Weight to Lose");
    }


    weightPieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
    weightPieDataSet.setDrawValues(false);
    weightPieLimit.setHoleRadius(30f);
    weightPieLimit.setTransparentCircleRadius(25f);
    weightPieLimit.animateXY(1400,1400);
    PieData caloriePieData = new PieData(weightPieDataSet);
    weightPieLimit.setData(caloriePieData);
    weightPieLimit.setNoDataText("");
    //Used to set the discription for the Pie chart
    Description description = new Description();
    description.setText("");
    weightPieLimit.setDescription(description);

    }

private void createPieChart(){

    stepPie.setUsePercentValues(true);
    List<PieEntry> stepPieList = new ArrayList<>();
    double[] setGoal = sql.getUserGoal();
    int toGo = (int)(setGoal[0]);
    String pieData = String.valueOf(steps);
    String toGoSteps = String.valueOf(toGo-steps);
    stepPieList.add(new PieEntry(steps,pieData)); //What the user has done
    stepPieList.add(new PieEntry(toGo-steps,toGoSteps)); //What the user still needs to do

    PieDataSet stepPieDataSet = new PieDataSet(stepPieList,"Steps To Reach");
    stepPieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
    stepPieDataSet.setDrawValues(false);
    stepPie.setHoleRadius(30f);
    stepPie.setTransparentCircleRadius(25f);

    PieData stepPieData = new PieData(stepPieDataSet);
    stepPie.setData(stepPieData);
    stepPie.setNoDataText("");
    //Used to set the discription for the Pie chart
    Description description = new Description();
    description.setText("");
    stepPie.setDescription(description);
    stepPie.invalidate();
    }

private void createLineGraph(){

    ArrayList<Entry>  yAxesstepHistory = new ArrayList<>();
    ArrayList<Entry>  yAxesweightHistory = new ArrayList<>();
    ArrayList<ILineDataSet> multipleData = new ArrayList<>();
    Cursor data = sql.getUserHistory();

if(data.getCount() != -1){
    for (int i = 0; i<data.getCount(); i++) {
        data.moveToNext();

        float stepdata;
        float weightdata;

        if (data.getString(2) != null) {

            if (imperial == false) { //convert the weight to Lbs or KG
                weightdata = Float.parseFloat(data.getString(2));
            } else {
                weightdata = (int) (Integer.parseInt(data.getString(2)) * 2.205);
            }
            yAxesweightHistory.add(new Entry(i, weightdata));


        }
        if (data.getString(1) != null) {
            stepdata = Float.parseFloat(data.getString(1));
            yAxesstepHistory.add(new Entry(i, stepdata));


        }
    }

        //if(data.getString(2) != null){
            if(yAxesweightHistory.size() > 1){
            LineDataSet dataSetWeightHistory;
            if (imperial == false){ //convert the weight to Lbs or KG
                dataSetWeightHistory = new LineDataSet(yAxesweightHistory, "Weight Kg");
            }else{
                dataSetWeightHistory = new LineDataSet(yAxesweightHistory, "Weight Lbs");
            }
            dataSetWeightHistory.setDrawValues(false);
            dataSetWeightHistory.setDrawCircles(false);
            dataSetWeightHistory.setColor(Color.GREEN);
            dataSetWeightHistory.setLineWidth(5);
            multipleData.add(dataSetWeightHistory);

        }

        if(data.getString(1) != null){
            LineDataSet dataSetStepHistory = new LineDataSet(yAxesstepHistory, "Steps");
            dataSetStepHistory.setDrawValues(false);
            dataSetStepHistory.setDrawCircles(false);
            dataSetStepHistory.setColor(Color.RED);
            dataSetStepHistory.setLineWidth(5);
            multipleData.add(dataSetStepHistory);

        }
}

    lineChartDiagram.setData(new LineData(multipleData));

    Description description = new Description();
if(data.getCount() < 2){
    txtWeNeedMoreDays.setText("We Need More Days to create this Graph");
    txtWeNeedMoreDays.setVisibility(View.VISIBLE);

}else{
    txtWeNeedMoreDays.setVisibility(View.GONE);
}

        description.setText("");
        lineChartDiagram.setDescription(description);



    //lineChartDiagram.setVisibleXRangeMaximum(65f);
    lineChartDiagram.animateY(1000);





}
 //-------------------------------------------------------------------------------------Delete Not needed

private void getTodayPhoto(){
      Cursor img = sql.getImage();

      try{
          if(img.getCount() != -1) {
              img.moveToNext();
              byte[] byteArray = img.getBlob(0); //gets the Bytes that the database holds
                if(byteArray != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length); //Converts the Bytes to BitMap

                    imgCurrentPhoto.setImageBitmap(bitmap);
                }
          }
      }catch (Exception e){

      }
}

private void getLoginDetails(){
    String name = sql.getName();
    String email = account.getEmail();
    this.name.setText(name);
    this.email.setText(email);
    try {
        String img_url = account.getPhotoUrl().toString();
        Glide.with(view.getContext()).load(img_url).into(loginCharicter);
    }catch (Exception x){

        loginCharicter.setBackgroundResource(R.drawable.login_charicter);
    }

    switchImperial.setChecked(imperial);
    double[] userGoal = sql.getUserGoal();                                                                                  //gets the users current set goal that they have Established
    textViewSetTstepGoal.setText("Current Step Goal: " + userGoal[0] + " steps");
    //int test1 = ((int)userGoal[0]/1000-5);
    seekBarStepGoal.setProgress(((int)userGoal[0]/1000-5));
    //seekBarStepGoal.setProgress(5);
    textViewSetWeightGoal.setText("Current Weight Goal: " + userGoal[1] + "Kg");
    //int test2 = (int)(userGoal[1]/10-4);
    seekBarWeightGoal.setProgress((int)(userGoal[1]/10-4));
    //seekBarWeightGoal.setProgress(4);

    //----------------------------------------------------------------------DEBUG ONLY
/*
("Current Step Goal: " + (seekBarStepGoal.getProgress()+5)*1000 + " steps");
                    textViewSetWeightGoal.setText("Current Weight Goal: " + (progress+4)*10 + "Kg");
 */
}


    @Override
    public boolean isViewFromObject(View view, Object object)
{
    return (view ==(LinearLayout)object);
}



    @Override
    public void onSensorChanged(SensorEvent event) {
        steps++;
        sql.saveSteps(steps);

        try{
            createPieChart();
            txtStepsDisplay.setText(steps + " ");

        }catch (Throwable t) {
            Throwable e = t;
            String james = "1";
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//----------------------------------------------------------------------------------Testing

public int getCurrentPageIndex(){
        return position;
}

    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }

}
