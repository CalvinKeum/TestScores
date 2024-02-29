package org.calvinkeum.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.calvinkeum.dto.StudentAvgScoreResponse;
import org.calvinkeum.dto.StudentPaginationResponse;
import org.calvinkeum.dto.StudentResponse;
import org.calvinkeum.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllStudents_ReturnsOkWithValidResponse() {
        StudentPaginationResponse mockResponse = new StudentPaginationResponse();
        mockResponse.setTotalStudents(50);
        mockResponse.setSkip(0);
        mockResponse.setLimit(10);
        mockResponse.setStudentResponses(Collections.singletonList(new StudentResponse()));

        when(studentService.getAllStudents(0, 10, "ASC")).thenReturn(mockResponse);
        ResponseEntity<?> responseEntity = studentController.getAllStudents(0, 10, "ASC");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetAllStudents_RateLimiterExceeded() throws Exception {
        Constructor<RequestNotPermitted> constructor = RequestNotPermitted.class.getDeclaredConstructor(String.class, boolean.class);
        constructor.setAccessible(true);

        RequestNotPermitted requestNotPermitted = constructor.newInstance("Rate limit exceeded", false);
        when(studentService.getStudentAverageScore(anyString()))
                .thenThrow(requestNotPermitted);

        Exception exception = assertThrows(RequestNotPermitted.class, () -> {
            studentController.getStudentAverageScore("john.doe");
        });

        assertNotNull(exception);
        assertTrue(exception instanceof RequestNotPermitted);
    }

    @Test
    public void testGetAllStudents_ReturnsNoResultsFoundWithEmptyList() {
        when(studentService.getAllStudents(0, 10, "ASC")).thenReturn(new StudentPaginationResponse());
        ResponseEntity<?> responseEntity = studentController.getAllStudents(0, 10, "ASC");

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertEquals("No results found.", responseEntity.getBody());
    }

    @Test
    public void testGetStudentAverageScore_RateLimiterExceeded() throws Exception {
        Constructor<RequestNotPermitted> constructor = RequestNotPermitted.class.getDeclaredConstructor(String.class, boolean.class);
        constructor.setAccessible(true);

        RequestNotPermitted requestNotPermitted = constructor.newInstance("Rate limit exceeded", false);
        when(studentService.getAllStudents(anyInt(), anyInt(), anyString())).thenThrow(requestNotPermitted);

        Exception exception = assertThrows(RequestNotPermitted.class, () -> {
            studentController.getAllStudents(0, 20, "ASC");
        });

        assertNotNull(exception);
        assertTrue(exception instanceof RequestNotPermitted);
    }

    @Test
    public void testGetStudentAverageScore_ReturnsNotFoundForInvalidId() {
        when(studentService.getStudentAverageScore("1")).thenReturn(null);
        ResponseEntity<?> responseEntity = studentController.getStudentAverageScore("1");

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("No results found for the provided id.", responseEntity.getBody());
    }

    @Test
    public void testGetStudentAverageScore_ReturnsOkWithValidResponse() {
        StudentAvgScoreResponse mockResponse = new StudentAvgScoreResponse();
        when(studentService.getStudentAverageScore("1")).thenReturn(mockResponse);
        ResponseEntity<?> responseEntity = studentController.getStudentAverageScore("1");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
    }

    @Test
    public void testRateLimiterFallback() {
        Throwable throwable = new RuntimeException();
        ResponseEntity<?> responseEntity = studentController.studentFallbackMethod(throwable);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().toString().contains("Fallback method invoked."));
    }
}
