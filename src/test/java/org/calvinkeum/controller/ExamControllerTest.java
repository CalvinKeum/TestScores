package org.calvinkeum.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.calvinkeum.dto.*;
import org.calvinkeum.service.ExamService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ExamControllerTest {

    @Mock
    private ExamService examService;

    @InjectMocks
    private ExamController examController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllExams_RateLimiterExceeded() throws Exception {
        Constructor<RequestNotPermitted> constructor = RequestNotPermitted.class.getDeclaredConstructor(String.class, boolean.class);
        constructor.setAccessible(true);

        RequestNotPermitted requestNotPermitted = constructor.newInstance("Rate limit exceeded", false);
        when(examService.getAllExams(anyInt(), anyInt(), anyString())).thenThrow(requestNotPermitted);

        Exception exception = assertThrows(RequestNotPermitted.class, () -> {
            examController.getAllExams(0, 20, "ASC");
        });

        assertNotNull(exception);
        assertTrue(exception instanceof RequestNotPermitted);
    }

    @Test
    public void testGetAllExams_ReturnsOkWithOneResult() {
        ExamPaginationResponse mockResponse = new ExamPaginationResponse();
        mockResponse.setTotalExams(10);
        mockResponse.setSkip(0);
        mockResponse.setLimit(10);
        mockResponse.setExamResponses(Collections.singletonList(new ExamResponse()));

        when(examService.getAllExams(0, 10, "ASC")).thenReturn(mockResponse);
        ResponseEntity<?> responseEntity = examController.getAllExams(0, 10, "ASC");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetAllExams_ReturnsNoContentWithEmptyList() {
        when(examService.getAllExams(0, 10, "ASC")).thenReturn(new ExamPaginationResponse());
        ResponseEntity<?> responseEntity = examController.getAllExams(0, 10, "ASC");

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertEquals("No results found.", responseEntity.getBody());
    }

    @Test
    public void testGetExamResults_RateLimiterExceeded() throws Exception {
        Constructor<RequestNotPermitted> constructor = RequestNotPermitted.class.getDeclaredConstructor(String.class, boolean.class);
        constructor.setAccessible(true);

        RequestNotPermitted requestNotPermitted = constructor.newInstance("Rate limit exceeded", false);
        when(examService.getExamResults(anyInt())).thenThrow(requestNotPermitted);

        Exception exception = assertThrows(RequestNotPermitted.class, () -> {
            examController.getExamResults(1000);
        });

        assertNotNull(exception);
        assertTrue(exception instanceof RequestNotPermitted);
    }

    @Test
    public void testGetExamResults_ReturnsOkWithValidResponse() {
        when(examService.getExamResults(1)).thenReturn(new ExamAvgScoreResponse());
        ResponseEntity<?> responseEntity = examController.getExamResults(1);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(ExamAvgScoreResponse.class, responseEntity.getBody().getClass());
    }

    @Test
    public void testGetExamResults_ReturnsNotFoundForInvalidNumber() {
        when(examService.getExamResults(1)).thenReturn(null);
        ResponseEntity<?> responseEntity = examController.getExamResults(1);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("No results found for the provided number.", responseEntity.getBody());
    }

    @Test
    public void testRateLimiterFallback() {
        Throwable throwable = new RuntimeException();
        ResponseEntity<?> responseEntity = examController.examFallbackMethod(throwable);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().toString().contains("Fallback method invoked."));
    }
}
