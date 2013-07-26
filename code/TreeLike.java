import java.util.*;
public abstract class TreeLike<E> {
    abstract E max(E rhs);
    abstract double logSize();
    abstract boolean lessThan(E rhs);
    abstract boolean equalTo(E rhs);
    /*abstract String toString(E match);
    @Override
    public String toString(){
        return toString(null);
    }*/

    // General instance methods
    private double logMassLoc = Double.NaN, logMassTot = Double.NaN;
    private boolean massIsCached = false;
    private boolean neq(double a, double b){
        return a < b - 1e-10 || b > a + 1e-10;
    }
    public void setLogMass(double logMassLoc, double logMassTot){
        this.logMassLoc = logMassLoc;
        this.logMassTot = logMassTot;
        this.massIsCached = true;
    }
    public boolean massIsCached(){
        return massIsCached;
    }
    public double getLogMassTot(){
        if(!massIsCached)
            throw new RuntimeException("mass is not cached");
        return logMassTot;
    }
    public double getLogMassLoc(){
        if(!massIsCached)
            throw new RuntimeException("mass is not cached");
        return logMassLoc;
    }
}
