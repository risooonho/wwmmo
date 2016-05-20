package au.com.codeka.warworlds.client.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;

import au.com.codeka.warworlds.client.App;
import au.com.codeka.warworlds.client.concurrency.Threads;
import au.com.codeka.warworlds.common.Log;

/** Represents a texture image. */
public class BitmapTexture extends Texture {
  private static final Log log = new Log("TextureBitmap");

  @Nullable private Loader loader;

  private BitmapTexture(@NonNull Loader loader) {
    this.loader = loader;
    this.loader.load();
  }

  @Override
  public void bind() {
    if (loader != null) {
      if (loader.isLoaded()) {
        setTextureId(loader.createGlTexture());
        loader = null;
      }
    }

    super.bind();
  }

  @Nullable
  public static BitmapTexture load(Context context, String fileName) {
    return new BitmapTexture(new Loader(context, fileName));
  }

  /**
   * Handles loading a texture into a {@link BitmapTexture}.
   */
  private static class Loader {
    private Context context;
    private String fileName;
    private Bitmap bitmap;

    public Loader(Context context, String fileName) {
      this.context = Preconditions.checkNotNull(context);
      this.fileName = Preconditions.checkNotNull(fileName);
    }

    public void load() {
      App.i.getTaskRunner().runTask(new Runnable() {
        @Override
        public void run() {
          try {
            InputStream ins = context.getAssets().open(fileName);
            bitmap = BitmapFactory.decodeStream(ins);
          } catch (IOException e) {
            log.warning("Error loading texture '%s'", fileName, e);
          }
        }
      }, Threads.BACKGROUND);
    }

    boolean isLoaded() {
      return bitmap != null;
    }

    public int createGlTexture() {
      Preconditions.checkState(bitmap != null);

      final int[] textureHandleBuffer = new int[1];
      GLES20.glGenTextures(1, textureHandleBuffer, 0);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandleBuffer[0]);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
      return textureHandleBuffer[0];
    }
  }
}