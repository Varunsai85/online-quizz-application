package org.varun.onlinequizzapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    private String selectedAnswer;
    private boolean isCorrect;
}
