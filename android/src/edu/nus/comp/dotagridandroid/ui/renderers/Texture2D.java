package edu.nus.comp.dotagridandroid.ui.renderers;

import edu.nus.comp.dotagridandroid.Closeable;
import android.graphics.*;
import android.opengl.*;
import static android.opengl.GLES20.*;

public class Texture2D implements Closeable {
	private int width, height;
	private int textureHandler;
	private boolean managed = false;
	public Texture2D(Bitmap image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		int[] t = new int[1];
		glGenTextures(1, t, 0);
		textureHandler = t[0];
		glBindTexture(GL_TEXTURE_2D, textureHandler);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, image, 0);
		glGenerateMipmap(GL_TEXTURE_2D);
	}
	public Texture2D(int textureHandler, int width, int height) {
		this.textureHandler = textureHandler;
		this.width = width;
		this.height = height;
		this.managed = true;
	}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getTexture() {return textureHandler;}
	@Override
	public void close() {
		if (managed)
			return;
		glDeleteTextures(1, new int[]{textureHandler}, 0);
	}
}
