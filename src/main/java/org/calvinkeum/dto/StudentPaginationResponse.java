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
public class StudentPaginationResponse {
    private int totalStudents;
    private int skip;
    private int limit;
    private List<StudentResponse> studentResponses;
}
