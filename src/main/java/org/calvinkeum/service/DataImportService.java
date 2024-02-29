package org.calvinkeum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.calvinkeum.model.StudentExamScore;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {
    private static final Pattern JSON_DATA_PATTERN = Pattern.compile("data: \\{.*\\}");

    public static void importStudentExamData(String data) {
        Matcher matcher = JSON_DATA_PATTERN.matcher(data);

        if (!matcher.find()) {
            return;
        }

        try {
            String jsonData = data.substring(data.indexOf('{'));
            JSONObject jsonObject = new JSONObject(jsonData);

            importStudentExamScoreData(jsonObject);
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private static void importStudentExamScoreData(JSONObject jsonObject) {
        try {
            String studentId = jsonObject.getString("studentId");
            int exam = jsonObject.getInt("exam");
            double score = jsonObject.getDouble("score");

            StudentExamScore studentExamScore = new StudentExamScore();
            studentExamScore.setStudentId(studentId);
            studentExamScore.setExam(exam);
            studentExamScore.setScore(score);

            // Kafka would be good here, especially if broken out into microservices
            StudentService.processStudentData(studentExamScore);
            ExamService.processExamData(studentExamScore);
        }
        catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
