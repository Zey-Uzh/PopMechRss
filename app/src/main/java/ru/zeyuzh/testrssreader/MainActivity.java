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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    static final String APP_PREFERENCES = "settings" ;
    static final String APP_TITLE = "title" ;
    static final String APP_DESCRIPTION = "description" ;
    static final String STATUS_RECEIVE = "status" ;

    public static final String SECTION_URL = "url" ;
    public final static String BROADCAST_ACTION = "ru.zeyuzh.rsspopmechservicebackbroadcast" ;

    private SharedPreferences mSettings;

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
                        String resievedTitle = intent.getStringExtra(APP_TITLE);
                        String resievedDescription = intent.getStringExtra(APP_DESCRIPTION);

                        Log.d("lg", "resievedTitle = " + resievedTitle);
                        Log.d("lg", "resievedDescription = " + resievedDescription);

                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(APP_TITLE, resievedTitle);
                        editor.putString(APP_DESCRIPTION, resievedDescription);
                        editor.apply();

                        tvTitle.setText(resievedTitle);
                        tvDescription.setText(resievedDescription);
                    setDataInList();
                } else {
                    Log.d("lg", "Receive signal is fail");
                    Toast.makeText(getApplicationContext(), getString(R.string.no_connection_to_internet), Toast.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    tvDescription.setText(mSettings.getString(APP_DESCRIPTION, getResources().getString(R.string.description_label)));
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

    private void renewData() {
        //Start service for download RSS data
        Intent intent;
        intent = new Intent(this, RSSNetworkService.class);
        intent.putExtra(SECTION_URL, mSettings.getString(SECTION_URL, RSSsections.all.getRSSsectionUrl()));
        startService(intent);
        tvDescription.setText(R.string.load_data);
    }

    private void setDataInList() {
        int positionInRV = 0;

        //save position
        LinearLayoutManager manager = (LinearLayoutManager) rv.getLayoutManager();
        positionInRV = manager.findFirstVisibleItemPosition();
        if (positionInRV < 0) {
            positionInRV = 0;
        }

        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        RSSRecyclerViewAdapter rvAdapter = new RSSRecyclerViewAdapter(cursor, this);
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

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    private void setNewSection(String url) {
        Log.d("lg", "Change section");
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(SECTION_URL, url);
        editor.apply();
        renewData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case (R.id.sub_menu_all): {
                setNewSection(RSSsections.all.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_science): {
                setNewSection(RSSsections.science.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_weapon): {
                setNewSection(RSSsections.weapon.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_technologies): {
                setNewSection(RSSsections.technologies.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_vehicles): {
                setNewSection(RSSsections.vehicles.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_gadgets): {
                setNewSection(RSSsections.gadgets.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_lectures_popular): {
                setNewSection(RSSsections.lectures_popular.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_commercial): {
                setNewSection(RSSsections.commercial.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_business_news): {
                setNewSection(RSSsections.business_news.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_editorial): {
                setNewSection(RSSsections.editorial.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_history): {
                setNewSection(RSSsections.history.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_made_in_russia): {
                setNewSection(RSSsections.made_in_russia.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_adrenalin): {
                setNewSection(RSSsections.adrenalin.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_diy): {
                setNewSection(RSSsections.diy.getRSSsectionUrl());
                return true;
            }
            case (R.id.sub_menu_design): {
                setNewSection(RSSsections.design.getRSSsectionUrl());
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
