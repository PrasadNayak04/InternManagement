package com.robosoft.internmanagement.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CandidateServiceTest {

    @Autowired
    CandidateServices candidateServices;

    @BeforeEach
    void setUp() {
        System.out.println("Candidate Service test started");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Candidate Service test finished");
    }

    @Test
    public void isVacantPositionTest(){
        assertTrue(candidateServices.isVacantPosition("cotlin"));
    }

    @Test
    public void isVacantPosition2Test(){
        assertTrue(candidateServices.isVacantPosition("kotlin"));
    }

    @Test
    public void alreadyShortlisted(){
        assertTrue(candidateServices.alreadyShortlisted("prasadhdk9@gmail.com"));
    }

    @Test
    public void alreadyShortlisted2(){
        assertTrue(candidateServices.alreadyShortlisted("candidate20@gmail.com"));
    }

}