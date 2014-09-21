package vanished.Simulator.Structure;

import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class DeliverRoomDef extends RunnableRoomDef {

	// ”[“ü‚É•K—v‚ÈŽžŠÔ
	long durationForDeliver;

	TreeMap<ItemDef, StockManagerInfo> materialStockManagerInfo = new TreeMap<ItemDef, StockManagerInfo>(new ItemDefComparator());

	public DeliverRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		String prefix = "stocking.";

		this.durationForDeliver = Long.parseLong(p.getProperty(prefix + "durationForDeliver"));

		TreeMap<ItemDef, Boolean> items = new TreeMap<ItemDef, Boolean>(new ItemDefComparator());
		for (String keyOrg : p.stringPropertyNames()) {
			if (keyOrg.startsWith(prefix) == false) continue;
			String key = keyOrg.replace(prefix, "");
			String[] parts = key.split("\\.");
			if (parts.length >= 2 && parts[0].equals("material")) {
				ItemDef itemDef = GlobalParameter.dm.GetItemDef(parts[1]);
				items.put(itemDef, true);
			}
		}

		for (ItemDef itemDef : items.keySet()) {
			StockManagerInfo smi = new StockManagerInfo(itemDef, prefix + "material." + itemDef.GetName() + ".", p);
			materialStockManagerInfo.put(itemDef, smi);
		}
	}
}