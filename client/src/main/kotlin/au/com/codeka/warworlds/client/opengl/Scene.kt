package au.com.codeka.warworlds.client.opengl

/**
 * A [Scene] is basically a collection of [SceneObject]s that we'll be rendering.
 */
class Scene(val dimensionResolver: DimensionResolver, val textureManager: TextureManager) {
  /** Gets the root [SceneObject] that you should add all your sprites and stuff to.  */
  val rootObject = SceneObject(dimensionResolver, "ROOT", scene = this)
  val spriteShader = SpriteShader()
  private val textTexture = TextTexture()

  // All modifications to the scene (adding children, modifying children, etc) should happen while
  // synchronized on this lock.
  val lock = Any()

  fun createSprite(tmpl: SpriteTemplate, debugName: String): Sprite {
    return Sprite(dimensionResolver, debugName, tmpl)
  }

  fun createText(text: String): TextSceneObject {
    return TextSceneObject(dimensionResolver, spriteShader, textTexture, text)
  }

  fun draw(camera: Camera) {
    rootObject.draw(camera.viewProjMatrix)
  }
}
