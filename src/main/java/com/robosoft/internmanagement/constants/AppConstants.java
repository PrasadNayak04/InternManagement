package com.robosoft.internmanagement.constants;

import com.robosoft.internmanagement.exception.Result;

public class AppConstants {
    public static final Result RECORD_NOT_EXIST = new Result("RECORD_NOT_EXIST", "Unable to find the record", "F");

    public static final Result RECORD_ALREADY_EXIST = new Result("RECORD_ALREADY_EXIST", "Record already exists with the given information", "F");

    public static final Result REQUIREMENTS_FAILED = new Result("REQUIREMENTS_FAILED", "Specified requirements not available", "F");

    public static final Result INVALID_INFORMATION = new Result("INVALID_INFORMATION", "Given information is not valid", "F");

    public static final Result TASK_FAILED = new Result("TASK_FAILED", "Task execution failed", "F");

    public static final Result SUCCESS = new Result("SUCCESS", "Successful", "T");

}
