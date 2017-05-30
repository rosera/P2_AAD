package com.udacity.richardrose.p2_aad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.udacity.richardrose.p2_aad.dummy.DummyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.udacity.richardrose.p2_aad.BuildConfig.TMDB_API_KEY;
import static com.udacity.richardrose.p2_aad.R.drawable.movie_placeholder;
import static java.lang.Boolean.FALSE;

/**
 * A fragment representing a single Medium detail screen.
 * This fragment is either contained in a {@link MediumListActivity}
 * in two-pane mode (on tablets) or a {@link MediumDetailActivity}
 * on handsets.
 */
public class MediumDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID          = "item_id";
    private static final String API_KEY             = TMDB_API_KEY;

    // TODO: Pass arguments across from Medium List Activity
    public static final String ARG_MEDIUM_ID        = "medium_id";
    public static final String ARG_MEDIUM_TITLE     = "medium_title";

    private static final int TMDB_MOVIES    = 0;
    private static final int TMDB_TRAILERS  = 1;
    private static final int TMDB_REVIEWS   = 2;
    private static final int TMDB_SIMILAR   = 3;

    private static View mRootView;

    private String                          mScreenDensity          = null;
    private int                             mScreenColumn           = 0;

    // Screen density settings
    private static final int DENSITY_280 = 280;
    private static final int DENSITY_480 = 480;
    private static final int DENSITY_570 = 570;

    private static final int DENSITY_185 = 185;
    private static final int DENSITY_342 = 342;
    private static final int DENSITY_500 = 500;
    private static final int DENSITY_780 = 780;

    private ArrayList<Media> mTrailerInformation    = null;
    private ArrayList<Media> mSimilarInformation    = null;

    private MediumDetailRecyclerViewAdapter    mTrailerAdapter = null;
    private MediumDetailRecyclerViewAdapter    mSimilarAdapter = null;

    private FloatingActionButton mFabFavourite;

    RecyclerView mRecyclerViewTrailer;
    RecyclerView mRecyclerViewSimilar;

    private String mID;
    private String mTitle;
    private String mRating;
    private String mPoster      = "";
    private String mYear        = "";
    private String mRuntime     = "";
    private String mThumbnail   = "";
    private String mOverview    = "";

    private SharedPreferences sharedPref;
    SharedPreferences.Editor    editor;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MediumDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Todo: Add other parameters
        if (getArguments().containsKey(ARG_MEDIUM_TITLE)) {
            mTitle = getArguments().getString(ARG_MEDIUM_TITLE);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mTitle);
            }
        }

        if (getArguments().containsKey(ARG_MEDIUM_ID)) {
            // TODO: Add ID to search against the Film details
            mID = getArguments().getString(ARG_MEDIUM_ID);

        }

        // Allocate memory for the trailer thumbnail
        if (mTrailerInformation == null)
            mTrailerInformation = new ArrayList<Media>();

        // Allocate memory for the similar movies thumbnail
        if (mSimilarInformation == null)
            mSimilarInformation = new ArrayList<Media>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.medium_detail, container, false);
        mRootView = inflater.inflate(R.layout.medium_detail, container, false);

        // Add RecyclerView for trailers
        mRecyclerViewTrailer = (RecyclerView) mRootView.findViewById(R.id.medium_detail_trailer);
        assert mRecyclerViewTrailer != null;
        setupRecyclerViewTrailer(mRecyclerViewTrailer);

        // Add RecyclerView for similar media
        mRecyclerViewSimilar = (RecyclerView) mRootView.findViewById(R.id.medium_detail_similar);
        assert mRecyclerViewSimilar != null;
        setupRecyclerViewSimilar(mRecyclerViewSimilar);

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) mRootView.findViewById(R.id.medium_detail_coordinatorlayout);
//        FloatingActionButton fab = (FloatingActionButton) mRootView.findViewById(R.id.medium_detail_fab);
        mFabFavourite = (FloatingActionButton) mRootView.findViewById(R.id.medium_detail_fab);
        mFabFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: Add/Remove id to the DB
                Boolean favouriteSetting = false;

                sharedPref = getContext().getSharedPreferences("P2_AAD", Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                favouriteSetting = sharedPref.getBoolean(mID, favouriteSetting);
                favouriteSetting = (favouriteSetting==true)? false:true;
                editor.putBoolean(mID, favouriteSetting);

                // TODO: Save the changes
                editor.commit();

                // TODO: Toggle the button
                if (favouriteSetting) {

                    try {
                        ((MediumListActivity) getActivity()).buttonOn();
                    } catch (ClassCastException ex) {
                        ex.printStackTrace();
                    }
                    mFabFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite));

                }
                else {
                    try {
                        ((MediumListActivity) getActivity()).buttonOff();
                    } catch (ClassCastException ex) {
                        ex.printStackTrace();
                    }
                    mFabFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_border));
                }

                // TODO: Show snackbar message on save/unsave
                Snackbar.make(coordinatorLayout, mTitle + " set to: " + favouriteSetting.toString(),
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        // Check the screen density for a rough guide to image size
        getScreenDensity();

        // Initiate the call to the TMDB
        onRequestMovieAPI(mID, TMDB_TRAILERS);

        onRequestMovieSimilar(mID, TMDB_SIMILAR);

        return mRootView;
    }

//    public void pushTheButton(boolean flag) {
//
//        if (flag) {
//            buttonInterface.buttonOn();
//            getContext().
//        }
//        else {
//            buttonInterface.buttonOff();
//        }
//    }


    public interface OnButtonSelectedListener {
        public void onButtonSelected(boolean flag);
    }

    /*
     * @Name: getScreenDensity
     * @return void
     *      "w92", "w154", "w185", "w342", "w500", "w780", or "original".
     * @Description: Check the screen density
     */


    public void getScreenDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

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

    private void setupRecyclerViewTrailer(@NonNull RecyclerView recyclerView) {

        // Autosize the grid layout - initial setting 342
        recyclerView.setLayoutManager(new GridAutofitLayoutManager(getActivity(), mScreenColumn, LinearLayoutManager.HORIZONTAL, FALSE));
        mTrailerAdapter = new MediumDetailFragment.MediumDetailRecyclerViewAdapter(mTrailerInformation);
    }

    private void setupRecyclerViewSimilar(@NonNull RecyclerView recyclerView) {

        // Autosize the grid layout - initial setting 240
        recyclerView.setLayoutManager(new GridAutofitLayoutManager(getActivity(), mScreenColumn, LinearLayoutManager.HORIZONTAL, FALSE));
        mSimilarAdapter = new MediumDetailFragment.MediumDetailRecyclerViewAdapter(mSimilarInformation);
    }



      /*
     * Name: onRequestMovieAPI
     * Comment: Access MovieAPI using the Volley library
     * Tasks:
     * 1. Check Network/Internet available
     * 2. Find "results" object and store it in an JSONArray
     * 3. Iterate through the JSONArray
     * 4.   Read item contents and add to the movie array
     * 5. Send a data set change notification to update the Adapter view
     * 6. Handle Exceptions
     *
     */

    private void onRequestMovieAPI(String mID, int searchType) {
        // TODO: Stage 2: mFilmAPI Query
        final String MOVIE_API_URI = getResources().getString(R.string.medium_tmdb_api);
        final String MOVIE_API_KEY = "?api_key=" + TMDB_API_KEY;
        final String MOVIE_API_VIDEO = "&append_to_response=videos";
        final String MOVIE_API_REVIEWS = "/reviews";
        final String MOVIE_API_SIMILAR = "/similar";
        String strQueryTMDB = "";


        // TODO: Make this a method
        switch (searchType){
            case TMDB_MOVIES: // Movie
                // Get the Sort Order preference

                // Store the api Query
//                strQueryTMDB =  MOVIE_API_URI+mSortOrder+MOVIE_API_KEY;
                break;
            case TMDB_TRAILERS: // Trailers
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_KEY+MOVIE_API_VIDEO;
                break;

            case TMDB_REVIEWS: // Reviews
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_REVIEWS+MOVIE_API_KEY;
                break;

            case TMDB_SIMILAR: // Similar
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_SIMILAR+MOVIE_API_KEY;
                break;

            default:
                break;
        }

        // TODO: sort_by=popularity.desc/popular
        // TODO: sort_by=vote_average.desc/top_rated

        // TODO: Alter setting - "w92", "w154", "w185", "w342", "w500", "w780", or "original".
        // TODO: Add a setting to allow the user to select image size
        final String MOVIE_IMAGE_URI = getResources().getString(R.string.medium_image_uri);
    /*
     *  JSON Request - Volley JSON example
     */

        // TODO: Request movie information based on ID passed from MovieFragment

        final JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, strQueryTMDB, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            // Remove the contents
//                            mYouTubeTrailers.clear();

                            // TODO: Make this a method
                            JSONObject videos = response.getJSONObject("videos");
                            JSONArray jsonArray = videos.getJSONArray("results");

                            // TODO: Allocate memory based on number of available trailers

                            // IF Videos are required
//
                            // TODO: Change these variables to local
                            mTitle = response.getString("original_title");
                            mYear = response.getString("release_date");
                            mOverview = response.getString("overview");
                            mRuntime = response.getString("runtime") + " mins";
                            mThumbnail = MOVIE_IMAGE_URI + mScreenDensity + response.getString("poster_path");
                            mRating = response.getString("vote_average") + "/10";

                            for (int i=0; i<jsonArray.length(); i++) {
                                JSONObject video = jsonArray.getJSONObject(i);

                                // TODO: Make an array list of trailer items
                                mTrailerInformation.add(new Media(video.getString("key"),
                                        "http://img.youtube.com/vi/" + video.getString("key") + "/0.jpg", video.getString("name"), 0));
                            }

                            // TODO: Recyclerview create
                            mRecyclerViewTrailer.setAdapter(mTrailerAdapter);

                            // TODO: populate the detail fragment
                            populateMovieDetails();

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
        Volley.newRequestQueue(getActivity()).add(mJsonObjectRequest);
    }

    private void onRequestMovieSimilar(String mID, int searchType) {
        // TODO: Stage 2: mFilmAPI Query
        final String MOVIE_API_URI = getResources().getString(R.string.medium_tmdb_api);
        final String MOVIE_API_KEY = "?api_key=" + TMDB_API_KEY;
        final String MOVIE_API_VIDEO = "&append_to_response=videos";
        final String MOVIE_API_REVIEWS = "/reviews";
        final String MOVIE_API_SIMILAR = "/similar";
        String strQueryTMDB = "";

        // TODO: Make this a method
        switch (searchType){
            case TMDB_MOVIES: // Movie
                // Get the Sort Order preference

                // Store the api Query
//                strQueryTMDB =  MOVIE_API_URI+mSortOrder+MOVIE_API_KEY;
                break;
            case TMDB_TRAILERS: // Trailers
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_KEY+MOVIE_API_VIDEO;
                break;

            case TMDB_REVIEWS: // Reviews
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_REVIEWS+MOVIE_API_KEY;
                break;

            case TMDB_SIMILAR: // Similar
                strQueryTMDB = MOVIE_API_URI+mID+MOVIE_API_SIMILAR+MOVIE_API_KEY;
                break;

            default:
                break;
        }

        // TODO: sort_by=popularity.desc/popular
        // TODO: sort_by=vote_average.desc/top_rated

        // TODO: Alter setting - "w92", "w154", "w185", "w342", "w500", "w780", or "original".
        // TODO: Add a setting to allow the user to select image size
        final String MOVIE_IMAGE_URI = getResources().getString(R.string.medium_image_uri);

    /*
     *  JSON Request - Volley JSON example
     */

        // TODO: Request movie information based on ID passed from MovieFragment

        final JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, strQueryTMDB, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            // TODO: Make this a method
                            JSONArray jsonArray = response.getJSONArray("results");

                            // Only get the first four images
                            for (int i=0; i<jsonArray.length(); i++) {
                                JSONObject video = jsonArray.getJSONObject(i);

                                // TODO: Make an array list of similar movie items
                                mSimilarInformation.add(new Media(video.getString("id"),
                                        MOVIE_IMAGE_URI + mScreenDensity + video.getString("poster_path"), video.getString("title"), 0));
                            }

                            // TODO: Recyclerview create
                            mRecyclerViewSimilar.setAdapter(mSimilarAdapter);

                            // TODO: populate the detail fragment
//                            populateSimilarDetails();

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
        Volley.newRequestQueue(getActivity()).add(mJsonObjectRequest);
    }


   /*
    * Populate the onscreen controls once the data has been downloaded
    */
    private void populateMovieDetails() {

        View rootView = getActivity().findViewById(android.R.id.content);

        // Get reference to the UI controls
        ((TextView) mRootView.findViewById(R.id.medium_detail_title)).setText(mTitle);
        ((TextView) mRootView.findViewById(R.id.medium_detail_releasedt)).setText(mYear);
        ((TextView) mRootView.findViewById(R.id.medium_detail_plot)).setText(mOverview);
        ((TextView) mRootView.findViewById(R.id.medium_detail_rating)).setText(mRating);

        // Output an image
        Picasso.with(getActivity())
                .load(mThumbnail)
                .placeholder(movie_placeholder)
                .into(((ImageView) rootView.findViewById(R.id.medium_detail_poster)));
    }

    public class MediumDetailRecyclerViewAdapter
            extends RecyclerView.Adapter<MediumDetailFragment.MediumDetailRecyclerViewAdapter.ViewHolder> {

        private final ArrayList<Media>  mMediaDetail;

        public MediumDetailRecyclerViewAdapter(ArrayList<Media> media) {
            mMediaDetail = media;
        }

        @Override
        public MediumDetailFragment.MediumDetailRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.medium_detail_content, parent, false);
            return new MediumDetailFragment.MediumDetailRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MediumDetailFragment.MediumDetailRecyclerViewAdapter.ViewHolder holder, int position) {

            Boolean favouriteSetting = false;

            // Display the title of the media
            holder.mMediumTitle.setText(mMediaDetail.get(position).getTitle());

            // Display image or placeholder image if not available
            Picasso.with(getContext())
                    .load(mMediaDetail.get(position).getThumbnail())
                    .placeholder(movie_placeholder)
                    .into(holder.mMediumPoster);
        }

        @Override
        public int getItemCount() {
            return mMediaDetail.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mMediumTitle;
            public final ImageView mMediumPoster;

            public ViewHolder(View view) {
                super(view);
                mView               = view;
                mMediumTitle        = (TextView) view.findViewById(R.id.medium_list_title);
                mMediumPoster       = (ImageView) view.findViewById(R.id.medium_list_poster);
            }
        }
    }


}
