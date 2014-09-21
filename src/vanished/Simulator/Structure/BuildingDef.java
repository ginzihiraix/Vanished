package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.ObjectDef;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Skill.SkillComparator;

public class BuildingDef extends ObjectDef {
	// ���O
	String name;

	// ����Building�𐶐����邽�߂̃��V�s
	Recipe recipe;

	// �����Ɍ��z�����ɎQ���ł��錚�z�҂̃L���p�V�e�B
	int capacityBuilder;

	// �eRoom�̒�`
	ArrayList<RoomDef> roomdefList = new ArrayList<RoomDef>();

	public class Recipe {
		// �����ɕK�v�ȃA�C�e�����Ɛ��ʂ̃}�b�v�B
		public TreeMap<ItemDef, Integer> material = new TreeMap<ItemDef, Integer>(new ItemDefComparator());

		// �����ɕK�v�ȘJ���͂Ǝ��Ԃ̃}�b�v�B
		public TreeMap<Skill, Long> workload = new TreeMap<Skill, Long>(new SkillComparator());

		public Recipe(Properties p) {
			for (String key : p.stringPropertyNames()) {
				if (key.startsWith("recipe.") == true) {
					String itemname = key.replace("recipe.", "");
					int num = Integer.parseInt(p.getProperty(key));

					ItemDef itemDef = GlobalParameter.dm.GetItemDef(itemname);
					this.material.put(itemDef, num);
				}
			}
		}
	}

	public class MaterialInvestDef {

	}

	public BuildingDef(String name, Properties p) throws Exception {
		this.name = name;

		recipe = new Recipe(p);

		this.capacityBuilder = Integer.parseInt(p.getProperty("capacityBuilder"));

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
