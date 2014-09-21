package vanished.Simulator.Structure;

import java.util.Properties;

import vanished.Simulator.Item.ItemDef;

public class StockManagerInfo {
	public ItemDef itemDef;
	public int lotmax;
	public int capacity;

	public StockManagerInfo(ItemDef itemDef, String prefix, Properties p) {
		this.itemDef = itemDef;
		lotmax = Integer.parseInt(p.getProperty(prefix + "lotmax"));
		capacity = Integer.parseInt(p.getProperty(prefix + "capacity"));
	}
}
