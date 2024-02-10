package com.one.above.fitness.service;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.one.above.fitness.database.SharedPreference;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WebService {
    public static String responseString = null;
    private static String NAMESPACE = "http://tempuri.org/";
    private static int TimeOut = 2000000;
    public static int timeoutFlag = 0;
    private static String URL_SOAP = "";
    private static final String URL_LOGIN = "http://oneabovefit.ezeeclub.net/MobileAppService.svc/UserLogin";
    private static final String URL_ATTENDANCE = "http://oneabovefit.ezeeclub.net/MobileAppService.svc/SaveAttendance";// Make
    private static final String SOAP_ACTION = "http://tempuri.org/";

    private static final String URL_BRANCH_DETAILS = "http://oneabovefit.ezeeclub.net/MobileAppService.svc/GetBranchDetails";// Make
    static SoapSerializationEnvelope envelope;
    static JSONArray jobj = null;

    public static JSONArray getSoapData(String UserName, String Password,
                                        String webMethName) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        SoapObject request = new SoapObject(NAMESPACE, webMethName);

        request.addProperty("UserName", UserName);
        request.addProperty("Password", Password);

        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        jobj = new JSONArray();
        jobj = serverConnection(webMethName);
        return jobj;

    }

    @SuppressWarnings("deprecation")
    public static JSONArray serverConnection(String webMethName) {
        JSONArray jsonObj = null;
        try {

            String url = URL_SOAP;

            Log.e("URL1", "MAIN" + url);

            timeoutFlag = 0;
            Log.w("Web Service ", "HTTP TRANSPORT : ");
            HttpTransportSE androidHttpTransport = new HttpTransportSE(url, TimeOut);
            Log.w("Web Service ", "URL : " + url);
            Log.w("Web Service ", "Method : " + webMethName);

            // Thread.sleep(20000);
            // Invoke web service
            androidHttpTransport.call(SOAP_ACTION + webMethName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            // Assign it to boolean variable variable
            Log.w("Web Service ", "Data " + response.toString());
            // loginStatus = Boolean.parseBoolean(response.toString());
            responseString = response.toString();

            jsonObj = new JSONArray(response.toString());

        } catch (ConnectTimeoutException e) {
            // showAlertDialog();
            timeoutFlag = 1;
            Log.w("Web Service", "ConnectTimeoutException");
        } catch (ConnectException e) {
            // showAlertDialog();
            timeoutFlag = 1;
            Log.w("Web Service", "ConnectException");
        } catch (EOFException e) {
            // showAlertDialog();
            Log.w("Web Service", "EOFException");
        } catch (ClassCastException e) {
            e.printStackTrace();
            // showAlertDialog();
            Log.w("Web Service", "ClassCastException");
        } catch (SocketTimeoutException e) {
            timeoutFlag = 1;
            // showAlertDialog();
            Log.w("Web Service", "Timed Out");
        } catch (Exception e) {
            // Assign Error Status true in static variable 'errored'

            // HomeActivity.errored = true;
            e.printStackTrace();
        }
        // Return booleam to calling object
        return jsonObj;
    }

    public static JSONArray saveAttendance(String MemberNo) {

        JSONArray jsonArray = new JSONArray();

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url = new URL(URL_ATTENDANCE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("MemberNo", MemberNo);

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

            int responseCode = conn.getResponseCode();

            System.out.println("response code >>>!" + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Request successful!");
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.w("Response HTTP >>>: ", response.toString());
                    if (!response.toString().equals(null))
                        jsonArray = new JSONArray(response.toString());
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    public static JSONArray getBranchDetails(Context context) {

        JSONArray jsonArray = new JSONArray();

        try {


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url = new URL(URL_BRANCH_DETAILS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

            int responseCode = conn.getResponseCode();

            System.out.println("response code >>>!" + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Request successful!");
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.w("Response HTTP >>>: ", response.toString());
                    if (!response.toString().equals(null)) {
                        jsonArray = new JSONArray(response.toString());
                        SharedPreference.setBranchServerDataList(context, jsonArray);
                    }
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("BranchDetails", new Gson().toJson(jsonArray));
        return jsonArray;
    }

    public static JSONArray getPlanDetails(String MemberNo, String BranchNo) {

        JSONArray jsonArray = new JSONArray();

        try {

            String Url = "http://oneabovefit.ezeeclub.net/MobileAppService.svc/GetPlanDetails";

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url = new URL(Url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("MemberNo", MemberNo);
            jsonParam.put("BranchNo", BranchNo);

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

            int responseCode = conn.getResponseCode();

            System.out.println("response code >>>!" + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Request successful!");
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.w("Response HTTP >>>: ", response.toString());
                    if (!response.toString().equals(null))
                        jsonArray = new JSONArray(response.toString());
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    public static JSONArray getLoginData(String userName, String password) {

        JSONArray jsonArray = new JSONArray();

        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            URL url = new URL(URL_LOGIN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("UserName", userName);
            jsonParam.put("Password", password);

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

            int responseCode = conn.getResponseCode();

            System.out.println("response code >>>!" + responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Request successful!");
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    Log.w("Response HTTP >>>: ", response.toString());
                    if (!response.toString().equals(null))
                        jsonArray = new JSONArray(response.toString());
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

}
