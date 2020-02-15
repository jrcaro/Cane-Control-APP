package com.example.canecontrol;

public class Activity implements Cloneable{
    private float[] data_act;
    private boolean printed;
    private String strData;
    Activity (){
        data_act = new float[4];
        strData = "No inicializado";
    }

    void insert(int index, float value) {
        data_act[index] = value;
    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public float getData(int index){
        return data_act[index];
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Activity temp = new Activity();
        for(int i = 0;i<=4;i++) {
            temp.insert(i,this.getData(i));
        }
        temp.setPrinted(this.isPrinted());

        return temp;
    }
}
