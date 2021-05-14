package app.olivs.OnGate.App;

import android.content.Context;

import app.olivs.OnGate.R;

public class Config {
    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "uploads";
    //public static String IMAGE_DIRECTORY_NAME = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator;


    public static boolean isTablet(Context context){
        return context.getResources().getBoolean(R.bool.isTablet);
    }




}