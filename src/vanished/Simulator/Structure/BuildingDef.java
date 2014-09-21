package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Properties;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.ObjectDef;

public class BuildingDef extends ObjectDef {
	// ���O
	String name;

	// �r�����݌���
	FactoryRoomDef buildRoomDef;

	// �eRoom�̒�`
	ArrayList<RoomDef> roomdefList = new ArrayList<RoomDef>();

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
	}
}
