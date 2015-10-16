package ru.zeyuzh.testrssreader;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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


public class MainActivity extends ActionBarActivity {

    final String FEED_ADDRESS = "http://www.popmech.ru/out/public-all.xml";
    RSSFeed rssFeed;
    TextView tvTitle;
    TextView tvDescription;
    RecyclerView rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.recyclerView);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDescription = (TextView) findViewById(R.id.tvDescription);


        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        //feed
        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() > 0) {
            Log.d("lg", "Set cached data");
            setDataInList();
        }
        cursor.close();

        DownloadRSS downloaded = new DownloadRSS(FEED_ADDRESS);
        downloaded.execute();

    }

    private void setDataInList() {
        Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
        RSSRecyclerViewAdapter rvAdapter = new RSSRecyclerViewAdapter(cursor);
        if (rv.getAdapter() == null) {
            Log.d("lg", "New adapter in setDataInList");
            rv.setAdapter(rvAdapter);
        } else {
            Log.d("lg", "Swap adapter in setDataInList");
            rv.swapAdapter(rvAdapter, true);
        }
    }

    private class DownloadRSS extends AsyncTask<String, Void, Void> {
        String url;
        InputStream is;

        public DownloadRSS(String url) {
            this.url = url;
        }

        protected Void doInBackground(String... urls) {

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                Log.d("lg", "Try HTTP request = " + url);
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
                Log.d("lg", "Success HTTP request");
                //Log.d("lg", "Input stream after GET request = " + Utils.convertStreamToString(is));

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
                                if (name.equals("title")) {
                                    message.setTitle(property.getFirstChild().getNodeValue());
                                } else if (name.equals("link")) {
                                    message.setLink(property.getFirstChild().getNodeValue());
                                } else if (name.equals("description")) {
                                    message.setDescription(property.getFirstChild().getNodeValue());
                                } else if (name.equals("pubDate")) {
                                    message.setPubDate(property.getFirstChild().getNodeValue().substring(5, 22));
                                } else if (name.equals("guid")) {
                                    message.setGuid(property.getFirstChild().getNodeValue());
                                }
                            }
                            //Adding message to feed
                            rssFeed.addEntries(message);
                            //Log.d("lg", "message = " + message.toString());
                            break;
                    }
                }
                is.close();
                Log.d("lg", "Success parsing");
                Log.d("lg", "Feed elements = " + rssFeed.getEntries().size() + " header of feed: " + rssFeed.toString());

            } catch (IOException e) {
                Log.d("lg", "Fail");
                Log.e("lg", e.getMessage());
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                Log.d("lg", "Fail");
                Log.e("lg", e.getMessage());
                e.printStackTrace();
            } catch (SAXException e) {
                Log.d("lg", "Fail");
                Log.e("lg", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("lg", "End of AsyncTask");

            Cursor cursor = getContentResolver().query(RSSContentProvider.CONTENT_URI, null, null, null, null);
            startManagingCursor(cursor);
            if (cursor.getCount() == 0) {
                ContentValues cv = new ContentValues();
                for (int i = 0; i < rssFeed.getEntries().size(); i++) {
                    cv.put(RSSContentProvider.COLUMN_NAME_TITLE, rssFeed.getEntries().get(i).getTitle());
                    cv.put(RSSContentProvider.COLUMN_NAME_DESCRIPTION, rssFeed.getEntries().get(i).getDescription());
                    cv.put(RSSContentProvider.COLUMN_NAME_LINK, rssFeed.getEntries().get(i).getLink());
                    cv.put(RSSContentProvider.COLUMN_NAME_PUBDATE, rssFeed.getEntries().get(i).getPubDate());
                    Uri newUri = getContentResolver().insert(RSSContentProvider.CONTENT_URI, cv);
                    Log.d("lg", "Insert in feed after download, result Uri : " + newUri.toString());
                }
            } else {
                ContentValues cv = new ContentValues();
                for (int i = 0; i < rssFeed.getEntries().size(); i++) {
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
            setDataInList();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
