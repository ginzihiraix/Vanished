package vanished.Simulator.Structure;

import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Structure.RunnableRoomDef.WorkerRequirement;

public class WorkerManager {

	// �J���҂̒���
	double wage = 1;

	// �����ɂ���J���҂̃��X�g
	private HumanExistRecordManager workerExistRecordManager = new HumanExistRecordManager();

	// �ߋ��̘J���҂̘J�����O�B
	private HumanExistRecordManager workerHistory = new HumanExistRecordManager();

	// ������Ԃ́A�����ɂ���J���҂̕��ϐ����A�Œ�ғ��l����葽�������ׂ�B
	public boolean IsRunnable(WorkerRequirement wc, long timeStart, long duration) {
		double aveNumWorker = workerExistRecordManager.AverageNum(timeStart, duration);
		if (aveNumWorker < wc.minAverageNumWorkerForRun) return false;
		return true;
	}

	// �J���҂������ɓ���邩���ׂ�B
	public boolean IsEnterableAsWorker(WorkerRequirement wc, long timeStart, long duration) {
		double numMaxWorker = this.workerExistRecordManager.MaxNum(timeStart, duration);
		if (numMaxWorker + 1 > wc.workerCapacityForRun) return false;
		return true;
	}

	// �����ɂ���J���҂̕��ϐ����A�W���p�t�H�[�}���X���̉ғ��l���̉��{�����ׂ�B�Œ�ғ��l������������ꍇ��0��Ԃ��B
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

	// �ߋ������Ԃ̕��ϘJ���Ґ������Ȃ���A�����𒲐�����B
	public void UpdateWedge(WorkerRequirement wc, long timeNow) {
		long duration = 60 * 24 * 10;
		double average = workerHistory.AverageNum(timeNow - duration, duration);
		if (average < wc.numWorkerForStandardPerformance) {
			wage *= 1.01;
		} else {
			wage /= 1.01;
		}
	}

	// �J���҂Ƃ��ē����B�����ɓ���邩���ׂāA�����ꍇ�́A���R�[�h���L�^����B
	public void Work(WorkerRequirement wc, long timeStart, long duration, boolean simulation) throws HumanSimulationException {
		if (this.IsEnterableAsWorker(wc, timeStart, duration) == false) throw new HumanSimulationException(
				"WorkerManager : no capacity for human resources");
		if (simulation == false) {
			this.workerExistRecordManager.Add(timeStart, duration, 1);
			this.workerHistory.Add(timeStart, duration, 1);
		}
	}
}
