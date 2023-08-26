package com.mq.listener.MQlistener.models.Errors;

import java.util.List;
import java.util.Map;

public class AuthErrorDetails extends ErrorDetails {

    private String userId;

    public AuthErrorDetails(int count, 
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

