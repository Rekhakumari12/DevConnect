package com.example.demo.controller;

import com.example.demo.dto.PostResponse;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping("api/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<PostResponse> posts = searchService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(posts);
    }
}
