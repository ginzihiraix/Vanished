package vanished.Simulator.Structure;

import vanished.Simulator.ExponentialMovingAverage;
import vanished.Simulator.Item.Item;
import vanished.Simulator.Item.ItemDef;

public class StockManager {

	StockManagerInfo stockManagerInfo;

	private int priceIndex = 0;
	private double priceStepSize = 1.01;

	private double numStock;

	public StockManager(StockManagerInfo stockManagerInfo) {
		this.stockManagerInfo = stockManagerInfo;
	}

	public double GetPriceWithRate() {
		return Math.pow(priceStepSize, priceIndex);
	}

	public int GetPriceIndex() {
		return priceIndex;
	}

	public void SetPriceWithIndex(int priceIndex) {
		this.priceIndex = priceIndex;
	}

	public void SetPrice(double price) {
		this.priceIndex = (int) (Math.log(price) / Math.log(priceStepSize) + 0.5);
	}

	public double GetNumStock() {
		return numStock;
	}

	public double Get(long timeNow, double numPick, boolean simulation) throws Exception {
		if (simulation == false) {
			numStock -= numPick;
			this.numStockEMA.Add(timeNow, numStock);
			this.outputStockEMA.Add(timeNow, numPick);
		}

		return numPick;
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

	public void Feedback(int priceIndex, double quantity) {
		// feedbackManager.Add(priceIndex, quantity);
	}

	public void ResetStatisticalParameters() {
		feedbackManager.ResetStatisticalParameters();
	}

}