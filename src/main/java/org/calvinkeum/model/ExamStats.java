package org.calvinkeum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ExamStats {
    private Double scoreSum;
    private int examCount;
    private Double averageScore;
}
