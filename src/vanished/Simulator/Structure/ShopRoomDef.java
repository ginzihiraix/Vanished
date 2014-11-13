package vanished.Simulator.Structure;

import java.util.Properties;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;

public class ShopRoomDef extends DeliverRoomDef {

	// 商品を購入（サービスを消費）するのに必要な時間
	long durationToSell;

	ItemDef productItemDef;

	public ShopRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		String productItemName = p.getProperty("product");
		productItemDef = GlobalParameter.dm.GetItemDef(productItemName);

		this.durationToSell = Long.parseLong(p.getProperty("durationForSale"));

	}
}
