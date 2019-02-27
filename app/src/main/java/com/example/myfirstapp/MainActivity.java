package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
{
    private static final int RC_BARCODE_CAPTURE = 9001;

    public static final String EXTRA_RESULT_STATE_POLITICS = "com.example.myfirstapp.RESULT_STATE_POLITICS_EXTRA";
    public static final String EXTRA_RESULT_STATE_CONTROL = "com.example.myfirstapp.RESULT_STATE_CONTROL_EXTRA";
    public static final String EXTRA_RESULT_STATE_CRITICISM = "com.example.myfirstapp.RESULT_STATE_CRITICISM_EXTRA";
    public static final String EXTRA_RESULT_STATE_COMMITMENT = "com.example.myfirstapp.RESULT_STATE_COMMITMENT_EXTRA";

    public static final String EXTRA_VENDOR = "com.example.myfirstapp.RESULT_STATE_COMMITMENT_EXTRA";

    public static final String TAG = "Read";

    private RequestQueue queue;

    private int openRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);
        openRequests = 0;
    }


    public void scan( View view )
    {
        reset();

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true );
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false );

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    private void reset()
    {
        TextView infoTextView = findViewById(R.id.infoTextView);
        infoTextView.setText("Scan barcode to get info");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        TextView statusMessage = findViewById(R.id.statusMessageView);

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    getVendorInformation(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getVendorInformation(String barcode)
    {
        String url ="http://opengtindb.org/?ean=" + barcode + "&cmd=query&queryid=400000000";
        //String url ="http://opengtindb.org/?ean=4260357350296&cmd=query&queryid=400000000";

        setAnswerTextfield( "Lookup for vendor..." );

        final ProgressBar progressBar1 = findViewById( R.id.progressBar1);
        progressBar1.setVisibility( View.VISIBLE );

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar1.setProgress( 1 );

                        String vendor = extractVendor(response);

                        updateAnswerTextfield(vendor + ": Retrieve information...");

                        for(String v : getPossibleVendors(vendor) ) {
                            getPossibleVendorInformation(v);
                            openRequests++;
                        }

                        ProgressBar progressBar2 = findViewById(R.id.progressBar2);
                        progressBar2.setMax(openRequests);
                        progressBar1.setVisibility( View.INVISIBLE );
                        progressBar2.setVisibility( View.VISIBLE );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView infoTextView = findViewById(R.id.infoTextView);
                progressBar1.setVisibility(View.INVISIBLE);
                infoTextView.setText("Vendor not found.");
                if(error.getMessage() != null)
                    infoTextView.setText(error.getMessage());
            }
        });

        queue.add(stringRequest);
    }

    private String extractVendor(String info)
    {
        String result = "No vendor found";

        StringTokenizer lineTokenizer  = new StringTokenizer(info, "\n");
        while(lineTokenizer.hasMoreElements()) {
            String token = lineTokenizer.nextToken();
            if(token.startsWith("vendor")){
                StringTokenizer vendorTokenizer = new StringTokenizer(token, "=");
                vendorTokenizer.nextToken();
                result = vendorTokenizer.nextToken();
            }
        }

        return result;
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
                        updateAnswerTextfield( "More Information:" );
                        updateAnswerTextfield( url );
                        Log.d("Result " + openRequests, "positive " + url );
                        openRequests--;

                        ProgressBar progressBar = findViewById( R.id.progressBar2);
                        progressBar.setProgress( progressBar.getMax() - openRequests) ;

                        SortedMap<Integer, String> lights = getLights( response );

                        Intent intent = new Intent( MainActivity.this, ShowResult.class );

                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": intent.putExtra(EXTRA_RESULT_STATE_POLITICS, Color.RED);
                                break;
                            case "gruen": intent.putExtra(EXTRA_RESULT_STATE_POLITICS, Color.GREEN);
                                break;
                            case "gelb": intent.putExtra(EXTRA_RESULT_STATE_POLITICS, Color.YELLOW);
                                break;
                            case "weiß": intent.putExtra(EXTRA_RESULT_STATE_POLITICS, Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": intent.putExtra(EXTRA_RESULT_STATE_CONTROL, Color.RED);
                                break;
                            case "gruen": intent.putExtra(EXTRA_RESULT_STATE_CONTROL, Color.GREEN);
                                break;
                            case "gelb": intent.putExtra(EXTRA_RESULT_STATE_CONTROL, Color.YELLOW);
                                break;
                            case "weiß": intent.putExtra(EXTRA_RESULT_STATE_CONTROL, Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": intent.putExtra(EXTRA_RESULT_STATE_CRITICISM, Color.RED);
                                break;
                            case "gruen": intent.putExtra(EXTRA_RESULT_STATE_CRITICISM, Color.GREEN);
                                break;
                            case "gelb": intent.putExtra(EXTRA_RESULT_STATE_CRITICISM, Color.YELLOW);
                                break;
                            case "weiß": intent.putExtra(EXTRA_RESULT_STATE_CRITICISM, Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": intent.putExtra(EXTRA_RESULT_STATE_COMMITMENT, Color.RED);
                                break;
                            case "gruen": intent.putExtra(EXTRA_RESULT_STATE_COMMITMENT, Color.GREEN);
                                break;
                            case "gelb": intent.putExtra(EXTRA_RESULT_STATE_COMMITMENT, Color.YELLOW);
                                break;
                            case "weiß": intent.putExtra(EXTRA_RESULT_STATE_COMMITMENT, Color.WHITE);
                        }

                        startActivity( intent );

                        if(openRequests == 0)
                            progressBar.setVisibility( View.INVISIBLE );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                openRequests--;
                ProgressBar progressBar = findViewById( R.id.progressBar2);
                progressBar.setProgress( progressBar.getMax() - openRequests);

                if(openRequests == 0)
                    progressBar.setVisibility( View.INVISIBLE );

                Log.d("Result " + openRequests, "negative " + url);
            }
        });

        queue.add(stringRequest);
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

    private void setAnswerTextfield( String text )
    {
        TextView infoTextView = findViewById(R.id.infoTextView);
        infoTextView.setText( text );
    }

    private void updateAnswerTextfield( String add )
    {
        TextView infoTextView = findViewById(R.id.infoTextView);
        infoTextView.setText(infoTextView.getText() + "\n" + add);
    }
}
