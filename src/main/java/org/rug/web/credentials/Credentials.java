package org.rug.web.credentials;


public class Credentials {

    public static Credentials noCredentials(){
        return new Credentials(null, null, null);
    }

    private String authToken;
    private String username;
    private String password;

    public Credentials(){}

    public Credentials(String authToken) {
        this.authToken = authToken;
    }

    public Credentials(String username, String password, String authToken) {
        this.username = username;
        this.password = password;
        this.authToken = authToken;
    }

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
