package com.udacity.richardrose.p2_aad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.udacity.richardrose.p2_aad.DataHandler.Media;
import com.udacity.richardrose.p2_aad.Util.GridAutofitLayoutManager;
import com.udacity.richardrose.p2_aad.Util.sqliteMediaDB;
import com.udacity.richardrose.p2_aad.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.udacity.richardrose.p2_aad.R.drawable.movie_placeholder;

/**
 * An activity representing a list of Media. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MediumDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MediumListActivity extends AppCompatActivity {


    private static final String TAG_NAME            = MediumListActivity.class.getSimpleName();
    private static final String API_KEY             = BuildConfig.TMDB_API_KEY;

    private MediumListRecyclerViewAdapter   mMovieAdapter           = null;
    private ArrayList<Media>                mMediaInformation       = null;
    private String                          mSortOrder              = null;
    private Media                           mMedium;

    // TODO: Change hardwire screen density
    private String                          mScreenDensity          = null;
    private int                             mScreenColumn           = 0;

    final String MOVIE_IMAGE_URI = "http://image.tmdb.org/t/p/";


    // Screen density settings
    private static final int DENSITY_280 = 280;
    private static final int DENSITY_480 = 480;
    private static final int DENSITY_570 = 570;

    private static final int DENSITY_185 = 185;
    private static final int DENSITY_342 = 342;
    private static final int DENSITY_500 = 500;
    private static final int DENSITY_780 = 780;

    private ImageButton mTestMediumFavourite;

    sqliteMediaDB databaseMedia;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medium_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        // Access the database
        databaseMedia = new sqliteMediaDB( this);

        // Allocate memory for the media download
        if (mMediaInformation == null) {
            mMediaInformation = new ArrayList<>();
        }


        // Set the default sort order
        mSortOrder = getResources().getString(R.string.media_popular);

        // Check the screen density for a rough guide to image size
        getScreenDensity();

        View recyclerView = findViewById(R.id.medium_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);


        if (findViewById(R.id.medium_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }


        // Check network/internet status
        if (getOnlineStatus()) {
            // Call to populate the film information
            onRequestMovieAPI();
        }
        else {
            // No network connection currently available
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.media_network_connection), Toast.LENGTH_SHORT).show();
        }

    }

    MediumDetailFragment.OnButtonSelectedListener mListener;


    public void buttonOn() {
        Toast.makeText(getApplicationContext(),
            "Button on", Toast.LENGTH_SHORT).show();

        mTestMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite));
        // Call database interface - add Media information
        Double tempRating = mMedium.getRating();
        databaseMedia.addMediaRow(mMedium.getID(), mMedium.getTitle(), mMedium.getThumbnail(), tempRating.toString());

        return ;
    }

    public void buttonOff() {
        Toast.makeText(getApplicationContext(),
                "Button off", Toast.LENGTH_SHORT).show();

        mTestMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_border));

        // Call database interface - delete media ID
        databaseMedia.deleteMedia(mMedium.getID());

        return ;
    }

    /*
     * Name: onCreateOptionsMenu
     * @return boolean
     * Description: Display settings menu
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.most_popular:
                mSortOrder = getResources().getString(R.string.media_popular);;
                // Call to populate the film information
                onRequestMovieAPI();
                return true;

            case R.id.highest_rated:
                mSortOrder = getResources().getString(R.string.media_top_rated);;
                // Call to populate the film information
                onRequestMovieAPI();
                return true;

            case R.id.show_favourites:
                // Show the items in the database

//                mSortOrder = getResources().getString(R.string.media_top_rated);;
                // Call to populate the film information
                onRequestDBAPI();
                return true;


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }




    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        // Autosize the grid layout
        recyclerView.setLayoutManager(new GridAutofitLayoutManager(getApplicationContext(), mScreenColumn));

        mMovieAdapter = new MediumListRecyclerViewAdapter(mMediaInformation);
        recyclerView.setAdapter(mMovieAdapter);
    }

        /*
     * Name: getOnlineStatus
     * @return boolean - flag to indicate network status
     *  False:  Offline
     *  True:   Online
     * Description: Check on the device network status
     * Comment: Standard method on which to check the network availability
     *          Ensure required permissions have been added to Android.Manifest
     */

    public boolean getOnlineStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    public void getScreenDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int density = metrics.densityDpi;

        if (density <= DENSITY_280) {
            mScreenDensity = getResources().getString(R.string.media_density_185);
            mScreenColumn = DENSITY_185;
        }

        else if (density <= DENSITY_480) {
            mScreenDensity = getResources().getString(R.string.media_density_342);
            mScreenColumn = DENSITY_342;
        }
        else if (density <= DENSITY_570) {

            mScreenDensity = getResources().getString(R.string.media_density_500);
            mScreenColumn =  DENSITY_500;
        }
        else {
            mScreenDensity = getResources().getString(R.string.media_density_780);
            mScreenColumn = DENSITY_780;
        }

        return;
    }


    private void onRequestDBAPI() {
        // TODO: Clear existing information
        mMediaInformation.clear();

        // TODO: Loop through the DB, for each item add to the mediaInformation structure

        databaseMedia.getAllMedia(mMediaInformation);

        // TODO: Notify a data set change
        mMovieAdapter.notifyDataSetChanged();
    }



    /*
     * Name: onRequestMovieAPI
     * Comment: Access MovieAPI using the Volley library
     * Tasks:
     * 1. Check Network/Internet available
     * 2. Find "results" object and store it in an JSONArray
     * 3. Iterate through the JSONArray
     * 4.   Read item contents and add to the movie array
     * 5. Send a dataset change notification to update the Adapter view
     * 6. Handle Exceptions
     *
     */

    private void onRequestMovieAPI() {
        // TODO: Add the relevant mFilmAPI Query
        final String MOVIE_API_URI = "http://api.themoviedb.org/3/movie/" ;

        // TODO: Add a valid API KEY
        final String MOVIE_API_KEY = "?api_key=" + API_KEY;

        // TODO: sort_by=popularity.desc/popular
        // TODO: sort_by=vote_average.desc/top_rated

        // TODO: Alter setting - "w92", "w154", "w185", "w342", "w500", "w780", or "original".
        // TODO: Add a setting to allow the user to select image size

        final JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, MOVIE_API_URI+mSortOrder+MOVIE_API_KEY,
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // TODO: Get the response array
                            JSONArray jsonArray = response.getJSONArray("results");
                            String id;
                            String title;
                            double  rating;
                            String poster_uri;

                            // TODO: Clear existing information
                            mMediaInformation.clear();

                            // TODO: Loop through the array
                            for (int i=0; i < jsonArray.length(); i++) {
                                // TODO: Get the movie object
                                JSONObject movie = jsonArray.getJSONObject(i);

                                // Get the required details: ID + poster_path
                                id = movie.getString("id");
                                title = movie.getString("title");
                                rating = movie.getDouble("vote_average");
                                poster_uri = movie.getString("poster_path");

                                // TODO: Add to movie structure
                                mMediaInformation.add(new Media(id,
                                        MOVIE_IMAGE_URI+mScreenDensity+poster_uri, title, rating));
                            }

                            // TODO: Notify a data set change
                            mMovieAdapter.notifyDataSetChanged();

                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.i("JSON", error.getMessage());
                    }
                });

        // Queue the async request
        Volley.newRequestQueue(getApplicationContext()).add(mJsonObjectRequest);
    }


    public class MediumListRecyclerViewAdapter
            extends RecyclerView.Adapter<MediumListRecyclerViewAdapter.ViewHolder> {

        private final ArrayList<Media>  mMedia;

        public MediumListRecyclerViewAdapter(ArrayList<Media> media) {
            mMedia = media;
        }

        @Override
        public MediumListActivity.MediumListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.medium_list_content, parent, false);

            return new MediumListActivity.MediumListRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MediumListActivity.MediumListRecyclerViewAdapter.ViewHolder holder, int position) {
            SharedPreferences           sharedPreferences;
            Boolean checkFavouriteSetting = false;

            holder.mMediumTitle.setText(mMedia.get(position).getTitle());
            holder.mMediumRating.setText("Rating: " + String.valueOf(mMedia.get(position).getRating()));

            Picasso.with(getApplicationContext())
                    .load(mMedia.get(position).getThumbnail())
                    .placeholder(movie_placeholder)
                    .into(holder.mMediumPoster);

            // Check SharedPref for setting
            sharedPreferences = getSharedPreferences("P2_AAD", Context.MODE_PRIVATE);

//            editor = sharedPref.edit();
            checkFavouriteSetting = sharedPreferences.getBoolean(mMedia.get(position).getID(), checkFavouriteSetting);
//            favouriteSetting = (favouriteSetting==true) ? false:true;
//            editor.commit();

            if (checkFavouriteSetting)
                holder.mMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite));
            else
                holder.mMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_border));


            // Initiate a click listener for each grid view item
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Media Medium =  mMediaInformation.get(holder.getLayoutPosition());

                    mMedium =  mMediaInformation.get(holder.getLayoutPosition());

                    // Store the reference to the appropriate button
                    mTestMediumFavourite = holder.mMediumFavourite;


                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(MediumDetailFragment.ARG_MEDIUM_ID, mMedium.getID());
                        arguments.putString(MediumDetailFragment.ARG_MEDIUM_TITLE, holder.mMediumTitle.getText().toString());

                        MediumDetailFragment fragment = new MediumDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.medium_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MediumDetailActivity.class);

                        intent.putExtra(MediumDetailFragment.ARG_MEDIUM_ID, mMedium.getID());
                        intent.putExtra(MediumDetailFragment.ARG_MEDIUM_TITLE, holder.mMediumTitle.getText().toString());
                        context.startActivity(intent);
                    }
                }
            });

//            // Ensure the favourite button triggers an event
//            holder.mMediumFavourite.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    SharedPreferences           sharedPref;
//                    SharedPreferences.Editor    editor;
//
//                    Media Medium =  mMediaInformation.get(holder.getLayoutPosition());
//
////                    // TODO: Add/Remove id to the DB
//                    Boolean favouriteSetting=false;
////
//                    sharedPref = getSharedPreferences("P2_AAD", Context.MODE_PRIVATE);
//
//                    editor = sharedPref.edit();
//                    favouriteSetting = sharedPref.getBoolean(Medium.getID(), favouriteSetting);
//                    favouriteSetting = (favouriteSetting==true) ? false:true;
//                    editor.putBoolean(Medium.getID(), favouriteSetting);
//
////                    // TODO: Save the changes
//                    editor.commit();
//
////                    // TODO: Toggle the button
//                    if (favouriteSetting) {
//                        // Add a favourite
//                        holder.mMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite));
//
//                        Double rating = Medium.getRating();
//
//                        // Call database interface - add Media information
//                        databaseMedia.addMediaRow(Medium.getID(), Medium.getTitle(), Medium.getThumbnail(), rating.toString());
//                    }
//                    else {
//                        // Delete a favourite
//                        holder.mMediumFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_border));
//
//                        // Call database interface - delete media ID
//                        databaseMedia.deleteMedia(Medium.getID());
//                    }
//
//                    // TODO: Show snackbar message on save/unsave
//                    Snackbar.make(v, "Pressed favourite button",
//                            Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return mMedia.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View       mView;
            public final TextView   mMediumTitle;
            public final TextView   mMediumRating;
            public final ImageView  mMediumPoster;
            public final ImageButton  mMediumFavourite;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mMediumTitle    = (TextView) view.findViewById(R.id.media_title);
                mMediumRating   = (TextView) view.findViewById(R.id.media_rating);
                mMediumPoster   = (ImageView) view.findViewById(R.id.media_image);
                mMediumFavourite = (ImageButton) view.findViewById(R.id.media_fav);
                mTestMediumFavourite = (ImageButton) view.findViewById(R.id.media_fav);
            }

        }
    }
}
