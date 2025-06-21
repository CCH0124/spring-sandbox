package com.example.firstcrud.controller;

import com.example.firstcrud.dtos.TutorialRequestDto;
import com.example.firstcrud.dtos.TutorialResponseDto;
import com.example.firstcrud.dtos.TutorialResponsePagingDto;
import com.example.firstcrud.service.TutorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Tutorial", description = "Tutorial management APIs")
public class TutorialController {

  @Autowired TutorialService tutorialService;

  @Operation(
      summary = "Retrieve all Tutorial by title.",
      description = "Get all Tutorial object by specifying title.",
      tags = {"tutorials", "get"})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
              schema = @Schema(implementation = TutorialResponsePagingDto.class),
              mediaType = "application/json")
        }),
    @ApiResponse(
        responseCode = "500",
        description = "System Error.",
        content = {@Content(schema = @Schema())})
  })
  @GetMapping("/tutorials")
  public ResponseEntity<TutorialResponsePagingDto> getAllTutorials(
      @Parameter(description = "Search by title.") @RequestParam(required = false, name = "title")
          final String title,
      @Parameter(description = "Page number, starting from 0", required = true)
          @RequestParam(defaultValue = "0", name = "page")
          int page,
      @Parameter(description = "Number of items per page", required = true)
          @RequestParam(defaultValue = "3", name = "size")
          int size) {
    var allTutorials = tutorialService.getAllTutorials(title, page, size);
    return ResponseEntity.ok(allTutorials);
  }

  @Operation(
      summary = "Retrieve a Tutorial by Id",
      description = "Get a Tutorial object by specifying its id. The response is Tutorial object with id, title, description and published status.",
      tags = { "tutorials", "get" })
  @ApiResponses({
    @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = TutorialResponseDto.class), mediaType = "application/json") }),
    @ApiResponse(responseCode = "404", description = "Not found Tutorial with id.", content = { @Content(schema = @Schema()) })
  })
  @GetMapping("/tutorials/{id}")
  public ResponseEntity<TutorialResponseDto> getTutorialById(@PathVariable("id") String id) {
    var tutorialData = tutorialService.getTutorialById(id);
    return ResponseEntity.ok(tutorialData);
  }

  @PostMapping("/tutorials")
  public ResponseEntity<TutorialResponseDto> createTutorial(
      @RequestBody TutorialRequestDto tutorial) {
    var data = tutorialService.createTutorial(tutorial);
    return new ResponseEntity<>(data, HttpStatus.CREATED);
  }

  @PutMapping("/tutorials/{id}")
  public ResponseEntity<TutorialResponseDto> updateTutorial(
      @PathVariable("id") String id, @RequestBody TutorialRequestDto tutorial) {
    TutorialResponseDto updateTutorial = tutorialService.updateTutorial(id, tutorial);
    return new ResponseEntity<>(updateTutorial, HttpStatus.OK);
  }

  @DeleteMapping("/tutorials/{id}")
  public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") String id) {
    try {
      tutorialService.deleteTutorial(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/tutorials")
  public ResponseEntity<HttpStatus> deleteAllTutorials() {
    try {
      tutorialService.deleteAllTutorials();
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/tutorials/published")
  public ResponseEntity<TutorialResponsePagingDto> findByPublished(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size) {
    var allTutorials = tutorialService.findByPublished(page, size);
    return ResponseEntity.ok(allTutorials);
  }
}
