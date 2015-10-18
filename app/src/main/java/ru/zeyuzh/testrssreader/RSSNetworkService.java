package ru.zeyuzh.testrssreader;


import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RSSNetworkService extends Service {
    //private final String feedAddress = "http://www.popmech.ru/out/public-all.xml";
    private String feedAddress = "http://www.popmech.ru/out/public-all.xml";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        feedAddress = intent.getExtras().getString(MainActivity.SECTION_URL);
        DownloadRSSTask downloaded = new DownloadRSSTask(feedAddress);
        new Thread(downloaded).start();

        return Service.START_FLAG_REDELIVERY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class DownloadRSSTask implements Runnable {
        String url;
        InputStream is;
        RSSFeed rssFeed = null;

        public DownloadRSSTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
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
                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(MainActivity.STATUS_RECEIVE, true);
                intent.putExtra(MainActivity.APP_TITLE, rssFeed.getTitle());
                intent.putExtra(MainActivity.APP_DESCRIPTION, rssFeed.getDescription());
                sendBroadcast(intent);

            } catch (ClientProtocolException e) {
                taskfailed();
                e.printStackTrace();
            } catch (IOException e) {
                taskfailed();
                e.printStackTrace();
            }

            stopSelf();
        }

        private void taskfailed() {
            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
            intent.putExtra(MainActivity.STATUS_RECEIVE, false);
            sendBroadcast(intent);
        }

        private boolean xmlParse(InputStream is) {
            try {
                //Parsing XML
                Log.d("lg", "Start DOM parser");
                rssFeed = new RSSFeed();

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
                            rssFeed.setTitle(allItems.item(i).getFirstChild().getNodeValue());
                            break;
                        case "description":
                            rssFeed.setDescription(allItems.item(i).getFirstChild().getNodeValue());
                            break;
                        case "link":
                            rssFeed.setLink(allItems.item(i).getFirstChild().getNodeValue());
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
                            rssFeed.addEntries(message);
                            //Log.d("lg", "message = " + message.toString());
                            break;
                    }
                }

                Log.d("lg", "Feed elements = " + rssFeed.getEntries().size() + " header of feed: " + rssFeed.toString());
                setDataInBD(rssFeed);

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        private void setDataInBD(RSSFeed rssFeed) {
            Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
            ContentValues cv = new ContentValues();
            int countFeed = rssFeed.getEntries().size();

            if (cursor.getCount() == 0) {
                for (int i = 0; i < countFeed; i++) {
                    cv.put(RSSContentProvider.COLUMN_NAME_TITLE, rssFeed.getEntries().get(i).getTitle());
                    cv.put(RSSContentProvider.COLUMN_NAME_DESCRIPTION, rssFeed.getEntries().get(i).getDescription());
                    cv.put(RSSContentProvider.COLUMN_NAME_LINK, rssFeed.getEntries().get(i).getLink());
                    cv.put(RSSContentProvider.COLUMN_NAME_PUBDATE, rssFeed.getEntries().get(i).getPubDate());
                    Uri newUri = getContentResolver().insert(RSSContentProvider.CONTENT_URI, cv);
                    Log.d("lg", "Insert in feed after download, result Uri : " + newUri.toString());
                }
            } else {
                for (int i = 0; i < countFeed; i++) {
                    cv.put(RSSContentProvider.COLUMN_NAME_TITLE, rssFeed.getEntries().get(i).getTitle());
                    cv.put(RSSContentProvider.COLUMN_NAME_DESCRIPTION, rssFeed.getEntries().get(i).getDescription());
                    cv.put(RSSContentProvider.COLUMN_NAME_LINK, rssFeed.getEntries().get(i).getLink());
                    cv.put(RSSContentProvider.COLUMN_NAME_PUBDATE, rssFeed.getEntries().get(i).getPubDate());
                    Uri uriWithID = Uri.parse(RSSContentProvider.CONTENT_URI_STRING + "/" + (i + 1));
                    int count = getContentResolver().update(uriWithID, cv, null, null);
                    Log.d("lg", "Update in feed after download, result Uri : " + count);
                }
            }

            cursor.close();
        }
    }
}
