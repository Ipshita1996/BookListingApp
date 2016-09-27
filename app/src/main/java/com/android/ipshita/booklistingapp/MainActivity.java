package com.android.ipshita.booklistingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** URL to query the Google Books API information */
    private static final String BOOKS_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes?q=java&key=AIzaSyABnoJx-Y-ujWseCd9Kxk33eGRWda7B7Ic&maxResults=10&country=IN";

    ArrayList<item> items=new ArrayList<item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText searchitem=(EditText)findViewById(R.id.search);
        Button searchButton=(Button)findViewById(R.id.search_button);
        assert searchButton != null;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTaskBook book = new AsyncTaskBook();
                String searching = BOOKS_REQUEST_URL+searchitem.toString();
                book.execute(searching);
                Toast toast=Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT);
                toast.show();


            }
        });

        itemadapter itemsAdapter = new itemadapter(this,items);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(itemsAdapter);
    }

    private void updateUi(item book) {
        items.add(book);
    }



    private class AsyncTaskBook extends AsyncTask<String, Void, item> {

        @Override
        protected item doInBackground(String... params) {
            // Create URL object
            URL url = createUrl(BOOKS_REQUEST_URL);
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG,"IO ERROR!",e);
            }
            // Extract relevant fields from the JSON response and create an {@link Event} object
            item book = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return book;
        }

        @Override
        protected void onPostExecute(item book) {
            if (book == null) {
                return;
            }

            updateUi(book);
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if(url==null){
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

                if(urlConnection.getResponseCode()==200){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
                else{
                    Log.e(LOG_TAG, "Problem parsing the JSON results");
                }
            } catch (IOException e) {
                // TODO: Handle the exception
            }
            finally {
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
        private item extractFeatureFromJson(String bookJSON) {

            if(TextUtils.isEmpty(bookJSON)){
                return null;
            }
            try{

                String authors = null;
                JSONObject root=new JSONObject(bookJSON);
                JSONArray items1=root.getJSONArray("items");
                for(int i=0;i<items1.length();i++)
                {
                    JSONObject book=items1.getJSONObject(i);
                    JSONObject volumeInfo=book.getJSONObject("volumeInfo");
                    String title=volumeInfo.getString("title");
                    JSONArray jsonArrayauthor=book.getJSONArray("authors");
                    for(int j=0;j<jsonArrayauthor.length();j++)
                    {
                        JSONObject author=jsonArrayauthor.getJSONObject(j);
                        authors=author.getString("authors");
                    }

                    if(bookJSON.contains(title)||bookJSON.contains(authors)){
                        return new item(title,authors);
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(),"No book found",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }catch(JSONException e){
                Log.e(LOG_TAG, "Problem parsing the JSON results lower block");
                e.printStackTrace();
            }
            return null;
        }
    }
}
