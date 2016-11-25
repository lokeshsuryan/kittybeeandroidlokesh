package com.riontech.socialconnection.social;

/**
 * Created by Pintu Riontech on 28/7/16.
 * vaghela.pintu31@gmail.com
 */
public class GooglePlusManager
{}



        /*implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    // for display log
    private static final String TAG = GooglePlusManager.class.getSimpleName();
    // for Context
    private Activity mActivity;
    // for connecting google
    public GoogleApiClient mGoogleApiClient;
    // for google connection result
    private ConnectionResult mConnectionResult;
    // for display loading dialog
    private ProgressDialog mDialog = null;
    // for permission code
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    // for token
    private String mAccessToken;
    // for callback
    private SocialConnectionListener mGooglePlusListener;
    // for social response object
    private SocialUserResponse mRespObject;

    *//**
     * Google Manager Constructor
     *
     * @param activity context
     * @param listener
     *//*
    public GooglePlusManager(Activity activity, SocialConnectionListener listener) {
        mGooglePlusListener = listener;
        mActivity = activity;
        initGoogle();
    }

    *//**
     * initialization Google
     *//*
    private void initGoogle() {
        if (SocialConnectionUtils.checkInternetConnection(mActivity)) {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage("Loading");
            mDialog.show();

            if (Build.VERSION.SDK_INT >= 23) {
                // Here, mActivityActivity is the current activity
                if (ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.GET_ACCOUNTS)
                        != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestAccountPermission();
                } else {
                    Log.d(getClass().getSimpleName(), "PERMISSION GRANTED... GO AHEAD..");
                    initGoogleAPI();
                }
            } else {
                initGoogleAPI();
            }
        } else {
            SocialConnectionUtils.showToast(mActivity.getResources().getString(R.string.no_internet_available), mActivity);
        }
    }

    *//**
     * initialization of Google API
     *//*
    private void initGoogleAPI() {
        mGoogleApiClient = (new GoogleApiClient.Builder(mActivity))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(new Scope("profile"))
                .addScope(new Scope("email"))
                .addScope(new Scope("https://www.googleapis.com/auth/plus.login"))
                .addScope(new Scope("https://www.googleapis.com/auth/plus.me"))
                .addScope(Plus.SCOPE_PLUS_PROFILE).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Plus.PeopleApi.loadVisible(mGoogleApiClient,
                (String) null).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.
                    getErrorDialog(connectionResult.getErrorCode(),
                            mActivity, 0).show();
        } else {
            mConnectionResult = connectionResult;
        }
        resolveSignInError();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(getClass().getSimpleName(), "PERMISSION GRANTED BY USER... GO AHEAD..");
                    initGoogleAPI();
                } else {
                    Log.d(getClass().getSimpleName(), "PERMISSION NOT GRANTED... BACK TO APP..");
                }
                break;

            default:
                mActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult loadPeopleResult) {
        Log.d(TAG, "result.getStatus():" + loadPeopleResult.getStatus());
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            mRespObject = new SocialUserResponse();
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

            String photo = currentPerson.getImage().getUrl();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String name = currentPerson.getDisplayName();
            String uID = currentPerson.getId();


            String location;
            if (currentPerson.hasPlacesLived()) {
                if (currentPerson.getPlacesLived().get(0).getValue() != null
                        && !currentPerson.getPlacesLived().get(0).getValue().isEmpty()) {

                    location = currentPerson.getPlacesLived().get(0).getValue();
                } else {
                    location = "NA";
                }
            } else {
                location = "NA";
            }


            String gender = String.valueOf(currentPerson.getGender());

            String Gender;
            if (gender.equalsIgnoreCase("0")) {
                Gender = "Male";
            } else if (gender.equalsIgnoreCase("1")) {
                Gender = "Female";
            } else {
                Gender = "Other";
            }
            mRespObject.setUserLocation(location);
            mRespObject.setUserName(name);
            mRespObject.setUserId(uID);
            mRespObject.setUserGender(Gender);
            mRespObject.setUserEmail(email);
            mRespObject.setImgUrl(photo);

            new GetToken().execute();
            *//* Log.d(TAG, "Access Token = " + mAccessToken);
            Log.d(TAG, "Person = " + currentPerson.toString());
            Log.d(TAG, "Photo = " + photo);
            Log.d(TAG, "Name = " + name);
            Log.d(TAG, "Email = " + email);
            Log.d(TAG, "Uid = " + uID);
            Log.d(TAG, "Location = " + location);
            Log.d(TAG, "Gender = " + Gender);*//*
        } else {
            Log.d(TAG, "ONConnected Fails");
        }
    }


    *//**
     * Get Google Token Task
     *//*
    private class GetToken extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            final String SCOPES = "https://www.googleapis.com/auth/plus.login ";
            try {
                mAccessToken = GoogleAuthUtil.getToken(
                        mActivity,
                        Plus.AccountApi.getAccountName(mGoogleApiClient),
                        "oauth2:" + SCOPES);
            } catch (IOException e) {
                mAccessToken = "NA";
                Log.e(TAG, e.getMessage(), e);
                mGooglePlusListener.onFailure(e);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage(), e);
                mAccessToken = "NA";
                mGooglePlusListener.onFailure(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRespObject.setUserAccessToken(mAccessToken);
            mRespObject.setUserName(mAccessToken);
            mGooglePlusListener.onSuccess(mRespObject);
        }
    }

    *//**
     * Request Permission for get Accounts
     *//*
    private void requestAccountPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                Manifest.permission.GET_ACCOUNTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    *//**
     * Resolving Error If Any occurred in Connection
     * And refuse the connection
     *//*
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mConnectionResult.startResolutionForResult(mActivity, 0);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
                mGooglePlusListener.onFailure(e);
            }
        }
    }*/


