package com.exerciting.Exerciting.Controller;

import com.exerciting.Exerciting.Entity.Team;
import com.exerciting.Exerciting.Repository.TeamRepository;
import com.exerciting.Exerciting.Service.TeamService;
import com.exerciting.Exerciting.dto.Team.TeamResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

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
