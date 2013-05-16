
package ui;

public class TickList {

    private TickList next;
    private String demoname;
    private int firsttick;
    private int secondtick;

    public TickList(String demoname, int firsttick, int secondtick) {
        this.demoname = demoname;
        this.firsttick = firsttick;
        this.secondtick = secondtick;
    }

    public void setNext(TickList next) {
        this.next = next;
    }

    public TickList getNext() {
        return next;
    }

    public String getDemoName() {
        return demoname;
    }

    public int getFirstTick() {
        return firsttick;
    }

    public int getSecondTick() {
        return secondtick;
    }

    public boolean equals(TickList t2) {
        return this.demoname.equals(t2.demoname) && this.firsttick == t2.firsttick
                && this.secondtick == t2.secondtick;
    }
}
