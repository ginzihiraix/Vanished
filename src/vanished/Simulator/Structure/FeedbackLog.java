package vanished.Simulator.Structure;

public class FeedbackLog {
	public double price;
	public double impressionTotal;
	public double quantityTotal;

	public FeedbackLog(double price) {
		this.price = price;
	}

	public FeedbackLog(double price, double impressionTotal, double quantityTotal) {
		this.price = price;
		this.impressionTotal = impressionTotal;
		this.quantityTotal = quantityTotal;
	}
}
