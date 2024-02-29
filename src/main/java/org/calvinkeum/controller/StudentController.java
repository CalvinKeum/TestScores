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
import org.calvinkeum.dto.StudentAvgScoreResponse;
import org.calvinkeum.dto.StudentPaginationResponse;
import org.calvinkeum.dto.StudentResponse;
import org.calvinkeum.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student", description = "Student APIs")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Get all Students that have received at least one test score")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found Students", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentResponse.class)) }),
        @ApiResponse(responseCode = "404", description = "Students not found", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content)})
    @RateLimiter(name = "student", fallbackMethod = "studentFallbackMethod")
    @GetMapping
    public ResponseEntity<?> getAllStudents(@RequestParam(defaultValue = "0") int skip,
                                            @RequestParam(defaultValue = "20") int limit,
                                            @RequestParam(defaultValue = "ASC") String sort_order) {
        log.info("GET /api/students called with skip={}, limit={}, sort_order={}",
                skip, limit, sort_order);
        StudentPaginationResponse studentPaginationResponse = studentService.getAllStudents(skip, limit, sort_order);

        if (studentPaginationResponse == null || studentPaginationResponse.getTotalStudents() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No results found.");
        }

        return ResponseEntity.ok(studentPaginationResponse);
    }

    @Operation(summary = "Get a Student's average score across all exams")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the Student", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentAvgScoreResponse.class)) }),
        @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
        @ApiResponse(responseCode = "404", description = "Student not found", content = @Content), })
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content)
    @RateLimiter(name = "student", fallbackMethod = "studentFallbackMethod")
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentAverageScore(@PathVariable String id) {
        log.info("GET /api/students/{id} called...");
        StudentAvgScoreResponse studentAvgScoreResponse = studentService.getStudentAverageScore(id);

        if (studentAvgScoreResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No results found for the provided id.");
        }

        return ResponseEntity.ok(studentAvgScoreResponse);
    }

    public ResponseEntity<?> studentFallbackMethod(Throwable throwable) {
        if (throwable instanceof RequestNotPermitted) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Please try again later.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fallback method invoked.");
        }
    }
}
