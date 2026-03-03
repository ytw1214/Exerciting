package com.exerciting.Exerciting.Domain.matching.repository;

import lombok.Getter;
import org.springframework.util.StringUtils;

public record MatchingCustomCond(String title, String description) {

    public MatchingCustomCond {
        title = StringUtils.hasText(title) ? title : null;
        description = StringUtils.hasText(description) ? description : null;
    }

}
