package vanished.GUI;

import javax.media.opengl.GL2;

public class LightSetting {
	boolean flag;
	float[] lightPos;
	float[] lightDiffuseColor;
	float[] lightAmbientColor;

	public LightSetting(boolean flag, float[] lightPos, float[] lightDiffuse, float[] lightAmbient) {
		this.flag = flag;
		this.lightPos = lightPos;
		this.lightDiffuseColor = lightDiffuse;
		this.lightAmbientColor = lightAmbient;
	}

	public void SetLight(GL2 gl) {
		if (flag == true) {
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_LIGHT0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuseColor, 0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbientColor, 0);
		} else {
			gl.glDisable(GL2.GL_LIGHTING);
		}
	}
}
