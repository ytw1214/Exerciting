package com.exerciting.Exerciting.Domain.matching.matching.event;

import java.util.List;

public record MatchingCompletedEvent (
        Long matchingId,
        Long hostUserId,
        List<Long> attendedUserIds
){
}
