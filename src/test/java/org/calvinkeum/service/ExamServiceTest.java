package org.calvinkeum.service;

import org.calvinkeum.dto.ExamAvgScoreResponse;
import org.calvinkeum.dto.ExamPaginationResponse;
import org.calvinkeum.model.StudentExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class ExamServiceTest {

    private ExamService examService;

    @BeforeEach
    public void setUp() {
        examService = new ExamService();
        ExamService.examEntriesMap = new TreeMap<>();
        ExamService.cachedExamAvgScoreResponseMap = new TreeMap<>();
    }

    @Test
    public void testGetAllExams_ReturnsEmptyListWhenNoData() {
        ExamPaginationResponse allExams = examService.getAllExams(0, 10, "ASC");
        assertNotNull(allExams);
        assertTrue(allExams.getExamResponses().isEmpty());
    }

    @Test
    public void testGetExamResults_ComputesAverageScoreCorrectly() {
        StudentExamScore studentExamScore = new StudentExamScore("John.Doe", 1000, 0.7225095851635466);
        ExamService.processExamData(studentExamScore);

        studentExamScore =  new StudentExamScore("Jane.Doe", 1000, 0.6592995722194341);
        ExamService.processExamData(studentExamScore);

        studentExamScore = new StudentExamScore("Dohn.Joe", 1000, 0.9085568050082964);
        ExamService.processExamData(studentExamScore);

        ExamAvgScoreResponse examAvgScoreResponse = examService.getExamResults(1000);
        assertNotNull(examAvgScoreResponse);
        assertEquals(1000, examAvgScoreResponse.getExam());
        assertNotNull(examAvgScoreResponse.getStudents());
        assertEquals(3, examAvgScoreResponse.getStudents().size());
        assertEquals(0.7634553207970924, examAvgScoreResponse.getAverageScore());
    }

    @Test
    public void testGetExamResults_ReturnsCachedResultIfAvailable() {
        StudentExamScore studentExamScore = new StudentExamScore("John.Doe", 1000, 0.8124807009026258);
        ExamService.processExamData(studentExamScore);

        assertTrue(ExamService.cachedExamAvgScoreResponseMap.isEmpty());
        ExamAvgScoreResponse examAvgScoreResponse = examService.getExamResults(1000);
        assertEquals(ExamService.cachedExamAvgScoreResponseMap.get(1000), examAvgScoreResponse);
    }

    @Test
    public void testGetExamResults_ReturnsNullForNonExistentExam() {
        ExamAvgScoreResponse examAvgScoreResponse = examService.getExamResults(1);
        assertNull(examAvgScoreResponse);
    }

    @Test
    public void testProcessExamData_UpdatesExamEntriesCorrectly() {
        StudentExamScore studentExamScore = new StudentExamScore("John.Doe", 1000, 0.8124807009026258);
        ExamService.processExamData(studentExamScore);

        List<StudentExamScore> studentExamScores = ExamService.examEntriesMap.get(1000);
        assertNotNull(studentExamScores);
        assertEquals(1, studentExamScores.size());
        assertEquals(studentExamScore, studentExamScores.get(0));
    }
}
