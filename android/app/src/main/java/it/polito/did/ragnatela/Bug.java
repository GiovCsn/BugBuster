package it.polito.did.ragnatela;


public class Bug {
    int tirante;
    int pos1;
    int pos2;
    int lifespan;
    boolean alive;
    boolean toCheck;
    int type;
    int step;

    public boolean isAlive() {
        return alive;
    }

    public boolean isChecked() {
        return toCheck;
    }

    public void kill() {
        this.alive = false;
    }

    public Bug() {
        double res = Math.random();
        if(res < 0.2) {
            tirante = 1;
            pos1 = 25;
            pos2 = 25;
            lifespan = 51;
        } else if((res >= 0.2) && (res < 0.4)) {
            tirante = 2;
            pos1 = 118;
            pos2 = 118;
            lifespan = 52 + 133;
        } else if((res >= 0.4) && (res < 0.6)) {
            tirante = 3;
            pos1 = 251;
            pos2 = 251;
            lifespan = 186 + 131;
        } else if((res >= 0.6) && (res < 0.8)) {
            tirante = 4;
            pos1 = 370;
            pos2 = 370;
            lifespan = 318 + 105;
        } else if((res >= 0.8)) {
            tirante = 5;
            pos1 = 472;
            pos2 = 472;
            lifespan = 424 + 97;
        }
        step = 1;
        toCheck = false;
        alive = true;
        type = 0;
    }

    public int getTirante() {
        return tirante;
    }

    public int getPos1() { return pos1; }

    public int getPos2() {
        return pos2;
    }

    public void setPos1(int pos) {
        this.pos1 = pos;
    }

    public void setPos2(int pos) {
        this.pos2 = pos;
    }

    public void update() {
        if(isAlive()) {
            this.pos1 = this.pos1 + step;
            this.pos2 = this.pos2 - step;
            if ((this.pos1 >= lifespan-step) || (this.pos2 <= step)) {
                toCheck = true;
            }
            if ((this.pos1 >= lifespan) || (this.pos2 <= 0)) {
                kill();
            }
        }
    }

}
