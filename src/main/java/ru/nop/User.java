package ru.nop;

public class User {

    private int id;
    private String groupName;


    public User(int id, String groupName){

        this.id = id;
        this.groupName = groupName;

    }


    public int getId(){

        return id;

    }

    public void setId(int id){

        this.id = id;


    }


    public String getgroupName(){

        return groupName;

    }

    public void setGroupName(String groupName){

        this.groupName = groupName;


    }


}
