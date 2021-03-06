package edu.rpi.pagr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

import edu.rpi.pagr.misc.Appetizer;
import edu.rpi.pagr.service.NotificationService;
import edu.rpi.pagr.utils.GatewayConnectionUtils;

public class ViewCartActivity extends SherlockFragmentActivity {

    private Button button_submit_order;
    private String mReservationID;
    private String mAppetizerID;
    private String mQuantity;
    private String mOrderID;
    private TextView mYourOrder;
    private AsyncTask<Void, Void, String> mSubmitOrderTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);

        Intent intent = getIntent();
        mReservationID = (String) intent.getSerializableExtra("RESERVATION_ID");
        mAppetizerID = (String) intent.getSerializableExtra("ORDER_ITEM");
        mQuantity = (String) intent.getSerializableExtra("QUANTITY");
        mOrderID = (String) intent.getSerializableExtra("ORDER_ID");

        int ID = Integer.parseInt( mAppetizerID );

//        Toast.makeText(getBaseContext(), mAppetizerID, Toast.LENGTH_SHORT).show();
        mYourOrder = (TextView) findViewById(R.id.text_your_order);
        mYourOrder.setText( "Your Order: " + mQuantity + " " + Appetizer.AppetizerName[ID] );

        button_submit_order = (Button) findViewById(R.id.button_submit_order);
        button_submit_order.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubmitOrderTask = new SubmitOrderTask().execute();
            }
        });
    }

    private class SubmitOrderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                if ( isCancelled() )
                    return null;

                // Send Values
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("item_id", mAppetizerID ) );
                nameValuePairs.add(new BasicNameValuePair("reservation_id", mReservationID ) );
                nameValuePairs.add(new BasicNameValuePair("order_id", mOrderID ) );
                nameValuePairs.add(new BasicNameValuePair("quantity", mQuantity ) );

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(GatewayConnectionUtils.getApplicationBridgeBase()+GatewayConnectionUtils.getAddOrderItem());

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);

                StatusLine status = response.getStatusLine();

                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    return new String(EntityUtils.toByteArray(response.getEntity()), "ISO-8859-1");
                }
            } catch (Exception ignored) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            mSubmitOrderTask = null;

            if (result.equals("OK")) {
                Toast.makeText(getBaseContext(), "We got your order!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
