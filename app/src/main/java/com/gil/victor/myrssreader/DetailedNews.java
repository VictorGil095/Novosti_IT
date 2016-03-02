package com.gil.victor.myrssreader;

import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class DetailedNews extends AppCompatActivity {

    Toolbar toolbar;
    TextView textView;
    Spanned content;
    ProgressBar bar;
    Drawable drawable;
    Handler handler;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detailed);
        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bar = (ProgressBar) findViewById(R.id.progress);
        textView = (TextView) findViewById(R.id.text_full);
        final String model = getIntent().getStringExtra("news");
        final String itModel = getIntent().getStringExtra("it");

        if (MainActivity.NOVOSTI_IT) {
            //форматирование строки, поскольку первая буква(или слово) во входящей строке расположена отдельно с картинкой, в результате чего менее красиво смотрится
            int index = model.indexOf("/>");
            String two = model.substring(index + 2);
            String[] one = model.split("/>");
            String indent = "<br />";
            final String res = one[0].concat(indent) + indent.concat(two);
            if (checkConnection()) {
                handler = new Handler();
                bar.setVisibility(ProgressBar.VISIBLE);
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        content = Html.fromHtml(res, new Html.ImageGetter() {
                            @Override
                            public Drawable getDrawable(String source) {
                                try {
                                    drawable = Drawable.createFromStream(new URL(source).openStream(), "src");
                                    //в зависимости от размеров экрана и картинки задается её размер
                                    drawable.setBounds(0, 0, proportionsWidth(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()), proportionsHeight(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
                                    return drawable;
                                } catch (IOException | NullPointerException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }
                        }, null);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                bar.setVisibility(ProgressBar.INVISIBLE);
                                textView.setText(content);
                            }
                        });
                    }
                });
                thread.start();
            }
        } else if (MainActivity.IT_NEWS) {
            if (checkConnection()) {
                handler = new Handler();
                bar.setVisibility(ProgressBar.VISIBLE);
                thread = new Thread((new Runnable() {
                    @Override
                    public void run() {
                        Spanned abc = null;
                        try {
                            Document doc = Jsoup.connect(itModel).timeout(5 * 1000).get();
                            //подробное описание
                            Elements element = doc.select("div[class=entry-content]");
                            for (Element el : element) {
                                String a = el.html();
                                int i = a.indexOf("<p>Присоединяйтесь к нам в");
                                abc = Html.fromHtml(a.substring(0, i), new Html.ImageGetter() {
                                    @Override
                                    public Drawable getDrawable(String source) {
                                        try {
                                            drawable = Drawable.createFromStream(new URL(source).openStream(), "image");
                                            drawable.setBounds(0, 0, proportionsWidth(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()), proportionsHeight(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
                                            return drawable;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            return null;
                                        }
                                    }
                                }, null);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final Spanned finalAbc = abc;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                bar.setVisibility(ProgressBar.INVISIBLE);
                                textView.setText(finalAbc);
                            }
                        });
                    }
                }));
                thread.start();
            }
        }
    }

    private int proportionsWidth(int width, int height) {
        int inSampleSize = 1;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        if (width >= height) {
            if (width < screenWidth / 2) {
                do {
                    inSampleSize += 1;
                }
                while (!(width * inSampleSize >= screenWidth / 2));
            }
        } else if (height < displayMetrics.heightPixels / 2) {
            do {
                inSampleSize += 1;
            }
            while (!(height * inSampleSize >= displayMetrics.heightPixels / 2));
        }
        return width * inSampleSize;
    }

    private int proportionsHeight(int width, int height) {
        int inSampleSize = 1;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        if (height >= width) {
            if (height < screenHeight / 2) {
                do {
                    inSampleSize += 1;
                }
                while (!(height * inSampleSize >= screenHeight / 2));
            }
        } else if (width < displayMetrics.widthPixels / 2) {
            do {
                inSampleSize += 1;
            }
            while (!(width * inSampleSize >= displayMetrics.widthPixels / 2));
        }
        return height * inSampleSize;
    }

    private boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}