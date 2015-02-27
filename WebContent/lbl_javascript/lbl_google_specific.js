/**
 * 
 * This file contains all javascript related to google login in.
 * 
 * The $(document).ready(function() { ... } also contains some additional
 * lines required by other functionality on the page.
 * 
 */



/**
 * jQuery initialization
 */

/*
 * These are declared and set here, and are then visible in lbl_specific.js
 */
 var idnum = ""; 
 var name = "";


$(function() {
	
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
	  
	  // Specific to lbl initial setup
	  $('#time_options').hide();
	  
	});
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


function signinCallback(authResult) {
	
	  if (authResult['status']['signed_in']) {
	    // Update the app to reflect a signed in user
	    // Hide the sign-in button now that the user is authorized, for example:
	    document.getElementById('signinButton').setAttribute('style', 'display: none');
	  } else {
	    // Update the app to reflect a signed out user
	    // Possible error values:
	    //   "user_signed_out" - User is signed-out
	    //   "access_denied" - User denied access to your app
	    //   "immediate_failed" - Could not automatically log in the user
	    console.log('Sign-in state: ' + authResult['error']);
	  }
	}

//////////////////

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
	    	  
	        
//	         This is needed as it shows the authOps div, hides/shows the sign-in button and the page content div
	        if (authResult['access_token']) {
	          $('#authOps').show();
	          $('#gConnect').hide();
	          
	          // Added to hide parts of authOps until needed
	          $('#time_options').hide();
	          
	          $('#logo_signin').hide();
	          helper.profile();
	          helper.people();
	        } else if (authResult['error']) {
	          // There was an error, which means the user is not signed in.
	          // As an example, you can handle by writing to the console:
	          console.log('There was an error: ' + authResult['error']);
	          
	          
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
	          
	          // Added to show logo after logout
	          $('#logo_signin').show();
	          
	          
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
	    	  
//	    	   removed as not needed
//	         var people = res.result;
	        $('#visiblePeople').empty();

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
	        
	        

	        
	        $('#profile').append($('<h5>Unique user ID: ' + profile.id + '</h5>'));
	        
	        // Sets the id variable
	        idnum = profile.id;
	        console.log(idnum);
	        
	        
//	      added PL
	        $('#usermsg').empty();
	        $('#usermsg').append($('<h4>Welcome ' + profile.displayName + '</h4>'));
	        
	        // Sets the name variable
	        name = profile.displayName;
	        console.log(name);        
	        
	        
	        
	      }, function(err) {
	        var error = err.result;
	        $('#profile').empty();
	        $('#profile').append(error.message);
	      });
	    }
	    
	  };
	})();