package com.robosoft.internmanagement.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberServices memberServices;

    @BeforeEach
    void setUp() {
        System.out.println("Member Service test started");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Member Service test finished");
    }

    @Test
    public void deleteCandidateTest(){
        assertFalse(memberServices.deleteExistingCandidate(19));
    }

    @Test
    public void deleteCandidateTest2(){
        assertFalse(memberServices.deleteExistingCandidate(21));
    }

}