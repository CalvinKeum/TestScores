package org.calvinkeum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.calvinkeum.dto.ExamAvgScoreResponse;
import org.calvinkeum.dto.ExamPaginationResponse;
import org.calvinkeum.dto.ExamResponse;
import org.calvinkeum.dto.StudentScoreResponse;
import org.calvinkeum.model.StudentExamScore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {
    protected static TreeMap<Integer, ExamAvgScoreResponse> cachedExamAvgScoreResponseMap = new TreeMap<>();
    protected static TreeMap<Integer, List<StudentExamScore>> examEntriesMap = new TreeMap<>();

    private static final int ALL_EXAMS_MAX_LIMIT = 50;

    public ExamPaginationResponse getAllExams(int skip, int limit, String sortOrder) {
        skip = Math.max(0, skip);
        limit = Math.min(ALL_EXAMS_MAX_LIMIT, Math.max(1, limit));
        sortOrder = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        List<ExamResponse> examResponses = examEntriesMap.keySet()
            .stream()
            .sorted(getComparator(sortOrder))
            .skip(skip)
            .limit(limit)
            .map(this::mapToExamResponse)
            .toList();

        int totalExams = examEntriesMap.size();

        ExamPaginationResponse response = new ExamPaginationResponse();
        response.setTotalExams(totalExams);
        response.setSkip(skip);
        response.setLimit(limit);
        response.setExamResponses(examResponses);

        return response;
    }

    public ExamAvgScoreResponse getExamResults(Integer exam) {
        if (!examEntriesMap.containsKey(exam)) {
            return null;
        }

        // grab and return from cache if it exists
        if (cachedExamAvgScoreResponseMap.containsKey(exam)) {
            return cachedExamAvgScoreResponseMap.get(exam);
        }

        // otherwise calculate data
        double scoreSum = 0;
        int examCount = 0;

        List<StudentScoreResponse> studentScoreResponses = new ArrayList<>();

        for (StudentExamScore studentExamScore : examEntriesMap.get(exam)) {
            StudentScoreResponse studentScoreResponse =
                mapToStudentScoreResponse(studentExamScore.getStudentId(), studentExamScore.getScore());

            studentScoreResponses.add(studentScoreResponse);

            scoreSum += studentExamScore.getScore();
            examCount += 1;
        }

        Double averageScore = calculateAverageScore(scoreSum, examCount);

        ExamAvgScoreResponse examAveScoreResponse =
            mapToExamAvgScoreResponse(exam, studentScoreResponses, averageScore);

        cachedExamAvgScoreResponseMap.put(exam, examAveScoreResponse);

        return examAveScoreResponse;
    }

    public static void processExamData(StudentExamScore studentExamScore) {
        if (studentExamScore == null) {
            return;
        }

        List<StudentExamScore> studentExamScores =
            examEntriesMap.getOrDefault(studentExamScore.getExam(), new ArrayList<>());

        studentExamScores.add(studentExamScore);

        examEntriesMap.put(studentExamScore.getExam(), studentExamScores);

        // removed cached exam data if adding additional data for the exam
        cachedExamAvgScoreResponseMap.remove(studentExamScore.getExam());
    }

    private Double calculateAverageScore(double scoreSum, int examCount) {
        if (examCount == 0) {
            log.error("No Student scores found for exam.");
            return null;
        }

        return (scoreSum / examCount);
    }

    private ExamAvgScoreResponse mapToExamAvgScoreResponse(
            Integer exam, List<StudentScoreResponse> studentScoreResponses, Double averageScore) {

        return ExamAvgScoreResponse.builder()
            .exam(exam)
            .students(studentScoreResponses)
            .averageScore(averageScore)
            .build();
    }

    private ExamResponse mapToExamResponse(Integer exam) {
        return ExamResponse.builder().exam(exam).build();
    }

    private StudentScoreResponse mapToStudentScoreResponse(String studentId, Double score) {
        return StudentScoreResponse.builder().studentId(studentId).score(score).build();
    }

    private Comparator<? super Integer> getComparator(String sortOrder) {
        if ("DESC".equals(sortOrder)) {
            return Comparator.reverseOrder();
        }
        else {
            return Comparator.naturalOrder();
        }
    }
}
