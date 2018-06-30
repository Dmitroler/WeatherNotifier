package com.finalProject.dmitroLer.weathernotifier;

import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class NotificationSettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private View rootView;

    private onUpdateTimeSelectedListener mListener;
    private Spinner updateTimeSpinner, skinColor, hairColor, eyeColor, cloth;

    final int[] skin_value={1,2,3,4,5};
    final int[] hair_value={1,2,3,4,5};
    final int[] eye_value={1,2,3,4};
    final int[] cloth_value={1,2,3};
    int age_value = 0;
    int weight_value = 0;
    String date_value = "30 05 2018";

    EditText age, weight;
    Button btnUpdate;
    TextView lastUpdated;

    public NotificationSettingsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(com.finalProject.dmitroLer.weathernotifier.R.layout.fragment_settings, container, false);


        skinColor = ((Spinner) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.skin_spinner));
        hairColor = ((Spinner) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.hair_spinner));
        eyeColor = ((Spinner) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.eye_spinner));
        cloth = ((Spinner) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.cloth_spinner));

        age = (EditText) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.edittextage);
        age.setFilters(new InputFilter[]{ new MinMaxFilter("1", "120")});
        weight = (EditText) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.edittexweight);
        weight.setFilters(new InputFilter[]{ new MinMaxFilter("1", "150")});
        lastUpdated = (TextView) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.last_updated);
        btnUpdate = (Button) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.btnUpdate);

        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_uv))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_UV, true));
        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_uv)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_UV, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_temperature))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true));
        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_temperature)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_TEMPRATURE, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_wind))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_WIND, true));
        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_wind)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_WIND, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_rain))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_RAIN, true));
        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_rain)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_RAIN, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_humidity))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true));
        ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_humidity)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_HUMIDITY, isChecked);
            }
        });


        ArrayList<ItemData> list=new ArrayList<>();
        list.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.skin1));
        list.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.skin2));
        list.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.skin3));
        list.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.skin4));
        list.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.skin5));


        SpinnerAdapter adapter=new SpinnerAdapter(getActivity(),
                com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner, com.finalProject.dmitroLer.weathernotifier.R.id.spinner_text,list);
        skinColor.setAdapter(adapter);
        skinColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesEditor.putInt(Constants.SKIN_COLOR, skin_value[position]);
                sharedPreferencesEditor.apply();
                Constants.UV_Skin_MED_Max=skin_value[position];
                //Toast.makeText(getActivity(), ""+skin_value[position], Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayList<ItemData> hair=new ArrayList<>();
        hair.add(new ItemData("A", com.finalProject.dmitroLer.weathernotifier.R.drawable.ic_spinner));
        hair.add(new ItemData("B", com.finalProject.dmitroLer.weathernotifier.R.drawable.ic_spinner));
        hair.add(new ItemData("C", com.finalProject.dmitroLer.weathernotifier.R.drawable.ic_spinner));
        hair.add(new ItemData("D", com.finalProject.dmitroLer.weathernotifier.R.drawable.ic_spinner));
        hair.add(new ItemData("E", com.finalProject.dmitroLer.weathernotifier.R.drawable.ic_spinner));

        SpinnerAdapter hairadapter=new SpinnerAdapter(getActivity(),
                com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner, com.finalProject.dmitroLer.weathernotifier.R.id.spinner_text,hair);
        hairColor.setAdapter(hairadapter);
        hairColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesEditor.putInt(Constants.HAIR_COLOR, hair_value[position]);
                sharedPreferencesEditor.apply();
                //Toast.makeText(getActivity(), ""+skin_value[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<ItemData> eye=new ArrayList<>();
        eye.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.eye1));
        eye.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.eye2));
        eye.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.eye3));
        eye.add(new ItemData("", com.finalProject.dmitroLer.weathernotifier.R.drawable.eye4));


        SpinnerAdapter eyeadapter=new SpinnerAdapter(getActivity(),
                com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner, com.finalProject.dmitroLer.weathernotifier.R.id.spinner_text,eye);
        eyeColor.setAdapter(eyeadapter);
        eyeColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferencesEditor.putInt(Constants.EYE_COLOR, eye_value[position]);
                sharedPreferencesEditor.apply();
                //Toast.makeText(getActivity(), ""+skin_value[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter clothadapter = ArrayAdapter.createFromResource(getContext(), com.finalProject.dmitroLer.weathernotifier.R.array.cloth, com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner_without);
        clothadapter.setDropDownViewResource(com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner_dropdown);
        cloth.setAdapter(clothadapter);
        cloth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Constants.clothType=cloth_value[position];
                sharedPreferencesEditor.putInt(Constants.CLOTH, cloth_value[position]);
                sharedPreferencesEditor.apply();
                //Toast.makeText(getActivity(), ""+skin_value[position], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Toast.makeText(getActivity(), ""+sharedPreferences.getInt(Constants.SKIN_COLOR,0), Toast.LENGTH_SHORT).show();
        for (int i = 0; i < skin_value.length; i++)
        {
            if (sharedPreferences.getInt(Constants.SKIN_COLOR,20) == skin_value[i])
                skinColor.setSelection(i,true);
        }
        hairColor.setSelection(sharedPreferences.getInt(Constants.HAIR_COLOR,1)-1,true);
        eyeColor.setSelection(sharedPreferences.getInt(Constants.EYE_COLOR,1)-1,true);
        cloth.setSelection(sharedPreferences.getInt(Constants.CLOTH,1)-1,true);
        age.setText(""+sharedPreferences.getInt(Constants.AGE,0));
        weight.setText(""+sharedPreferences.getInt(Constants.WEIGHT,0));
        lastUpdated.setText(sharedPreferences.getString(Constants.UPDATE_DATE,"05 04 2018"));

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!age.getText().toString().equals(""))
                {
                    sharedPreferencesEditor.putInt(Constants.AGE, Integer.parseInt(age.getText().toString()));
                    sharedPreferencesEditor.apply();
                    Constants.ageOfUser=Integer.parseInt(age.getText().toString().trim());
                    if (!weight.getText().toString().equals(""))
                    {
                        String date = new SimpleDateFormat("dd MM yyyy", Locale.getDefault()).format(new Date());
                        sharedPreferencesEditor.putInt(Constants.WEIGHT, Integer.parseInt(weight.getText().toString()));
                        sharedPreferencesEditor.putString(Constants.UPDATE_DATE,date);
                        lastUpdated.setText(date);
                        sharedPreferencesEditor.apply();
                        Constants.weightOfUSer=Integer.parseInt(weight.getText().toString().trim());
                    }else
                        Toast.makeText(getActivity(), "Please enter weight", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(getActivity(), "Please enter age", Toast.LENGTH_SHORT).show();
            }
        });

        setupSpinner();
        return rootView;
    }

    private void setupSpinner() {
        updateTimeSpinner = ((Spinner) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.timeIntervalspinner));
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), com.finalProject.dmitroLer.weathernotifier.R.array.update_times, com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner_without);
        adapter.setDropDownViewResource(com.finalProject.dmitroLer.weathernotifier.R.layout.item_spinner_dropdown);
        updateTimeSpinner.setAdapter(adapter);
        updateTimeSpinner
                .setSelection(sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0));
        updateTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mListener!=null && position!=sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION,0)) mListener.onUpdateTimeSelected();
                sharedPreferencesEditor.putInt(Constants.UPDATE_TIME_SELECTION
                        , position);
                sharedPreferencesEditor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onUpdateTimeSelectedListener) {
            mListener = (onUpdateTimeSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentTimeOutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        sharedPreferencesEditor.putBoolean(Constants.OPTION_UV,
                ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_uv)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_TEMPRATURE,
                ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_temperature)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_WIND,
                ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_wind)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_RAIN,
                ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_rain)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_HUMIDITY,
                ((CheckBox) rootView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.option_humidity)).isChecked());
        sharedPreferencesEditor.putInt(Constants.UPDATE_TIME_SELECTION
                , updateTimeSpinner.getSelectedItemPosition());
        sharedPreferencesEditor.apply();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface onUpdateTimeSelectedListener {
        void onUpdateTimeSelected();
    }

    public class SpinnerAdapter extends ArrayAdapter<ItemData> {
        int groupid;
        Activity context;
        ArrayList<ItemData> list;
        LayoutInflater inflater;
        public SpinnerAdapter(Activity context, int groupid, int id, ArrayList<ItemData>
                list){
            super(context,id,list);
            this.list=list;
            inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.groupid=groupid;
        }

        public View getView(int position, View convertView, ViewGroup parent ){
            View itemView=inflater.inflate(groupid,parent,false);
            ImageView imageView=(ImageView)itemView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.spinner_icon);
            imageView.setImageResource(list.get(position).getImageId());
            TextView textView=(TextView)itemView.findViewById(com.finalProject.dmitroLer.weathernotifier.R.id.spinner_text);
            textView.setText(list.get(position).getText());
            return itemView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup
                parent){
            return getView(position,convertView,parent);

        }
    }

    public class ItemData {

        String text;
        Integer imageId;
        public ItemData(String text, Integer imageId){
            this.text=text;
            this.imageId=imageId;
        }

        public String getText(){
            return text;
        }

        public Integer getImageId(){
            return imageId;
        }
    }


    public class MinMaxFilter implements InputFilter {

        private int mIntMin, mIntMax;

        public MinMaxFilter(int minValue, int maxValue) {
            this.mIntMin = minValue;
            this.mIntMax = maxValue;
        }

        public MinMaxFilter(String minValue, String maxValue) {
            this.mIntMin = Integer.parseInt(minValue);
            this.mIntMax = Integer.parseInt(maxValue);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(mIntMin, mIntMax, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}
