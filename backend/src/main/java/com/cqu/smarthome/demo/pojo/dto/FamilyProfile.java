package com.cqu.smarthome.demo.pojo.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FamilyProfile {
    String familyName;
    String hostName;
    List<ViewHomeUser> familyMembers;

    public FamilyProfile(String familyName, String hostName, List<ViewHomeUser> familyMembers) {
        this.familyName = familyName;
        this.hostName = hostName;
        this.familyMembers = familyMembers;
    }
}
