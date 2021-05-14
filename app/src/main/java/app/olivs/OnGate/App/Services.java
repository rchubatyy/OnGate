package app.olivs.OnGate.App;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;

public class Services{
    private static final String[] serviceURLCommands = {
            "device pin validation",
            "user registration pin recognition",
            "register check information",
            "receive update settings",
            "confirm update settings",
            "receive request status"};

    private static String serviceURL = "https://webservice4.olivs.cloud/";

    public static final String AUTHENTICATION_TOKEN_KEY = "AuthenticationToken";

    public static String getServiceURL(int index){
        String url = serviceURL;
        String command;
        try{
            command = serviceURLCommands[index-1];
        }
        catch(IndexOutOfBoundsException e){
            return "";
        }
        command = command.replace(" ", "-");
        command = command.toLowerCase();
        url += "api/en-au/ongates/service";
        url += index;
        url += "-";
        url += command;
        System.out.println(url);
        return url;
    }

    public static Map<String, String> getDefaultHeaders(Context context) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Api-Key", context.getString(R.string.api_key));
        headers.put("Olivs-Root-Password", context.getString(R.string.olivs_root_password));
        headers.put("Olivs-API-Real-IP", "127.0.0.1");
        headers.put("Olivs-API-Computer-Name", "BTMSOFTPC");
        return headers;
    }

    public static void updateSettings(Context context, OnUpdatedSettingsListener listener){
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        String token = databaseAccess.getAuthenticationToken();
        JSONObject tokenBody = new JSONObject();
        try {
            tokenBody.put(AUTHENTICATION_TOKEN_KEY, token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener!=null)
                    listener.onUpdatedSettings(false);
            }
        };
        final ServiceRequest request = new ServiceRequest(context, Request.Method.POST, getServiceURL(4), tokenBody, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String newAdminPIN = response.getString("NewAdminPIN");
                    if (!newAdminPIN.equals(""))
                        databaseAccess.changeSetting("AdminPIN", newAdminPIN);
                        databaseAccess.changeSetting("CompanyName",response.getString("CompanyName"));
                        databaseAccess.changeSetting("CompanyMessage",response.getString("CompanyMessage"));
                        databaseAccess.changeSetting("SiteName",response.getString("SiteName"));
                        databaseAccess.changeSetting("PhotoResolution",response.getString("PhotoResolution"));
                        databaseAccess.changeSetting("ServiceURL",response.getString("ServiceURL"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                    if (listener!=null)
                    listener.onUpdatedSettings(true);
                final ServiceRequest request = new ServiceRequest(context, Request.Method.POST, getServiceURL(5), tokenBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String currentAdminPIN = response.getString("CurrentAdminPIN");
                            databaseAccess.changeSetting("AdminPIN", currentAdminPIN);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error);
                Volley.newRequestQueue(context).add(request);
            }
        }, error);
        Volley.newRequestQueue(context).add(request);
    }


    public interface OnUpdatedSettingsListener{
        void onUpdatedSettings(boolean success);
    }

    public static class ServiceRequest extends JsonObjectRequest{

        private final Context context;

        public ServiceRequest(Context context, int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
            this.context = context;
            this.setRetryPolicy(new DefaultRetryPolicy(10000,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
            @Override
            public Map<String, String> getHeaders() {
                return getDefaultHeaders(context);
        }

    }
}
