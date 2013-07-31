import java.util.*;
public class AbstractState extends TreeLike<AbstractState> {
    public static final int ALL = -1;
    static int K, S;
    final int t;
    final int symbol;
    final AbstractState prev;
    public AbstractState(int symbol, AbstractState prev){
        this.symbol = symbol;
        this.prev = prev;
        this.t = (prev == null) ? 0 : prev.t + 1;
    }
    
    AbstractState prev(){
        return prev;
    }

    boolean isAbstract(){
        return symbol == ALL;
    }

    private boolean sizeIsCached = false;
    private double cachedLogSize;
    public double logSize(){
        if(sizeIsCached) return cachedLogSize;
        else {
            sizeIsCached = true;
            if(symbol == ALL)
                cachedLogSize = K * Math.log(S) + Math.log(K);
            else
                cachedLogSize = (K-1) * Math.log(S);
            if(prev != null)
                cachedLogSize += prev.logSize();
            return cachedLogSize;
        }
    }

    public AbstractState max(AbstractState rhs){
        if(symbol != rhs.symbol)
            return root();
        else if(prev == null)
            return new AbstractState(symbol, null);
        else
            return new AbstractState(symbol, prev.max(rhs.prev));
    }

    List<AbstractState> initStates(){
        List<AbstractState> ret = new LinkedList<AbstractState>();
        ret.add(new AbstractState(ALL, null));
        for(int c = 0; c < K; c++)
            ret.add(new AbstractState(c, null));
        return ret;
    }

    AbstractState nextRoot(){
        return new AbstractState(ALL, root());
    }

    private AbstractState root(){
        if(prev == null) return new AbstractState(ALL, null);
        else return new AbstractState(ALL, prev.root());
    }

    public List<AbstractState> getRefinements(){
        List<AbstractState> ret = new LinkedList<AbstractState>();
        if(symbol == ALL) {
            ret.add(new AbstractState(ALL, this));
        }
        for(int c = 0; c < K; c++){
            ret.add(new AbstractState(c, this));
        }
        return ret;
    }

    private int compare(AbstractState rhs){
        if(symbol == rhs.symbol){
            if(prev == null)
                return 0;
            else
                return prev.compare(rhs.prev);
        }
        else if(symbol == ALL) return 1;
        else if(rhs.symbol == ALL) return -1;
        else return 5;
    }
    public boolean lessThan(AbstractState rhs){
        return compare(rhs) == -1;
    }
    public boolean equalTo(AbstractState rhs){
        return compare(rhs) == 0;
    }

    public String toString(){
        String s = symbol == ALL ? "A" : ""+symbol;
        return prev == null ? s : s + prev.toString();
    }
    public String toString(AbstractState match){
        char first;
        if(match != null && symbol == match.symbol){
            first = ' ';
        } else {
            match = null;
            if(symbol == ALL) first = 'A';
            else {
                if(K<=10)
                    first = (char)('0'+symbol);
                else
                    first = (char)('a' + symbol);
            }
        }
        String rest = "";
        if(prev != null)
            rest = prev.toString(match == null ? null : match.prev);
        return first + rest;
    }

    @Override
    public int hashCode(){
        if(prev == null)
            return symbol;
        else
            return symbol + 101 * prev.hashCode();
    }

    @Override
    public boolean equals(Object other){
        return equalTo((AbstractState)other);
    }

}
