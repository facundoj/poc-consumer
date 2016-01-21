package org.k12.caliper.poc.persistence;

import java.io.Serializable;

/**
 * Created by tomas on 21/01/16.
 * This is a simple serializable bean that maps to a row in the student_performance table created in hive.
 */
public class StudentPerformanceRow implements Serializable {
    private String student_id;
    private String objective_id;
    private double obtained_score;
    private double total_score;

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getObjective_id() {
        return objective_id;
    }

    public void setObjective_id(String objective_id) {
        this.objective_id = objective_id;
    }

    public double getObtained_score() {
        return obtained_score;
    }

    public void setObtained_score(double obtained_score) {
        this.obtained_score = obtained_score;
    }

    public double getTotal_score() {
        return total_score;
    }

    public void setTotal_score(double total_score) {
        this.total_score = total_score;
    }
}
