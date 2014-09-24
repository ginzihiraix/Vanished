package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Properties;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.ObjectDef;

public class BuildingDef extends ObjectDef {
	// 名前
	String name;

	// ビル建設現場
	FactoryRoomDef buildRoomDef;

	// 各Roomの定義
	ArrayList<RoomDef> roomdefList = new ArrayList<RoomDef>();

	// サイズ
	int width;
	int height;

	// 道路と接することができる方向
	boolean[] open = new boolean[4];

	public BuildingDef(String name, Properties p) throws Exception {
		this.name = name;

		buildRoomDef = new FactoryRoomDef(name, p);

		for (String key : p.stringPropertyNames()) {
			if (key.startsWith("room.") == false) continue;

			String roomname = key.replace("room.", "");
			int num = Integer.parseInt(p.getProperty(key));
			RoomDef roomDef = GlobalParameter.dm.GetRoomDef(roomname);
			for (int i = 0; i < num; i++) {
				roomdefList.add(roomDef);
			}
		}

		// サイズ
		{
			String temp = p.getProperty("size");
			temp = temp.replace("(", "").replace(")", "");
			String[] part = temp.split(",");

			width = Integer.parseInt(part[0]);
			height = Integer.parseInt(part[0]);
		}

		// 道路と接しているかのフラグ。
		{
			String temp = p.getProperty("open");
			String[] part = temp.split(",");
			for (String f : part) {
				if (f.equals("N")) {
					open[0] = true;
				} else if (f.equals("E")) {
					open[1] = true;
				} else if (f.equals("S")) {
					open[2] = true;
				} else if (f.equals("W")) {
					open[3] = true;
				}
			}
		}
	}
}
