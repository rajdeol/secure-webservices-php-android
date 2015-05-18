public class Test extends ActionBarActivity {
  /**
   * Example on how to use the ApiAccess class in your code
   */
  public void testApiCall(){
        Map<String,String> paramsList = new HashMap<String, String>();
        // we have used a parameter named "bar_code" 
        paramsList.put("bar_code","9876543210");

        ApiAccess apiRequest = new ApiAccess();

        apiRequest.callApi(this,paramsList,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              // write your code here to handle success response from server
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
              // write your code here to handle fail response 
            }
        });

    }
