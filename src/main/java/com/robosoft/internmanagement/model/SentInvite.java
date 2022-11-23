package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class SentInvite
{

    private int candidateInviteId;
    private String name;
    private String designation;
    private String location;
    private String email;

}
