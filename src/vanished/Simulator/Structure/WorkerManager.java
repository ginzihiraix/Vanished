package vanished.Simulator.Structure;

import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Structure.RunnableRoomDef.WorkerRequirement;

public class WorkerManager {

	// 労働者の賃金
	double wage = 1;

	// 部屋にいる労働者のリスト
	private HumanExistRecordManager workerExistRecordManager = new HumanExistRecordManager();

	// 過去の労働者の労働ログ。
	private HumanExistRecordManager workerHistory = new HumanExistRecordManager();

	// ある期間の、部屋にいる労働者の平均数が、最低稼動人数より多いか調べる。
	public boolean IsRunnable(WorkerRequirement wc, long timeStart, long duration) {
		double aveNumWorker = workerExistRecordManager.AverageNum(timeStart, duration);
		if (aveNumWorker < wc.minAverageNumWorkerForRun) return false;
		return true;
	}

	// 労働者が部屋に入れるか調べる。
	public boolean IsEnterableAsWorker(WorkerRequirement wc, long timeStart, long duration) {
		double numMaxWorker = this.workerExistRecordManager.MaxNum(timeStart, duration);
		if (numMaxWorker + 1 > wc.workerCapacityForRun) return false;
		return true;
	}

	// 部屋にいる労働者の平均数が、標準パフォーマンス時の稼動人数の何倍か調べる。最低稼動人数を下回った場合は0を返す。
	public double ComputeEfficiency(WorkerRequirement wc, long timeStart, long duration) {
		double aveNumWorker = workerExistRecordManager.AverageNum(timeStart, duration);
		if (aveNumWorker < wc.minAverageNumWorkerForRun) return 0;
		if (wc.numWorkerForStandardPerformance == 0) {
			return Double.MAX_VALUE;
		} else {
			double efficiency = aveNumWorker / wc.numWorkerForStandardPerformance;
			return efficiency;
		}
	}

	// 過去一定期間の平均労働者数を見ながら、賃金を調整する。
	public void UpdateWedge(WorkerRequirement wc, long timeNow) {
		long duration = 60 * 24 * 10;
		double average = workerHistory.AverageNum(timeNow - duration, duration);
		if (average < wc.numWorkerForStandardPerformance) {
			wage *= 1.01;
		} else {
			wage /= 1.01;
		}
	}

	// 労働者として働く。部屋に入れるか調べて、入れる場合は、レコードを記録する。
	public void Work(WorkerRequirement wc, long timeStart, long duration, boolean simulation) throws HumanSimulationException {
		if (this.IsEnterableAsWorker(wc, timeStart, duration) == false) throw new HumanSimulationException(
				"WorkerManager : no capacity for human resources");
		if (simulation == false) {
			this.workerExistRecordManager.Add(timeStart, duration, 1);
			this.workerHistory.Add(timeStart, duration, 1);
		}
	}
}
