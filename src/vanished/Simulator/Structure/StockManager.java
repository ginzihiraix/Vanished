package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Inventory;
import vanished.Simulator.OtherUtility;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class StockManager {

	private ItemDef itemDef;

	StockManagerInfo stockManagerInfo;

	double price;

	private Inventory inventory;

	public StockManager(ItemDef itemDef, StockManagerInfo stockManagerInfo) {
		this.itemDef = itemDef;
		this.stockManagerInfo = stockManagerInfo;
		price = 1 + OtherUtility.RandGaussian() * 0.1;
		inventory = new Inventory(stockManagerInfo.capacity * itemDef.GetWeight());
	}

	public double GetCapacity() {
		double ret = inventory.GetCapacity() / itemDef.GetWeight();
		return ret;
	}

	public double FindStockSpace() {
		return this.inventory.FindSpace(itemDef);
	}

	public double GetNumStock() {
		return this.inventory.GetNumStock(itemDef);
	}

	public Item Get(long timeNow, double numPick, boolean simulation) throws Exception {
		Item ret;
		if (simulation == false) {
			ret = this.inventory.Get(itemDef, numPick);
			this.numStockEMA.Add(timeNow, this.inventory.GetNumStock(itemDef));
			this.outputStockEMA.Add(timeNow, numPick);
		} else {
			ret = this.inventory.Peek(itemDef, numPick);
		}
		return ret;
	}

	public void Put(long timeNow, Item item, boolean simulation) throws Exception {
		if (item.GetItemDef() != this.itemDef) throw new Exception("fatal error");
		if (simulation == false) {
			this.inventory.Put(item);
			this.numStockEMA.Add(timeNow, this.inventory.GetNumStock(itemDef));
			this.inputStockEMA.Add(timeNow, item.GetQuantity());
		} else {
			this.inventory.PutTest(item);
		}
	}

	public double GetCurrentMaxLot() {
		double space = this.inventory.FindSpace(itemDef);
		double lot;
		if (space < this.stockManagerInfo.lotmax) {
			lot = space;
		} else {
			lot = this.stockManagerInfo.lotmax;
		}
		return lot;
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ���O�p�B�݌ɗʂ̕ω����L�^��������B����I�ɑ�̂̃��O�͏����B
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	private ExponentialMovingAverage numStockEMA = new ExponentialMovingAverage(60L * 24L * 10, false);
	private ExponentialMovingAverage inputStockEMA = new ExponentialMovingAverage(60L * 24L * 10, true);
	private ExponentialMovingAverage outputStockEMA = new ExponentialMovingAverage(60L * 24L * 10, true);

	public double GetNumStockEMA(long timeNow) {
		return numStockEMA.GetAverage(timeNow);
	}

	public double GetInputStockEMA(long timeNow) {
		return inputStockEMA.GetAverage(timeNow);
	}

	public double GetOutputStockEMA(long timeNow) {
		return outputStockEMA.GetAverage(timeNow);
	}

	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	// ���v�p�B���i�̒����Ɏg���B���i�������ɃN���A����B
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////

	public FeedbackManager feedbackManager = new FeedbackManager();

	public void Feedback(double price, double quantity) {
		feedbackManager.Add(price, quantity);
	}

	public void ResetStatisticalParameters() {
		feedbackManager.ResetStatisticalParameters();
	}

}