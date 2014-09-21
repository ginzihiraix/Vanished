package vanished.Simulator.Structure;

import java.util.Properties;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;

public class ShopRoomDef extends DeliverRoomDef {

	// ���i���w���i�T�[�r�X������j����̂ɕK�v�Ȏ���
	long durationToSell;

	ItemDef productItemDef;

	StockManagerInfo productStockManagerInfo;

	public ShopRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		String prefix = "shop.";

		this.durationToSell = Long.parseLong(p.getProperty(prefix + "durationToSell"));

		String productItemName = p.getProperty(prefix + "product");
		productItemDef = GlobalParameter.dm.GetItemDef(productItemName);
		productStockManagerInfo = new StockManagerInfo(productItemDef, prefix, p);
	}
}
