public class WithMass<E> implements Comparable {
	E particle;
	double logMassLoc;
	public WithMass(E particle, double logMassLoc){
		this.particle = particle;
		this.logMassLoc = logMassLoc;
	}

	@Override
	public int compareTo(Object other){
		return -(int)Math.signum(logMassLoc - ((WithMass)other).logMassLoc);
	}
}
