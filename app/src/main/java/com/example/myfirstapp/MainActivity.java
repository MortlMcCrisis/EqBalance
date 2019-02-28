package com.example.myfirstapp;

import android.content.Intent;
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

import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity
{
    private static final int RC_BARCODE_CAPTURE = 9001;

    public static final String EXTRA_VENDOR = "com.example.myfirstapp.RESULT_STATE_COMMITMENT_EXTRA";

    public static final String TAG = "Read";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        final ProgressBar spinner = findViewById( R.id.spinner);
        spinner.setVisibility( View.VISIBLE );

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String vendor = extractVendor(response);

                        spinner.setVisibility( View.INVISIBLE );

                        Intent intent = new Intent( MainActivity.this, ShowResult.class );
                        intent.putExtra(EXTRA_VENDOR, vendor);

                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView infoTextView = findViewById(R.id.infoTextView);
                spinner.setVisibility(View.INVISIBLE);
                infoTextView.setText("Vendor not found.");
                if(error.getMessage() != null)
                    infoTextView.setText(error.getMessage());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
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

    private void setAnswerTextfield( String text )
    {
        TextView infoTextView = findViewById(R.id.infoTextView);
        infoTextView.setText( text );
    }
}
