package vanished.GUI;

import ibm.ANACONDA.MatrixUtility;
import ibm.ANACONDA.MyMatrix;

public class Camera {

	private MyMatrix lookat = new MyMatrix(3, 1);
	private MyMatrix z = new MyMatrix(3, 1);
	private int distanceLevel = 0;

	public Camera() {
		z.set(1, 0, -1);
		z.set(2, 0, 1);
		z = MatrixUtility.NormalizeRowVector(z);
	}

	public Camera(Camera c) {
		this.lookat = new MyMatrix(c.lookat);
		this.z = new MyMatrix(c.z);
		this.distanceLevel = c.distanceLevel;
	}

	public MyMatrix GetLookat() {
		return lookat;
	}

	public static MyMatrix CrossProduct(MyMatrix am, MyMatrix bm) {
		double[] c = new double[3];
		double[] a = am.transpose().getArray()[0];
		double[] b = bm.transpose().getArray()[0];
		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];

		double[][] ret = new double[1][];
		ret[0] = c;
		return new MyMatrix(ret).transpose();
	}

	public MyMatrix GetR() throws Exception {
		z = MatrixUtility.NormalizeRowVector(z);
		double[][] yData = { { 0.0 }, { 1.0 }, { 0.0 } };
		MyMatrix y = new MyMatrix(yData);
		MyMatrix x = MatrixUtility.NormalizeRowVector(CrossProduct(y, z));
		y = MatrixUtility.NormalizeRowVector(CrossProduct(z, x));

		MyMatrix R = x;
		R = MatrixUtility.ConnectColumn(R, y);
		R = MatrixUtility.ConnectColumn(R, z);

		return R;
	}

	public double GetDistance() {
		return Math.pow(1.1, distanceLevel) * 10;
	}

	public void ChangeDistanceLevel(int def) {
		this.distanceLevel += def;
	}

	public MyMatrix GetT() throws Exception {
		return lookat.minus(z.times(GetDistance()));
	}

	public float[] GetRi4() throws Exception {
		MyMatrix R = this.GetR();
		MyMatrix t = this.GetT();
		return MU.ExpandMat4(MU.GetRi4(R, t));
	}

	public void MoveLookat(MyMatrix def) throws Exception {
		lookat.plusEquals(def);
	}

	public void ZMove(MyMatrix def) throws Exception {
		MyMatrix temp = z.plus(def);

		double bottom = -0.5;
		if (temp.get(1, 0) > bottom) {
			temp.set(1, 0, bottom);
		}
		z = MatrixUtility.NormalizeRowVector(temp);
	}

}
