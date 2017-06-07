package it.polito.did.ragnatela;

public class Ragno {
    int lifebar;
    int score;
    double angle;

    public Ragno() {
        lifebar = 100;
        score = 0;
        angle = 0;
    }

    public int getScore() { return score; }

    public int getLifebar() {
        return lifebar;
    }

    public double getAngle() {
        return angle;
    }

    public void update(double increment) {
        angle += increment;
        if(angle > (Math.PI * 2)) {
            angle = angle - (Math.PI * 2);
        }
    }

    public void hit() {
        lifebar -= 10;
    }

    public void upScore() {
        score += 10;
    }
}
