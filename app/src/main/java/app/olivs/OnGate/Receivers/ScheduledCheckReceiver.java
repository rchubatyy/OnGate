package app.olivs.OnGate.Receivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import app.olivs.OnGate.App.Services;
import app.olivs.OnGate.Databases.DatabaseAccess;

import static app.olivs.OnGate.App.Services.ServiceRequest;
import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class ScheduledCheckReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        String token = databaseAccess.getAuthenticationToken();
        if (token != null & !token.equals("")) {
            JSONObject body = new JSONObject();
            try {
                body.put(AUTHENTICATION_TOKEN_KEY, token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final ServiceRequest request = new ServiceRequest(context, Request.Method.POST, Services.getServiceURL(6), body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String requestStatus;
                    try {
                        requestStatus = response.getString("RequestStatusValue");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    switch (requestStatus) {
                        case "D":
                            databaseAccess.deRegister(context);
                            break;
                        case "S":
                            Services.updateSettings(context, null);
                            break;
                        case "A":
                            System.out.println("OK!");
                            break;
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof ServerError) {
                        String message = new String (error.networkResponse.data, StandardCharsets.UTF_8);
                        if (message.contains("token")){
                            databaseAccess.deRegister(context);
                        }
                    }
                }
            });
            Volley.newRequestQueue(context).add(request);
        }
    }

}