package vanished.GUI;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

public class VoxelManager {
	private int numx = 32;
	private int numy = 16;
	private int numz = 32;

	private Voxel[][][] voxel;
	private ArrayList<RenderObject> renderObjects = new ArrayList<RenderObject>();

	public VoxelManager(int numx, int numy, int numz) {
		this.numx = numx;
		this.numy = numy;
		this.numz = numz;

		voxel = new Voxel[numx][numy][numz];

		for (int x = 0; x < numx; x++) {
			for (int y = 0; y < numy; y++) {
				for (int z = 0; z < numz; z++) {
					voxel[x][y][z] = new Voxel(0);
				}
			}
		}
	}

	public void PutVoxel(int x, int y, int z, int type) {
		voxel[x][y][z] = new Voxel(type);
	}

	@SuppressWarnings("unchecked")
	public void Reconstruct() {
		renderObjects.clear();

		if (true) {
			// 表示する四角形の数を数える。
			int numPlane = 0;
			for (int x = 0; x < numx; x++) {
				for (int y = 0; y < numy; y++) {
					for (int z = 0; z < numz; z++) {
						if (voxel[x][y][z].type == 0) continue;

						if (x != 0) {
							if (voxel[x - 1][y][z].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}

						if (x != numx - 1) {
							if (voxel[x + 1][y][z].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}

						if (y != 0) {
							if (voxel[x][y - 1][z].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}

						if (y != numy - 1) {
							if (voxel[x][y + 1][z].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}

						if (z != 0) {
							if (voxel[x][y][z - 1].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}

						if (z != numz - 1) {
							if (voxel[x][y][z + 1].type == 0) {
								numPlane++;
							}
						} else {
							numPlane++;
						}
					}
				}
			}

			float[] coord = new float[2 * numPlane * 4];
			float[] color = new float[4 * numPlane * 4];
			float[] vertex = new float[3 * numPlane * 4];
			float[] norm = new float[3 * numPlane * 4];
			short[] index = new short[numPlane * 6];

			for (int p = 0; p < numPlane; p++) {
				for (int i = 0; i < 4; i++) {
					color[p * 16 + i * 4 + 0] = 1;
					color[p * 16 + i * 4 + 1] = 1;
					color[p * 16 + i * 4 + 2] = 1;
					color[p * 16 + i * 4 + 3] = 1;
				}
			}

			for (int p = 0; p < numPlane; p++) {
				coord[p * 8 + 0] = 0;
				coord[p * 8 + 1] = 1;
				coord[p * 8 + 2] = 1;
				coord[p * 8 + 3] = 1;
				coord[p * 8 + 4] = 1;
				coord[p * 8 + 5] = 0;
				coord[p * 8 + 6] = 0;
				coord[p * 8 + 7] = 0;
			}

			for (int p = 0; p < numPlane; p++) {
				index[p * 6 + 0] = (short) (0 + p * 4);
				index[p * 6 + 1] = (short) (1 + p * 4);
				index[p * 6 + 2] = (short) (2 + p * 4);
				index[p * 6 + 3] = (short) (0 + p * 4);
				index[p * 6 + 4] = (short) (2 + p * 4);
				index[p * 6 + 5] = (short) (3 + p * 4);
			}

			float[][][] box = { { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 1, 1 }, { 0, 0, 1 } }, { { 1, 1, 0 }, { 1, 0, 0 }, { 1, 0, 1 }, { 1, 1, 1 } },
					{ { 0, 0, 0 }, { 0, 0, 1 }, { 1, 0, 1 }, { 1, 0, 0 } }, { { 0, 1, 1 }, { 0, 1, 0 }, { 1, 1, 0 }, { 1, 1, 1 } },
					{ { 0, 0, 0 }, { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 } }, { { 1, 0, 1 }, { 0, 0, 1 }, { 0, 1, 1 }, { 1, 1, 1 } } };

			float[][] boxNorm = { { -1, 0, 0 }, { 1, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

			int numPlane2 = 0;
			for (int x = 0; x < numx; x++) {
				for (int y = 0; y < numy; y++) {
					for (int z = 0; z < numz; z++) {
						if (voxel[x][y][z].type == 0) continue;

						boolean[] planeFlag = new boolean[6];
						if (x != 0) {
							if (voxel[x - 1][y][z].type == 0) {
								planeFlag[0] = true;
							}
						} else {
							planeFlag[0] = true;
						}

						if (x != numx - 1) {
							if (voxel[x + 1][y][z].type == 0) {
								planeFlag[1] = true;
							}
						} else {
							planeFlag[1] = true;
						}

						if (y != 0) {
							if (voxel[x][y - 1][z].type == 0) {
								planeFlag[2] = true;
							}
						} else {
							planeFlag[2] = true;
						}

						if (y != numy - 1) {
							if (voxel[x][y + 1][z].type == 0) {
								planeFlag[3] = true;
							}
						} else {
							planeFlag[3] = true;
						}

						if (z != 0) {
							if (voxel[x][y][z - 1].type == 0) {
								planeFlag[4] = true;
							}
						} else {
							planeFlag[4] = true;
						}

						if (z != numz - 1) {
							if (voxel[x][y][z + 1].type == 0) {
								planeFlag[5] = true;
							}
						} else {
							planeFlag[5] = true;
						}

						for (int pi = 0; pi < 6; pi++) {
							if (planeFlag[pi] == false) continue;

							for (int i = 0; i < 4; i++) {
								vertex[numPlane2 * 12 + i * 3 + 0] = x + box[pi][i][0];
								vertex[numPlane2 * 12 + i * 3 + 1] = y + box[pi][i][1];
								vertex[numPlane2 * 12 + i * 3 + 2] = z + box[pi][i][2];

								norm[numPlane2 * 12 + i * 3 + 0] = boxNorm[pi][0];
								norm[numPlane2 * 12 + i * 3 + 1] = boxNorm[pi][1];
								norm[numPlane2 * 12 + i * 3 + 2] = boxNorm[pi][2];
							}
							numPlane2++;
						}
					}
				}
			}

			RenderObject ro = new RenderObject(numPlane * 6, 1, -1, -1, vertex, norm, coord, color, index);

			renderObjects.add(ro);
		}

		// ===============================================================================
		// 四隅の影部分のポリゴンを構築する。
		if (true) {
			float[][][] Rp = new float[6][4][4];

			Rp[0][0][2] = 1;
			Rp[0][1][0] = 1;
			Rp[0][2][1] = 1;
			Rp[0][3][3] = 1;

			Rp[1][0][2] = -1;
			Rp[1][1][0] = -1;
			Rp[1][2][1] = 1;
			Rp[1][1][3] = 1;
			Rp[1][0][3] = 1;
			Rp[1][3][3] = 1;

			Rp[2][0][1] = 1;
			Rp[2][1][2] = 1;
			Rp[2][2][0] = 1;
			Rp[2][3][3] = 1;

			Rp[3][0][1] = 1;
			Rp[3][1][2] = -1;
			Rp[3][2][0] = -1;
			Rp[3][2][3] = 1;
			Rp[3][1][3] = 1;
			Rp[3][3][3] = 1;

			Rp[4][0][0] = 1;
			Rp[4][1][1] = 1;
			Rp[4][2][2] = 1;
			Rp[4][3][3] = 1;

			Rp[5][0][0] = -1;
			Rp[5][1][1] = 1;
			Rp[5][2][2] = -1;
			Rp[5][0][3] = 1;
			Rp[5][2][3] = 1;
			Rp[5][3][3] = 1;

			float[][][] Rc = new float[4][4][4];
			Rc[0][0][0] = 1;
			Rc[0][1][1] = 1;
			Rc[0][2][2] = 1;
			Rc[0][3][3] = 1;

			Rc[1][0][1] = -1;
			Rc[1][1][0] = 1;
			Rc[1][2][2] = 1;
			Rc[1][0][3] = 1;
			Rc[1][3][3] = 1;

			Rc[2][0][0] = -1;
			Rc[2][1][1] = -1;
			Rc[2][2][2] = 1;
			Rc[2][0][3] = 1;
			Rc[2][1][3] = 1;
			Rc[2][3][3] = 1;

			Rc[3][0][1] = 1;
			Rc[3][1][0] = -1;
			Rc[3][2][2] = 1;
			Rc[3][1][3] = 1;
			Rc[3][3][3] = 1;

			float[][][][] Rmix = new float[6][4][4][4];
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4; j++) {
					Rmix[i][j] = MU.MatrixMultiply(Rp[i], Rc[j]);
				}
			}

			float[][] checkPos = new float[3][4];
			checkPos[0][0] = -0.5f;
			checkPos[0][1] = 0.5f;
			checkPos[0][2] = -0.5f;
			checkPos[0][3] = 1;

			checkPos[1][0] = 0.5f;
			checkPos[1][1] = -0.5f;
			checkPos[1][2] = -0.5f;
			checkPos[1][3] = 1;

			checkPos[2][0] = -0.5f;
			checkPos[2][1] = -0.5f;
			checkPos[2][2] = -0.5f;
			checkPos[2][3] = 1;

			float[][] cornerPointTemplate = new float[4][4];
			cornerPointTemplate[0][0] = 0;
			cornerPointTemplate[0][1] = 0;
			cornerPointTemplate[0][2] = -0.001f;
			cornerPointTemplate[0][3] = 1;

			cornerPointTemplate[1][0] = 1;
			cornerPointTemplate[1][1] = 0;
			cornerPointTemplate[1][2] = -0.001f;
			cornerPointTemplate[1][3] = 1;

			cornerPointTemplate[2][0] = 1;
			cornerPointTemplate[2][1] = 1;
			cornerPointTemplate[2][2] = -0.001f;
			cornerPointTemplate[2][3] = 1;

			cornerPointTemplate[3][0] = 0;
			cornerPointTemplate[3][1] = 1;
			cornerPointTemplate[3][2] = -0.001f;
			cornerPointTemplate[3][3] = 1;

			float[][] coordTemplate = new float[4][2];
			coordTemplate[0][0] = 0;
			coordTemplate[0][1] = 1;

			coordTemplate[1][0] = 1;
			coordTemplate[1][1] = 1;

			coordTemplate[2][0] = 1;
			coordTemplate[2][1] = 0;

			coordTemplate[3][0] = 0;
			coordTemplate[3][1] = 0;

			float[] nTemplate = new float[4];
			nTemplate[0] = 0;
			nTemplate[1] = 0;
			nTemplate[2] = -1;
			nTemplate[3] = 0;

			float[] colorTemplate = new float[4];
			colorTemplate[0] = 1;
			colorTemplate[1] = 1;
			colorTemplate[2] = 1;
			colorTemplate[3] = 1;

			short[] indexTemplate = new short[6];
			indexTemplate[0] = 0;
			indexTemplate[1] = 1;
			indexTemplate[2] = 2;
			indexTemplate[3] = 0;
			indexTemplate[4] = 2;
			indexTemplate[5] = 3;

			// 表示する四角形の数を数える。
			class Plane {
				int x, y, z;
				int pi;
				int ci;

				public Plane(int x, int y, int z, int pi, int ci) {
					this.x = x;
					this.y = y;
					this.z = z;
					this.pi = pi;
					this.ci = ci;
				}
			}

			ArrayList<Plane>[] planes = new ArrayList[8];
			for (int i = 0; i < 8; i++) {
				planes[i] = new ArrayList<Plane>();
			}

			for (int x = 0; x < numx; x++) {
				for (int y = 0; y < numy; y++) {
					for (int z = 0; z < numz; z++) {
						if (voxel[x][y][z].type == 0) continue;

						boolean[] planeFlag = new boolean[6];
						if (x != 0) {
							if (voxel[x - 1][y][z].type == 0) {
								planeFlag[0] = true;
							}
						}
						if (x != numx - 1) {
							if (voxel[x + 1][y][z].type == 0) {
								planeFlag[1] = true;
							}
						}
						if (y != 0) {
							if (voxel[x][y - 1][z].type == 0) {
								planeFlag[2] = true;
							}
						}
						if (y != numy - 1) {
							if (voxel[x][y + 1][z].type == 0) {
								planeFlag[3] = true;
							}
						}
						if (z != 0) {
							if (voxel[x][y][z - 1].type == 0) {
								planeFlag[4] = true;
							}
						}
						if (z != numz - 1) {
							if (voxel[x][y][z + 1].type == 0) {
								planeFlag[5] = true;
							}
						}

						for (int pi = 0; pi < 6; pi++) {
							if (planeFlag[pi] == false) continue;

							for (int ci = 0; ci < 4; ci++) {

								boolean[] closeFlag = new boolean[3];
								for (int li = 0; li < 3; li++) {
									float[] location = MU.MatrixVectorMultiply(Rmix[pi][ci], checkPos[li]);
									int x2 = (int) Math.floor(x + location[0]);
									int y2 = (int) Math.floor(y + location[1]);
									int z2 = (int) Math.floor(z + location[2]);

									if (x2 < 0 || x2 >= numx || y2 < 0 || y2 >= numy || z2 < 0 || z2 >= numz) continue;
									if (voxel[x2][y2][z2].type != 0) {
										closeFlag[li] = true;
									}
								}

								int textureID = 0;
								int[] add = { 1, 2, 4 };
								for (int li = 0; li < 3; li++) {

									if (closeFlag[li] == true) {
										textureID += add[li];
									}
								}

								if (textureID == 0) continue;

								Plane plane = new Plane(x, y, z, pi, ci);
								planes[textureID].add(plane);
							}
						}
					}
				}
			}

			for (int ti = 0; ti < 8; ti++) {
				int numPlane = planes[ti].size();
				if (numPlane == 0) continue;

				float[] coord = new float[2 * numPlane * 4];
				float[] vertex = new float[3 * numPlane * 4];
				float[] norm = new float[3 * numPlane * 4];
				float[] color = new float[4 * numPlane * 4];
				short[] index = new short[numPlane * 6];

				int count = 0;
				for (Plane plane : planes[ti]) {
					int x = plane.x;
					int y = plane.y;
					int z = plane.z;
					int pi = plane.pi;
					int ci = plane.ci;

					float[] basePosition = new float[3];
					basePosition[0] = x;
					basePosition[1] = y;
					basePosition[2] = z;

					for (int i = 0; i < 4; i++) {
						for (int d = 0; d < 2; d++) {
							coord[count * 2 * 4 + i * 2 + d] = coordTemplate[i][d];
						}

						float[] location = MU.MatrixVectorMultiply(Rmix[pi][ci], MU.VectorMultiply(0.5f, cornerPointTemplate[i]));
						float[] n = MU.MatrixVectorMultiply(Rmix[pi][ci], nTemplate);
						for (int d = 0; d < 3; d++) {
							vertex[count * 3 * 4 + i * 3 + d] = location[d] + basePosition[d];
							norm[count * 3 * 4 + i * 3 + d] = n[d];
						}

						for (int d = 0; d < 4; d++) {
							color[count * 4 * 4 + i * 4 + d] = colorTemplate[d];
						}
					}

					for (int d = 0; d < 6; d++) {
						index[count * 6 + d] = (short) (count * 4 + indexTemplate[d]);
					}

					count++;
				}

				RenderObject ro = new RenderObject(numPlane * 6, 1000 + ti, GL.GL_ZERO, GL.GL_SRC_COLOR, vertex, norm, coord, color, index);
				renderObjects.add(ro);
			}
		}
	}

	public void Draw(GL2 gl, LightSetting lightGlobal) {
		for (RenderObject ro : renderObjects) {
			ro.Draw(gl, lightGlobal);
		}
	}

	public class HitInfo {
		float[] hitPos;
		int[] hitIndex;
		int[] hitPlane;

		public HitInfo(float[] hitPos, int[] hitIndex, int[] hitPlane) {
			this.hitPos = hitPos;
			this.hitIndex = hitIndex;
			this.hitPlane = hitPlane;
		}
	}

	public HitInfo HitVoxel(float[] start, float[] vec, boolean infFlag) {
		int[] num = new int[3];
		num[0] = numx;
		num[1] = numy;
		num[2] = numz;

		int[] index = new int[3];
		for (int i = 0; i < 3; i++) {
			index[i] = (int) Math.floor(start[i]);
		}

		int[] vecIndex = new int[3];
		for (int i = 0; i < 3; i++) {
			if (vec[i] > 0) {
				vecIndex[i] = 1;
			} else {
				vecIndex[i] = -1;
			}
		}

		// 次にぶつかる面を探す。
		while (true) {
			if (true) {
				boolean flag = true;
				for (int d = 0; d < 3; d++) {
					if (vec[d] > 0) {
						if (index[d] >= num[d]) {
							flag = false;
							break;
						}
					} else if (vec[d] < 0) {
						if (index[d] < 0) {
							flag = false;
							break;
						}
					} else {
						continue;
					}
				}
				if (flag == false) return null;
			}

			float mink = Float.MAX_VALUE;
			int mind = -1;
			for (int d = 0; d < 3; d++) {
				float next;
				if (vec[d] > 0) {
					next = index[d] + 1;
				} else if (vec[d] < 0) {
					next = index[d];
				} else {
					continue;
				}
				float k = (next - start[d]) / vec[d];
				if (k < mink) {
					mink = k;
					mind = d;
				}
			}

			if (infFlag == false && mink > 1.0) return null;

			index[mind] += vecIndex[mind];

			if (true) {
				boolean flag = true;
				for (int d = 0; d < 3; d++) {
					if (index[d] < 0 || index[d] >= num[d]) {
						flag = false;
						break;
					}
				}
				if (flag == false) continue;
			}

			if (voxel[index[0]][index[1]][index[2]].type == 1) {
				float[] hitPos = new float[3];
				for (int d = 0; d < 3; d++) {
					hitPos[d] = start[d] + mink * vec[d];
				}
				int[] hitPlane = new int[3];
				hitPlane[mind] = -vecIndex[mind];
				return new HitInfo(hitPos, index, hitPlane);
			}
		}
	}
}
