package vanished.Simulator.Structure;

import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class DeliverRoomDef extends RoomDef {

	// 納入に必要な時間
	long durationForDeliver;

	//
	int capacityDeliver;

	TreeMap<ItemDef, StockManagerInfo> materialStockManagerInfo = new TreeMap<ItemDef, StockManagerInfo>(new ItemDefComparator());

	public DeliverRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		TreeMap<ItemDef, Boolean> items = new TreeMap<ItemDef, Boolean>(new ItemDefComparator());
		for (String key : p.stringPropertyNames()) {
			String[] parts = key.split("\\.");
			if (parts.length >= 2 && parts[0].equals("material")) {
				ItemDef itemDef = GlobalParameter.dm.GetItemDef(parts[1]);
				items.put(itemDef, true);
			}
		}

		for (ItemDef itemDef : items.keySet()) {
			StockManagerInfo smi = new StockManagerInfo("material." + itemDef.GetName() + ".", p);
			materialStockManagerInfo.put(itemDef, smi);
		}

		if (items.size() > 0) {
			this.durationForDeliver = Long.parseLong(p.getProperty("durationForDeliver"));
			this.capacityDeliver = Integer.parseInt(p.getProperty("capacityDeliver"));
		}
	}
}