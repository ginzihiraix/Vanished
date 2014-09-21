package vanished.Simulator.Item;

import java.util.Properties;

public class MoveMethod extends ItemDef {

	double speed;

	public MoveMethod(String name, Properties p) throws Exception {
		super(name, p);
	}

	public double GetSpeed() {
		return speed;
	}

}
