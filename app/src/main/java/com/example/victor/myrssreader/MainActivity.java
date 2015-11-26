package com.example.victor.myrssreader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class MainActivity extends Activity {

    ListView listView;
    Button button;

    @Override
    protected void onResume() {
        super.onResume();
        //checkForCrashes();
        //checkForUpdates();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_news);
        listView = (ListView) findViewById(R.id.list_reader);
        button = (Button) findViewById(R.id.button_parse);
        new MyAsyncTask().execute("http://news.tut.by/rss/42/videogames.rss");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyAsyncTask().execute("http://news.tut.by/rss/42/videogames.rss");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void checkForCrashes() {
        CrashManager.register(this, "3103471272e94ff6b7024dd880070af9");
    }

    private void checkForUpdates() {
        UpdateManager.register(this, "3103471272e94ff6b7024dd880070af9");
    }

    private class RSSHandler extends DefaultHandler {
        ReaderModel model = null;
        ArrayList<ReaderModel> modelsList = new ArrayList<>();
        String rssResult = "";
        boolean item = false;

        ArrayList<ReaderModel> getModels() {
            return modelsList;
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
            if (localName.equals("item")) {
                item = true;
                model = new ReaderModel();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            item = false;
            if (model != null) {
                if (localName.equalsIgnoreCase("title"))
                    model.setTitle(rssResult);
                else if (localName.equalsIgnoreCase("description"))
                    model.setDescription(rssResult);
                else if (localName.equalsIgnoreCase("pubDate"))
                    model.setDate(rssResult);
                else if (localName.equalsIgnoreCase("item"))
                    modelsList.add(model);
                rssResult = "";
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (item)
                rssResult += new String(ch, start, length);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, ArrayList<ReaderModel>> {

        ProgressDialog dialog;

        MyAsyncTask() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading");
        }

        private ArrayList<ReaderModel> downloadNews(String myUrl) throws IOException {
            InputStream is = null;
            ArrayList<ReaderModel> readerModels;
            try {
                URL url = new URL(myUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                Log.w("MainActivity", "Begin");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                RSSHandler rssHandler = new RSSHandler();
                xmlReader.setContentHandler(rssHandler);
                InputSource inputSource = new InputSource();
                Log.w("MainActivity", "Number 1");
                is = connection.getInputStream();
                inputSource.setCharacterStream(new InputStreamReader(is));
                Log.w("MainActivity", "Number 2");
                xmlReader.parse(inputSource);
                readerModels = rssHandler.getModels();
                Log.w("MainActivity", "Finishing");
                return readerModels;
            } catch (ParserConfigurationException | SAXException | IOException e) {
                Log.w("MainActivity", e);
            } finally {
                if (is != null)
                    is.close();
            }
            return null;
        }

        @Override
        protected ArrayList<ReaderModel> doInBackground(String... params) {
            try {
                return downloadNews(params[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ReaderModel> s) {
            super.onPostExecute(s);
            dialog.dismiss();
            MyListAdapter adapter = new MyListAdapter(getApplicationContext(), s);
            listView.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }
    }
}