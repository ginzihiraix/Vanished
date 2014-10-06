package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.OtherUtility;
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
		System.out.println("product price : " + this.shopStockManager.price);
	}

	// 販売価格を取得する。
	public double GetProductItemPrice() {
		return shopStockManager.price;
	}

	public double GetProductItemStock() {
		return shopStockManager.GetNumStock();
	}

	public String GetProductItemName() {
		return shopStockManager.stockManagerInfo.itemDef.GetName();
	}

	public class ItemCatalog {
		public ItemDef itemDef;
		public double price;
		public double numPick;
		public double durationToBuy;

		public ItemCatalog(ItemDef itemDef, double price, double numConsume, long duration) {
			this.itemDef = itemDef;
			this.price = price;
			this.numPick = numConsume;
			this.durationToBuy = duration;
		}
	}

	// 販売商品の一覧を取得する。
	public ItemCatalog GetProductItem(double maxMoney, double maxNumPick, double price) {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

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
		long duration = (long) (shopRoomDef.durationToSell * minNumPick) + 1;

		ItemCatalog itemCatalog = new ItemCatalog(this.shopStockManager.stockManagerInfo.itemDef, price, minNumPick, duration);
		return itemCatalog;
	}

	public ItemCatalog GetProductItemForConsumeWithNewPrice(double maxMoney, double maxNumPick) {
		// 価格を決める。
		double price = this.shopStockManager.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		return this.GetProductItem(maxMoney, maxNumPick, price);
	}

	public ItemCatalog GetProductItemWithFixedPrice(double maxMoney, double maxNumPick, double price) {
		return this.GetProductItem(maxMoney, maxNumPick, price);
	}

	public ItemCatalog GetProductItemForUtilityEvaluation(double maxMoney, double maxNumPick) {
		double price = this.shopStockManager.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		return this.GetProductItem(maxMoney, maxNumPick, price);
	}

	public ItemCatalog GetProductItemForMakeInKind(double maxNumPick) {
		return this.GetProductItem(Double.MAX_VALUE, maxNumPick, 0);
	}

	// 商品を買う
	public Item BuyProductItem(long timeNow, ItemCatalog itemCatalog, boolean simulation) throws Exception {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

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
		this.shopStockManager.Feedback(price, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage productInputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 10, true);

	// public ArrayList<EventLog> GetNumProcutLog(int numSample) throws Exception {
	// return this.shopStockManager.GetNumMakeLog(numSample);
	// }
	//
	// public ArrayList<EventLog> GetNumProcutLog() throws Exception {
	// return this.shopStockManager.GetNumMakeLog();
	// }

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
