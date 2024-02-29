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
public class ExamPaginationResponse {
    private int totalExams;
    private int skip;
    private int limit;
    private List<ExamResponse> examResponses;
}
