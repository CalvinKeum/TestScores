package org.calvinkeum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataImportServiceTest {
    private StudentService studentService;
    private ExamService examService;

    @BeforeEach
    public void setUp() {
        studentService = new StudentService();
        StudentService.studentIdExamStatsMap = new TreeMap<>();

        examService = new ExamService();
        ExamService.examEntriesMap = new TreeMap<>();
        ExamService.cachedExamAvgScoreResponseMap = new TreeMap<>();
    }

    @Test
    void importStudentExamData_ShouldImportDataWhenValid() {
        String validData = "data: {\"studentId\":\"john.doe\",\"exam\":1,\"score\":0.7428269186548633}";

        DataImportService.importStudentExamData(validData);

        assertEquals(1, studentService.getAllStudents(0,20,"ASC").getTotalStudents());
        assertEquals(1, examService.getAllExams(0, 20, "ASC").getTotalExams());
    }

    @Test
    void importStudentExamData_ShouldNotImportDataWhenInvalid() {
        String validData = "data: {}";

        DataImportService.importStudentExamData(validData);

        assertEquals(0, studentService.getAllStudents(0,20,"ASC").getTotalStudents());
        assertEquals(0, examService.getAllExams(0, 20, "ASC").getTotalExams());
    }
}
