package ru.zeyuzh.testrssreader;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

    final String feedUrl ="http://www.popmech.ru/out/public-all.xml";
    public static RSSFeed rssFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadRSS downloaded = new DownloadRSS(feedUrl);
        downloaded.execute();

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
                Log.d("lg", "topItems.getLength() = " + topItems.getLength());
                Node topItem = topItems.item(0);
                NodeList allItems = topItem.getChildNodes();
                for (int ii = 0; ii < allItems.getLength(); ii++) {
                    //Log.d("lg", ii + " of allItems = " + allItems.item(ii).getNodeName());
                    switch (allItems.item(ii).getNodeName()) {
                        case "title":
                            Log.d("lg", "it's title !!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            rssFeed.setTitle(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case "description":
                            Log.d("lg", "it's description !!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            rssFeed.setDescription(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case "link":
                            Log.d("lg", "it's link !!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            rssFeed.setLink(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case "item":
                            Log.d("lg", "it's item !!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            RSSMessage message = new RSSMessage();
                            Node item = allItems.item(ii);
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
                                    message.setPubDate(property.getFirstChild().getNodeValue());
                                } else if (name.equals("guid")) {
                                    message.setGuid(property.getFirstChild().getNodeValue());
                                }
                            }
                            //Adding message to feed
                            rssFeed.addEntries(message);
                            Log.d("lg", "message = " + message.toString());
                            break;
                    }
                }
                Log.d("lg", "feed = " + MainActivity.rssFeed.toString());
                is.close();

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


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
