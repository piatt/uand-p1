package com.piatt.udacity.popularmovies.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.piatt.udacity.popularmovies.MoviesApplication;
import com.piatt.udacity.popularmovies.R;
import com.piatt.udacity.popularmovies.adapter.MovieReviewsAdapter;
import com.piatt.udacity.popularmovies.adapter.MovieVideosAdapter;
import com.piatt.udacity.popularmovies.event.MovieSelectionEvent;
import com.piatt.udacity.popularmovies.model.ApiResponse;
import com.piatt.udacity.popularmovies.model.MovieDetail;
import com.piatt.udacity.popularmovies.model.MovieReview;
import com.piatt.udacity.popularmovies.model.MovieVideo;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment {
    private static final String MOVIE_ID_KEY = "MOVIE_ID";
    @BindString(R.string.favorite_on_icon) String favoriteOnIcon;
    @BindString(R.string.favorite_off_icon) String favoriteOffIcon;
    @BindView(R.id.back_button) TextView backButton;
    @BindView(R.id.title_view) TextView titleView;
    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.poster_view) ImageView posterView;
    @BindView(R.id.release_date_view) TextView releaseDateView;
    @BindView(R.id.rating_view) TextView ratingView;
    @BindView(R.id.runtime_view) TextView runtimeView;
    @BindView(R.id.overview_view) TextView overviewView;
    @BindView(R.id.favorite_button) TextView favoriteButton;
    @BindView(R.id.videos_layout) LinearLayout videosLayout;
    @BindView(R.id.videos_toggle_button) TextView videosToggleButton;
    @BindView(R.id.video_list) RecyclerView videoList;
    @BindView(R.id.reviews_layout) LinearLayout reviewsLayout;
    @BindView(R.id.reviews_toggle_button) TextView reviewsToggleButton;
    @BindView(R.id.review_list) RecyclerView reviewList;
    private Unbinder unbinder;

    public static MovieFragment newInstance(int movieId) {
        MovieFragment fragment = new MovieFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(MOVIE_ID_KEY, movieId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int movieId = getArguments().getInt(MOVIE_ID_KEY);
        MoviesApplication.getApp().getApiManager().getEndpoints().getMovieDetails(movieId).enqueue(movieDetailCallback);
        MoviesApplication.getApp().getApiManager().getEndpoints().getMovieVideos(movieId).enqueue(movieVideoCallback);
        MoviesApplication.getApp().getApiManager().getEndpoints().getMovieReviews(movieId).enqueue(movieReviewCallback);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * Called on fragment creation or rotation, this method smartly updates the existing bound views, if available.
     * Additionally, although it appears to make a network call each time the method is invoked via the updateMovieDetailView method,
     * the ContextManager handles response caching, delivering the data to the movieDetailsCallback instantly from cache, if available, even offline.
     * This eliminates the need for the fragment itself to handle instance state.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Subscribe
    public void updateMovieFragment(MovieSelectionEvent event) {
        if (MoviesApplication.getApp().isLargeLayout()) {
            MoviesApplication.getApp().getApiManager().getEndpoints().getMovieDetails(event.getMovieId()).enqueue(movieDetailCallback);
            MoviesApplication.getApp().getApiManager().getEndpoints().getMovieVideos(event.getMovieId()).enqueue(movieVideoCallback);
            MoviesApplication.getApp().getApiManager().getEndpoints().getMovieReviews(event.getMovieId()).enqueue(movieReviewCallback);
        }
    }

    private Callback<MovieDetail> movieDetailCallback = new Callback<MovieDetail>() {
        @Override
        public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
            if (response.isSuccessful()) {
                MovieDetail movieDetail = response.body();
                Picasso.with(posterView.getContext()).load(movieDetail.getPosterUrl()).into(posterView);
                titleView.setText(movieDetail.getTitle());
                appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                    int visibilityOffset = appBarLayout.getTotalScrollRange() - toolbar.getHeight();
                    if (Math.abs(verticalOffset) >= visibilityOffset) {
                        backButton.setVisibility(View.VISIBLE);
                        titleView.setVisibility(View.VISIBLE);
                        float alpha = (1f / toolbar.getHeight()) * (Math.abs(verticalOffset) - visibilityOffset);
                        backButton.setAlpha(alpha);
                        titleView.setAlpha(alpha);
                    } else {
                        backButton.setVisibility(View.INVISIBLE);
                        titleView.setVisibility(View.INVISIBLE);
                    }
                });
                releaseDateView.setText(movieDetail.getReleaseDate());
                ratingView.setText(movieDetail.getRating());
                runtimeView.setText(movieDetail.getRuntime());
                overviewView.setText(movieDetail.getOverview());
                favoriteButton.setText(favoriteOffIcon);
            }
        }

        @Override
        public void onFailure(Call<MovieDetail> call, Throwable t) {}
    };

    private Callback<ApiResponse<MovieVideo>> movieVideoCallback = new Callback<ApiResponse<MovieVideo>>() {
        @Override
        public void onResponse(Call<ApiResponse<MovieVideo>> call, Response<ApiResponse<MovieVideo>> response) {
            if (response.isSuccessful() && !response.body().getResults().isEmpty()) {
                videoList.setHasFixedSize(true);
                videoList.setLayoutManager(new LinearLayoutManager(videoList.getContext()));
                videoList.setAdapter(new MovieVideosAdapter(response.body().getResults()));
                videosLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<MovieVideo>> call, Throwable t) {}
    };

    private Callback<ApiResponse<MovieReview>> movieReviewCallback = new Callback<ApiResponse<MovieReview>>() {
        @Override
        public void onResponse(Call<ApiResponse<MovieReview>> call, Response<ApiResponse<MovieReview>> response) {
            if (response.isSuccessful() && !response.body().getResults().isEmpty()) {
                reviewList.setHasFixedSize(true);
                reviewList.setLayoutManager(new LinearLayoutManager(reviewList.getContext()));
                reviewList.setAdapter(new MovieReviewsAdapter(response.body().getResults()));
                reviewsLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<MovieReview>> call, Throwable t) {}
    };

    @OnClick(R.id.back_button)
    public void onClick() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.favorite_button)
    public void onFavoriteButtonClick() {
        boolean isFavorite = favoriteButton.getText().equals(favoriteOnIcon);
        favoriteButton.setText(isFavorite ? favoriteOffIcon : favoriteOnIcon);
    }

    @OnClick(R.id.videos_toggle_button)
    public void onVideosToggleButtonClick() {
        videosToggleButton.setText(videoList.isShown() ? R.string.expand_icon : R.string.contract_icon);
        videoList.setVisibility(videoList.isShown() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.reviews_toggle_button)
    public void onReviewsToggleButtonClick() {
        reviewsToggleButton.setText(reviewList.isShown() ? R.string.expand_icon : R.string.contract_icon);
        reviewList.setVisibility(reviewList.isShown() ? View.GONE : View.VISIBLE);
    }
}