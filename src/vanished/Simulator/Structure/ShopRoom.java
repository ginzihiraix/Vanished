package vanished.Simulator.Structure;

import java.util.ArrayList;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.EventLogManager.EventLog;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class ShopRoom extends DeliverRoom {

	StockManager shopStockManager;

	public class ItemCatalog {
		public ItemDef itemDef;
		public double price;
		public double lotmax;

		public ItemCatalog(ItemDef itemDef, double price, double maxLot) {
			this.itemDef = itemDef;
			this.price = price;
			this.lotmax = maxLot;
		}
	}

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

	// 販売商品の一覧を取得する。稼動してるかどうかは気にしない。
	public ItemCatalog GetProductItem(double maxMoney, boolean stockCheck) {

		double price = this.shopStockManager.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		if (price > maxMoney) return null;

		double numStock = this.shopStockManager.GetNumStock();
		if (stockCheck == true) {
			if (numStock == 0) return null;
		}

		ItemCatalog itemCatalog = new ItemCatalog(this.shopStockManager.stockManagerInfo.itemDef, price, numStock);

		return itemCatalog;
	}

	public ItemCatalog GetProductItem(double maxMoney, boolean stockCheck, ItemDef itemDef) {
		if (this.shopStockManager.stockManagerInfo.itemDef != itemDef) return null;

		double price = this.shopStockManager.price * Math.pow(1.02, OtherUtility.rand.nextInt(11) - 11 / 2);
		if (price > maxMoney) return null;

		double numStock = this.shopStockManager.GetNumStock();
		if (stockCheck == true) {
			if (numStock == 0) return null;
		}

		ItemCatalog itemCatalog = new ItemCatalog(this.shopStockManager.stockManagerInfo.itemDef, price, numStock);

		return itemCatalog;
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

	// 購入にかかる所要時間を取得する。
	public long GetDurationToBuy() {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;
		return shopRoomDef.durationToSell;
	}

	// 商品を買う
	public Item BuyProductItem(long timeNow, double maxMoney, ItemCatalog itemCatalog, double numPick, boolean simulation) throws Exception {
		ShopRoomDef shopRoomDef = (ShopRoomDef) roomDef;

		this.Enter(timeNow, shopRoomDef.durationToSell, simulation);

		if (maxMoney < itemCatalog.price * numPick) throw new HumanSimulationException("BuyProductItems : less money to buy");

		// 商品を取り出す。
		Item item = this.shopStockManager.Get(timeNow, numPick, simulation);

		if (simulation == false) {
			this.AddMoney(timeNow, itemCatalog.price * numPick);
			productInputMoneyEMA.Add(timeNow, itemCatalog.price * numPick);
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
