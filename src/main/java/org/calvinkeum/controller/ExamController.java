package org.calvinkeum.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.calvinkeum.dto.ExamAvgScoreResponse;
import org.calvinkeum.dto.ExamPaginationResponse;
import org.calvinkeum.dto.ExamResponse;
import org.calvinkeum.service.ExamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Exam", description = "Exam APIs")
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "Get all Exams")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found exams", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ExamResponse.class)) }),
        @ApiResponse(responseCode = "404", description = "Exams not found", content = @Content), })
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content)
    @RateLimiter(name = "exam", fallbackMethod = "examFallbackMethod")
    @GetMapping
    public ResponseEntity<?> getAllExams(@RequestParam(defaultValue = "0") int skip,
                                         @RequestParam(defaultValue = "20") int limit,
                                         @RequestParam(defaultValue = "ASC") String sort_order) {
        log.info("GET /api/exams with skip={}, limit={}, sort_order={}",
                skip, limit, sort_order);
        ExamPaginationResponse examPaginationResponse = examService.getAllExams(skip, limit, sort_order);

        if (examPaginationResponse == null || examPaginationResponse.getTotalExams() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No results found.");
        }

        return ResponseEntity.ok(examPaginationResponse);
    }

    @Operation(summary = "Retrieve the exam details based on its unique identifier, includes the individual student scores and the average score calculated across all students.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the Exam", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ExamAvgScoreResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "Invalid number supplied", content = @Content),
        @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content), })
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content)
    @RateLimiter(name = "exam", fallbackMethod = "examFallbackMethod")
    @GetMapping("/{number}")
    public ResponseEntity<?> getExamResults(@PathVariable Integer number) {
        log.info("GET /api/exams/{number}");
        ExamAvgScoreResponse examAvgScoreResponse = examService.getExamResults(number);

        if (examAvgScoreResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No results found for the provided number.");
        }

        return ResponseEntity.ok(examAvgScoreResponse);
    }

    public ResponseEntity<?> examFallbackMethod(Throwable throwable) {
        if (throwable instanceof RequestNotPermitted) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Please try again later.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fallback method invoked.");
        }
    }

}
