package ru.zeyuzh.testrssreader;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RSSNetworkIntentService extends IntentService {

    List<RSSMessage> entries = new ArrayList<RSSMessage>();
    String title = "";
    String description = "";
    String link = "";

    NotificationManager nm;
    final int NOTIF_ID = 42;

    private SharedPreferences mSettings;

    public RSSNetworkIntentService() {
        super("RSSNetworkIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String feedAddress;
        int broadcastPos;

        mSettings = getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
        nm = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);
        if (mSettings.getBoolean("notif", false)) {
            sendNotif();
        }

        feedAddress = intent.getExtras().getString(MainActivity.SECTION_URL);
        broadcastPos = intent.getIntExtra(MainActivity.BROADCAST_POSITION, -1);
        String url = feedAddress;
        InputStream is;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            Log.d("lg", "Try HTTP request = " + url);
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
            Log.d("lg", "Success HTTP request");
            //Log.d("lg", "Input stream after GET request = " + Utils.convertStreamToString(is));

            if (xmlParse(is)) {
                Log.d("lg", "Success parsing");
            }

            is.close();

            //send broadcast to MainActivity
            Intent intentOut = new Intent(MainActivity.BROADCAST_ACTION);
            intentOut.putExtra(MainActivity.STATUS_RECEIVE, true);
            intentOut.putExtra(MainActivity.APP_TITLE, title);
            intentOut.putExtra(MainActivity.APP_DESCRIPTION, description);
            intentOut.putExtra(MainActivity.BROADCAST_POSITION, broadcastPos);
            sendBroadcast(intentOut);

            Log.d("lg", "Remove notification");
            nm.cancel(NOTIF_ID);

        } catch (ClientProtocolException e) {
            taskfailed();
            e.printStackTrace();
        } catch (IOException e) {
            taskfailed();
            e.printStackTrace();
        }
    }

    private void taskfailed() {
        Intent intentOut = new Intent(MainActivity.BROADCAST_ACTION);
        intentOut.putExtra(MainActivity.STATUS_RECEIVE, false);
        sendBroadcast(intentOut);

        Log.d("lg", "Remove notification");
        nm.cancel(NOTIF_ID);
    }

    void sendNotif() {
        Log.d("lg", "Send notification");
        Notification notif = new Notification(R.drawable.pm, getString(R.string.loading_rss), System.currentTimeMillis());
        //Intent intent = new Intent(this, MainActivity.class);
        //PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //notif.setLatestEventInfo(this, getString(R.string.loading_rss), getString(R.string.loading_rss_summary), pIntent);
        notif.setLatestEventInfo(this, getString(R.string.loading_rss), getString(R.string.loading_rss_summary), null);
        notif.flags |= Notification.FLAG_ONGOING_EVENT;
        nm.notify(NOTIF_ID, notif);
    }

    private boolean xmlParse(InputStream is) {
        try {
            //Parsing XML
            Log.d("lg", "Start DOM parser");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(is);
            Element root = dom.getDocumentElement();

            NodeList topItems = root.getElementsByTagName("channel");
            Node topItem = topItems.item(0);
            NodeList allItems = topItem.getChildNodes();
            for (int i = 0; i < allItems.getLength(); i++) {
                //Log.d("lg", i + " of allItems = " + allItems.item(i).getNodeName());
                switch (allItems.item(i).getNodeName()) {
                    case "title":
                        title = allItems.item(i).getFirstChild().getNodeValue();
                        break;
                    case "description":
                        description = allItems.item(i).getFirstChild().getNodeValue();
                        break;
                    case "link":
                        link = allItems.item(i).getFirstChild().getNodeValue();
                        break;
                    case "item":
                        RSSMessage message = new RSSMessage();
                        Node item = allItems.item(i);
                        NodeList properties = item.getChildNodes();
                        for (int j = 0; j < properties.getLength(); j++) {
                            Node property = properties.item(j);
                            String name = property.getNodeName();
                            switch (name) {
                                case "title":
                                    message.setTitle(property.getFirstChild().getNodeValue());
                                    break;
                                case "link":
                                    message.setLink(property.getFirstChild().getNodeValue());
                                    break;
                                case "description":
                                    String tmp = property.getFirstChild().getNodeValue();
                                    message.setDescription(tmp.substring(tmp.indexOf("<br />") + 6));
                                    break;
                                case "pubDate":
                                    message.setPubDate(property.getFirstChild().getNodeValue().substring(5, 22));
                                    break;
                                case "guid":
                                    message.setGuid(property.getFirstChild().getNodeValue());
                                    break;
                            }
                        }
                        //Adding message to feed
                        entries.add(message);
                        //Log.d("lg", "message = " + message.toString());
                        break;
                }
            }

            Log.d("lg", "Feed elements = " + entries.size() + " header of feed: [ Title: " + title + " Description: " + description + " Link: " + link + " ]");
            setDataInBD(entries);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void setDataInBD(List<RSSMessage> entries) {
        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        ContentValues cv = new ContentValues();
        int countFeed = entries.size();

        if (cursor.getCount() == 0) {
            for (int i = 0; i < countFeed; i++) {
                cv.put(RSSContentProvider.COLUMN_NAME_TITLE, entries.get(i).getTitle());
                cv.put(RSSContentProvider.COLUMN_NAME_DESCRIPTION, entries.get(i).getDescription());
                cv.put(RSSContentProvider.COLUMN_NAME_LINK, entries.get(i).getLink());
                cv.put(RSSContentProvider.COLUMN_NAME_PUBDATE, entries.get(i).getPubDate());
                getContentResolver().insert(RSSContentProvider.CONTENT_URI, cv);
                //Log.d("lg", "Insert in feed after download, result Uri : " + newUri.toString());
            }
        } else {
            for (int i = 0; i < countFeed; i++) {
                cv.put(RSSContentProvider.COLUMN_NAME_TITLE, entries.get(i).getTitle());
                cv.put(RSSContentProvider.COLUMN_NAME_DESCRIPTION, entries.get(i).getDescription());
                cv.put(RSSContentProvider.COLUMN_NAME_LINK, entries.get(i).getLink());
                cv.put(RSSContentProvider.COLUMN_NAME_PUBDATE, entries.get(i).getPubDate());
                Uri uriWithID = Uri.parse(RSSContentProvider.CONTENT_URI_STRING + "/" + (i + 1));
                getContentResolver().update(uriWithID, cv, null, null);
                //Log.d("lg", "Update in feed after download, result Uri : " + count);
            }
        }

        cursor.close();
    }
}
