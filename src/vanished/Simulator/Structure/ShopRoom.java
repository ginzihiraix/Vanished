package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class ShopRoom extends DeliverRoom {

	StockManager shopStockManager;

	public ShopRoom(Building building, ShopRoomDef roomDef) {
		super(building, roomDef);

		shopStockManager = new StockManager(roomDef.productItemDef, roomDef.productStockManagerInfo);
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		System.out.println("===Shop Department===");
		System.out.println("product name : " + this.shopStockManager.stockManagerInfo.itemDef.GetName());
		System.out.println("product stock : " + this.shopStockManager.GetNumStock());
		System.out.println("product price : " + this.shopStockManager.GetPriceWithRate());
	}

	// 販売価格を取得する。
	public double GetProductItemPrice() {
		return shopStockManager.GetPriceWithRate();
	}

	public double GetProductItemStock() {
		return shopStockManager.GetNumStock();
	}

	public String GetProductItemName() {
		return shopStockManager.stockManagerInfo.itemDef.GetName();
	}

	public ItemDef GetProductItemDef() {
		return shopStockManager.stockManagerInfo.itemDef;
	}

	public class ItemCatalog {
		public ItemDef itemDef;
		public double price;
		public double numPick;
		public long durationToBuy;

		public ItemCatalog(ItemDef itemDef, double price, double numConsume, long duration) {
			this.itemDef = itemDef;
			this.price = price;
			this.numPick = numConsume;
			this.durationToBuy = duration;
		}
	}

	// 販売商品の一覧を取得する。
	private ItemCatalog GetProductItem(double maxMoney, double maxNumPick, double price) {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

		if (this.shopStockManager.IsOpen() == false) return null;

		// 個数を決める。
		double minNumPick = Double.MAX_VALUE;
		{
			// 希望の買取量による制約
			if (maxNumPick < minNumPick) minNumPick = maxNumPick;

			// 価格による制約
			double numPickForMoney = maxMoney / price;
			if (numPickForMoney < minNumPick) minNumPick = numPickForMoney;

			// 一回に運び込める量による制約
			if (this.shopStockManager.stockManagerInfo.lotmax < minNumPick) minNumPick = this.shopStockManager.stockManagerInfo.lotmax;
		}

		// 購入に要する時間を決める。
		// TODO:個数に比例してかかる。仮
		// long duration = (long) (shopRoomDef.durationToSell * minNumPick) + 1;
		long duration = shopRoomDef.durationToSell;

		ItemCatalog itemCatalog = new ItemCatalog(this.shopStockManager.stockManagerInfo.itemDef, price, minNumPick, duration);
		return itemCatalog;
	}

	public ItemCatalog GetProductItem(double maxMoney, double maxNumPick) {
		// 価格を決める。
		double price = this.shopStockManager.GetPriceWithRate();
		return this.GetProductItem(maxMoney, maxNumPick, price);
	}

	public void SetProductPriceRate(double rate) {
		this.shopStockManager.SetPriceRate(rate);
	}

	// 商品を買う
	public Item BuyProductItem(long timeNow, ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

		if (this.shopStockManager.IsOpen() == false) throw new Exception("aaa");

		this.Enter(timeNow, shopRoomDef.durationToSell, simulation);

		// 商品を取り出す。
		Item item = this.shopStockManager.Get(timeNow, itemCatalog.numPick, simulation);

		if (simulation == false) {
			// 売り上げを計上する。
			this.AddMoney(timeNow, itemCatalog.price * itemCatalog.numPick);
			productInputMoneyEMA.Add(timeNow, itemCatalog.price * itemCatalog.numPick);
		}

		return item;
	}

	// 商品価格に対してフォードバックを与える。いくらだったらNo1の選択肢になったのか、各Humanがフィードバックを与える。
	public void FeedbackAboutProductPrice(double price, double quantity) {
		// System.out.println("FeedbackAboutProductPrice : " + this.roomDef.name + ", " + price + ", " + quantity);
		this.shopStockManager.Feedback(price, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage productInputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 10, true);

	public double GetProductInputStockEMA(long timeNow) {
		return this.shopStockManager.GetInputStockEMA(timeNow);
	}

	public double GetProductInputMoneyEMA(long timeNow) {
		return this.productInputMoneyEMA.GetAverage(timeNow);
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		super.DiscardOldLog(timeNow);
		// long duration = 60L * 24L * 365L * 10L;
		// this.shopStockManager.DiscardOldLog(timeNow - duration);
	}

}
