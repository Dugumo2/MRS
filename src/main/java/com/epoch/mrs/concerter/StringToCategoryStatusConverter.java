package com.epoch.mrs.concerter;

import com.epoch.mrs.domain.enums.CategoryStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToCategoryStatusConverter implements Converter<String, CategoryStatus> {

    @Override
    public CategoryStatus convert(String source) {
        return CategoryStatus.fromValue(source);
    }
}