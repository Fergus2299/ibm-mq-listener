package com.mq.listener.MQlistener.utils;

import java.util.Map;

import com.mq.listener.MQlistener.models.Issue.Issue;

public class ConsoleLogger {
    // Print current issues to the console for a datastructure which has {mqobjects: issues}
	public static void printQueueCurrentIssues(Map<String, ? extends Issue> issueObjectMap, String title) {
	    System.out.println("---- " + title + " ----");
	    if (issueObjectMap.isEmpty()) {
	        System.out.println("No issues detected.");
	    } else {
	        issueObjectMap.forEach((queue, issue) -> {
	            System.out.println("Issue detected for: " + queue);
	            issue.printIssueDetails();
	            System.out.println("-------------------------------");
	        });
	    }
	    System.out.println("--------------------------");
	}
}
