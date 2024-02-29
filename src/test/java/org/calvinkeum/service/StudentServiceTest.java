package org.calvinkeum.service;

import org.calvinkeum.dto.StudentAvgScoreResponse;
import org.calvinkeum.dto.StudentPaginationResponse;
import org.calvinkeum.model.ExamStats;
import org.calvinkeum.model.StudentExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudentServiceTest {

    private StudentService studentService;

    @BeforeEach
    public void setUp() {
        studentService = new StudentService();
        StudentService.studentIdExamStatsMap = new TreeMap<>();
    }

    @Test
    public void testGetAllStudents_ReturnsEmptyListWhenNoData() {
        StudentPaginationResponse allStudents = studentService.getAllStudents(0, 10, "ASC");
        assertNotNull(allStudents);
        assertEquals(0, allStudents.getTotalStudents());
    }

    @Test
    public void testGetStudentAverageScore_ReturnsNullForNonExistentStudent() {
        StudentAvgScoreResponse studentAvgScoreResponse = studentService.getStudentAverageScore("nonExistentId");
        assertNull(studentAvgScoreResponse);
    }

    @Test
    public void testGetStudentAverageScore_ReturnsNullWhenNoScoresAvailable() {
        TreeMap<String, ExamStats> studentIdExamStatsMapMock = mock(TreeMap.class);
        StudentService.studentIdExamStatsMap = studentIdExamStatsMapMock;
        when(studentIdExamStatsMapMock.get("John.Doe")).thenReturn(null);

        StudentAvgScoreResponse studentAvgScoreResponse = studentService.getStudentAverageScore("John.Doe");
        assertNull(studentAvgScoreResponse);
    }

    @Test
    public void testProcessStudentData_UpdatesExamStatsCorrectly() {
        StudentExamScore studentExamScore = new StudentExamScore("John.Doe", 1000, 0.5357000219593212);
        StudentService.processStudentData(studentExamScore);

        ExamStats examStats = StudentService.studentIdExamStatsMap.get("John.Doe");
        assertNotNull(examStats);
        assertEquals(1, examStats.getExamCount());
        assertEquals(0.5357000219593212, examStats.getScoreSum());
    }

    @Test
    public void testProcessStudentData_UpdatesMultipleExamStatsCorrectly() {
        StudentExamScore studentExamScore1 = new StudentExamScore("John.Doe", 1000, 0.5357000219593212);
        StudentExamScore studentExamScore2 = new StudentExamScore("John.Doe", 1001, 0.780310326039997);
        StudentExamScore studentExamScore3 = new StudentExamScore("John.Doe", 1002, 0.7161821077444079);

        StudentService.processStudentData(studentExamScore1);
        StudentService.processStudentData(studentExamScore2);
        StudentService.processStudentData(studentExamScore3);

        ExamStats examStats = StudentService.studentIdExamStatsMap.get("John.Doe");
        assertNotNull(examStats);
        assertEquals(3, examStats.getExamCount());
        assertEquals(2.032192455743726, examStats.getScoreSum());
    }
}
