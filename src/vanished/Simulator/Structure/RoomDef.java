package vanished.Simulator.Structure;

import java.util.Properties;

public class RoomDef {
	String name;

	// 部屋に入れる人のキャパシティ
	int capacityHuman;

	// 部屋に格納できるアイテムのキャパシティ
	double capacityItemWeight;

	public RoomDef(String name, Properties p) {
		this.name = name;

		this.capacityHuman = Integer.parseInt(p.getProperty("all.capacityHuman"));

		this.capacityItemWeight = Double.parseDouble(p.getProperty("all.capacityItemWeight"));

	}
}