package com.byagowi.persiancalendar.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.arna.NetGlobe;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.ui.stepSetting.StepAzan;
import com.byagowi.persiancalendar.ui.stepSetting.StepEvents;
import com.byagowi.persiancalendar.ui.stepSetting.StepListener;
import com.byagowi.persiancalendar.ui.stepSetting.StepLocation;
import com.byagowi.persiancalendar.ui.stepSetting.StepStartDayOfWeek;
import com.byagowi.persiancalendar.ui.stepSetting.StepWelcome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

import static com.byagowi.persiancalendar.Constants.PREF_ATHAN_ALARM;
import static com.byagowi.persiancalendar.Constants.PREF_HOLIDAY_TYPES;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_ENDS;
import static com.byagowi.persiancalendar.Constants.PREF_WEEK_START;

public class BrainActivity extends AppCompatActivity implements StepperFormListener, SharedPreferences.OnSharedPreferenceChangeListener, MActivity , StepListener {
    public final static String SH_LANG="lng";//+System.currentTimeMillis();
    public final static String SH_DONE="done";//+System.currentTimeMillis();
    public final static String SH_FDONE="firstdone";//+System.currentTimeMillis();
    public final static String SH_NAME="settings";
    private SharedPreferences sh;
    VerticalStepperFormView v;
    private SharedPreferences prefs;
    private StepAzan azanSteps;
    public static void getFirebaseId(Context c){
        FirebaseMessaging.getInstance().subscribeToTopic("testdevices").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "ok";
                if (!task.isSuccessful()) {
                    msg = "You couldn't recive notification from us";
                }
                Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
            }
        });;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        sh = getSharedPreferences(SH_NAME, MODE_PRIVATE);
        if(!sh.getBoolean(SH_FDONE,false)) {
            Set<String> str=new LinkedHashSet<>();
            String[] st=getResources().getStringArray(R.array.holidays_values);
            for (String s:st
                 ) {
                if(!s.contains("afghanistan")){
                    str.add(s);
                }
            }
            prefs.edit().putStringSet(PREF_HOLIDAY_TYPES,str).apply();
            sh.edit().putBoolean(SH_FDONE, true).apply();
        }
        if(!sh.getBoolean(SH_DONE,false)) {



            setContentView(R.layout.brain_activity);
            StepWelcome userNameStep = new StepWelcome(getString(R.string.welcomenote)).setStepListener(this, "");
            StepEvents eventSteps = new StepEvents(getString(R.string.events_summary), getResources().getStringArray(R.array.holidays_types), prefs.getStringSet(PREF_HOLIDAY_TYPES, new HashSet<>()).toArray(new String[]{}), new boolean[]{true, false, false, false, false, false, false}, "eventSteps", this).setStepListener(this, "eventSteps");
            StepEvents holidaySteps = new StepEvents(getString(R.string.week_ends), getResources().getStringArray(R.array.week_days), prefs.getStringSet(PREF_WEEK_ENDS, new HashSet<>()).toArray(new String[]{}), new boolean[]{false, false, false, false, false, false, true}, "dayOfWeekSteps", this).setStepListener(this, "dayOfWeekSteps");
            StepStartDayOfWeek dayOfWeekSteps = new StepStartDayOfWeek(getString(R.string.week_start), getResources().getStringArray(R.array.week_days), prefs.getString(PREF_WEEK_START, "0")).setStepListener(this, "holidaySteps");
            StepLocation locationSteps = new StepLocation(getString(R.string.location_select)).setStepListener(this, "locationSteps");
            azanSteps = new StepAzan(getString(R.string.athan_alarm_summary),getResources().getStringArray(R.array.prayerTimeNames),prefs.getString(PREF_ATHAN_ALARM,"").split(","),new boolean[]{false, false, false, false, false},"azanstep",this).setStepListener(this, "azanstep");

            v = findViewById(R.id.stepper_form);
            v.setup(this, userNameStep, eventSteps, dayOfWeekSteps, holidaySteps, locationSteps,azanSteps)
//
                    .init();
            if (!sh.getBoolean(SH_LANG, false)) {
                AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

                alertbox.setTitle("Please select your language/لطفا زبان خود را انتخاب کنید").setCancelable(false)
                        .setItems(R.array.languageNames, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int pos) {
                                sh.edit().putBoolean(SH_LANG, true).apply();
                                String[] lngKeys = getResources().getStringArray(R.array.languageKeys);
                                prefs.edit().putString(Constants.PREF_APP_LANGUAGE, lngKeys[pos]).apply();
                            }
                        });
                alertbox.show();
            }
        }
        else
        {
            gotoMAinActivity();
        }

        NetGlobe.start(this);
    }
    private void restartToSettings() {
        Intent intent = getIntent();
        intent.setAction("SETTINGS");
        finish();
        startActivity(intent);
    }
    @Override
    public void onCompletedForm() {
        sh.edit().putBoolean(SH_DONE, true).apply();
        prefs.edit().putBoolean("astronomicalFeatures",true).apply();
        prefs.edit().putBoolean("showWeekOfYearNumber",true).apply();
        String str[]=getResources().getStringArray(R.array.themeKeys);
        prefs.edit().putString(Constants.PREF_THEME,str[str.length-1]).apply();
        gotoMAinActivity();
    }
    private void gotoMAinActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public void onCancelledForm() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MainActivity.Blah.onSharedPreferenceChangeds(sharedPreferences,key,getApplicationContext(),this);
    }

    @Override
    public FragmentActivity myself() {
        return this;
    }

    @Override
    public void restartToSettingss() {
            restartToSettings();
    }

    @Override
    public void onStepStart(String tag, Object data) {

    }

    @Override
    public void onStepEnd(String tag, Object data) {
        try {
            switch (tag) {
                case "eventSteps": {
                    Set<String> str = (Set<String>) data;
                    prefs.edit().putStringSet(PREF_HOLIDAY_TYPES, str).apply();
                    v.goToNextStep(true);
                    break;
                }
                case "azanstep": {
                    prefs.edit().putString(PREF_ATHAN_ALARM, (String) data).apply();
                    v.goToNextStep(true);
                    break;
                }
                case "holidaySteps": {
                    prefs.edit().putString(Constants.PREF_WEEK_START, (String) data).apply();
                    v.goToNextStep(true);
                    break;
                }

                case "dayOfWeekSteps": {
                    Set<String> str = (Set<String>) data;
                    prefs.edit().putStringSet(Constants.PREF_WEEK_ENDS, str).apply();
                    v.goToNextStep(true);
                    break;
                }
                case "locationSteps": {
                    prefs.edit().putString(Constants.PREF_SELECTED_LOCATION, (String) data).apply();
                    if(Constants.DEFAULT_CITY.equals(data)||data.equals("")){
                        azanSteps.enable(false);
                    }
                    else
                    {
                        azanSteps.enable(true);
                    }
                    v.goToNextStep(true);
                    break;
                }
                default:
                    break;
            }
        }catch (Throwable tr){

        }

    }

}
