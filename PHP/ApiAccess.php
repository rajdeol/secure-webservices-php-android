<?php
/**
 * class to check and validate API access calls
 */
class ApiAccess {
    // array to hold API details
    protected $apiCredentials = array();
    // db connection object
    protected $db;
    // request headers
    protected $headers;
    // request timestamp
    protected $timeStamp;
    // error message
    protected $errorString = "";
    // error messages
    const INVALID_API_KEY = "Invalid API key";
    const MISSING_API_KEY = "Missing API key";
    const MISSING_TIME_STAMP = "Missing Time Stamp";
    const MISSING_ENCODED_HASH = "Missing request hash";
    const EXPIRED_TIME_STAMP = "Time stamp expired";
    const INVALID_REQUEST = "Authorization failed invalid request";

    const HEADER_API_KEY = "API-KEY";
    const HEADER_ENCODED_HASH = "X-HASH";
    const HEADER_TIME_STAMP = "API-REQUEST-TIME";
    const REQUEST_LIFE_TIME = 5000;//in miliseconds

    public function __construct() {
        // load request headers
        $this->loadHeaders();
        //load DB connection
        $this->loadDb();
        // load API credentials
        $this->loadCredentials();
    }

    /**
     * function to load saved APi credentials
     * self note - cache the details in file to reduce DB lookup
     */
    protected function loadCredentials(){
        if($this->db){
            $query = "SELECT api_key,secret_key,"
                . "status  FROM ".DB_PREFIX."api_access"
                . " where status = 1;";

            $result = $this->db->query($query);

            if($result->num_rows > 0){
                while($row = $result->fetch_assoc()){
                    $this->apiCredentials[$row['api_key']] = $row['secret_key'];
                }

            }

        }
    }

    /**
     * function to get encoded request hash
     * @return boolean false if hash is missing
     */
    protected function getRequestHash(){
        if(isset($this->headers[self::HEADER_ENCODED_HASH])){
            return $this->headers[self::HEADER_ENCODED_HASH];
        }else{
            $this->errorString = self::MISSING_ENCODED_HASH;
            return FALSE;
        }
    }

    /**
     * function to get secret key
     * @param string $api_key
     * @return boolean false if invalid APi key else secret key
     */
    protected function getSecretKey($api_key){
        if(isset($this->apiCredentials[$api_key])){
            return $this->apiCredentials[$api_key];
        }else{
            $this->errorString = self::INVALID_API_KEY;
            return FALSE;
        }
    }

    /**
     * function to get api key from request
     * @return boolean false if api key missing
     */
    protected function getApiKey(){
        if(isset($this->headers[self::HEADER_API_KEY])){
            return $this->headers[self::HEADER_API_KEY];
        }else{
            $this->errorString = self::MISSING_API_KEY;
            return FALSE;
        }
    }

    /**
     * function to get time stamp
     * @return boolean false if time stamp is missing
     */
    protected function getTimeStamp() {
        if(isset($this->headers[self::HEADER_TIME_STAMP])) {
            $time_stamp = $this->headers[self::HEADER_TIME_STAMP];
	          $microtime = microtime();
		        $comps = explode(' ', $microtime);
		        $militime = sprintf('%d%03d', $comps[1], $comps[0] * 1000);

            // calculate the time defference
            if(self::REQUEST_LIFE_TIME >= ($militime - $time_stamp)) {
                return $this->headers[self::HEADER_TIME_STAMP];
            } else {
                $this->errorString = self::EXPIRED_TIME_STAMP;
                return FALSE;
            }
        } else {
            $this->errorString = self::MISSING_TIME_STAMP;
            return FALSE;
        }
    }

    /**
     * function to prepare HTTP request header array
     */
    protected function loadHeaders() {
       $headers = array();
       foreach ($_SERVER as $name => $value) {
           if (substr($name, 0, 5) == 'HTTP_') {
               //$headers[str_replace(' ', '-', ucwords(strtolower(str_replace('_', ' ', substr($name, 5)))))] = $value;
               $headers[str_replace(' ', '-', str_replace('_', ' ', substr($name, 5)))] = $value;
           }
       }
       $this->headers = $headers;
    }

    /**
     * function to load DB object
     */
    private function loadDb() {
        $link = mysqli_connect(DB_HOSTNAME, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

        if(mysqli_errno($link)) {
            $this->db = false;
            $this->errorString = mysqli_error($link) ;
        } else {
            $this->db = $link;
        }
    }

    /**
     * Function to validate API request
     * @return boolean True/False
     */
    public function validateRequest() {
        // check if API key is present
        if(!$this->getApiKey()) {
            return FALSE;
        } else {
            $api_key = $this->getApiKey();
        }

        // check if Time Stamp is present and valid
        if(!$this->getTimeStamp()){
            return FALSE;
        }else {
            $time_stamp = $this->getTimeStamp();
        }

        // check secret key
        if(!$this->getSecretKey($api_key)){
            return FALSE;
        }else{
            $secret_key = $this->getSecretKey($api_key);
        }

        // check request hash
        if(!$this->getRequestHash()){
            return FALSE;
        }else{
            $request_hash = $this->getRequestHash();
        }

        // prepare encoded hash from request
        $hash = $this->prepareHash($api_key, $time_stamp, $secret_key);
        // validate the generated hash
        if($hash === $request_hash){
            return TRUE;
        }else{
            $this->errorString = INVALID_REQUEST;
            return FALSE;
        }
    }

    /**
     * Function to create encoded hash
     * @param String $api_key
     * @param String $time_stamp
     * @param String $secret_key
     * @return String generated hash
     */
    protected function prepareHash($api_key,$time_stamp,$secret_key){
        // get request params
        $params = $_REQUEST;
        // change param names to lower case
        $params = array_change_key_case($params, CASE_LOWER);
        // sort params alphabetically
        ksort($params);

        // convert params into json string
        $paramsJsonStr = json_encode($params);
        // append APi key and time stamp
        $paramsStrtoHash = $paramsJsonStr.$api_key.$time_stamp;

        return hash_hmac('sha256', $paramsStrtoHash, $secret_key);
    }

    /**
     * function to get error message
     * @return string error message
     */
    public function getErrorString(){
        return $this->errorString;
    }

    /**
     * function to check if there is an error
     * @return boolean true/false
     */
    public function hasError(){
        if($this->errorString == ""){
            return false;
        }else{
            return true;
        }
    }

}
