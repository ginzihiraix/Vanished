package vanished.GUI;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL2;

public class RenderObject {
	public int numIndex = 0;
	public FloatBuffer vertexBuffer;
	public FloatBuffer normBuffer;
	public FloatBuffer colorBuffer;
	public FloatBuffer coordBuffer;
	public ShortBuffer indexBuffer;
	public int textureID;
	public int blendSrc;
	public int blendDst;

	public RenderObject(int numIndex, int textureID, int blendSrc, int blendDst, float[] vertex, float[] norm, float[] coord, float[] color,
			short[] index) {
		this.numIndex = numIndex;
		this.textureID = textureID;
		this.blendSrc = blendSrc;
		this.blendDst = blendDst;

		ByteBuffer vertexBB = ByteBuffer.allocateDirect(vertex.length * 4);
		vertexBB.order(ByteOrder.nativeOrder());
		vertexBuffer = vertexBB.asFloatBuffer();
		vertexBuffer.put(vertex);
		vertexBuffer.position(0);

		ByteBuffer normBB = ByteBuffer.allocateDirect(norm.length * 4);
		normBB.order(ByteOrder.nativeOrder());
		normBuffer = normBB.asFloatBuffer();
		normBuffer.put(norm);
		normBuffer.position(0);

		ByteBuffer coordBB = ByteBuffer.allocateDirect(coord.length * 4);
		coordBB.order(ByteOrder.nativeOrder());
		coordBuffer = coordBB.asFloatBuffer();
		coordBuffer.put(coord);
		coordBuffer.position(0);

		ByteBuffer colorBB = ByteBuffer.allocateDirect(color.length * 4);
		colorBB.order(ByteOrder.nativeOrder());
		colorBuffer = colorBB.asFloatBuffer();
		colorBuffer.put(color);
		colorBuffer.position(0);

		ByteBuffer indexBB = ByteBuffer.allocateDirect(index.length * 2);
		indexBB.order(ByteOrder.nativeOrder());
		indexBuffer = indexBB.asShortBuffer();
		indexBuffer.put(index);
		indexBuffer.position(0);
	}

	public void Draw(GL2 gl, LightSetting lightSetting) {
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL2.GL_FLOAT, 0, normBuffer);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, colorBuffer);
		gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, coordBuffer);

		if (textureID == 0) {
			gl.glDisable(GL2.GL_TEXTURE_2D);
		} else {
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);
		}

		if (blendDst == -1 || blendSrc == -1) {
			lightSetting.SetLight(gl);
			gl.glDisable(GL2.GL_BLEND);
		} else {
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(blendSrc, blendDst);
		}

		gl.glDrawElements(GL2.GL_TRIANGLES, numIndex, GL2.GL_UNSIGNED_SHORT, indexBuffer);

		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	}
}
