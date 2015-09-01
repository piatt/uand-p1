package com.piatt.udacity.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieDetailAdapter extends BaseExpandableListAdapter {
    private static final String LOG_TAG = MovieDetailAdapter.class.getSimpleName();

    private Context context;
    private String synopsis;
    private ArrayList<MovieReviewItem> movieReviewItems = new ArrayList<>();
    private ArrayList<MovieVideoItem> movieVideoItems = new ArrayList<>();
    private ArrayList<String> groupHeaders = new ArrayList<>();

    public MovieDetailAdapter(Context context) {
        this.context = context;
        groupHeaders.add(MovieListService.DETAIL_SYNOPSIS_LABEL);
        groupHeaders.add(MovieListService.DETAIL_REVIEWS_LABEL);
        groupHeaders.add(MovieListService.DETAIL_VIDEOS_LABEL);
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
        notifyDataSetChanged();
    }

    public void setMovieReviewItems(ArrayList<MovieReviewItem> movieReviewItems) {
        this.movieReviewItems = movieReviewItems;
        notifyDataSetChanged();
    }

    public void setMovieVideoItems(ArrayList<MovieVideoItem> movieVideoItems) {
        this.movieVideoItems = movieVideoItems;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int count = 0;

        switch (groupPosition) {
            case 0: count = synopsis.isEmpty() ? 0 : 1;
                break;
            case 1: count = movieReviewItems.size();
                break;
            case 2: count = movieVideoItems.size();
                break;
        }

        return count;
    }

    @Override
    public String getGroup(int groupPosition) {
        return groupHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Object child = null;

        switch (groupPosition) {
            case 0: child = synopsis;
                break;
            case 1: child = movieReviewItems.get(childPosition);
                break;
            case 2: child = movieVideoItems.get(childPosition);
                break;
        }

        return child;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null || (convertView.getTag() != null && !convertView.getTag().equals(groupPosition))) {
            convertView = LayoutInflater.from(context).inflate(R.layout.detail_extras_header, null);
        }
        if (convertView != null) {
            if (convertView.getTag() == null) {
                convertView.setTag(groupPosition);
                ((TextView) convertView.findViewById(R.id.detail_extras_header)).setText(groupHeaders.get(groupPosition));
            }
            if (isExpanded) {
                ((TextView) convertView.findViewById(R.id.detail_extras_indicator)).setText(MovieListService.DETAIL_CONTRACT_INDICATOR);
            } else {
                ((TextView) convertView.findViewById(R.id.detail_extras_indicator)).setText(MovieListService.DETAIL_EXPAND_INDICATOR);
            }
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null || (convertView.getTag() != null && !convertView.getTag().equals(groupPosition))) {
            switch (groupPosition) {
                case 0: convertView = LayoutInflater.from(context).inflate(R.layout.detail_synopsis_item, null);
                    break;
                case 1: convertView = LayoutInflater.from(context).inflate(R.layout.detail_review_item, null);
                    break;
                case 2: convertView = LayoutInflater.from(context).inflate(R.layout.detail_video_item, null);
                    break;
            }
        }
        if (convertView != null && convertView.getTag() == null) {
            convertView.setTag(groupPosition);

            switch (groupPosition) {
                case 0: ((TextView) convertView.findViewById(R.id.detail_synopsis)).setText(getChild(groupPosition, childPosition).toString());
                    break;
                case 1: ((TextView) convertView.findViewById(R.id.detail_review_content)).setText(((MovieReviewItem) getChild(groupPosition, childPosition)).getContent());
                    ((TextView) convertView.findViewById(R.id.detail_review_author)).setText(((MovieReviewItem) getChild(groupPosition, childPosition)).getAuthor());
                    break;
                case 2: ((TextView) convertView.findViewById(R.id.detail_video_name)).setText(((MovieVideoItem) getChild(groupPosition, childPosition)).getName());
                    break;
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}