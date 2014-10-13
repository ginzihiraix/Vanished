package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class StockManager {

	private ItemDef itemDef;

	StockManagerInfo stockManagerInfo;

	private double price;
	private double priceRate = 1;

	private boolean open = true;

	private double numStock;

	public StockManager(ItemDef itemDef, StockManagerInfo stockManagerInfo) {
		this.itemDef = itemDef;
		this.stockManagerInfo = stockManagerInfo;
		price = 1;
	}

	public boolean IsOpen() {
		return open;
	}

	public void Close() {
		open = false;
	}

	public void Open() {
		open = true;
	}

	public double GetPriceWithRate() {
		return price * priceRate;
	}

	public void SetPrice(double price) {
		this.price = price;
	}

	public void SetPriceRate(double priceRate) {
		this.priceRate = priceRate;
	}

	public double GetNumStock() {
		return numStock;
	}

	public Item Get(long timeNow, double numPick, boolean simulation) throws Exception {
		if (simulation == false) {
			numStock -= numPick;
			this.numStockEMA.Add(timeNow, numStock);
			this.outputStockEMA.Add(timeNow, numPick);
		} else {
		}
		Item ret = new Item(itemDef, numPick);
		return ret;
	}

	public void Put(long timeNow, double numPut, boolean simulation) throws Exception {
		if (simulation == false) {
			numStock += numPut;
			this.numStockEMA.Add(timeNow, numStock);
			this.inputStockEMA.Add(timeNow, numPut);
		} else {
		}
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