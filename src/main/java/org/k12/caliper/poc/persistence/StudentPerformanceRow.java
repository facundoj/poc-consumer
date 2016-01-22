package org.k12.caliper.poc.persistence;

import java.io.Serializable;

/**
 * Created by tomas on 21/01/16.
 * This is a simple serializable bean that maps to a row in the student_performance table created in hive.
 */
public class StudentPerformanceRow implements Serializable {
    private String a_student_id;
    private String b_objective_id;
    private double c_obtained_score;
    private double d_total_score;

    public String getA_student_id() {
        return a_student_id;
    }

    public void setA_student_id(String a_student_id) {
        this.a_student_id = a_student_id;
    }

    public String getB_objective_id() {
        return b_objective_id;
    }

    public void setB_objective_id(String b_objective_id) {
        this.b_objective_id = b_objective_id;
    }

    public double getC_obtained_score() {
        return c_obtained_score;
    }

    public void setC_obtained_score(double c_obtained_score) {
        this.c_obtained_score = c_obtained_score;
    }

    public double getD_total_score() {
        return d_total_score;
    }

    public void setD_total_score(double d_total_score) {
        this.d_total_score = d_total_score;
    }

    // TODO: look for a better way to force this order for the serialized fields

}
