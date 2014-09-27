package vanished.GUI;

import ibm.ANACONDA.MyMatrix;

public class MU {
	public static float[][] MatrixMultiply(float[][] R1, float[][] R2) {
		float[][] ret = new float[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				float total = 0;
				for (int k = 0; k < 4; k++) {
					total += R1[i][k] * R2[k][j];
				}
				ret[i][j] = total;
			}
		}
		return ret;
	}

	public static float[] MatrixVectorMultiply(float[][] R, float[] vec) {
		float[] ret = new float[4];
		for (int i = 0; i < 4; i++) {
			float total = 0;
			for (int k = 0; k < 4; k++) {
				total += R[i][k] * vec[k];
			}
			ret[i] = total;
		}
		return ret;
	}

	public static float[] VectorMultiply(float a, float[] vec) {
		float[] ret = new float[4];
		for (int i = 0; i < 3; i++) {
			ret[i] = vec[i] * a;
		}
		ret[3] = 1;
		return ret;
	}

	public static float[] Add(float[] a, float[] b) {
		float[] ret = new float[4];
		ret[0] = a[0] + b[0];
		ret[1] = a[1] + b[1];
		ret[2] = a[2] + b[2];
		ret[3] = 1;
		return ret;
	}

	public static float[] Sub(float[] a, float[] b) {
		float[] ret = new float[4];
		ret[0] = a[0] - b[0];
		ret[1] = a[1] - b[1];
		ret[2] = a[2] - b[2];
		ret[3] = 1;
		return ret;
	}

	public static float[] CrossProduct(float[] a, float[] b) {
		float[] c = new float[4];
		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];
		c[3] = 1;
		return c;
	}

	public static float DotProduct(float[] a, float[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}

	public static float Length(float[] a) {
		return (float) Math.sqrt(DotProduct(a, a));
	}

	public static float[] Normalize(float[] a) {
		float len = (float) Math.sqrt(DotProduct(a, a));
		return VectorMultiply(1 / len, a);
	}

	public static float[][] GetR(float[] x, float[] y, float[] z) {
		float[][] R = new float[4][4];
		R[0][0] = x[0];
		R[1][0] = x[1];
		R[2][0] = x[2];

		R[0][1] = y[0];
		R[1][1] = y[1];
		R[2][1] = y[2];

		R[0][2] = z[0];
		R[1][2] = z[1];
		R[2][2] = z[2];

		R[3][3] = 1;

		return R;
	}

	public static float[][] GetR4(float[] x, float[] y, float[] z, float[] p) {
		float[][] R = new float[4][4];
		R[0][0] = x[0];
		R[1][0] = x[1];
		R[2][0] = x[2];

		R[0][1] = y[0];
		R[1][1] = y[1];
		R[2][1] = y[2];

		R[0][2] = z[0];
		R[1][2] = z[1];
		R[2][2] = z[2];

		R[0][3] = p[0];
		R[1][3] = p[1];
		R[2][3] = p[2];
		R[3][3] = 1;

		return R;
	}

	public static float[][] GetR4(MyMatrix R, MyMatrix t) throws Exception {
		double[][] RD = R.getArray();
		float[][] ret = new float[4][4];

		ret[0][0] = (float) RD[0][0];
		ret[0][1] = (float) RD[0][1];
		ret[0][2] = (float) RD[0][2];

		ret[1][0] = (float) RD[1][0];
		ret[1][1] = (float) RD[1][1];
		ret[1][2] = (float) RD[1][2];

		ret[2][0] = (float) RD[2][0];
		ret[2][1] = (float) RD[2][1];
		ret[2][2] = (float) RD[2][2];

		double[][] qData = t.getArray();

		ret[0][3] = (float) qData[0][0];
		ret[1][3] = (float) qData[1][0];
		ret[2][3] = (float) qData[2][0];
		ret[3][3] = 1;

		return ret;
	}

	public static float[][] GetRi4(float[] x, float[] y, float[] z, float[] p) {
		float[][] R = new float[4][4];
		R[0][0] = x[0];
		R[0][1] = x[1];
		R[0][2] = x[2];

		R[1][0] = y[0];
		R[1][1] = y[1];
		R[1][2] = y[2];

		R[2][0] = z[0];
		R[2][1] = z[1];
		R[2][2] = z[2];

		R[0][3] = -DotProduct(x, p);
		R[1][3] = -DotProduct(y, p);
		R[2][3] = -DotProduct(z, p);
		R[3][3] = 1;

		return R;
	}

	public static float[][] GetRi4(MyMatrix R, MyMatrix t) throws Exception {
		double[][] RD = R.getArray();
		float[][] ret = new float[4][4];

		ret[0][0] = (float) RD[0][0];
		ret[0][1] = (float) RD[1][0];
		ret[0][2] = (float) RD[2][0];

		ret[1][0] = (float) RD[0][1];
		ret[1][1] = (float) RD[1][1];
		ret[1][2] = (float) RD[2][1];

		ret[2][0] = (float) RD[0][2];
		ret[2][1] = (float) RD[1][2];
		ret[2][2] = (float) RD[2][2];

		MyMatrix q = R.transpose().times(t.times(-1.0));
		double[][] qData = q.getArray();

		ret[0][3] = (float) qData[0][0];
		ret[1][3] = (float) qData[1][0];
		ret[2][3] = (float) qData[2][0];
		ret[3][3] = 1;

		return ret;
	}

	public static float[] ExpandMat4(float[][] R) {
		float[] ret = new float[16];

		if (true) {
			ret[0] = R[0][0];
			ret[1] = R[1][0];
			ret[2] = R[2][0];
			ret[3] = R[3][0];

			ret[4] = R[0][1];
			ret[5] = R[1][1];
			ret[6] = R[2][1];
			ret[7] = R[3][1];

			ret[8] = R[0][2];
			ret[9] = R[1][2];
			ret[10] = R[2][2];
			ret[11] = R[3][2];

			ret[12] = R[0][3];
			ret[13] = R[1][3];
			ret[14] = R[2][3];
			ret[15] = R[3][3];
		}

		return ret;
	}

}
