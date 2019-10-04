package com.example.books;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private ProgressBar mLoadingProgress;
    private RecyclerView rvBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mLoadingProgress = (ProgressBar) findViewById(R.id.pb_loading);
        setSupportActionBar(toolbar);
        rvBooks = (RecyclerView) findViewById(R.id.rv_books);
        LinearLayoutManager booksLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvBooks.setLayoutManager(booksLayoutManager);
        Intent intent = getIntent ();
        String query  = intent.getStringExtra ("Query");
        URL bookUrl;

        try {
            if (query==null || query.isEmpty ()) {
                bookUrl = apiUtil.buildUrl ("cooking");
            }else{
                bookUrl  = new URL(query);
            }
           new BooksQueryTask().execute(bookUrl);


        }
        catch(Exception e){
            Log.e("Error",e.getMessage());

        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.book_list_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        ArrayList<String> recentList = SpUtil.getQueryList (getApplicationContext ());
        int itemNum = recentList.size ();
        MenuItem recentMenu;
        for(int i = 0;i<itemNum; i++){
            recentMenu = menu.add (Menu.NONE, i, Menu.NONE, recentList.get (i));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId ()) {
            case R.id.action_advanced_search:
                Intent intent = new Intent (this, SearchActivity.class);
                startActivity (intent);
                return true;

            default:
                int position  = item.getItemId () + 1;
                String preferenceNmae = SpUtil.Query + String.valueOf (position) ;
                String query  = SpUtil.getPreferenceString (getApplicationContext (),preferenceNmae);
                String[] prefParams = query.split ("\\,");
                String[] queryParams = new String[4];
                for(int i=0; i<prefParams.length;i++) {
                    queryParams[i] = prefParams[i];

            }
                URL bookUrl = apiUtil.buildUrl(
                        (queryParams[0]==null)?"":queryParams[0],
                        (queryParams[1]==null)?"":queryParams[1],
                        (queryParams[2]==null)?"":queryParams[2],
                        (queryParams[3]==null)?"":queryParams[3]);
                return super.onOptionsItemSelected (item);

        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        try{
            URL bookUrl = apiUtil.buildUrl(query);
            new BooksQueryTask ().execute (bookUrl);


        }catch (Exception e){
            Log.d("error",e.getMessage());
        }

        return false;

        }





    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public class BooksQueryTask extends AsyncTask<URL,Void, String>{


        @Override
        protected String doInBackground(URL... urls) {
            URL searchURL = urls[0];
            String result = null;
            try {
                result = apiUtil.getJson(searchURL);
            } catch(IOException e) {
                Log.e("Error",e.getMessage());
            }
            return result;

        }
        @Override
        protected void onPostExecute(String result){
            TextView tvError = (TextView) findViewById(R.id.tv_error);
            mLoadingProgress.setVisibility(View.INVISIBLE);
            if(result==null){
                rvBooks.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.VISIBLE);
            }
            else {
                rvBooks.setVisibility (View.VISIBLE);
                tvError.setVisibility (View.INVISIBLE);
                ArrayList<Book> books = apiUtil.getBooksFromJson (result);
                String resultString = "";
                BooksAdapter adapter = new BooksAdapter (books);
                rvBooks.setAdapter (adapter);
            }

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgress.setVisibility(View.VISIBLE);
        }
    }
}
