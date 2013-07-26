import java.util.*;
import fig.basic.LogInfo;
public class PiSystem<E extends TreeLike<E>> {
    Tree<E> tree;
    public PiSystem(Model<E> model, E root){
        tree = new Tree<E>(model, root);
    }
    public PiSystem(Tree<E> tree){
        this.tree = tree;
    }
    void add(E toAdd){
        tree = tree.add(toAdd);
    }
    List<E> getRefinements(){
        List<E> currentStates = tree.flatten();
        List<E> refinements = new LinkedList<E>();
        for(E state : currentStates)
            refinements.addAll(state.getRefinements());
        return refinements;
    }
    double score(){
        return tree.score();
    }
    double scoreToAdd(E toAdd){
        Tree<E> newTree = tree.add(toAdd);
        return newTree.score();
    }

    private static <F extends TreeLike<F>> double scoreParticles(List<F> particles,
                                                                 Model<F> model){
        LogInfo.begin_track("Computing score against model");
        double score = 0.0, mass = 0.0;
        for(F particle : particles){
            double particleScore = model.score(particle),
                   particleMass  = Math.exp(particle.getLogMassLoc());
            if(!Double.isNaN(particleScore)){
                mass += particleMass;
                score += particleScore * particleMass;
						}
        }
        double ret;
        if(mass <= 0.0)
            ret = Double.NaN;
        else
            ret = score / mass;
        LogInfo.logs("score = %f", ret);
        LogInfo.end_track();
        return ret;
    }

    static HashMap<Wrapper, Pair> memoized;
    static <F extends TreeLike<F>> Pair<F> optimalSubtree(Tree<F> ancestor, Tree<F> subtree,
                                                          int childIndex, int numPlacements){

        //if(k < 0) return new Pair<F>(Double.POSITIVE_INFINITY, null);
        //if(k >= subtree.size()) return new Pair<F>(0.0, subtree.flatten());
        Wrapper index = new Wrapper(ancestor, subtree, childIndex, numPlacements);
        Pair<F> ans = memoized.get(index);
        if(ans != null) return ans;
        else ans = new Pair<F>(Double.POSITIVE_INFINITY, null);
        if(childIndex == subtree.numChildren()){
            if(numPlacements > 0){
                ans = optimalSubtree(ancestor, subtree, childIndex, 0);
            } else {
                double score = ancestor.model.KL(subtree.state, subtree.state, ancestor.state);
                for(Tree<F> child : subtree.children)
                    score -= ancestor.model.KL(child.state, subtree.state, ancestor.state);
                ans = new Pair<F>(score, new ArrayList<F>());
            }
        } else {
            Tree<F> child = subtree.children.get(childIndex);
            int maxPlacements = Math.min(numPlacements, child.size());
            for(int i = 0; i <= numPlacements; i++){
                Pair<F> ansChild = optimalSubtree(ancestor, child, 0, i);
                Pair<F> ansRest = optimalSubtree(ancestor, subtree, childIndex+1, numPlacements-i);
                if(ansChild.score + ansRest.score < ans.score){
                    double ansScore = ansChild.score + ansRest.score;
                    ArrayList<F> ansList = new ArrayList<F>();
                    ansList.addAll(ansChild.list);
                    ansList.addAll(ansRest.list);
                    ans = new Pair<F>(ansScore, ansList);
                }
                if(i < numPlacements){
                    Pair<F> ansChild2 = optimalSubtree(child, child, 0, i);
                    Pair<F> ansRest2 = optimalSubtree(ancestor, subtree, childIndex+1, numPlacements-i-1);
                    if(ansChild2.score + ansRest2.score < ans.score){
                        double ansScore = ansChild2.score + ansRest2.score;
                        ArrayList<F> ansList = new ArrayList<F>();
                        ansList.add(child.state);
                        ansList.addAll(ansChild2.list);
                        ansList.addAll(ansRest2.list);
                        ans = new Pair<F>(ansScore, ansList);
                    }
                }
            }
        }
        //LogInfo.logs("DP[%s] = %f", index, ans.score);
        memoized.put(index, ans);
        return ans;
    }

    static <F extends TreeLike<F>> List<Double> inferNew(Model<F> model,
                                                      F root, int numParticles){
        LogInfo.begin_track("PiSystem inference");
        int T = model.T();
        model.init_t();
        List<Double> ret = new LinkedList<Double>();

        LogInfo.begin_track("t=0");
        PiSystem<F> piAll = new PiSystem<F>(model, root);
        for(F state : root.initStates()){
            piAll.add(state);
        }
        ret.add(scoreParticles(piAll.tree.flatten(), model));
        memoized = new HashMap<Wrapper, Pair>();
        piAll.tree.makeGuids(0);
        piAll.tree.print();
        Pair<F> treeAndScore = optimalSubtree(piAll.tree, piAll.tree, 0, numParticles);
        LogInfo.logs("Score after pruning: %.4f", treeAndScore.score);
        PiSystem<F> pi = new PiSystem<F>(model, root);
        for(F state : treeAndScore.list)
            pi.add(state);
        pi.tree.print();
        LogInfo.end_track();

        for(int t = 1; t < T; t++){
            LogInfo.begin_track("t=%d", t);
            model.increment_t();
            root = root.nextRoot();
            piAll = new PiSystem<F>(model, root);
            for(F state : pi.getRefinements())
                piAll.add(state);
            ret.add(scoreParticles(piAll.tree.flatten(), model));
            memoized = new HashMap<Wrapper, Pair>();
            piAll.tree.makeGuids(0);
            piAll.tree.print();
            treeAndScore = optimalSubtree(piAll.tree, piAll.tree, 0, numParticles);
            pi = new PiSystem<F>(model, root);
            for(F state : treeAndScore.list)
                pi.add(state);
            LogInfo.logs("Score after pruning: %.4f", treeAndScore.score);
            pi.tree.print();
            LogInfo.end_track();
        }
        LogInfo.end_track();
        return ret;
    }

    static <F extends TreeLike<F>> List<Double> infer(Model<F> model,
                                                      F root, int numParticles){
        LogInfo.begin_track("PiSystem inference");
        int T = model.T();
        model.init_t();
        List<Double> ret = new LinkedList<Double>();

        LogInfo.begin_track("t=0");
        PiSystem<F> pi = new PiSystem<F>(model, root);
        for(F state : root.initStates()){
            pi.add(state);
        }
        pi.tree.print();
        ret.add(scoreParticles(pi.tree.flatten(), model));
        LogInfo.end_track();        

        for(int t = 1; t < T; t++){
            LogInfo.begin_track("t=%d", t);
            model.increment_t();
            LogInfo.logs("Score before refining: %f", pi.score());
            root = root.nextRoot();
            List<F> proposalsWithoutMass = pi.getRefinements();

            // compute mass of all refinements
            PiSystem<F> piAll = new PiSystem<F>(model, root);
            for(F state : proposalsWithoutMass)
                piAll.add(state);
            LogInfo.logs("Score after refining: %f", piAll.score());
            List<F> proposals = new LinkedList<F>();
            for(F state : piAll.tree.flatten())
                if(state.getLogMassTot() > Double.NEGATIVE_INFINITY)
                    proposals.add(state);
            ret.add(scoreParticles(proposals, model));
            
            // NOTE: proposals.remove(0) needs to return the root
            // we can't defer this until later because it messes up 
            // massTot otherwise
            PiSystem<F> piNew = new PiSystem<F>(model, proposals.remove(0));
            int numAdded = 1;
            while(numAdded < numParticles && proposals.size() > 0){
                int index = 0, bestIndex = -1;
                double curScore = piNew.score();
                double bestScore = Double.NEGATIVE_INFINITY;
                for(F state : proposals){
                    double score = piNew.scoreToAdd(state);
                    if(bestIndex == -1 || score > bestScore){
                        bestIndex = index;
                        bestScore = score;
                    }
                    index++;
                }
                F toAdd = proposals.remove(bestIndex);
                piNew.add(toAdd);
                numAdded++;
            }
            LogInfo.logs("Score after pruning: %f", piNew.score());
            piNew.tree.print();
            pi = piNew;
            LogInfo.end_track();
        }
        LogInfo.end_track();
        return ret;
    }
}

class Tree<E extends TreeLike<E>> {
    // invariant 1: y \in C(x) => y.state < x.state
    // invariant 2: y, y' \in C(x) => max(y.state,y'.state) = x.state
    Model<E> model;
    E state;
    List<Tree<E> > children;
    int guid;
    final int size;
    final static double EPS = 1e-3;
    private double logSizeLoc, logSizeTot, logMassLoc, logMassTot, score;

    public Tree(Model<E> model, E state){
        this(model, state, new LinkedList<Tree<E> >());
    }
    public Tree(Model<E> model, E state, List<Tree<E> > children){
        int size = 1;
        for(Tree<E> child : children)
            size += child.size;
        this.size = size;
        this.model = model;
        this.state = state;
        this.children = children;
        // TODO fix this documentation
        // keep track of:
        //   logSizeTot: log(# of elements in state) (including children)
        //   logSizeLoc: log(# of elements in state) (excluding children)
        //   logMassTot: log(mass of state) (including children)
        //   logMassLoc: log(mass of state) (excluding children)
        //   score: score for this subtree
        if(model != null){
            boolean cached = state.massIsCached();
            logSizeTot = state.logSize();
            logSizeLoc = logSizeTot;
            if(!cached){
                logMassLoc = model.mu(state, state);
                logMassTot = Double.NEGATIVE_INFINITY;
            } else {
                logMassTot = state.getLogMassTot();
                logMassLoc = logMassTot;
            }
            score = 0.0;

            for(Tree<E> c : children){
                logSizeLoc = Util.logMinus(logSizeLoc, c.logSizeTot);
                if(!cached){
                    logMassLoc = Util.logMinus(logMassLoc, model.mu(state, c.state));
                    logMassTot = Util.logPlus(logMassTot, c.logMassTot);
                } else {
                    logMassLoc = Util.logMinus(logMassLoc, c.logMassTot);
                }
                score += c.score;
            }
            if(logSizeLoc < -0.01){
                logMassLoc = Double.NEGATIVE_INFINITY;
                logSizeLoc = Double.NEGATIVE_INFINITY;
            }
            if(!cached)
                logMassTot = Util.logPlus(logMassTot, logMassLoc);
            if(logMassLoc > Double.NEGATIVE_INFINITY)
                score += Math.exp(logMassLoc) * (logMassLoc - logSizeLoc);
        }
    }

    int makeGuids(int index){
        guid = index++;
        for(Tree<E> c : children)
            index = c.makeGuids(index);
        return index;
    }
    int size(){
        return size;
    }
    int numChildren(){
        return children.size();
    }

    // TODO double-check this formula
    double score(){
        if(model == null) throw new RuntimeException("no model detected");
        else return score*Math.exp(-logMassTot) - logMassTot;
    }

    Tree<E> add(E toAdd){
        if(toAdd.equalTo(state)){ // then we can stop here
            //but we might need to update the mass
            if(toAdd.massIsCached())
                return setMass(toAdd);
            else
                return this;
        }
        else if(!toAdd.lessThan(state)){
            E parent = state.max(toAdd);
            List<Tree<E> > newChildren = new LinkedList<Tree<E> >();
            if(!parent.equalTo(state)){
                newChildren.add(this);
            } else {
                if(state.massIsCached())
                    parent.setLogMass(state.getLogMassLoc(),
                                      state.getLogMassTot());
            }
            if(!parent.equalTo(toAdd)){
                newChildren.add(new Tree<E>(model, toAdd));
            } else {
                if(toAdd.massIsCached())
                    parent.setLogMass(toAdd.getLogMassLoc(),
                                      toAdd.getLogMassTot());
            }
            return new Tree<E>(model, parent, newChildren);
        } else {
            for(Tree<E> c : children){
                if(toAdd.max(c.state).lessThan(state)){
                    return setChild(c, c.add(toAdd));
                }
            }
            return addChild(new Tree<E>(model, toAdd));
        }
    }

    private Tree<E> setMass(E toCopy){
        E newState = state.max(state); // TODO kind of hacky
        newState.setLogMass(toCopy.getLogMassLoc(), toCopy.getLogMassTot());
        return new Tree<E>(model, newState, children);
    }

    private Tree<E> addChild(Tree<E> newChild){
        return setChild(null, newChild);
    }

    // NOTE: oldChild passed by pointer!
    private Tree<E> setChild(Tree<E> oldChild, Tree<E> newChild){
        List<Tree<E>> newChildren = new LinkedList<Tree<E>>();
        newChildren.add(newChild);
        for(Tree<E> c : children)
            if(c != oldChild)
                newChildren.add(c);
        return new Tree<E>(model, state, newChildren);
    }    

    // NOTE: it's important that the first element of this be the root
    LinkedList<E> flatten(){
        return flattenHelper(logMassTot);
    }
    LinkedList<E> flattenHelper(double Z){
        LinkedList<E> ret = new LinkedList<E>();
        state.setLogMass(logMassLoc - Z, logMassTot - Z);
        ret.add(state);
        for(Tree<E> c : children)
            ret.addAll(c.flattenHelper(Z));
        return ret;
    }

    void print(){
        LogInfo.begin_track("Printing tree");
        print(null, Double.NaN);
        LogInfo.end_track();
    }
    void print(E previous, double Z){
        if(previous == null) Z = logMassTot;
        LogInfo.logs("%s, (mass=%.3f, logMass=%.3f)",
            state.toString(previous),
            Math.exp(logMassLoc-Z), logMassLoc-Z);
        for(Tree<E> c : children)
            c.print(state, Z);
    }
}

class Pair<F> {
    double score;
    ArrayList<F> list;
    public Pair(double score, ArrayList<F> list){
        this.score = score;
        this.list = list;
    }
}

class Wrapper {
    int a, b, c, d, hash;
    public Wrapper(Tree ancestor, Tree subtree, int childIndex, int numPlacements){
        a = ancestor.guid;
        b = subtree.guid;
        c = childIndex;
        d = numPlacements;
        hash = a + (b<<8) + (c<<16) + (d<<24); // WARNING: will collide if tree size is > 256
    }
    @Override
    public boolean equals(Object other){
        Wrapper w = (Wrapper)other;
        return a == w.a && b == w.b && c == w.c && d == w.d;
    }
    @Override
    public int hashCode(){
        return hash;
    }
    @Override
    public String toString(){
        return a+","+b+","+c+","+d;
    }
}
