package vanished.Simulator.Structure;

import java.util.Properties;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;

public class ShopRoomDef extends DeliverRoomDef {

	// ���i���w���i�T�[�r�X������j����̂ɕK�v�Ȏ���
	long durationToSell;

	ItemDef productItemDef;

	public ShopRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		String productItemName = p.getProperty("product");
		productItemDef = GlobalParameter.dm.GetItemDef(productItemName);

		this.durationToSell = Long.parseLong(p.getProperty("durationForSale"));

	}
}
