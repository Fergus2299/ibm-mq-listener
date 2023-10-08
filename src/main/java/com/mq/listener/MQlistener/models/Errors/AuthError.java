package com.mq.listener.MQlistener.models.Errors;

import java.util.Map;

public class AuthError extends Error {

    private String userId;

    public AuthError(int count, 
    		String userId, 
    		String appName
    		) {
        super(count, appName);
        this.userId = userId;
    }


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


    @Override
    public String toString() {
        return super.toString().replace("}", "") +
               ", userId='" + userId + '\'' +
               " }";
    }
    
    
    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> map = super.toHashMap();
        map.put("userId", this.userId);
        return map;
    }


}    

