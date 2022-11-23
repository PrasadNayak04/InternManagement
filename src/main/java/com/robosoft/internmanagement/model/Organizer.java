package com.robosoft.internmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class Organizer implements Comparable<Organizer>
{
    private String emailId;
    private String name;
    private String photoUrl;
    private int interviews;

    @Override
    public int compareTo(Organizer o) {
        if(this.interviews>o.interviews)
            return -1;

        if(this.interviews<o.interviews)
            return 1;

        return 0;

    }
}
