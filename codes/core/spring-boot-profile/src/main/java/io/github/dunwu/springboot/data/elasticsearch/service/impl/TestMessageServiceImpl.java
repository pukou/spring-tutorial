package io.github.dunwu.springboot.data.elasticsearch.service.impl;

import io.github.dunwu.springboot.data.elasticsearch.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestMessageServiceImpl implements MessageService {

    @Value("${dunwu.message:Test Begin!}")
    private String message;

    @Override
    public String getMessage() {
        return this.message;
    }

}