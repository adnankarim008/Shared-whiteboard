package org.example;

public class CommandWrapper {
    public String Type ;
    public double x1 ;
    public double x2 ;
    public double y1 ;
    public double y2 ;
    public int colorRed ;
    public int colorGreen ;
    public int colorBlue ;
    public String message ;
    public String from ;

    public CommandWrapper(){}

    public CommandWrapper(String type, String message){
        this.Type = type;
        this.message = message;
    }
    public CommandWrapper(String type, String message, String from){
        this.Type = type;
        this.message = message;
        this.from = from;
    }
}
