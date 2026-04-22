package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GameRepositoryCustom {
    List<GameQueryResponseDto> search(GameCustomCond gameCustomCond, LocalDateTime start, LocalDateTime end);
}
