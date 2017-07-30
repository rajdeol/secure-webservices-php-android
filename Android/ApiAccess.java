/**
 * Create {Contributed} by Faruk on 7/18/16.
 */
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * class to handle hand shake with API
 * Created by rajinder deol on 17/3/15.
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ApiAccess {

    // add your API key in below variable
    protected final String api_key = "Your-API-KEY";

    // add your API Secret in below variable
    protected final String api_secret = "Your-API-SECRET";

    protected String time_stamp;
    protected static final String HEXES = "0123456789abcdef";

    protected static final int DEFAULT_TIMEOUT = 120 * 1000;

    // header name in which we will send APi key
    protected final String API_KEY_HEADER = "API-KEY";

    // header name in which we wil send security token
    protected final String API_SIGNATURE_HEADER = "X-HASH";

    // header name in which we will send Unix time stamp
    protected final String TIME_STAMP_HEADER = "API-REQUEST-TIME";

    // 
    protected final int DEFAULT_READ_TIMEOUT = 10000;

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result, sbParams;
    URL urlObj;
    JSONObject jObj = null;
    String paramsString;

    /**
     * constructor to set default values
     */

    public ApiAccess() {
        // set time stamp, equivlant of php microtime(TRUE)
        time_stamp = String.valueOf(System.currentTimeMillis());
    }

    public JSONObject makeHttpRequest(String url, String method,
                                      HashMap<String, String> params) {
        String signature = getSignature(params);
        HashMap<String, String> headers = getHeaders(signature);
        Iterator iterator = headers.keySet().iterator();

        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (method.equals("POST")) {
            // request method is POST
            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept-Charset", charset);

                while(iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = (String) headers.get(key);
                    conn.setRequestProperty(key, value);
                }

                conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
                conn.setConnectTimeout(DEFAULT_TIMEOUT);

                conn.connect();

                paramsString = sbParams.toString();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("GET")){
            // request method is GET

            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(false);

                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept-Charset", charset);

                while(iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = (String) headers.get(key);
                    conn.setRequestProperty(key, value);
                }

                conn.setConnectTimeout(DEFAULT_TIMEOUT);

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            //Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON Object
        return jObj;
    }

    /**
     * Function to create signature with following rules:
     * 1.Convert all keys to lowercase
     * 2.Alphabetically sort the keys
     * 3.Create Json String
     * 4.Append Api_Key and Time_Stamp
     * 5.create sha256 hash
     * @param params list of parameters
     * @return String Signature
     */
    protected String getSignature(HashMap<String,String> params){
        String signature = "";
        HashMap<String,String> lowerCaseParams = new HashMap<String,String>();

        Iterator it = params.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry mapEntry = (Map.Entry) it.next();
            lowerCaseParams.put(mapEntry.getKey().toString().toLowerCase(),mapEntry.getValue().toString());
        }

        Map<String,String> sortedParam = new TreeMap<String,String>(lowerCaseParams);

        JSONObject jsonParams = new JSONObject(sortedParam);
        String jsonParamsStr = jsonParams.toString();
        String strToHash = jsonParamsStr+api_key+time_stamp;
        try {
            signature =  encode(api_secret,strToHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return signature;
    }

    /**
     * Function to encode data with secrect key
     * @param key Secret key
     * @param data data to encode
     * @return String Signature
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    private String encode(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"),"HmacSHA256");
        sha256_HMAC.init(secret_key);

        final byte[] mac_data = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return getHex(mac_data);
    }

    /**
     * function to generate hex string
     * @param mac_data
     * @return String
     */
    private String getHex(byte[] mac_data) {
        if(mac_data == null){
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * mac_data.length );
        for ( final byte b : mac_data ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * function to prepare headers for the request
     * @param signature
     * @return headers
     */
    protected HashMap<String,String> getHeaders(String signature) {
        HashMap<String, String> headers =  new HashMap<>();
        headers.put(API_KEY_HEADER,api_key);
        headers.put(API_SIGNATURE_HEADER,signature);
        headers.put(TIME_STAMP_HEADER,time_stamp);

        return headers;
    }
}
