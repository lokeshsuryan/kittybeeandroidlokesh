package com.kittyapplication.chat.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.kittyapplication.chat.R;
import com.kittyapplication.chat.utils.SharedPreferencesUtil;
import com.kittyapplication.chat.utils.qb.QBUserUtils;
import com.kittyapplication.core.ui.activity.CoreBaseActivity;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

/**
 * Created by MIT on 8/16/2016.
 */
public abstract class QBSignUpSingInActivity extends CoreBaseActivity {
    private static final String QUICK_BLOX_PASSWORD = "KittyBeeArun";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void signUp(final QBUser qbUser, final String profilePic)
    {
        if (!isValidData(qbUser.getLogin())) {
            return;
        }
//

        qbUser.setLogin("0" + qbUser.getLogin());
        qbUser.setPassword(QUICK_BLOX_PASSWORD);
        /*QBUsers.signUpSignInTask(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                qbUser.setPassword(QUICK_BLOX_PASSWORD);
                SharedPreferencesUtil.saveQbUser(qbUser);

                if (profilePic != null && profilePic.length() > 0)
                    QBUserUtils.updateProfilePicture(qbUser, profilePic, null);
                onSignUp(qbUser);
            }

            @Override
            public void onError(QBResponseException error) {
                showSnackbarError(getRootView(), R.string.errors, error, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qbUser.setLogin(qbUser.getLogin().substring(0));
//                        signUp(qbUser, profilePic);
                    }
                });
            }
        });*/

        QBUsers.signUpSignInTask(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {
                qbUser.setPassword(QUICK_BLOX_PASSWORD);
                SharedPreferencesUtil.saveQbUser(qbUser);

                if (profilePic != null && profilePic.length() > 0)
                    QBUserUtils.updateProfilePicture(qbUser, profilePic, null);
                onSignUp(qbUser);
            }

            @Override
            public void onError(QBResponseException errors) {
                showSnackbarError(getRootView(), R.string.errors, errors, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qbUser.setLogin(qbUser.getLogin().substring(0));
//                        signUp(qbUser, profilePic);
                    }
                });
            }
        });

    }


    public void signIn(final String login) {
        if (!isValidData(login)) {
            return;
        }

        final QBUser qbUser = new QBUser();
        qbUser.setLogin(login);
        qbUser.setPassword(QUICK_BLOX_PASSWORD);

        /*QBUsers.signIn(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                qbUser.setPassword(QUICK_BLOX_PASSWORD);
                SharedPreferencesUtil.saveQbUser(qbUser);
                onSignIn(qbUser);
            }

            @Override
            public void onError(QBResponseException errors) {
                showSnackbarError(getRootView(), R.string.errors, errors, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn(login);
                    }
                });
            }
        });*/

        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {

            @Override
            public void onSuccess(QBUser user, Bundle params) {
                qbUser.setPassword(QUICK_BLOX_PASSWORD);
                SharedPreferencesUtil.saveQbUser(qbUser);
                onSignIn(qbUser);
            }

            @Override
            public void onError(QBResponseException errors) {
                showSnackbarError(getRootView(), R.string.errors, errors, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn(login);
                    }
                });
            }
        });
    }

    public void logout() {
//        try {
//            QBUsers.signOut();
//        } catch (QBResponseException e) {
//            e.printStackTrace();
//        }
        try {
            QBUsers.signOut().perform();
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidData(String login) {
        if (TextUtils.isEmpty(login)) {
            return false;
        }
        return true;
    }

    protected abstract View getRootView();

    public void onSignUp(QBUser qbUser) {
    }

    public void onSignIn(QBUser qbUser) {
    }
}
