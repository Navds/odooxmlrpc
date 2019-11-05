package com.navds;

import java.util.Arrays;

public class Domain {
    
    private String field;
    private String operator;
    private Object value;

    public Domain() {
        this.field = "id";
        this.operator = ">";
        this.value = 0;
    }
    public Domain(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String toString() {
        return Arrays.asList(field, operator, value).toString();
    }
}
