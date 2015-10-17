package ru.zeyuzh.testrssreader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    static final String APP_PREFERENCES = "settings";
    static final String APP_TITLE = "title";
    static final String APP_DESCRIPTION = "description";
    static final String STATUS_RECEIVE = "status";

    private SharedPreferences mSettings;

    public final static String BROADCAST_ACTION = "ru.zeyuzh.rsspopmechservicebackbroadcast";

    TextView tvTitle;
    TextView tvDescription;
    RecyclerView rv;
    SwipeRefreshLayout mSwipeRefreshLayout;
    BroadcastReceiver br;
    IntentFilter intFilt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        rv = (RecyclerView) findViewById(R.id.recyclerView);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDescription = (TextView) findViewById(R.id.tvDescription);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                renewData();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        //BroadcastReceiver for work with RSSNetworkService
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("lg", "Receive signal from BroadcastReceiver");
                if (intent.getBooleanExtra(STATUS_RECEIVE, false)) {
                    Log.d("lg", "Receive signal is success");
                    if (!mSettings.contains(APP_TITLE)) {
                        String resievedTitle = intent.getStringExtra(APP_TITLE);
                        String resievedDescription = intent.getStringExtra(APP_DESCRIPTION);

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(APP_TITLE, resievedTitle);
                        editor.putString(APP_DESCRIPTION, resievedDescription);
                        editor.apply();

                        tvTitle.setText(resievedTitle);
                        tvDescription.setText(resievedDescription);
                    }
                    setDataInList();
                } else {
                    Log.d("lg", "Receive signal is fail");
                    Toast.makeText(getApplicationContext(),getString(R.string.no_connection_to_internet),Toast.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        };
        // Create intent-filter for BroadcastReceiver
        intFilt = new IntentFilter(BROADCAST_ACTION);

        Log.d("lg", "Register Receiver in onCreate()");
        registerReceiver(br, intFilt);

        //Check cached data
        //Feeds in DB
        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            Log.d("lg", "Set cached data");
            setDataInList();
        }
        cursor.close();
        //Title and description in SharedPreference
        if (mSettings.contains(APP_TITLE)) {
            tvTitle.setText(mSettings.getString(APP_TITLE, getResources().getString(R.string.title_label)));
            tvDescription.setText(mSettings.getString(APP_DESCRIPTION, getResources().getString(R.string.description_label)));
        }

        //start downloading and updating data
        renewData();
    }

    private void renewData(){
        //Start service for download RSS data
        Intent intent;
        intent = new Intent(this, RSSNetworkService.class);
        startService(intent);
    }

    private void setDataInList() {
        int positionInRV = 0;

        //save position
        LinearLayoutManager manager = (LinearLayoutManager) rv.getLayoutManager();
        positionInRV = manager.findFirstVisibleItemPosition();
        if (positionInRV<0){positionInRV=0;}

        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        RSSRecyclerViewAdapter rvAdapter = new RSSRecyclerViewAdapter(cursor);
        if (rv.getAdapter() == null) {
            Log.d("lg", "New adapter in setDataInList");
            rv.setAdapter(rvAdapter);
        } else {
            Log.d("lg", "Swap adapter in setDataInList");
            rv.swapAdapter(rvAdapter, true);
        }
        mSwipeRefreshLayout.setRefreshing(false);

        //restore position
        rv.scrollToPosition(positionInRV);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lg", "Unegister Receiver in onDestroy()");
        //When low memory, OS can skip onPause() and onStop(). Need unredistering reciever.
        unregisterReceiver(br);
    }
}
