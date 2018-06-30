package com.finalProject.dmitroLer.weathernotifier;

final class Constants {
    //codes
    static final int SUCCESS_RESULT = 0;
    static final int FAILURE_RESULT = 1;
    static final int RECEIVE_TO_MAIN = 0;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    static final int RECEIVE_TO_FRAGMENT = 1;

    private static final String PACKAGE_NAME = "com.finalProject.dmitroLer.weathernotifier";
    static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    static final String RECEIVE_TYPE_EXTRA = PACKAGE_NAME + ".RECEIVE_TYPE_EXTRA";
    static final String LAST_KNOWN_LOCATION = "last known location";


    //Api keys
    public final String API_KEY0 = "ba5b4ae760ee1d74eea0e5d70514cdf4";
    public final String API_KEY1 = "4e687457bbdb40a25dd4a30b8d92ec0c";
    static final String API_KEY2 = "c2696e35230e0ed1e972c2b9b83d0387";



    //SharedPreferences keys
    static final String SKIN_COLOR = "skin color";
    static final String HAIR_COLOR = "hair color";
    static final String EYE_COLOR = "eye color";
    static final String AGE = "age";
    static final String WEIGHT = "weight";
    static final String CLOTH = "cloth";
    static final String UPDATE_DATE = "update date";

    static final String UPDATE_TIME_SELECTION = "update time";
    static final String ADDRESSES_PREFERENCE = "addresses preference";
    static final String SET_ALARM_TIME = "Set alarm time";
    static final String ALARM_HOUR = "alarm hour";
    static final String ALARM_MINUTE = "alarm minute";
    static final String ALARM_IS_ACTIVE = "alarm is active";
    static final String WEATHER_UPDATE_ITEMS = "weather update items";
    static final String ADDRESSES_HASH_MAP = "addresses hash map";
    static final String NUMBER_OF_ADDRESSES = "number of addresses";
    static final String OPTION_TEMPRATURE = "option_temprature";
    static final String OPTION_WIND = "option_wind";
    static final String OPTION_RAIN = "option_rain";
    static final String OPTION_UV = "option_uv";
    static final String OPTION_HUMIDITY = "option_humidity";
    static final String DAILY_WEATHER_ITEMS = "daily weather items";
    static final String NEW_LOCATION = "NewLocation";
    static final String EXIT_WITH_BACK_BUTTON = "exit with back button";

    //Strings
    static final String APPLICATION_NEEDS_PERMISSION = "Application needs permission";
    static final String NO_OPTION_SELECTED_TITLE = "No option selected";
    static final String NO_LOCATION_SELECTED = "No location selected";
    static final String NO_OPTION_SELECTED_TEXT = "If you don't select any option, Weather Notifier wont sent you notifications";
    static final String GOT_IT = "Got it";
    static final String GO_BACK = "Go back";
    static final String DEGREE = "°";
    static final String PERCENT = "%";

    protected static boolean isConnnected = false;
    protected static boolean isVisible = false;
    protected static boolean isToShowAlert = false;
    protected static int lastUVTimeLeft = 0;
    protected static int lastUVPercentDone = 0;
    protected static int lastUVIndex = 0;
    protected static int lastHiPercentDone = 0;
    protected static int lastHiIndex = 0;
    protected static int lastHITimeLeft = 0;
    protected static int lastTemperature = 0;
    protected static int lastHumidity = 0;
    protected static int lastBatteryStatus = 0;

    protected static String  currentMonitorSate = "Monitor";
    protected static String CONNECT = "com.finalProject.dmitroLer.CONNECT";
    protected static String CONNECTED = "com.finalProject.dmitroLer.CONNECTED";
    protected static String DISCONNECTED = "com.finalProject.dmitroLer.DISCONNECTED";
    protected static String DATA = "com.finalProject.dmitroLer.DATA";
    protected static String UNABLE_TO_CONNECT = "com.finalProject.dmitroLer.UNABLE_TO_CONNECT";
    protected static String START_N_ALARAM_TONE = "com.finalProject.dmitroLer.START";
    protected static String STOP_N_ALARAM_TONE = "com.finalProject.dmitroLer.STOP";
    protected static String SEND_MONITOR_DATA_INTENT = "com.finalProject.dmitroLer.SEND_MONITOR_DATA";
    protected static String ALERT_DILOGE_INTENT = "com.finalProject.dmitroLer.ALERT_DILOGE_INTENT";
    protected static String DO_NOTHING_INTENT = "com.finalProject.dmitroLer.DO_NOTHING_INTENT";
    protected static String RESET_VALUES_INTENT = "com.finalProject.dmitroLer.RESET_VALUES_INTENT";

    //ints
    protected static int arduinoStatusFlag=1;
    protected static int coverControlFlag=0;
    protected static int resetHiAlarmFlag=0;
    protected static int motorControlFlag=0;
    protected static int ageOfUser=0;
    protected static int weightOfUSer=0;
    protected static int UV_Skin_MED_Max=0;
    protected static double SPF_factor=1;
    protected static int clothType=0;
}
