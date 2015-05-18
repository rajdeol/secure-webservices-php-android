<?php
// DB credentials change as per your setup
define('DB_HOSTNAME', 'HOSTNAME_OF_DB');
define('DB_USERNAME', 'USER_NAME_OF_DB');
define('DB_PASSWORD', 'PASSWORD_OF_DB');
define('DB_DATABASE', 'NAME_OF_DB');


// verify API request
  require_once('apiaccess.php');  
    $api = new ApiAccess();                                   
    if (!$api->validateRequest()) {                             
        // exit here if authentication fails
        // print error message
        echo $api->getErrorString();
        exit;                                                     
    }
  // do your regular coding after this
?>
