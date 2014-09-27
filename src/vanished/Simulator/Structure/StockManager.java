package vanished.Simulator.Structure;

import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Inventory;
import vanished.Simulator.AverageComputer;
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

	public Item Get(long timeNow, double numPick, boolean simulation) throws HumanSimulationException {
		Item ret;
		if (simulation == false) {
			ret = this.inventory.Get(itemDef, numPick);
			this.outputAverage.Add(numPick);
		} else {
			ret = this.inventory.Peek(itemDef, numPick);
			this.outputAverageSimulation.Add(numPick);
		}
		return ret;
	}

	public void Put(long timeNow, Item item, boolean simulation) throws HumanSimulationException {
		if (item.GetItemDef() != this.itemDef) throw new HumanSimulationException("fatal error");
		if (simulation == false) {
			this.inventory.Put(item);
			this.inputAverage.Add(item.GetQuantity());
		} else {
			this.inventory.PutTest(item);
			this.inputAverageSimulation.Add(item.GetQuantity());
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
	// ���v�p
	// ////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////
	private AverageComputer inputAverage = new AverageComputer();
	private AverageComputer outputAverage = new AverageComputer();

	private AverageComputer inputAverageSimulation = new AverageComputer();
	private AverageComputer outputAverageSimulation = new AverageComputer();

	public double GetInputTotal() {
		return this.inputAverage.GetTotal();
	}

	public double GetOutputTotal() {
		return this.outputAverage.GetTotal();
	}

	public double GetInputTotalSimulation() {
		return this.inputAverageSimulation.GetTotal();
	}

	public double GetOutputTotalSimulation() {
		return this.outputAverageSimulation.GetTotal();
	}

	public void ResetStatisticalParameters() {
		this.inputAverage.Clear();
		this.outputAverage.Clear();
		this.inputAverageSimulation.Clear();
		this.outputAverageSimulation.Clear();

		feedbackManager.ResetStatisticalParameters();
	}

	public FeedbackManager feedbackManager = new FeedbackManager();

	public void Feedback(double price, double quantity) {
		feedbackManager.Add(price, quantity);
	}

}