package com.example.canecontrol;

public class State {
    //I_State->1, S*_State->2, S_State->3, NS*_State->4, NS_State->5
    private int prev_state;
    private int act_state;
    private boolean prev_soporte;
    private boolean act_soporte;

    State(){
        prev_soporte = true;
        prev_state = 1;
        act_soporte = false;
    }

    public int getPrev_state() {
        return prev_state;
    }

    public void setPrev_state(int prev_state) {
        this.prev_state = prev_state;
    }

    public int getAct_state() {
        return act_state;
    }

    public void setAct_state(int act_state) {
        this.act_state = act_state;
    }

    public boolean isPrev_soporte() {
        return prev_soporte;
    }

    public void setPrev_soporte(boolean prev_soporte) {
        this.prev_soporte = prev_soporte;
    }

    public boolean isAct_soporte() {
        return act_soporte;
    }

    public void setAct_soporte(boolean act_soporte) {
        this.act_soporte = act_soporte;
    }

    void currentState(int s){
        prev_state = act_state;
        act_state = s;
    }

    void currentSupport(boolean b){
        prev_soporte = act_soporte;
        act_soporte = b;
    }
}
