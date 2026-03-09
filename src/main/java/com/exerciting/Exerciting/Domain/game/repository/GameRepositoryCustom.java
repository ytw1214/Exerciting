package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;

import java.util.List;

public interface GameRepositoryCustom {
    List<GameQueryResponseDto> search(GameCustomCond gameCustomCond);
}
