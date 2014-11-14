package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;

public class DeliverRoom extends Room {

	TreeMap<ItemDef, StockManager> deliverStockManager = new TreeMap<ItemDef, StockManager>(new ItemDefComparator());

	// �����ɂ���l�̃��X�g
	private HumanCapacityManager deliverHumanCapacityManager;

	public DeliverRoom(Building building, DeliverRoomDef roomDef) {
		super(building, roomDef);

		for (Entry<ItemDef, StockManagerInfo> e : roomDef.materialStockManagerInfo.entrySet()) {
			ItemDef itemDef = e.getKey();
			StockManagerInfo smi = e.getValue();
			StockManager sm = new StockManager(smi);
			deliverStockManager.put(itemDef, sm);
		}

		deliverHumanCapacityManager = new HumanCapacityManager(roomDef.capacityDeliver);
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

	// ����A�C�e���ꗗ���擾����B
	public ArrayList<CallForItem> GetDesiredItemList(double maxMoney, double maxNumPick) {
		ArrayList<CallForItem> callForItemList = new ArrayList<CallForItem>();

		// �~�����A�C�e�����X�g��Ԃ��B
		for (Entry<ItemDef, StockManager> e : deliverStockManager.entrySet()) {
			ItemDef itemDef = e.getKey();
			CallForItem callForItem = this.GetDesiredItem(itemDef, maxMoney, maxNumPick);
			if (callForItem == null) continue;
			callForItemList.add(callForItem);
		}
		return callForItemList;
	}

	// �����������擾����B
	public CallForItem GetDesiredItem(ItemDef itemDef, double maxMoney, double maxNumPick) {
		DeliverRoomDef deliverRoomDef = (DeliverRoomDef) roomDef;

		StockManager sm = deliverStockManager.get(itemDef);
		if (sm == null) return null;

		double price = sm.GetPriceWithRate();
		int priceIndex = sm.GetPriceIndex();

		// �w������������肷��B
		double minNumPick = Double.MAX_VALUE;
		{
			// ��]�̔[�i���ɂ�鐧��
			if (maxNumPick < minNumPick) minNumPick = maxNumPick;

			// ���i�ɂ�鐧��
			double numPickForMoney = maxMoney / price;
			if (numPickForMoney < minNumPick) minNumPick = numPickForMoney;
		}

		// TODO:�w�����Ԃ��v�Z����B���ɔ�Ⴗ��B
		long durationToSell = deliverRoomDef.durationForDeliver;

		CallForItem callForItem = new CallForItem(itemDef, price, priceIndex, minNumPick, durationToSell);
		return callForItem;
	}

	public void SetMaterialPriceIndex(ItemDef itemDef, int priceIndex) {
		StockManager sm = deliverStockManager.get(itemDef);
		sm.SetPriceWithIndex(priceIndex);
	}

	public class DeliverResult {
		long timeEnd;

		public DeliverResult(long timeEnd) {
			this.timeEnd = timeEnd;
		}
	}

	// �A�C�e���𔄂�B
	public DeliverResult SellItem(long timeNow, CallForItem callForItem, double prob, boolean simulation) throws Exception {

		long timeEnd = this.deliverHumanCapacityManager.Add(timeNow, callForItem.durationToSell, prob, simulation);

		// �A�C�e�����i�[����B
		StockManager sm = deliverStockManager.get(callForItem.itemDef);
		sm.Put(timeNow, callForItem.numPick * prob, simulation);

		// ���𕥂��B
		double pay = callForItem.price * callForItem.numPick;
		this.AddMoney(timeNow, -pay * prob, simulation);

		// ���v�����L�^����B
		if (simulation == false) {
			materialOutputMoneyEMA.Add(timeNow, pay * prob);
		}

		return new DeliverResult(timeEnd);
	}

	// ���i���i�ɑ΂��ăt�H�[�h�o�b�N��^����B�����炾������No1�̑I�����ɂȂ����̂��A�eHuman���t�B�[�h�o�b�N��^����B
	// public void FeedbackAboutDeliverPrice(ItemDef itemDef, int priceIndex, double quantity) {
	// System.out.println("FeedbackAboutDeliverPrice : " + this.roomDef.name + ", " + itemDef.GetName() + ", " + price + ", " + quantity);
	// StockManager sm = deliverStockManager.get(itemDef);
	// sm.Feedback(priceIndex, quantity);
	// }

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// �C�x���g�L�^�p
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
