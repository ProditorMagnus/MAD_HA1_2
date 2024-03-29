package ee.ut.mad_ha1_2;

/*
 * https://github.com/googlesamples/android-BasicContactables
 */

import android.content.Intent;
import android.database.Cursor;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper class to handle all the callbacks that occur when interacting with loaders.  Most of the
 * interesting code in this sample app will be in this file.
 */
public class ContactablesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    Context mContext;

    public static final String QUERY_KEY = "query";

    public static final String TAG = "Rav_Con";

    public ContactablesLoaderCallbacks(Context context) {
        mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderIndex, Bundle args) {
        // Where the Contactables table excels is matching text queries,
        // not just data dumps from Contacts db.  One search term is used to query
        // display name, email address and phone number.  In this case, the query was extracted
        // from an incoming intent in the handleIntent() method, via the
        // intent.getStringExtra() method.
        Log.v(TAG, "loader created");
        // BEGIN_INCLUDE(uri_with_query)
        String query = args.getString(QUERY_KEY);
//        Uri uri = Uri.withAppendedPath(
//                CommonDataKinds.Contactables.CONTENT_FILTER_URI, query);
//        Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "contactables");
        Uri uri = ContactsContract.Data.CONTENT_URI;
//        uri = Uri.withAppendedPath(uri, "filter");
//        uri = Uri.withAppendedPath(uri, query);
        // END_INCLUDE(uri_with_query)


        // BEGIN_INCLUDE(cursor_loader)
        // Easy way to limit the query to contacts with phone numbers.
        String selection = CommonDataKinds.Contactables.HAS_PHONE_NUMBER + " = " + 1;
        selection = CommonDataKinds.Contactables.DISPLAY_NAME + " LIKE ?";

        String[] selectionArgs = {"%" + query + "%"};

        // Sort results such that rows for the same contact stay together.
        String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;

        return new CursorLoader(
                mContext,  // Context
                uri,       // URI representing the table/resource to be queried
                null,      // projection - the list of columns to return.  Null means "all"
                selection, // selection - Which rows to return (condition rows must match)
                selectionArgs,      // selection args - can be provided separately and subbed into selection.
                sortBy);   // string specifying sort order
        // END_INCLUDE(cursor_loader)
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        final ListView lv = (ListView) ((Activity) mContext).findViewById(R.id.list_view);
        List<String> contactList = new ArrayList<>();
        Log.v(TAG, "load finished");
        if (cursor.getCount() == 0) {
            Log.v(TAG, "cursor count 0");
            Toast.makeText(mContext, "No results.", Toast.LENGTH_LONG).show();
            return;
        }

        // Pulling the relevant value from the cursor requires knowing the column index to pull
        // it from.
        // BEGIN_INCLUDE(get_columns)
        int phoneColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
        int emailColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
        int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
        int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);
        int typeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.MIMETYPE);
        // END_INCLUDE(get_columns)

        cursor.moveToFirst();
        // Lookup key is the easiest way to verify a row of data is for the same
        // contact as the previous row.
        String lookupKey = "";
        do {
            // BEGIN_INCLUDE(lookup_key)
            String currentLookupKey = cursor.getString(lookupColumnIndex);
            if (!lookupKey.equals(currentLookupKey)) {
                String displayName = cursor.getString(nameColumnIndex);
//                tv.append(displayName + "\n");
                contactList.add(displayName);
                Log.v(TAG, displayName);
                lookupKey = currentLookupKey;
            }
            // END_INCLUDE(lookup_key)

            // BEGIN_INCLUDE(retrieve_data)
            // The data type can be determined using the mime type column.
            String mimeType = cursor.getString(typeColumnIndex);
            if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                Log.v(TAG, "Phone Number: " + cursor.getString(phoneColumnIndex));
            } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                Log.v(TAG, "Email Address: " + cursor.getString(emailColumnIndex));
            }
            // END_INCLUDE(retrieve_data)

            // Look at DDMS to see all the columns returned by a query to Contactables.
            // Behold, the firehose!
//            for (String column : cursor.getColumnNames()) {
//                String string = cursor.getString(cursor.getColumnIndex(column));
//                if (string == null) continue;
//                Log.d(TAG, column + column + ": " +
//                        string + "\n");
//            }
        } while (cursor.moveToNext());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_item, R.id.product_name, contactList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = lv.getItemAtPosition(position).toString();
                TextView textView = (TextView) ((Activity) mContext).findViewById(R.id.selectedContact);
                textView.setText(s);
                Log.v(TAG, s);

            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
