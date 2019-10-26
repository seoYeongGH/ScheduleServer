package com.schedule.web;

public class USession {
	private static USession instance;

    private boolean isLogin;
    private String id;

    private USession(){
        isLogin = false;
        id = null;
    }

    public static USession getInstance(){
        if(instance == null)
            instance = new USession();

        return instance;
    }

    public void setIsLogin(boolean login) {
        isLogin = login;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsLogin() {
        return isLogin;
    }

    public String getId() {
        return id;
    }
}
