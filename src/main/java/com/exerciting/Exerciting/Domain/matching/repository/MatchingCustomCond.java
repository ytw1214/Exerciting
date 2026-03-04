package com.exerciting.Exerciting.Domain.matching.repository;
import org.springframework.util.StringUtils;

public record MatchingCustomCond(String title, String description, String TeamName) {

    public MatchingCustomCond {
        title = StringUtils.hasText(title) ? title : null;
        description = StringUtils.hasText(description) ? description : null;
        teamName = StringUtils.hasText(teamName) ? teamName : null;
    }
}
