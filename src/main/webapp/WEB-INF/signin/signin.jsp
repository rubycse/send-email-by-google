<html itemscope itemtype="http://schema.org/Article">
<head>
    <title>Sign in by Google</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js">
    </script>
    <script src="https://apis.google.com/js/client:platform.js?onload=start" async defer>
    </script>
    <script>
        function start() {
            gapi.load('auth2', function() {
                auth2 = gapi.auth2.init({
                    client_id: '${gapiClientId}'
                });
            });
        }
    </script>
</head>
<body>
<button id="signinButton">Sign in with Google</button>
<script>
    var scopes =
            'https://www.googleapis.com/auth/gmail.readonly '+
                    'https://www.googleapis.com/auth/gmail.send';
    $('#signinButton').click(function() {
        auth2.grantOfflineAccess({'scope': 'https://www.googleapis.com/auth/gmail.readonly https://www.googleapis.com/auth/gmail.send', 'redirect_uri': 'postmessage'})
                .then(signInCallback);
    });

    function signInCallback(authResult) {
        if (authResult['code']) {

            // Hide the sign-in button now that the user is authorized, for example:
            $('#signinButton').attr('style', 'display: none');

            // Send the code to the server
            $.post('<c:url value="validateSignIn"/>',
                    {authCode: authResult['code']}, function (data) {
                    }).done(function (xhr, status, error) {
                        if (xhr == "SUCCESS") {
                            window.location = '<c:url value="showSignedIn"/>';
                        }
                    });
        } else {
            alert("Error while signing in");
        }
    }
</script>
</body>
