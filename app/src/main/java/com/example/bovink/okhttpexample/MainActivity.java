package com.example.bovink.okhttpexample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.bovink.okhttpexample.model.Repo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView output;

    private List<Repo> repos;

    private OkHttpClient okHttpClient;
    private Request request;

    private static int REQUEST_TYPE = -1;

    private final static int REQUEST_SYNCHRONOUS = 0;
    private final static int REQUEST_ASYNCHRONOUS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.tv_output);
        repos = new ArrayList<>();

        REQUEST_TYPE = REQUEST_ASYNCHRONOUS;

        config();
        request();
    }

    private void config() {
        okHttpClient = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.github.com/users/bovink/repos").newBuilder();

        urlBuilder.addQueryParameter("page", "1");
        urlBuilder.addQueryParameter("per_page", "5");

        String url = urlBuilder.build().toString();

        request = new Request.Builder()
                .url(url)
                .build();
    }

    private void request() {
        if (REQUEST_TYPE == REQUEST_SYNCHRONOUS) {
            synchronousRequest();
        } else if (REQUEST_TYPE == REQUEST_ASYNCHRONOUS){
            asynchronousRequest();
        }
    }

    private void synchronousRequest() {

        SynchronousRequestThread requestThread = new SynchronousRequestThread();
        requestThread.start();
    }

    private class SynchronousRequestThread extends Thread {
        Gson gson = new Gson();

        @Override
        public void run() {

            try {
                Response response = okHttpClient.newCall(request).execute();
                repos = gson.fromJson(response.body().charStream(), new TypeToken<List<Repo>>() {
                }.getType());
                handler.sendEmptyMessage(0);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void asynchronousRequest() {
        final Gson gson = new Gson();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                repos = gson.fromJson(response.body().charStream(), new TypeToken<List<Repo>>() {
                }.getType());
                handler.sendEmptyMessage(0);

            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            StringBuilder stringBuilder = new StringBuilder();
            if (REQUEST_TYPE == REQUEST_SYNCHRONOUS) {
                stringBuilder.append("from synchronous")
                        .append("\n");
            } else if (REQUEST_TYPE == REQUEST_ASYNCHRONOUS){
                stringBuilder.append("from asynchronous")
                        .append("\n");
            }
            for (int i = 0; i < repos.size(); i++) {
                stringBuilder.append(repos.get(i).getName())
                        .append("\n");
            }
            output.setText(stringBuilder.toString());
        }
    };
}
