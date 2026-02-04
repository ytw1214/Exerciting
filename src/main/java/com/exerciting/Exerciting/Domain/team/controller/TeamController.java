package com.exerciting.Exerciting.Domain.team.controller;

import com.exerciting.Exerciting.Domain.team.service.TeamService;
import com.exerciting.Exerciting.Domain.team.dto.TeamResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {
    private final TeamService teamService;
    /*
    public TeamController(TeamRepository teamRepository, TeamService teamService) {
        this.teamRepository = teamRepository;
        this.teamService = teamService;
    }

     */
    /*
    @GetMapping("/search")
    public String searchTeam(@RequestParam("teamName") String teamName, Model model) {

        teamRepository.findByName(teamName)
                .ifPresentOrElse(
                        team -> model.addAttribute("team", team),
                        () -> model.addAttribute("team", null)
                );

        return "team"; // team.html
    }
     */

    @GetMapping("/search")
    public ResponseEntity<TeamResponseDto> getTeam(@RequestParam(name="name")String name) {
        TeamResponseDto team = teamService.getTeam(name);

        return ResponseEntity.ok(team);
    }
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponseDto> getTeam(@PathVariable Long teamId) {

        return ResponseEntity.ok(teamService.getTeam(teamId));
    }
    @GetMapping("/team/{teamId}")
    public String getTeamPage(@PathVariable Long teamId, Model model) {
        TeamResponseDto teamDto = teamService.getTeam(teamId);
        model.addAttribute("team", teamDto); // "team"이라는 이름으로 DTO 전달
        return "team"; // teamDetail.html 파일을 찾아감
    }
}
