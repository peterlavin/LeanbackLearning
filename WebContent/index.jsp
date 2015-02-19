<!--
/*
 *
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Taken from ... (Jan 2015)
 * https://github.com/googleplus/gplus-quickstart-javascript/blob/master/index.html
 *
 */
-->

<!DOCTYPE html>
<html>
<head>
  <title>LeanbackLearning</title>
  <script src="https://apis.google.com/js/client:platform.js" async defer></script>
  
  
  <!-- JavaScript specific to this application that is not related to API calls -->	
  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js" ></script>
  
  		
  		<!-- 		Two imports related to bootstrap -->
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
		
        <script src="bootstrap/3.2.0/js/bootstrap.min.js"></script>
		
		<!-- 		Bootstrap css  -->
		<meta name="viewport" content="width=device-width, initial-scale=1">
    	<link rel="stylesheet" href="bootstrap/3.2.0/css/bootstrap.min.css">
  

<!-- 		Own css file for local settings -->
  		<link rel="stylesheet" href="css/lbl_specific.css">


</head>


<body>


<!-- 	This div shows the red rectangular sign-in button -->
  <div id="gConnect">
    <button class="g-signin"
        data-scope="https://www.googleapis.com/auth/plus.login"
        data-requestvisibleactions="http://schemas.google.com/AddActivity"
        data-clientId="1014444466376-781shj0dnkd1igkfv39scjto8bs1kkdk.apps.googleusercontent.com"
        data-callback="onSignInCallback"
        data-theme="dark"
        data-cookiepolicy="single_host_origin">
    </button>
  </div>
  



  
  
  
  
<!--   Div which is hidden/shown when signin is successful -->
  <div id="authOps" style="display:none">
  









  

<!--   Put all lbl stuff in here -->

    <div class="container">
      <h2>Leanback Learning</h2>
      <form class="form-horizontal" role="form">
        <div class="form-group">
          <div class="col-sm-10">
            <input type="text" class="form-control" id="topics" placeholder="What do you want to learn about?">
          </div>
        </div>
      </form>
      
      <form class="form-inline" role="form">
      
        <label for="email">Output Language </label>
      
        <div class="form-group">
        	<button type="submit" class="btn btn-default">French</button>
        </div>
        
        <div class="form-group">
        	<button type="submit" class="btn btn-default">English</button>
        </div>
        
        <div class="form-group">
        	<button type="submit" class="btn btn-default">German</button>
        </div>

      </form>
      
      
      <form class="form-inline" role="form">
      
        <label for="email">Lesson level </label>
      
        <div class="form-group">
        	<button type="submit" class="btn btn-default">Overview</button>
        </div>
        
        <div class="form-group">
        	<button type="submit" class="btn btn-default">Normal</button>
        </div>
        
        <div class="form-group">
        	<button type="submit" class="btn btn-default">Detailed</button>
        </div>

      <div class="form-group">        
         <div class="col-sm-offset-2 col-sm-10">
           <button type="button" class="btn btn-success" OnClick="testFn()">Continue</button>
         </div>
      </div>
      </form>
      
    </div>


























<!-- 	end of Lbl stuff -->

<hr>



  <div id="usermsg"></div>
    
<!--     <p>If the user chooses to disconnect, the app must delete all stored information retrieved from Google for the given user.</p> -->
    


<!-- 		Not needed (for now) -->
<!--     <h2>User's profile information</h2> -->
    <div id="profile"></div>
    
    

<!-- 		Not needed (for now) -->
<!--     <h2>User's friends that are visible to this app</h2> -->
    <div id="visiblePeople"></div>

<!-- 		Not needed (for now) -->
<!--     <h2>Authentication Logs</h2> -->
    <pre id="authResult"></pre>
    
    <button id="disconnect" >Disconnect your Google account from Leanback Learning</button>
  
  </div>
  
  
  
  
  
  
  
<!--   Not usually seen, if things work, this is hidden -->
  <div id="loaderror">
    This section will be hidden by JQuery. If you can see this message, you
    may be viewing the file rather than running a web server.<br />
    The sample must be run from http or https. See instructions at
    <a href="https://developers.google.com/+/quickstart/javascript">
    https://developers.google.com/+/quickstart/javascript</a>.
  </div>
  
  
  
  
  
  
<script type="text/javascript">
var helper = (function() {
  var BASE_API_PATH = 'plus/v1/';
  return {
    /**
     * Hides the sign in button and starts the post-authorization operations.
     *
     * @param {Object} authResult An Object which contains the access token and
     *   other authentication information.
     */
    onSignInCallback: function(authResult) {
      gapi.client.load('plus','v1').then(function() {
    	  
//     	Not needed  
//         $('#authResult').html('Auth Result:<br/>');
//         for (var field in authResult) {
//           $('#authResult').append(' ' + field + ': ' +
//               authResult[field] + '<br/>');
//         }
        
//         This is needed as it shows the authOps div, hides/shows the sign-in button and the page content div
        if (authResult['access_token']) {
          $('#authOps').show('slow');
          $('#gConnect').hide();
          helper.profile();
          helper.people();
        } else if (authResult['error']) {
          // There was an error, which means the user is not signed in.
          // As an example, you can handle by writing to the console:
          console.log('There was an error: ' + authResult['error']);
          
  
          
//           Removed as it shows 'Logged out' in error at first attempt to login
//           $('#authResult').append('Logged out');
          
          $('#authOps').hide('slow');
          $('#gConnect').show();
        }
        
        console.log('authResult', authResult);
        
      });
    },
    
    /**
     * Calls the OAuth2 endpoint to disconnect the app for the user.
     */
    disconnect: function() {
      // Revoke the access token.
      $.ajax({
        type: 'GET',
        url: 'https://accounts.google.com/o/oauth2/revoke?token=' +
            gapi.auth.getToken().access_token,
        async: false,
        contentType: 'application/json',
        dataType: 'jsonp',
        success: function(result) {
          console.log('revoke response: ' + result);
          $('#authOps').hide();
          $('#profile').empty();
          $('#visiblePeople').empty();
          $('#authResult').empty();
          $('#gConnect').show();
        },
        error: function(e) {
          console.log(e);
        }
      });
    },
    /**
     * Gets and renders the list of people visible to this app.
     */
    people: function() {
      gapi.client.plus.people.list({
        'userId': 'me',
        'collection': 'visible'
      }).then(function(res) {
    	  
//    	   removed as not needed
//         var people = res.result;
        $('#visiblePeople').empty();
        
        // removed as not needed
//         $('#visiblePeople').append('Number of people visible to this app: ' +
//             people.totalItems + '<br/>');
//         for (var personIndex in people.items) {
//           person = people.items[personIndex];
//           $('#visiblePeople').append('<img src="' + person.image.url + '">');
//         }
      });
    },
    /**
     * Gets and renders the currently signed in user's profile data.
     */
    profile: function(){
      gapi.client.plus.people.get({
        'userId': 'me'
      }).then(function(res) {
        var profile = res.result;
        $('#profile').empty();
        
        
//         Adds the user's image
//         $('#profile').append($('<p><img src=\"' + profile.image.url + '\"></p>'));





        
        $('#profile').append($('<p>' + profile.displayName + ', unique ID is: ' + profile.id + '<br /></p>'));
        
//         if (profile.cover && profile.coverPhoto) {
//           $('#profile').append(
//               $('<p><img src=\"' + profile.cover.coverPhoto.url + '\"></p>'));
//         }
        
        
//      added PL
        $('#usermsg').empty();
        $('#usermsg').append($('<h1>' + profile.displayName + '</h1>'));
        
      }, function(err) {
        var error = err.result;
        $('#profile').empty();
        $('#profile').append(error.message);
      });
    }
  };
})();

/**
 * jQuery initialization
 */
$(document).ready(function() {
  $('#disconnect').click(helper.disconnect);
  $('#loaderror').hide();
  if ($('[data-clientid="YOUR_CLIENT_ID"]').length > 0) {
    alert('This sample requires your OAuth credentials (client ID) ' +
        'from the Google APIs console:\n' +
        '    https://code.google.com/apis/console/#:access\n\n' +
        'Find and replace YOUR_CLIENT_ID with your client ID.'
    );
  }
});
/**
 * Calls the helper method that handles the authentication flow.
 *
 * @param {Object} authResult An Object which contains the access token and
 *   other authentication information.
 */
function onSignInCallback(authResult) {
  helper.onSignInCallback(authResult);
}
</script>


</body>
</html>