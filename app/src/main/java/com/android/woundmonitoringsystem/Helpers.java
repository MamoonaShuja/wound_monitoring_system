package com.android.woundmonitoringsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;

import java.io.InputStream;

public class Helpers {
    SharedPreferences prefs;
    Context context;
    ProgressDialog nDialog;
    public static final String SHARED_PREFS = "shared_pref";
    public Helpers(Context context){
        this.context = context;
    }

    public String capitalizeIstLetter(String str){
        String output = "";
        if(!str.equals("")) {
            output = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return output;
    }
    public boolean savePreferences(String prefName , String prefKey , String prefValue){
        prefs = this.context.getSharedPreferences(
                prefName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefKey, prefValue);
        if(editor.commit()) {
            return true;
        }else{
            return false;
        }
    }
    public String getPreferences(String prefName , String prefKey){
        String output = null;

        prefs = context.getSharedPreferences(
                prefName, 0);
        try{
            output = prefs.getString(prefKey , null);
        }catch (Exception e){

        }
        return output;
    }
    public void removePref(String prefName , String prefKey){
        prefs = this.context.getSharedPreferences(
                prefName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefKey, null).apply();
    }
    public void addLoadingDialog(String title  , String Message ){
        nDialog = new ProgressDialog(context);
        nDialog.setMessage(Message);
        nDialog.setTitle(title);
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }
    public void dismissLoadingDialog(){
        nDialog.dismiss();
    }
    public String FuzzyEngine(double btemp , float oxy , float tmp , float hmid , float qual , float gcon )
            throws Exception {
        // Load from 'FCL' file
        InputStream inputStream = context.getAssets().open("tipper.fcl");
        FIS fis = FIS.load(inputStream, true);

        // Error while loading?
        if (fis == null) {
            System.err.println("Can't load file: '" + inputStream + "'");
//            return 0;
        }

        // Show
//        JFuzzyChart.get().chart(fis);

        // Set inputs
        fis.setVariable("AirHumidity", hmid);
        fis.setVariable("AirQuality", qual);
        fis.setVariable("AirGases", gcon);
        fis.setVariable("AirTmp", tmp);
        fis.setVariable("BodyTmp", btemp);
        fis.setVariable("SpO2", oxy);

        // Evaluate
        fis.evaluate();

        // Show output variable's chart
        Variable feasiblity = fis.getVariable("Feasiblity");
        String feas = String.valueOf(feasiblity.getValue());

        return String.valueOf(feas);
    }
}
