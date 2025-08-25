package com.example.andrapp.legacy;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private static UserSessionManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.commit();
    }

    public User getLoggedInUser() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        if (userId != null && username != null) {
            return new User(userId, username);
        }
        return null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
