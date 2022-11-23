package com.robosoft.internmanagement.modelAttributes;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Link {

    private String url;
    private String website;

}
