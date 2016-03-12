package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class Payload {
    private Key Key;
    private String TableName;

    public Payload(com.budoudoh.bask_it.domain.Key key, String tableName) {
        Key = key;
        TableName = tableName;
    }

    public com.budoudoh.bask_it.domain.Key getKey() {
        return Key;
    }

    public void setKey(com.budoudoh.bask_it.domain.Key key) {
        Key = key;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }
}
