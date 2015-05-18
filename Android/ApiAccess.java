import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * class to handle hand shake with API
 * Created by rajinder deol on 17/3/15.
 */
public class ApiAccess {
    // add your API key in below variable
    protected final String api_key = "Your-API-KEY";
    
    // add your API Secret in below variable
    protected final String api_secret = "Your-API-SECRET";
    
    // add your websrvice/server URL
    protected final String api_url = "Path-OF-Your-API-SERVER";
    protected String time_stamp;
    protected static final String HEXES = "0123456789abcdef";
    protected static final int DEFAULT_TIMEOUT = 120 * 1000;
    
    // header name in which we will send APi key
    protected final String API_KEY_HEADER = "API-KEY";
    
    // header name in which we wil send security token
    protected final String API_SIGNATURE_HEADER = "X-HASH";
    
    // header name in which we will send Unix time stamp
    protected final String TIME_STAMP_HEADER = "API-REQUEST-TIME";

    /**
     * constructor to set default values
     */
    public ApiAccess(){
        // set time stamp, equivlant of php microtime(TRUE)
        time_stamp = String.valueOf(System.currentTimeMillis());
    }

    public void callApi(Context context, Map<String, String> params, AsyncHttpResponseHandler responseHandler){
        String signature = getSignature(params);
        Header[] headers = getHeaders(signature);

        RequestParams reqParams = new RequestParams(params);
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(DEFAULT_TIMEOUT);

        final RequestHandle requestHandle = httpClient.get(context, api_url, headers, reqParams, responseHandler);
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
    protected String getSignature(Map<String,String> params){
        String signature = "";
        Map<String,String> lowerCaseParams = new HashMap<String,String>();

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
    protected Header[] getHeaders(String signature){
        Header [] headers = {
            new BasicHeader(API_KEY_HEADER,api_key),
            new BasicHeader(API_SIGNATURE_HEADER,signature),
            new BasicHeader(TIME_STAMP_HEADER,time_stamp)
        };

        return headers;
    }
}
