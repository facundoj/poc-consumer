package org.k12.caliper.poc.persistence;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by tomas on 21/01/16.
 * This is a simple serializable bean that maps to a row in the student_performance table created in hive.
 */
public class StudentPerformanceRow implements Serializable {
    private String studentId;
    private String objectiveId;
    private double obtainedScore;
    private double totalScore;

    public StudentPerformanceRow () {

    };

    public StudentPerformanceRow (String studentId,
                                  String objectiveId,
                                  double obtainedScore,
                                  double totalScore) {
        // Is this necessary?
        this.studentId = studentId;
        this.objectiveId = objectiveId;
        this.obtainedScore = obtainedScore;
        this.totalScore = totalScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(String objectiveId) {
        this.objectiveId = objectiveId;
    }

    public double getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(double obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    // There is really no need for implementing toString, writeObject or readObject

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(this.studentId);
        stream.writeObject(this.objectiveId);
        stream.writeDouble(this.obtainedScore);
        stream.writeDouble(this.totalScore);
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.studentId = (String) stream.readObject();
        this.objectiveId = (String) stream.readObject();
        this.obtainedScore = stream.readDouble();
        this.totalScore = stream.readDouble();
    }

    @Override
    public String toString() {
        return this.studentId + '\t' + this.objectiveId + '\t' + this.obtainedScore + '\t' + this.totalScore + '\t';
    }
}
