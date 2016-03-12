package com.budoudoh.bask_it.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budoudoh.bask_it.R;
import com.budoudoh.bask_it.domain.DynamoRequest;
import com.budoudoh.bask_it.domain.DynamoResponse;
import com.budoudoh.bask_it.domain.Item;
import com.budoudoh.bask_it.domain.Key;
import com.budoudoh.bask_it.domain.ListItem;
import com.budoudoh.bask_it.domain.Payload;
import com.budoudoh.bask_it.domain.SMSRequest;
import com.budoudoh.bask_it.service.GimbalBaskitService;
import com.budoudoh.bask_it.service.GimbalDAO;
import com.budoudoh.bask_it.service.GimbalEvent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_order)
public class OrderActivity extends RoboActivity {

    private GimbalEventReceiver gimbalEventReceiver;

    private static final int BARCODE_SCAN = 0;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "OrderActivity";

    private int checkoutCount = 0;

    private Context context;

    private double totalPrice;
    private double taxes;
    private double discounts;
    private double totalSum;

    @InjectView(R.id.scan)
    Button scan;
    @InjectView(R.id.total)
    TextView total;
    @InjectView(R.id.items)
    ListView items;
    @InjectView(R.id.gotocheckout)
    Button checkout;


    private Dialog checkoutDialog;
    private ProgressDialog itemWait;
    private ArrayList<ListItem> orderItems;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private ArrayAdapter<ListItem> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        startService(new Intent(this, GimbalBaskitService.class));
        orderItems = new ArrayList<ListItem>();
        adapter = new ListItemArrayAdapter(context, orderItems);
        items.setAdapter(adapter);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get selected items
                ListItem selectedValue = (ListItem) items.getAdapter().getItem(position);
                Item item = selectedValue.getItem();
                if(item.getDiscount() != null){
                    createDiscountDialog(item);
                }

            }
        });


        scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                //intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkoutCount > 0 && orderItems.size() > 0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.checkout));
                    taxes = totalPrice*.07;
                    discounts = totalPrice*.10;
                    totalSum = totalPrice + taxes - discounts;
                    final NumberFormat nf = NumberFormat.getCurrencyInstance();
                    builder.setMessage(getString(R.string.checkout_dialog,
                            nf.format(totalPrice),
                            nf.format(discounts),
                            nf.format(taxes),
                            nf.format(totalSum)))
                            .setPositiveButton(R.string.pay, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    launchPayDialog();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    // Create the AlertDialog object and return it
                    checkoutDialog = builder.create();
                    checkoutDialog.show();
                }else{
                    if(checkoutCount == 0){
                        Toast.makeText(context, getString(R.string.checkout_location_error), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, getString(R.string.checkout_error), Toast.LENGTH_SHORT).show();
                    }

                    checkoutCount++;
                }
            }
        });

        calculateTotal();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton("OK", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener(){

                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        gimbalEventReceiver = new GimbalEventReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GimbalDAO.GIMBAL_NEW_EVENT_ACTION);
        registerReceiver(gimbalEventReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(gimbalEventReceiver);
    }

    // --------------------
    // EVENT RECEIVER
    // --------------------

    class GimbalEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GimbalEvent event = GimbalDAO.getEvents(getApplicationContext()).get(0);
            Gson gson = new Gson();
            Log.i(TAG, gson.toJson(event));
            //adapter.setEvents(GimbalDAO.getEvents(getApplicationContext()));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case BARCODE_SCAN:
                processBarcodeScan(resultCode, intent);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void processBarcodeScan(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                fetchNewItem(contents);
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(context, getString(R.string.scan_error), Toast.LENGTH_SHORT).show();
        }
    }

    private DynamoRequest generateRequest(String sku) {
        Key key = new Key(sku);
        Payload payload = new Payload(key, context.getString(R.string.table_name));
        DynamoRequest request = new DynamoRequest(context.getString(R.string.operation), payload);
        return request;
    }

    private void fetchNewItem(String sku){
        try {
            final Gson gson = new Gson();

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = context.getString(R.string.api_url);

            final Map<String, String> mHeaders = new ArrayMap<String, String>();
            mHeaders.put("x-api-key", context.getString(R.string.api_key));

            JSONObject request = new JSONObject(gson.toJson(generateRequest(sku)));
            // Request a string response from the provided URL.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            DynamoResponse dynamoResponse = gson.fromJson(response.toString(), DynamoResponse.class);
                            addItemToOrder(dynamoResponse.getItem());
                            itemWait.dismiss();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            itemWait.dismiss();
                            Toast.makeText(context, getString(R.string.item_error), Toast.LENGTH_SHORT).show();
                        }
                    }){
                public Map<String, String> getHeaders() {
                    return mHeaders;
                }
            };
            // Add the request to the RequestQueue.
            itemWait = ProgressDialog.show(context, "Please wait ...", "Fetching Item...", true);
            itemWait.setCancelable(false);
            itemWait.show();
            queue.add(jsonRequest);
        } catch (JSONException ex) {
            Toast.makeText(context, getString(R.string.item_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateTotal(){
        double currentTotal = 0;
        for (ListItem listItem: orderItems){
            currentTotal = currentTotal + listItem.getQuantity()*listItem.getItem().getPrice();
        }

        totalPrice = currentTotal;
        final NumberFormat nf = NumberFormat.getCurrencyInstance();
        total.setText("Cart Total: "+nf.format(totalPrice));
    }

    private void addItemToOrder(Item item){
        boolean added = false;
        for (ListItem listItem: orderItems){
            if(listItem != null) {
                if (listItem.getItem().getSku().equalsIgnoreCase(item.getSku())) {
                    listItem.setQuantity(listItem.getQuantity() + 1);
                    added = true;
                    break;
                }
            }
        }

        if(!added){
            ListItem listItem = new ListItem(item, 1);
            orderItems.add(listItem);
        }

        calculateTotal();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class ListItemArrayAdapter extends ArrayAdapter<ListItem> {
        private final Context context;
        private final ArrayList<ListItem> values;

        public ListItemArrayAdapter(Context context, ArrayList<ListItem> values) {
            super(context, R.layout.list_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.list_item, parent, false);
            TextView name = (TextView) rowView.findViewById(R.id.item_name);
            TextView desc = (TextView) rowView.findViewById(R.id.item_desc);
            TextView price = (TextView) rowView.findViewById(R.id.item_price);
            TextView quantity = (TextView) rowView.findViewById(R.id.item_quantity);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.item_image);
            LinearLayout friend_holder = (LinearLayout) rowView.findViewById(R.id.friend_holder);
            TextView friend_count = (TextView) rowView.findViewById(R.id.friend_count);
            LinearLayout offer_holder = (LinearLayout) rowView.findViewById(R.id.offer_holder);

            // Change icon based on name
            ListItem listItem = values.get(position);
            Item item = listItem.getItem();

            name.setText(item.getName());
            desc.setText(item.getDesc());
            price.setText("$"+item.getPrice());
            quantity.setText("Quantity: "+listItem.getQuantity());

            if(item.getFriends() != null){
                friend_count.setText(item.getFriends()+" friends bought this item");
            }else{
                friend_holder.setVisibility(View.GONE);
            }

            if(item.getDiscount() == null){
                offer_holder.setVisibility(View.GONE);
            }

            DisplayImageOptions currentOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .showImageOnLoading(R.drawable.icon_cart)
                    .build();

            ImageLoader.getInstance().displayImage(item.getImage(), imageView, currentOptions);

            return rowView;
        }
    }

    private void createDiscountDialog(final Item item){
        try{
            final Gson gson = new Gson();
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = context.getString(R.string.api_url);

            final Map<String, String> mHeaders = new ArrayMap<String, String>();
            mHeaders.put("x-api-key", context.getString(R.string.api_key));

            JSONObject request = new JSONObject(gson.toJson(generateRequest(item.getSimilar()[0])));
            // Request a string response from the provided URL.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            DynamoResponse dynamoResponse = gson.fromJson(response.toString(), DynamoResponse.class);
                            launchDiscountDialog(item, dynamoResponse.getItem());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, getString(R.string.item_error), Toast.LENGTH_SHORT).show();
                        }
                    }){
                public Map<String, String> getHeaders() {
                    return mHeaders;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(jsonRequest);
        } catch (JSONException ex) {
            Toast.makeText(context, getString(R.string.item_error), Toast.LENGTH_SHORT).show();
        }

    }

    public void launchDiscountDialog(Item selected, Item item){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflator = this.getLayoutInflater();

        View view = inflator.inflate(R.layout.dialog_discount, null);

        TextView message = (TextView)view.findViewById(R.id.discount);
        ImageView image = (ImageView)view.findViewById(R.id.item_image);
        TextView name = (TextView)view.findViewById(R.id.item_name);
        TextView desc = (TextView)view.findViewById(R.id.item_desc);
        TextView price = (TextView)view.findViewById(R.id.item_price);

        name.setText(item.getName());
        desc.setText(item.getDesc());
        price.setText("$"+item.getPrice());

        message.setText(getString(R.string.discount_dialog,
                selected.getDiscount()+"%"));

        DisplayImageOptions currentOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.icon_cart)
                .build();

        ImageLoader.getInstance().displayImage(item.getImage(), image, currentOptions);

        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void launchPayDialog(){
        final ProgressDialog ringProgressDialog = ProgressDialog.show(context, "Please wait ...", "Processing Transaction...", true);
        ringProgressDialog.setCancelable(true);
        final NumberFormat nf = NumberFormat.getCurrencyInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        String date = df.format(new Date());
       String message = getString(R.string.checkout_SMS,
                date,
                nf.format(totalPrice),
                nf.format(discounts),
                nf.format(taxes),
                nf.format(totalSum));
        final Gson gson = new Gson();
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        try{

            String url = context.getString(R.string.sms_url);

            final Map<String, String> mHeaders = new ArrayMap<String, String>();
            mHeaders.put("x-api-key", context.getString(R.string.api_key));

            SMSRequest sms = new SMSRequest(message);
            JSONObject request = new JSONObject(gson.toJson(sms));
            // Request a string response from the provided URL.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, request,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }){
                public Map<String, String> getHeaders() {
                    return mHeaders;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(jsonRequest);
            ringProgressDialog.show();
        } catch (JSONException ex) {
        }

        String paymentUrl = getString(R.string.global_payments);
        StringRequest paymentRequest = new StringRequest(Request.Method.GET, paymentUrl, new Response.Listener<String>(){

            @Override
            public void onResponse(String response) {

            }
        },
        new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(paymentRequest);
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ringProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setMessage(R.string.checkout_success)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.create().show();
                    }
                });
            }
        }, 5000);



    }

}