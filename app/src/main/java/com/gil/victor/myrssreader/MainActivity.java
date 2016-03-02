package com.gil.victor.myrssreader;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    ProgressBar bar;
    RecyclerView recyclerview;
    MyListAdapter adapter;
    //static final Map<String, WeakReference<Drawable>> mCache = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<Drawable>>());
    private static final DateFormat orig = new SimpleDateFormat("\n\t\tE, dd MMM yyyy HH:mm:ss Z");
    private static final DateFormat target = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    public static boolean NOVOSTI_IT, IT_NEWS;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        NOVOSTI_IT = true;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        bar = (ProgressBar) findViewById(R.id.progress_list);
        recyclerview = (RecyclerView) findViewById(R.id.list_reader);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(manager);
        recyclerview.setHasFixedSize(true);
        //проверка на наличие соединения с сетью
        if (checkConnection()) {
            new MyAsyncTask().execute("http://www.novostiit.net/feed");
            recyclerview.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main) {
            NOVOSTI_IT = true;
            IT_NEWS = false;
            if (checkConnection()) {
                new MyAsyncTask().execute("http://www.novostiit.net/feed");
            }
        } else if (id == R.id.itnews) {
            IT_NEWS = true;
            NOVOSTI_IT = false;
            if (checkConnection()) {
                new NewAsyncTask().execute();
            }
        } else if (id == R.id.exit) {
            this.finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String convertDate(String d) throws ParseException {
        return target.format(orig.parse(d));
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
            if (model != null) {
                if (localName.equalsIgnoreCase("title")) {
                    if (item)
                        model.setTitle(rssResult + '\n');
                } else if (localName.equalsIgnoreCase("pubDate")) {
                    try {
                        //входящая дата форматируется в другой формат
                        model.setDate(convertDate(rssResult));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        model.setDate("");
                    }
                } else if (localName.equalsIgnoreCase("description")) {
                    if (item) {
                        model.setDescription(Html.fromHtml(rssResult));
                    }
                } else if (localName.equalsIgnoreCase("encoded")) {
                    model.setStringContent(rssResult);
                } else if (localName.equalsIgnoreCase("item")) {
                    //как только находится закрывающийся тег item, создается объект модели и добавляется в общий список моделей
                    modelsList.add(new ReaderModel(model.getTitle(), model.getDate(), model.getDescription(), model.getStringContent()));
                }
                rssResult = "";
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (item)
                rssResult += new String(ch, start, length);
        }
    }


    public static class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

        private ArrayList<ReaderModel> models;
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            void onItemClick(ReaderModel item);
        }

        public MyListAdapter(ArrayList<ReaderModel> models) {
            this.models = models;
        }

        public void setListener(OnItemClickListener listener) {
            mListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_briefly, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ReaderModel mModel = models.get(position);
            if (NOVOSTI_IT) {
                String n = models.get(position).getTitle() + '\n' + models.get(position).getDate() + '\n' + models.get(position).getDescription();
                holder.tv.setText(n);
                holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClick(mModel);
                        }
                    }
                });
            } else if (IT_NEWS) {
                String ab = '\n' + models.get(position).getList() + '\n';
                holder.tv.setText(ab);
                holder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClick(mModel);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return models.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            View parent;
            TextView tv;

            public ViewHolder(View itemView) {
                super(itemView);
                parent = itemView;
                tv = (TextView) itemView.findViewById(R.id.text_rss);
            }

            public void setOnClickListener(View.OnClickListener listener) {
                parent.setOnClickListener(listener);
            }
        }
    }


    private class MyAsyncTask extends AsyncTask<String, Void, ArrayList<ReaderModel>> {

        private ArrayList<ReaderModel> downloadNews(String myUrl) throws IOException {
            InputStream is = null;
            ArrayList<ReaderModel> readerModels;
            try {
                URL url = new URL(myUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                RSSHandler rssHandler = new RSSHandler();
                xmlReader.setContentHandler(rssHandler);
                InputSource inputSource = new InputSource();
                is = connection.getInputStream();
                inputSource.setCharacterStream(new InputStreamReader(is));
                xmlReader.parse(inputSource);
                readerModels = rssHandler.getModels();
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
            bar.setVisibility(ProgressBar.INVISIBLE);
            adapter = new MyListAdapter(s);
            adapter.setListener(new MyListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(ReaderModel item) {
                    Intent intent = new Intent(MainActivity.this, DetailedNews.class);
                    intent.putExtra("news", item.getStringContent());
                    startActivity(intent);
                }
            });
            recyclerview.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkConnection()) {
                bar.setVisibility(ProgressBar.VISIBLE);
            }
        }
    }


    public class NewAsyncTask extends AsyncTask<String, Void, ArrayList<ReaderModel>> {

        private ArrayList<ReaderModel> itNews = new ArrayList<>();
        private ArrayList<String> list = new ArrayList<>();
        private String result = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkConnection()) {
                bar.setVisibility(ProgressBar.VISIBLE);
            }
        }

        @Override
        protected ArrayList<ReaderModel> doInBackground(String... params) {
            Document it;
            String link;
            try {
                /*doc = Jsoup.connect("http://www.psychologies.ru/self-knowledge/individuality/finansovaya-nestabilnost-prichina-fizicheskoy-boli/").timeout(5 * 1000).get();
                Elements title = doc.select("h1");
                for (Element tit : title) {
                    result = tit.text() + '\n' + '\n';
                    linkList.add(result);
                }
                Elements descr = doc.select("div[class=article-annotation]");
                for (Element des : descr) {
                    result = des.text() + '\n' + '\n';
                    linkList.add(result);
                }
                Elements attr = doc.select("p");
                for (Element el : attr) {
                    if (!el.select("a").hasAttr("href") && !el.hasClass("section__read-also-text") && !el.text().contains("Интерактивная версия журнала") && !el.text().contains("Ваш любимый журнал в обычном") && !el.text().contains("Любимые тесты от журнала")) {
                        result = el.text() + '\n' + '\n';
                        linkList.add(result);
                    }
                }*/
                it = Jsoup.connect("http://it-news.club").timeout(5 * 1000).get();
                //заголовки
                Elements elIt = it.select("header[class=entry-header]");
                //краткаое описание
                Elements elements = it.select("div[class=entry-content]");
                for (Element elem : elements) {
                    list.add(elem.text());
                }
                for (int i = 0; i < elIt.size(); i++) {
                    //сохранение ссылок в переменную link
                    link = elIt.get(i).select("h2[class=entry-title]").select("a").first().attr("href");
                    //в result сохраняется заголовок статьи и её краткое описание
                    result = elIt.get(i).text() + '\n' + '\n' + list.get(i) + '\n';
                    itNews.add(new ReaderModel(result, link));
                }
                return itNews;
            } catch (IOException e) {
                e.printStackTrace();
                result = "Ошибка";
                itNews.add(new ReaderModel(result, null));
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ReaderModel> s) {
            super.onPostExecute(s);
            bar.setVisibility(ProgressBar.INVISIBLE);
            MyListAdapter listAdapter = new MyListAdapter(s);
            listAdapter.setListener(new MyListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(ReaderModel item) {
                    Intent intent = new Intent(MainActivity.this, DetailedNews.class);
                    intent.putExtra("it", item.getLink());
                    startActivity(intent);
                }
            });
            recyclerview.setAdapter(listAdapter);
        }
    }
}