package com.finalProject.dmitroLer.weathernotifier;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.finalProject.dmitroLer.weathernotifier.LocationsFragment.OnListFragmentInteractionListener;
import com.finalProject.dmitroLer.weathernotifier.items.RecyclerItems.RecyclerItem;

import java.util.ArrayList;
import java.util.List;

public class LocationsRecyclerViewAdapter extends RecyclerView.Adapter<LocationsRecyclerViewAdapter.ViewHolder> {

    private final List<RecyclerItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final ArrayList<ViewHolder> viewHolders = new ArrayList<>();

    public LocationsRecyclerViewAdapter(List<RecyclerItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.item_location, parent, false);
        Animation animation = new AlphaAnimation(0,1);
        animation.setDuration(1000);
        view.startAnimation(animation);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final TextView mContentView;
        final Button mButton;
        RecyclerItem mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.id);
            mContentView = (TextView) view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.content);
            mButton = (Button) view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.recycler_button);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(getAdapterPosition());
                    if (null != mListener) mListener.onListFragmentInteraction(mItem);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        private void removeAt(int position) {
            mValues.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mValues.size());
            for (int i = mValues.size(); i > position; i--) {
                mValues.get(i-1).id = String.valueOf(i);
                viewHolders.get(i-1).mIdView.setText(String.valueOf(i));
            }
        }

    }
}
