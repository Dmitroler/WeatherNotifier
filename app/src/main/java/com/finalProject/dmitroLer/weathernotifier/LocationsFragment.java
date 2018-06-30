package com.finalProject.dmitroLer.weathernotifier;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finalProject.dmitroLer.weathernotifier.items.RecyclerItems;
import com.finalProject.dmitroLer.weathernotifier.items.RecyclerItems.RecyclerItem;

public class LocationsFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    Location location;
    Address address;
    RecyclerView recyclerView;

    public LocationsFragment() {
    }

    @SuppressWarnings("unused")
    public static LocationsFragment newInstance(int columnCount) {
        LocationsFragment fragment = new LocationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.fragment_locations_list, container, false);

        // Set the adapter
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = ((ViewGroup) view);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i) instanceof RecyclerView) {
                    recyclerView = (RecyclerView) viewGroup.getChildAt(i);
                    Context context = viewGroup.getChildAt(i).getContext();
                    if (mColumnCount <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                    }
                    recyclerView.setAdapter(new LocationsRecyclerViewAdapter(RecyclerItems.ITEMS, mListener));
                }
            }
        }
        MainActivity activity = (MainActivity) getActivity();
        location = activity.getLastKnownLocation();
        address = activity.getLastKnownAddress();
        view.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.add_new_address_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DefineLocationActivity.class);
                intent.putExtra("Location", location);
                intent.putExtra("Address", address);
                startActivityForResult(intent, 100);
            }
        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(RecyclerItem item);
    }

    public LocationsRecyclerViewAdapter getRecyclerViewAdapter() {
        return (LocationsRecyclerViewAdapter) recyclerView.getAdapter();
    }
}
