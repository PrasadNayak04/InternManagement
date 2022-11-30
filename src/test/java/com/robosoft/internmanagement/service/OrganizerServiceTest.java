package com.robosoft.internmanagement.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrganizerServiceTest {

    @Autowired
    OrganizerServices organizerServices;

    @BeforeEach
    void setUp() {
        System.out.println("Organizer Service test started");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Organizer Service test finished");
    }

    @Test
    public void getAnyVacancy(){
        int result = organizerServices.getAnyLocationVacancy("kotlin");
        assertTrue(result > 0);
    }

    @Test
    public void getAnyVacancy2(){
        int result = organizerServices.getAnyLocationVacancy("react");
        assertTrue(result > 0);
    }

}