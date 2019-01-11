package com.example.qw.memo;


public class OneMemo {
    private int tag;
    private String textDate;
    private String textTime;
    private boolean alarm;
    private String textTitle;
    private String mainText;

    public OneMemo(int tag, String textDate, String textTime,boolean alarm,String textTitle,String mainText) {
        this.tag=tag;
        this.textDate=textDate;
        this.textTime=textTime;
        this.alarm=alarm;
        this.textTitle=textTitle;
        this.mainText=mainText;
    }



    //getter
    public int getTag(){
        return tag;
    }
    public String getTextDate(){
        return textDate;
    }
    public String getTextTime(){
        return textTime;
    }
    public boolean getAlarm(){ return alarm; }
    public String getTextTitle() {
        return textTitle;
    }
    public String getMainText(){
        return mainText;
    }

    //setter
    public void setTag(int tag){
        this.tag=tag;
    }
    public void setTextDate(String textDate){
        this.textDate=textDate;
    }
    public void setTextTime(String textTime){
        this.textTime=textTime;
    }
    public void setAlarm(boolean alarm){
        this.alarm=alarm;
    }
    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }
    public void setMainText(String mainText){
        this.mainText=mainText;
    }
}
