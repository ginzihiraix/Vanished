package vanished.Simulator.Structure;

public class FeedbackLog {
	public int priceIndex;
	public double impressionTotal;
	public double quantityTotal;

	public FeedbackLog(int priceIndex) {
		this.priceIndex = priceIndex;
	}

	public FeedbackLog(int priceIndex, double impressionTotal, double quantityTotal) {
		this.priceIndex = priceIndex;
		this.impressionTotal = impressionTotal;
		this.quantityTotal = quantityTotal;
	}
}
