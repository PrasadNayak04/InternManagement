package com.robosoft.internmanagement.modelAttributes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class MemberCredentials {

    @Pattern(regexp = "[a-zA-Z( )]+")
    private String name;

    @NotBlank
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.(com|in|org)")
    private String emailId;

    private String otp;

    public MemberCredentials(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
    }
}
