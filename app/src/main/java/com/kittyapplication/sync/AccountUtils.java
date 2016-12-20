package com.kittyapplication.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.kittyapplication.providers.KittyBeeContract;

import java.io.IOException;


/**
 * Created by Scorpion on 02-09-2015.
 */
public class AccountUtils {
    /**
     * @param context
     * @return
     */
    public static boolean isAccountExist(final Context context) {
        try {
            AccountManager accountManager = AccountManager.get(context);
            Account availableAccounts[] = accountManager
                    .getAccountsByType(AccountConstants.ACCOUNT_TYPE);
            if (availableAccounts.length == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create new account of type
     *
     * @param context
     * @return true if created else false
     */
    public static boolean addAccount(final Context context, final String username, final String password) {
        boolean result = false;
        try {
            AccountManager accountManager = AccountManager.get(context);
            Account account = new Account(username, AccountConstants.ACCOUNT_TYPE);
            result = accountManager.addAccountExplicitly(account, password, null);
            if (result) {
                Account availableAccounts[] = accountManager
                        .getAccountsByType(AccountConstants.ACCOUNT_TYPE);

                if (availableAccounts.length > 0) {
                    Account myAccount = availableAccounts[0];
                    ContentResolver.setSyncAutomatically(myAccount,
                            KittyBeeContract.AUTHORITY, true);
                    ContentResolver.addPeriodicSync(
                            myAccount,
                            KittyBeeContract.AUTHORITY,
                            Bundle.EMPTY,
                            AccountConstants.SYNC_INTERVAL);

                    // Request to sync immediately
                    /*ContentResolver.requestSync(myAccount,
                            KittyBeeContract.AUTHORITY, Bundle.EMPTY);*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param context
     */
    public static void removeAccount(final Context context) {
        try {
            AccountManager accountManager = AccountManager.get(context);
            Account availableAccounts[] = accountManager
                    .getAccountsByType(AccountConstants.ACCOUNT_TYPE);
            if (availableAccounts.length == 0) {
                accountManager.removeAccount(availableAccounts[0],
                        new AccountManagerCallback<Boolean>() {
                            @Override
                            public void run(AccountManagerFuture<Boolean> future) {
                                try {
                                    future.getResult();
                                } catch (OperationCanceledException e) {
                                } catch (AuthenticatorException e) {
                                } catch (IOException e) {
                                }
                            }
                        }, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isSyncEnabled(Context context) {
        try {
            AccountManager accountManager = AccountManager.get(context);
            Account availableAccounts[] = accountManager
                    .getAccountsByType(AccountConstants.ACCOUNT_TYPE);
            if (availableAccounts.length > 0) {
                if (ContentResolver.isSyncActive(availableAccounts[0], KittyBeeContract.AUTHORITY)) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}