package com.example.canecontrol;

import android.util.Log;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import static java.lang.StrictMath.sqrt;

public class Fsm {
    private int stepMean;
    private int noStepMean;
    private int stepVar;
    private int noStepVar;
    private int stepCount;
    private int noStepCount;
    private Vector<Activity> act_buffer;
    private int cont_d;
    private State state;
    private float tickerInsert;
    private int readForceIdleCounter;
    private static final int beginLrmAfter = 4;
    private static final float minimunStep = 0.3f;
    private float num_read;
    private SimpleDateFormat dateStr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    Fsm(float t){
        stepMean = 0;
        noStepMean = 0;
        stepVar = 0;
        noStepVar = 0;
        stepCount = 0;
        noStepCount = 0;
        act_buffer = new Vector<Activity>();
        cont_d = 2;
        state = new State();
        readForceIdleCounter = 0;
        tickerInsert = t;
        num_read = 0.5f;
    }

    public void setTickerInsert(float val){
        tickerInsert = val;
    }

    public int getStepMean() {
        return stepMean;
    }

    public void setStepMean(int stepMean) {
        this.stepMean = stepMean;
    }

    public int getNoStepMean() {
        return noStepMean;
    }

    public void setNoStepMean(int noStepMean) {
        this.noStepMean = noStepMean;
    }

    public int getStepVar() {
        return stepVar;
    }

    public void setStepVar(int stepVar) {
        this.stepVar = stepVar;
    }

    public int getNoStepVar() {
        return noStepVar;
    }

    public void setNoStepVar(int noStepVar) {
        this.noStepVar = noStepVar;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getNoStepCount() {
        return noStepCount;
    }

    public void setNoStepCount(int noStepCount) {
        this.noStepCount = noStepCount;
    }

    public int getCont_d() {
        return cont_d;
    }

    public void setCont_d(int cont_d) {
        this.cont_d = cont_d;
    }

    public State getState() {
        return state;
    }

    public void setState(int val) {
        state.currentState(val);
    }

    public int numActivitiesNoPrinted(){
        int cont = 0;
        Iterator<Activity> value = act_buffer.iterator();
        while(value.hasNext()){
            if(!value.next().isPrinted()){
                cont++;
            }
        }
        return cont;
    }

    public String getActivity(){
        Iterator<Activity> value = act_buffer.iterator();
        Activity act;
        String result = "";
        boolean out = false;

        while(value.hasNext() && !out) {
            act = value.next();
            out = !act.isPrinted();
            result = act.getStrData();
            act.setPrinted(true);
        }

        return result;
    }

    private void MeasurementFunction()
    {
        float meanS, meanNS, sdS, sdNS;
        Activity tempAct = new Activity();

        meanS = stepMean/(stepCount * 1.0f); //media aritmetica
        meanNS = noStepMean/(noStepCount * 1.0f);

        sdS = (float)sqrt((stepVar/(stepCount * 1.0f)) - (meanS*meanS)); //varianza
        sdNS = (float)sqrt((noStepVar/(noStepCount * 1.0f)) - (meanNS*meanNS));
        sdS = Math.round(sdS*1000.0f)/1000.0f;
        sdNS = Math.round(sdNS*1000.0f)/1000.0f;

        tempAct.insert(0, meanS); //media temporal de los pasos de soporte
        tempAct.insert(1, meanNS); //media temporal de los pasos de no soporte
        tempAct.insert(2, sdS); //varianza
        tempAct.insert(3, sdNS); //varianza
        tempAct.setPrinted(false);

        tempAct.setStrData(dateStr.format(new Date()) + " Activity " + act_buffer.size() + "\n" + "Step mean: " + Math.round((meanS/1000.0f)*1000.0f)/1000.0f + " s\n" + "NoStep mean: "
                + Math.round((meanNS/1000.0f)*1000.0f)/1000.0f + " s\n" + "Step standard deviation: " + sdS + " ms\n" + "NoStep standard deviation: " + sdNS + " ms\n\n");

        act_buffer.add(tempAct);
    }

    public void stateMachine(boolean soporte)
    {
        int sTemp;
        int nsTemp;

        state.setAct_soporte(soporte);

        //Estimating the actual state
        switch (state.getPrev_state()) {
            case 1:
                if (state.isPrev_soporte() != state.isAct_soporte()) {
                    num_read = 0.5f;
                    if (state.isAct_soporte()) {
                        state.setAct_state(2);
                    } else {
                        state.setAct_state(4);
                    }
                } else {
                    state.setAct_state(1);
                }
                break;
            case 2:
                if ((readForceIdleCounter * tickerInsert >= beginLrmAfter) || ((readForceIdleCounter * tickerInsert < minimunStep) && !(state.isAct_soporte()))) {
                    if(readForceIdleCounter < 5) {
                        state.setAct_state(state.getPrev_state());
                    } else {
                        state.setAct_state(1);
                    }
                } else if ((readForceIdleCounter * tickerInsert > minimunStep) && !(state.isAct_soporte())) {
                    state.setAct_state(3);
                } else {
                    state.setAct_state(2);
                }
                break;
            case 3:
                state.setAct_state(4);
                break;
            case 4:
                if ((readForceIdleCounter * tickerInsert >= beginLrmAfter) || ((readForceIdleCounter * tickerInsert < minimunStep) && (state.isAct_soporte()))) {
                    if(readForceIdleCounter < 5) {
                        state.setAct_state(state.getPrev_state());
                    } else {
                        state.setAct_state(1);
                    }
                } else if ((readForceIdleCounter * tickerInsert > minimunStep) && (state.isAct_soporte())) {
                    state.setAct_state(5);
                } else {
                    state.setAct_state(4);
                }
                break;
            case 5:
                state.setAct_state(2);
                break;
        }

        //State's actions
        switch (state.getAct_state()) {
            case 1:
                readForceIdleCounter = 0;
                if (((state.getPrev_state() == 2) || (state.getPrev_state() == 4)) && ((stepMean > 0) && (noStepMean > 0) && (stepCount > 0) && (noStepCount > 0))) {
                    MeasurementFunction();
                    stepMean = 0;
                    noStepMean = 0;
                    stepVar = 0;
                    noStepVar = 0;
                    stepCount = 0;
                    noStepCount = 0;
                }
                break;
            case 2:
                readForceIdleCounter++;
                num_read += 1;
                break;
            case 3:
                num_read += 0.5;
                sTemp = (int)(num_read * tickerInsert * 1000);
                stepMean += sTemp;
                stepVar += (sTemp * sTemp);
                stepCount++;
                readForceIdleCounter = 0;
                num_read = 0.5f;
                break;
            case 4:
                readForceIdleCounter++;
                num_read += 1;
                break;
            case 5:
                num_read += 0.5;
                nsTemp = (int)(num_read * tickerInsert * 1000);
                noStepMean += nsTemp;
                noStepVar += (nsTemp * nsTemp);
                noStepCount++;
                num_read = 0.5f;
                readForceIdleCounter = 0;
                break;
        }
        //Log.d("Cane","state: " + state.getAct_state() + " " + readForceIdleCounter + " " + state.isAct_soporte());
        state.setPrev_state(state.getAct_state());
        state.currentSupport(soporte);

    }
}
