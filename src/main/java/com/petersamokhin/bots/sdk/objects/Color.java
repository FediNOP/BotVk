package com.petersamokhin.bots.sdk.objects;

public enum  Color {
    Primary,Default,Negative,Positive;


    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
