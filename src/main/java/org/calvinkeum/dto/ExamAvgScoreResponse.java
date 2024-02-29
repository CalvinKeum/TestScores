package org.calvinkeum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamAvgScoreResponse {
    private Integer exam;
    private List<StudentScoreResponse> students;
    private Double averageScore;
}
