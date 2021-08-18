package com.jay.instasearch.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jay.instasearch.pojo.SearchSchema;
import com.jay.instasearch.service.ElasticSearchService;
import com.jay.instasearch.service.PostSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {
    @Autowired
    PostSearchService postSearchService;

    @Override
    public List<Long> search(String searchInput) {
        List<SearchSchema> result = doSearch(searchInput);
        List<Long> resultIdList = new LinkedList<>();
        for (SearchSchema searchSchema : result) {
            resultIdList.add(searchSchema.getPostId());
        }
        return resultIdList;
    }

    @Override
    public List<SearchSchema> doSearch(String searchInput) {
        JSONObject jsonObject = splitSearchInput(searchInput);
        if (jsonObject.containsKey("username")) {
            return postSearchService.getPostsByUsername((String) jsonObject.get("username"));
        }
        Map<Long, SearchSchema> resultMap = new HashMap<>();
        List<SearchSchema> resultList = new LinkedList<>();
        if (jsonObject.containsKey("caption")) {
            resultList.addAll(postSearchService.getPostByCaption((String) jsonObject.get("caption")));
        }
        if (jsonObject.containsKey("hashtags")) {
            resultList.addAll(postSearchService.getPostsByHashtags((List<String>) jsonObject.get("hashtags")));
        }
        for (SearchSchema schema : resultList) {
            resultMap.putIfAbsent(schema.getPostId(), schema);
        }
        return resultMap.values().stream().toList();
    }

    private JSONObject splitSearchInput(String searchInput) {
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("hashtags", new LinkedList<String>());
//        jsonObject.put("caption", null);
//        jsonObject.put("username", null);
        String[] queryList = searchInput.split(" ");
        if (queryList.length == 1) {
            jsonObject.put("username", queryList[0]);
            return jsonObject;
        }
        String caption = "";
        List<String> hashtags = new LinkedList<>();
        StringBuilder captionBuilder = new StringBuilder();
        for (String query : queryList) {
            if (query.startsWith("#")) {
                hashtags.add(query);
            } else {
                if (captionBuilder.isEmpty()) {
                    captionBuilder.append(query);
                } else {
                    captionBuilder.append(" ").append(query);
                }
            }
        }
        if (!captionBuilder.isEmpty()) {
            caption = captionBuilder.toString();
            jsonObject.put("caption", caption);
        }
        if (hashtags.size() != 0) {
            jsonObject.put("hashtags", hashtags);
        }
        return jsonObject;
    }
}