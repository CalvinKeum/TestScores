package org.calvinkeum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.calvinkeum.dto.StudentAvgScoreResponse;
import org.calvinkeum.dto.StudentPaginationResponse;
import org.calvinkeum.dto.StudentResponse;
import org.calvinkeum.model.ExamStats;
import org.calvinkeum.model.StudentExamScore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {
    protected static TreeMap<String, ExamStats> studentIdExamStatsMap = new TreeMap<>();
    private static final int ALL_STUDENTS_MAX_LIMIT = 100;

    public StudentPaginationResponse getAllStudents(int skip, int limit, String sortOrder) {
        skip = Math.max(0, skip);
        limit = Math.min(ALL_STUDENTS_MAX_LIMIT, Math.max(1, limit));
        sortOrder = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        List<StudentResponse> studentResponses = studentIdExamStatsMap.keySet()
            .stream()
            .sorted(getComparator(sortOrder))
            .skip(skip)
            .limit(limit)
            .map(this::mapToStudentResponse)
            .toList();

        int totalStudents = studentIdExamStatsMap.size();

        StudentPaginationResponse response = new StudentPaginationResponse();
        response.setTotalStudents(totalStudents);
        response.setSkip(skip);
        response.setLimit(limit);
        response.setStudentResponses(studentResponses);

        return response;
    }

    public StudentAvgScoreResponse getStudentAverageScore(String studentId) {
        if (!studentIdExamStatsMap.containsKey(studentId)) {
            return null;
        }

        return mapToStudentAvgScoreResponse(studentId, calculateAverageScore(studentId));
    }

    public static void processStudentData(StudentExamScore studentExamScore) {
        ExamStats examStats =
            studentIdExamStatsMap.getOrDefault(
                studentExamScore.getStudentId(),
                new ExamStats(0D, 0, 0D));

        examStats.setScoreSum(examStats.getScoreSum() + studentExamScore.getScore());
        examStats.setExamCount(examStats.getExamCount() + 1);

        studentIdExamStatsMap.put(studentExamScore.getStudentId(), examStats);
    }

    private Double calculateAverageScore(String studentId) {
        ExamStats examStats = studentIdExamStatsMap.get(studentId);

        if (examStats == null || examStats.getExamCount() == 0) {
            log.error("No Student scores found.");
            return null;
        }

        return (examStats.getScoreSum() / examStats.getExamCount());
    }

    private StudentAvgScoreResponse mapToStudentAvgScoreResponse(String studentId, Double averageScore) {
        return StudentAvgScoreResponse.builder().studentId(studentId).averageScore(averageScore).build();
    }

    private StudentResponse mapToStudentResponse(String studentId) {
        return StudentResponse.builder().studentId(studentId).build();
    }

    private Comparator<? super String> getComparator(String sortOrder) {
        if ("DESC".equals(sortOrder)) {
            return Comparator.reverseOrder();
        }
        else {
            return Comparator.naturalOrder();
        }
    }
}
