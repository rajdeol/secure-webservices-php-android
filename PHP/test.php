<?php
  // DB credentials change as per your setup
  define('DB_HOSTNAME', 'HOSTNAME_OF_DB');
  define('DB_USERNAME', 'USER_NAME_OF_DB');
  define('DB_PASSWORD', 'PASSWORD_OF_DB');
  define('DB_DATABASE', 'NAME_OF_DB');


  // verify API request
  require_once('apiaccess.php');
  $response = array();
  $response["success"] = 0;
  $api = new ApiAccess();
  if (!$api->validateRequest()) {
      // exit here if authentication fails
      // print error message
      $response["error-msg"] = $api->getErrorString();
    	echo json_encode($response);
    	die();
  }
  // do your regular coding after this
  $response["success"] = 1;
  echo json_encode($response);
?>
