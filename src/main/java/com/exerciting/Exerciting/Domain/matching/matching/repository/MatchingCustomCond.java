package com.exerciting.Exerciting.Domain.matching.matching.repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public record MatchingCustomCond(String title, String description, String teamName, LocalDateTime meetTime) {

    public MatchingCustomCond {
        title = StringUtils.hasText(title) ? title : null;
        description = StringUtils.hasText(description) ? description : null;
        teamName = StringUtils.hasText(teamName) ? teamName : null;
    }
}
