package vanished.Simulator;

import java.io.File;

public class GlobalParameter {
	static public DefManager dm = new DefManager();

	static public double smallValue = 1.0e-10;

	static {
		try {
			File dir = new File("system/objectdef");
			dm.InitSkill(dir);
			dm.InitItemDef(dir);
			dm.InitRoomDef(dir);
			dm.IniteBuildingDef(dir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
