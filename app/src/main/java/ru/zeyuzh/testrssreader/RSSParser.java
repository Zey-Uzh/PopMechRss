package ru.zeyuzh.testrssreader;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class RSSParser {
    final String TITLE = "title";
    final String DESCRIPTION = "description";
    final String CHANNEL = "channel";
    final String IMAGE = "image";
    final String URL_IMAGE = "url";
    final String LINK = "link";
    final String ITEM = "item";
    final String PUB_DATE = "pubDate";
    final String GUID = "guid";

    final String feedUrl;

    public RSSParser(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public RSSFeed readFeed() {
        RSSFeed feed = null;


            DownloadRSS downloaded = new DownloadRSS(feedUrl);
            downloaded.execute();

        return feed;
    }

    private class DownloadRSS extends AsyncTask<String, Void, Void> {
        String url;
        InputStream is;

        public DownloadRSS(String url) {
            this.url = url;
        }

        protected Void doInBackground(String... urls) {

            /*
    final String TITLE = "title";
    final String DESCRIPTION = "description";
    final String CHANNEL = "channel";
    final String IMAGE = "image";
    final String URL_IMAGE = "url";
    final String LINK = "link";
    final String ITEM = "item";
    final String PUB_DATE = "pubDate";
    final String GUID = "guid";
            */
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
                MainActivity.rssFeed = new RSSFeed();

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
                            MainActivity.rssFeed.setTitle(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case DESCRIPTION:
                            Log.d("lg", "it's " + DESCRIPTION + "!!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            MainActivity.rssFeed.setDescription(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case LINK:
                            Log.d("lg", "it's " + LINK + "!!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            MainActivity.rssFeed.setLink(allItems.item(ii).getFirstChild().getNodeValue());
                            break;
                        case ITEM:
                            Log.d("lg", "it's item !!! " + allItems.item(ii).getFirstChild().getNodeValue());
                            RSSMessage message = new RSSMessage();
                            Node item = allItems.item(ii);
                            NodeList properties = item.getChildNodes();
                            for (int j = 0; j < properties.getLength(); j++) {
                                Node property = properties.item(j);
                                String name = property.getNodeName();
                                if (name.equals(TITLE)) {
                                    message.setTitle(property.getFirstChild().getNodeValue());
                                } else if (name.equals(LINK)) {
                                    message.setLink(property.getFirstChild().getNodeValue());
                                } else if (name.equals(DESCRIPTION)) {
                                    message.setDescription(property.getFirstChild().getNodeValue());
                                } else if (name.equals(PUB_DATE)) {
                                    message.setPubDate(property.getFirstChild().getNodeValue());
                                } else if (name.equals(GUID)) {
                                    message.setGuid(property.getFirstChild().getNodeValue());
                                }
                            }
                            //Adding message to feed
                            MainActivity.rssFeed.addEntries(message);
                            Log.d("lg", "message = " + message.toString());
                            break;
                    }
                }


                //MainActivity.rssFeed = new RSSFeed(TITLE, DESCRIPTION, LINK, IMAGE, PUB_DATE);
                Log.d("lg", "feed = " + MainActivity.rssFeed.toString());
/*
                NodeList items = root.getElementsByTagName("item");
                Log.d("lg", "items = " + items.getLength());
                for (int i = 0; i < items.getLength(); i++) {
                    RSSMessage message = new RSSMessage();
                    Node item = items.item(i);
                    NodeList properties = item.getChildNodes();
                    for (int j = 0; j < properties.getLength(); j++) {
                        Node property = properties.item(j);
                        String name = property.getNodeName();

                        if (name.equals(TITLE)) {
                            message.setTitle(property.getFirstChild().getNodeValue());
                        } else if (name.equals(LINK)) {
                            message.setLink(property.getFirstChild().getNodeValue());
                        } else if (name.equals(DESCRIPTION)) {
                            message.setDescription(property.getFirstChild().getNodeValue());
                        } else if (name.equals(PUB_DATE)) {
                            message.setPubDate(property.getFirstChild().getNodeValue());
                        } else if (name.equals(GUID)) {
                            message.setGuid(property.getFirstChild().getNodeValue());
                        }

                    }
                    //Adding message to feed
                    MainActivity.rssFeed.addEntries(message);
                    //Log.d("lg", "message = " + message.toString());
                }
*/
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

}
