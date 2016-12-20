package com.riontech.socialconnection.listeners;

import com.riontech.socialconnection.model.SocialUserResponse;

/**
 * Created by Pintu Riontech on 28/7/16.
 * vaghela.pintu31@gmail.com
 */
public interface SocialConnectionListener {


    /**
     * On Success Response
     *
     * @param response
     */
    void onSuccess(SocialUserResponse response);

    /**
     * On Failure Response
     *
     * @param e
     */
    void onFailure(Exception e);
}
