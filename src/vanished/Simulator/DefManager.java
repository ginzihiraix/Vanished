package vanished.Simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.Item.ConsumeDef;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.BuildingDef;
import vanished.Simulator.Structure.DeliverRoomDef;
import vanished.Simulator.Structure.FactoryRoomDef;
import vanished.Simulator.Structure.RoomDef;
import vanished.Simulator.Structure.ShopRoomDef;

public class DefManager {

	TreeMap<String, Skill> skillList = new TreeMap<String, Skill>();

	TreeMap<String, ItemDef> itemDefList = new TreeMap<String, ItemDef>();

	TreeMap<String, RoomDef> roomDefList = new TreeMap<String, RoomDef>();

	TreeMap<String, BuildingDef> buildingDefList = new TreeMap<String, BuildingDef>();

	public void InitSkill(File dir) throws Exception {
		File dir2 = new File(dir + "/skill");
		ReadSkill(dir2);
	}

	public void InitItemDef(File dir) throws Exception {
		// itemDefを読み込む。
		File dir2 = new File(dir + "/item");
		ReadItemDef(dir2);
	}

	public void InitRoomDef(File dir) throws Exception {
		// RoomDefを読み込む。
		File dir2 = new File(dir + "/room");
		ReadRoomDef(dir2);
	}

	public void IniteBuildingDef(File dir) throws Exception {
		File dir2 = new File(dir + "/building");
		ReadBuilding(dir2);

	}

	public ItemDef GetItemDef(String name) {
		return itemDefList.get(name);
	}

	public RoomDef GetRoomDef(String name) {
		return roomDefList.get(name);
	}

	public BuildingDef GetBuilding(String name) {
		return buildingDefList.get(name);
	}

	public Skill GetSkill(String name) {
		return skillList.get(name);
	}

	private void ReadItemDef(File dir) throws Exception {
		File defFile = new File(dir + "/def.txt");
		if (defFile.exists()) {
			String name = dir.getName();

			Properties p = new Properties();
			p.load(new InputStreamReader(new FileInputStream(defFile), "SJIS"));

			System.out.println("Read item definition for " + name);

			String type = p.getProperty("type").toLowerCase();

			ItemDef itemDef = null;
			if (type.equals("general")) {
				itemDef = new ItemDef(name, p);
			} else if (type.equals("consumable")) {
				itemDef = new ConsumeDef(name, p);
			} else if (type.equals("movemethod")) {

			} else if (type.equals("carrymethod")) {

			} else {
				throw new Exception("アイテム定義がおかしい。");
			}
			itemDefList.put(name, itemDef);
		}

		for (File dir2 : dir.listFiles()) {
			if (dir2.isDirectory() == false) continue;
			ReadItemDef(dir2);
		}
	}

	private void ReadRoomDef(File dir) throws Exception {
		File defFile = new File(dir + "/def.txt");
		if (defFile.exists()) {
			String name = dir.getName();

			Properties p = new Properties();
			p.load(new InputStreamReader(new FileInputStream(defFile), "SJIS"));

			System.out.println("Read room definition for " + name);

			String type = p.getProperty("type").toLowerCase();

			RoomDef roomDef = null;
			if (type.equals("general")) {
				roomDef = new RoomDef(name, p);
			} else if (type.equals("deliver")) {
				roomDef = new DeliverRoomDef(name, p);
			} else if (type.equals("shop")) {
				roomDef = new ShopRoomDef(name, p);
			} else if (type.equals("factory")) {
				roomDef = new FactoryRoomDef(name, p);
			}
			roomDefList.put(name, roomDef);
		}

		for (File dir2 : dir.listFiles()) {
			if (dir2.isDirectory() == false) continue;
			ReadRoomDef(dir2);
		}
	}

	private void ReadSkill(File dir) throws Exception {
		File defFile = new File(dir + "/def.txt");
		if (defFile.exists()) {
			String name = dir.getName();

			Properties p = new Properties();
			p.load(new InputStreamReader(new FileInputStream(defFile), "SJIS"));

			System.out.println("Read skill definition for " + name);

			Skill skill = new Skill(name, p);

			skillList.put(name, skill);
		}

		for (File dir2 : dir.listFiles()) {
			if (dir2.isDirectory() == false) continue;
			ReadSkill(dir2);
		}
	}

	private void ReadBuilding(File dir) throws Exception {
		File defFile = new File(dir + "/def.txt");
		if (defFile.exists()) {
			String name = dir.getName();

			Properties p = new Properties();
			p.load(new InputStreamReader(new FileInputStream(defFile), "SJIS"));

			System.out.println("Read building definition for " + name);

			BuildingDef b = new BuildingDef(name, p);
			buildingDefList.put(name, b);
		}

		for (File dir2 : dir.listFiles()) {
			if (dir2.isDirectory() == false) continue;
			ReadBuilding(dir2);
		}

	}

}
