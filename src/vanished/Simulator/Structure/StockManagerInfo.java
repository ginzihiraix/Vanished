package vanished.Simulator.Structure;

import java.util.Properties;

import vanished.Simulator.Item.ItemDef;

public class StockManagerInfo {
	public ItemDef itemDef;
	public double lotmax;
	public double capacity;

	public StockManagerInfo(ItemDef itemDef, String prefix, Properties p) {
		this.itemDef = itemDef;
		lotmax = Double.parseDouble(p.getProperty(prefix + "lotmax"));
		capacity = Double.parseDouble(p.getProperty(prefix + "capacity"));
	}
}
