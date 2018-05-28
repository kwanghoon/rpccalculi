
public class Var extends Value {
	private String var;
	
	public Var(String var) {
		super();
		this.var = var;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	public String toString() {
		return var;
	}
}
