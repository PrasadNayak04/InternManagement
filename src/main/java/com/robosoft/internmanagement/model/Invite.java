package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Invite
{
    private int today;
    private int yesterday;
    private int pastMonth;
    private int twoMonthBack;
    private int pastYear;
    private int twoYearBack;

}
