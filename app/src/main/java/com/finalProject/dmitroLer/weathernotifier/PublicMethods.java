package com.finalProject.dmitroLer.weathernotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

class PublicMethods {
    static void saveObjectToSharedPreference(Context context, String preferenceFileName, String serializedObjectKey, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    static <GenericClass> GenericClass getSavedObjectFromPreference(Context context, String preferenceFileName, String preferenceKey, Class<GenericClass> classType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    static void changeIcon(String icon, ImageView imageToChange, boolean bigIcon) {
        switch (icon) {
            case "clear-day":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_clear_day);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_clear_day);
                }
                break;
            case "clear-night":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_clear_night);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_clear_night);
                }
                break;
            case "rain":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_rainy_day);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_rainy_day);
                }
                break;
            case "snow":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_snow);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_snow);
                }
                break;
            case "sleet":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_snow);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_snow);
                }
                break;
            case "wind":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_wind);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_wind);
                }
                break;
            case "fog":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_fog_cloud);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_fog_cloud);
                }
                break;
            case "cloudy":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_fog_cloud);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_fog_cloud);
                }
                break;
            case "partly-cloudy-day":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_cloudy_day);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_cloudy_day);
                }
                break;
            case "partly-cloudy-night":
                if (bigIcon) {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.big_icon_weather_cloudy_night);
                } else {
                    imageToChange.setImageResource(com.finalProject.dmitroLer.weathernotifier.R.drawable.icon_weather_cloudy_night);
                }
                break;
            default:

                break;
        }
        imageToChange.setVisibility(View.VISIBLE);
    }

    static void changeRecommendationText(WeatherResponse weatherResponse, TextView recommendation, TextView recommendationText) {
        String icon = weatherResponse.getHourly().getIcon();
        StringBuilder stringBuilder = new StringBuilder();
        double maxTemprature = weatherResponse.getDaily().getData().get(0).getTemperatureMax();
        if (maxTemprature > 30) {
            stringBuilder.append("Short t-shirt, ");
        }
        double minTemprature = weatherResponse.getDaily().getData().get(0).getTemperatureMin();
        if (minTemprature < 10) {
            stringBuilder.append("Warm clothing, ");
        }
        switch (icon) {
            case "clear-day": {
                stringBuilder.append("Sunglasses, Hat, Sunscreen");
            }
            break;
            case "clear-night": {

            }
            break;
            case "rain": {
                stringBuilder.append("Rain suit, Umbrella");
            }
            break;
            case "snow": {
                stringBuilder.append("Coat, Boots");
            }
            break;
            case "sleet": {

            }
            break;
            case "wind": {
                stringBuilder.append("Wind suit");
            }
            break;
            case "fog": {

            }
            break;
            case "cloudy": {
                stringBuilder.append("Umbrella");
            }
            break;
            case "partly-cloudy-day": {
                stringBuilder.append("Umbrella");
            }
            break;
            case "partly-cloudy-night": {
                //stringBuilder.append("Umbrella");
            }
            break;
            default:

                break;
        }

        String text = stringBuilder.toString();
        if (!text.matches("")) {
            recommendation.setText(text);
        } else {
            recommendation.setVisibility(View.GONE);
            recommendationText.setVisibility(View.GONE);
        }
    }
}
