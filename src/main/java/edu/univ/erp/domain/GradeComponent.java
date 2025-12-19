package edu.univ.erp.domain;

import java.util.HashMap;
import java.util.Map;

public class GradeComponent {
    private final int enrollmentId;
    private final String rollNo;
    private final String username;
    private final Map<String, Double> scores;

    private String finalGrade;

    public GradeComponent(int enrollmentId, String rollNo, String username) {
        this.enrollmentId = enrollmentId;
        this.rollNo = rollNo;
        this.username = username;
        this.scores = new HashMap<>();
        this.finalGrade = "not appointed yet";
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getUsername() {
        return username;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public void setScore(String componentName, Double score) {
        if (score != null) {
        this.scores.put(componentName, score);
        }
    }

    public Double getScore(String componentName) {
        return scores.getOrDefault(componentName, null);
    }

    public String calculateFinalGrade() {
        double quizScore = scores.getOrDefault("Quiz/Assignments", 0.0);
        double midtermScore = scores.getOrDefault("Midterm", 0.0);
        double finalExamScore = scores.getOrDefault("Final Exam", 0.0);

        if (!scores.containsKey("Quiz/Assignments") || !scores.containsKey("Midterm") || !scores.containsKey("Final Exam")) {
            return "Incomplete";
        }

        double weightedScore = (quizScore * 0.20) + (midtermScore * 0.30) + (finalExamScore * 0.50);


        if (weightedScore >= 90) return "A+";
        if (weightedScore >= 80) return "A";
        if (weightedScore >= 70) return "B+";
        if (weightedScore >= 60) return "B";
        if (weightedScore >= 50) return "C";
        if (weightedScore >= 40) return "D";
        return "F";
    }
    
    public String calculateFinalGrade(Map<String, Integer> componentWeights) {
        if (componentWeights == null || componentWeights.isEmpty()) {
            return "Incomplete";
        }
        
        for (String component : componentWeights.keySet()) {
            if (!scores.containsKey(component)) {
                return "Incomplete";
            }
        }
        
        double weightedScore = 0.0;
        int totalWeight = 0;
        
        for (Map.Entry<String, Integer> entry : componentWeights.entrySet()) {
            String component = entry.getKey();
            int weight = entry.getValue();
            Double score = scores.get(component);
            
            if (score != null) {
                weightedScore += score * (weight / 100.0);
                totalWeight += weight;
            }
        }
        
        if (totalWeight != 100 && totalWeight > 0) {
            weightedScore = (weightedScore / totalWeight) * 100;
        }

        if (weightedScore >= 90) return "A+";
        if (weightedScore >= 80) return "A";
        if (weightedScore >= 70) return "B+";
        if (weightedScore >= 60) return "B";
        if (weightedScore >= 50) return "C";
        if (weightedScore >= 40) return "D";
        return "F";
    }
}