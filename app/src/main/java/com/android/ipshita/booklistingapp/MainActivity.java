package com.android.ipshita.booklistingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public String URL_RESULT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchButton = (Button) findViewById(R.id.search_button);
        final EditText searchitem = (EditText) findViewById(R.id.search);

        assert searchButton != null;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL_RESULT = "https://www.googleapis.com/books/v1/volumes?q=";
                String searchString = searchitem.getText().toString();
                searchString = searchString.trim();
                searchString = searchString.replace(" ", "+");
                URL_RESULT += searchString + "&key=AIzaSyABnoJx-Y-ujWseCd9Kxk33eGRWda7B7Ic&maxResults=10";
                BookAsyncTask task = new BookAsyncTask();
                ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    task.execute();
                }

            }
        });
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<item>>{

        @Override
        protected ArrayList<item> doInBackground(URL... urls) {
            URL url=createUrl(URL_RESULT);
            String jsonResponse="";
            try{
                jsonResponse =makeHttpRequest(url);
            }catch (IOException e){
                Log.e("MainActivity.java","IO error");
            }
            ArrayList<item> items;

            items=extractFeatureFromJson(jsonResponse);

            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<item> items){
            if (items == null) {
                return;
            }

            itemadapter adapter = new itemadapter(MainActivity.this, items);

            ListView bookList = (ListView) findViewById(R.id.list);

            bookList.setAdapter(adapter);

            TextView prompt = (TextView) findViewById(R.id.prompt);
            if (prompt.getVisibility() == View.VISIBLE) {
                prompt.setVisibility(View.GONE);
            }
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e("MainActivity.java", "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("MainActivity.java", "The HTTP response code was not 200: " + urlConnection.getResponseCode());
                    return "";
                }
            } catch (IOException e) {
                Log.e("MainActivity.java", "Something went wrong with the IO");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private ArrayList<item> extractFeatureFromJson(String bookJSON){

            try{
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");
                ArrayList<item> bookList = new ArrayList<item>();

                for(int i=0;i<itemsArray.length();i++)
                {
                    JSONObject current=itemsArray.getJSONObject(i);
                    JSONObject volumeInfo=current.getJSONObject("volumeInfo");
                    String bookTitle = volumeInfo.getString("title");
                    String bookAuthors = "";
                    if (volumeInfo.has("authors")) {
                        JSONArray authors = volumeInfo.getJSONArray("authors");
                        // creates a list of the authors as a string
                        bookAuthors = authors.join(", ") + ".";
                        bookAuthors = bookAuthors.replaceAll("\"", "");
                    }
                    bookList.add(new item(bookTitle, bookAuthors));
                }
                return bookList;
            }catch(JSONException e)
            {
                Log.e("MainActivity.java", "Problem parsing the book JSON results lower block", e);
            }

            return null;
        }
    }

}
