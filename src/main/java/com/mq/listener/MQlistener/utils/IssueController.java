//package com.mq.listener.MQlistener.utils;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.mq.listener.MQlistener.models.Issue.Issue;
//
//@RestController
//public class IssueController {
//    private final IssueService issueService;
//
//    public IssueController(IssueService issueService) {
//        this.issueService = issueService;
//    }
//
//    @PostMapping("/post-issue")
//    public void postIssue(@RequestBody Issue issue) {
//        issueService.postIssue(issue);
//    }
//}