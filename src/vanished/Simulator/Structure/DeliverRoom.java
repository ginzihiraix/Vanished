package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class DeliverRoom extends Room {

	TreeMap<ItemDef, StockManager> deliverStockManager = new TreeMap<ItemDef, StockManager>(new ItemDefComparator());

	public DeliverRoom(Building building, DeliverRoomDef roomDef) {
		super(building, roomDef);

		for (Entry<ItemDef, StockManagerInfo> e : roomDef.materialStockManagerInfo.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManagerInfo smi = e.getValue();
			StockManager sm = new StockManager(smi);
			deliverStockManager.put(itemDef, sm);
		}
	}

	public void DumpStatus(long timeNow) {
		super.DumpStatus(timeNow);

		System.out.println("===Deliver Department===");
		for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManager sm = e.getValue();
			System.out.println("material name : " + itemDef.GetName());
			System.out.println("material stock : " + sm.GetNumStock());
			System.out.println("material price : " + sm.GetPriceWithRate());
		}
	}

	public class CallForItem {
		public ItemDef itemDef;
		public double price;
		public int priceIndex;
		public double numPick;
		public long durationToSell;

		public CallForItem(ItemDef itemDef, double price, int priceIndex, double numPick, long durationToSell) {
			this.itemDef = itemDef;
			this.price = price;
			this.priceIndex = priceIndex;
			this.numPick = numPick;
			this.durationToSell = durationToSell;
		}
	}

	// 買取アイテム一覧を取得する。
	public ArrayList<CallForItem> GetDesiredItemList(double maxMoney, double maxNumPick) {
		ArrayList<CallForItem> callForItemList = new ArrayList<CallForItem>();

		// 欲しいアイテムリストを返す。
		for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
			ItemDef itemDef = e.getKey();
			CallForItem callForItem = this.GetDesiredItem(itemDef, maxMoney, maxNumPick);
			if (callForItem == null) continue;
			callForItemList.add(callForItem);
		}
		return callForItemList;
	}

	// 買い取り情報を取得する。
	public CallForItem GetDesiredItem(ItemDef itemDef, double maxMoney, double maxNumPick) {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;

		StockManager sm = deliverStockManager.get(itemDef);
		if (sm == null) return null;

		double price = sm.GetPriceWithRate();
		int priceIndex = sm.GetPriceIndex();

		// 購入する個数を決定する。
		double minNumPick = Double.MAX_VALUE;
		{
			// 希望の納品数による制約
			if (maxNumPick < minNumPick) minNumPick = maxNumPick;

			// 価格による制約
			double numPickForMoney = maxMoney / price;
			if (numPickForMoney < minNumPick) minNumPick = numPickForMoney;
		}

		// TODO:購入時間を計算する。個数に比例する。
		long durationToSell = deliverRoomDef.durationForDeliver;

		CallForItem callForItem = new CallForItem(itemDef, price, priceIndex, minNumPick, durationToSell);
		return callForItem;
	}

	public void SetMaterialPriceIndex(ItemDef itemDef, int priceIndex) {
		StockManager sm = deliverStockManager.get(itemDef);
		sm.SetPriceWithIndex(priceIndex);
	}

	// アイテムを売る。
	public void SellItem(long timeNow, CallForItem callForItem, boolean simulation) throws Exception {

		// アイテムを格納する。
		StockManager sm = deliverStockManager.get(callForItem.itemDef);

		sm.Put(timeNow, callForItem.numPick, simulation);

		if (simulation == false) {
			// 金を払う。
			this.AddMoney(timeNow, -callForItem.price * callForItem.numPick);
			materialOutputMoneyEMA.Add(timeNow, callForItem.price * callForItem.numPick);
		}
	}

	// 商品価格に対してフォードバックを与える。いくらだったらNo1の選択肢になったのか、各Humanがフィードバックを与える。
	public void FeedbackAboutDeliverPrice(ItemDef itemDef, int priceIndex, double quantity) {
		// System.out.println("FeedbackAboutDeliverPrice : " + this.roomDef.name + ", " + itemDef.GetName() + ", " + price + ", " + quantity);
		StockManager sm = deliverStockManager.get(itemDef);
		sm.Feedback(priceIndex, quantity);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// イベント記録用
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage materialOutputMoneyEMA = new ExponentialMovingAverage(60L * 24L * 1, true);

	public double GetMaterialOutputMoneyEMA(long timeNow) {
		return this.materialOutputMoneyEMA.GetAverage(timeNow);
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		super.DiscardOldLog(timeNow);
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Test
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// Obsolete
	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
}
