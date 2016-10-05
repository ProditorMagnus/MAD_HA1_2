package ee.ut.mad_ha1_2;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/*
 * Initial code from http://www.androidhive.info/2012/09/android-adding-search-functionality-to-listview/
 * http://stackoverflow.com/questions/29893534/how-to-add-search-button-on-toolbar-in-material-design
 * http://stackoverflow.com/questions/9208827/how-to-extract-the-text-from-the-selected-item-on-the-listview
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Rav";
    public static final int CONTACT_QUERY_LOADER = 0;
    public static final String QUERY_KEY = "query";
    public static final String SELECTED_NAME = "selected_name";
    public static final String SEARCH_TEXT = "search_text";
    private String mSearchText = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String value = ((TextView) findViewById(R.id.selectedContact)).getText().toString();
        outState.putString(SELECTED_NAME, value);
        outState.putString(SEARCH_TEXT, mSearchText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ((TextView) findViewById(R.id.selectedContact)).setText(savedInstanceState.getString(SELECTED_NAME));
        Log.v(TAG, "changing textview back to " + savedInstanceState.getString(SELECTED_NAME));
        mSearchText = savedInstanceState.getString(SEARCH_TEXT);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // Special processing of the incoming intent only occurs if the if the action specified
        // by the intent is ACTION_SEARCH.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // SearchManager.QUERY is the key that a SearchManager will use to send a query string
            // to an Activity.
            String query = intent.getStringExtra(SearchManager.QUERY);

            // We need to create a bundle containing the query string to send along to the
            // LoaderManager, which will be handling querying the database and returning results.
            Bundle bundle = new Bundle();
            bundle.putString(QUERY_KEY, query);

            ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);

            // Start the loader with the new query, and an object that will handle all callbacks.
            getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);
        }
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Bundle bundle = new Bundle();
            bundle.putString(QUERY_KEY, "");

            ContactablesLoaderCallbacks loaderCallbacks = new ContactablesLoaderCallbacks(this);

            getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolmenu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQuery(mSearchText, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchText = "";
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchText = newText;
                return false;
            }
        });
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    public void selectedContactClicked(View view) {
        final String value = ((TextView) findViewById(R.id.selectedContact)).getText().toString();
        if (value.equals(getString(R.string.nothing_selected_yet))) {
            Log.v(TAG, "contact clicked with empty selection");
            return;
        }
        final ContactLoaderCallbacks loaderCallbacks = new ContactLoaderCallbacks(this);
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_KEY, value);

        getLoaderManager().restartLoader(CONTACT_QUERY_LOADER, bundle, loaderCallbacks);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // gmail result
                Log.v("Rav_", "Ok");
                TextView textView = (TextView) findViewById(R.id.selectedContact);
                textView.setText(String.format("Sent email to %s", textView.getText()));
                setResult(Activity.RESULT_OK);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // while task description said this means success, testing (on phone) showed that this is not the case
                Log.v("Rav_", "Canceled");
                TextView textView = (TextView) findViewById(R.id.selectedContact);
                /// once again, from how emulator does it
                textView.setText(String.format("Possibly sent email to %s", textView.getText()));
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    public void clearSearch(MenuItem item) {
        mSearchText = "";
        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        searchView.setQuery("%", true);
    }
}