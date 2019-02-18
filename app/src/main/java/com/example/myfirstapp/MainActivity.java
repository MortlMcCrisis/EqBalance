package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

//    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

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

/*    public void sendMessage( View view )
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class );
        EditText editText = findViewById( R.id.editText );
        String message = editText.getText().toString();
        intent.putExtra( EXTRA_MESSAGE, message );
        startActivity( intent );
    }*/

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

        Button politik = findViewById( R.id.button7 );
        Button kontrolle = findViewById( R.id.button8 );
        Button vorwuerfe = findViewById( R.id.button9 );
        Button engagement = findViewById( R.id.button10 );

        TextView politikText = findViewById( R.id.textView11 );
        TextView kontrolleText = findViewById( R.id.textView12 );
        TextView vorwuerfeText = findViewById( R.id.textView13 );
        TextView engagementText = findViewById( R.id.textView14 );

        politik.setVisibility(View.INVISIBLE);
        kontrolle.setVisibility(View.INVISIBLE);
        vorwuerfe.setVisibility(View.INVISIBLE);
        engagement.setVisibility(View.INVISIBLE);

        politikText.setVisibility(View.INVISIBLE);
        kontrolleText.setVisibility(View.INVISIBLE);
        vorwuerfeText.setVisibility(View.INVISIBLE);
        engagementText.setVisibility(View.INVISIBLE);
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

                        Button politik = findViewById( R.id.button7 );
                        Button kontrolle = findViewById( R.id.button8 );
                        Button vorwuerfe = findViewById( R.id.button9 );
                        Button engagement = findViewById( R.id.button10 );

                        TextView politikText = findViewById( R.id.textView11 );
                        TextView kontrolleText = findViewById( R.id.textView12 );
                        TextView vorwuerfeText = findViewById( R.id.textView13 );
                        TextView engagementText = findViewById( R.id.textView14 );

                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": politik.setBackgroundColor(Color.RED);
                                break;
                            case "gruen": politik.setBackgroundColor(Color.GREEN);
                                break;
                            case "gelb": politik.setBackgroundColor(Color.YELLOW);
                                break;
                            case "weiß": politik.setBackgroundColor(Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": kontrolle.setBackgroundColor(Color.RED);
                                break;
                            case "gruen": kontrolle.setBackgroundColor(Color.GREEN);
                                break;
                            case "gelb": kontrolle.setBackgroundColor(Color.YELLOW);
                                break;
                            case "weiß": kontrolle.setBackgroundColor(Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": vorwuerfe.setBackgroundColor(Color.RED);
                                break;
                            case "gruen": vorwuerfe.setBackgroundColor(Color.GREEN);
                                break;
                            case "gelb": vorwuerfe.setBackgroundColor(Color.YELLOW);
                                break;
                            case "weiß": vorwuerfe.setBackgroundColor(Color.WHITE);
                        }

                        lights = lights.tailMap(lights.firstKey()+1);
                        switch(lights.get(lights.firstKey()))
                        {
                            case "rot": engagement.setBackgroundColor(Color.RED);
                                break;
                            case "gruen": engagement.setBackgroundColor(Color.GREEN);
                                break;
                            case "gelb": engagement.setBackgroundColor(Color.YELLOW);
                                break;
                            case "weiß": engagement.setBackgroundColor(Color.WHITE);
                        }

                        politik.setVisibility(View.VISIBLE);
                        politikText.setVisibility(View.VISIBLE);

                        vorwuerfe.setVisibility(View.VISIBLE);
                        vorwuerfeText.setVisibility(View.VISIBLE);

                        kontrolle.setVisibility(View.VISIBLE);
                        kontrolleText.setVisibility(View.VISIBLE);

                        engagement.setVisibility(View.VISIBLE);
                        engagementText.setVisibility(View.VISIBLE);

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
        SortedMap<Integer, String> realResult = new TreeMap<>();
        int i = -1;
        while( html.indexOf("/rot.gif", i+1) != -1)
        {
            i = html.indexOf("rot.gif", i);
            realResult.put(i, "rot");
        }
        i = -1;
        while( html.indexOf("/gruen.gif", i+1) != -1)
        {
            i = html.indexOf("gruen.gif", i);
            realResult.put(i, "gruen");
        }
        i = -1;
        while( html.indexOf("/gelb.gif", i+1) != -1)
        {
            i = html.indexOf("gelb.gif", i);
            realResult.put(i, "gelb");
        }
        i = -1;
        while( html.indexOf("/.gif", i+1) != -1)
        {
            i = html.indexOf("/.gif", i);
            realResult.put(i, "weiß");
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
