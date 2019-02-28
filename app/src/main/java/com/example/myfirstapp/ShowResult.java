package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class ShowResult extends AppCompatActivity {

    private int openRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        openRequests = 0;

        Intent intent = getIntent();

        for(String v : getPossibleVendors(intent.getStringExtra(MainActivity.EXTRA_VENDOR)) ) {
            getPossibleVendorInformation(v);
            openRequests++;
        }

        ProgressBar progressBar2 = findViewById(R.id.progressBar);
        progressBar2.setMax(openRequests);
        progressBar2.setVisibility( View.VISIBLE );
    }

    public List<String> getPossibleVendors(String vendor){
        List<String> result = new ArrayList<>();

        List<String> vendorParts = Arrays.asList(vendor.split(" "));
        for(int i=0; i<vendorParts.size(); i++){
            result.add(vendorParts.get(i));
            String current = vendorParts.get(i);
            for(int j=i+1; j<vendorParts.size(); j++){

                if(j<vendorParts.size()){
                    current += " " + vendorParts.get(j);
                    result.add(current);
                    Log.d("VENDOR","Possible vendor: " + current);
                }
            }
        }

        return result;
    }

    private void getPossibleVendorInformation(final String vendor)
    {
        final String url ="https://www.aktiv-gegen-kinderarbeit.de/firma/" + vendor.replaceAll(" ", "-");
        //final String url ="https://www.aktiv-gegen-kinderarbeit.de/firma/adidas";
        //final String url = "https://www.aktiv-gegen-kinderarbeit.de/firma/albi-fruchtsafte/";

        Log.d("Create Request", url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //updateAnswerTextfield( "More Information:" );
                        //updateAnswerTextfield( url );
                        Log.d("Result " + openRequests, "positive " + url );
                        openRequests--;

                        ProgressBar progressBar = findViewById( R.id.progressBar);
                        progressBar.setProgress( progressBar.getMax() - openRequests) ;

                        SortedMap<Integer, String> lights = getLights( response );

                        ImageView politicsState = findViewById(R.id.politicsState);
                        setLights(lights, politicsState);

                        ImageView controlState = findViewById(R.id.controlState);
                        lights = lights.tailMap(lights.firstKey()+1);
                        setLights(lights, controlState);

                        ImageView criticismState = findViewById(R.id.criticismState);
                        lights = lights.tailMap(lights.firstKey()+1);
                        setLights(lights, criticismState);

                        ImageView commitment = findViewById(R.id.commitmentState);
                        lights = lights.tailMap(lights.firstKey()+1);
                        setLights(lights, commitment);

                        if(openRequests == 0)
                            progressBar.setVisibility( View.INVISIBLE );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                openRequests--;
                ProgressBar progressBar = findViewById( R.id.progressBar);
                progressBar.setProgress( progressBar.getMax() - openRequests);

                if(openRequests == 0)
                    progressBar.setVisibility( View.INVISIBLE );

                Log.d("Result " + openRequests, "negative " + url);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void setLights(SortedMap<Integer, String> lights, ImageView state){
        switch(lights.get(lights.firstKey()))
        {
            case "rot": state.setBackgroundColor(Color.RED);
                break;
            case "gruen": state.setBackgroundColor(Color.GREEN);
                break;
            case "gelb": state.setBackgroundColor(Color.YELLOW);
                break;
            case "weiß": state.setBackgroundColor(Color.LTGRAY);
        }
    }

    private SortedMap<Integer, String> getLights( String html )
    {
        int safety = 0;
        SortedMap<Integer, String> realResult = new TreeMap<>();
        int i = -1;
        while( html.indexOf("/rot.gif", i+1) != -1)
        {
            i = html.indexOf("/rot.gif", i+1);
            realResult.put(i, "rot");
            safety++;
            if(safety > 16)
                break;
        }
        i = -1;
        while( html.indexOf("/gruen.gif", i+1) != -1)
        {
            i = html.indexOf("/gruen.gif", i+1);
            realResult.put(i, "gruen");
            safety++;
            if(safety > 16)
                break;
        }
        i = -1;
        while( html.indexOf("/gelb.gif", i+1) != -1)
        {
            i = html.indexOf("/gelb.gif", i+1);
            realResult.put(i, "gelb");
            safety++;
            if(safety > 16)
                break;
        }
        i = -1;
        while( html.indexOf("/.gif", i+1) != -1)
        {
            i = html.indexOf("/.gif", i+1);
            realResult.put(i, "weiß");
            safety++;
            if(safety > 16)
                break;
        }

        return realResult;
    }
}
