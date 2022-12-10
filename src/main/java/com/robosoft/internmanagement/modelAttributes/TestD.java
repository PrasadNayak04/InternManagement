package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TestD {
    private List<WorkHistory> list;
}
