package au.com.codeka.warworlds.server.net

import au.com.codeka.warworlds.common.Log
import au.com.codeka.warworlds.common.debug.PacketDebug
import au.com.codeka.warworlds.common.net.PacketDecoder
import au.com.codeka.warworlds.common.net.PacketEncoder
import au.com.codeka.warworlds.common.proto.Account
import au.com.codeka.warworlds.common.proto.Empire
import au.com.codeka.warworlds.common.proto.HelloPacket
import au.com.codeka.warworlds.common.proto.Packet
import au.com.codeka.warworlds.server.concurrency.TaskRunner
import au.com.codeka.warworlds.server.concurrency.Threads
import au.com.codeka.warworlds.server.world.Player
import au.com.codeka.warworlds.server.world.WatchableObject
import java.io.IOException
import java.io.OutputStream
import java.net.Socket

/**
 * Represents an established connection to a client.
 */
class Connection internal constructor(
    private val manager: ServerSocketManager,
    helloPacket: HelloPacket,
    private val account: Account,
    private val empire: WatchableObject<Empire>,
    private val encryptionKey: ByteArray?,
    private val socket: Socket,
    private val decoder: PacketDecoder,
    outs: OutputStream) : PacketDecoder.PacketHandler {
  private val encoder: PacketEncoder = PacketEncoder(outs)
  private val player: Player = Player(this, helloPacket, empire)

  init {
    decoder.setPacketHandler(this)
  }

  fun start() {
    encoder.setPacketHandler(packetEncodeHandler)
  }

  fun send(pkt: Packet) {
    try {
      encoder.send(pkt)
    } catch (e: IOException) {
      log.warning("Error", e)
    }
  }

  override fun onPacket(decoder: PacketDecoder, packet: Packet, encodedSize: Int) {
    if (log.isDebugEnabled) {
      log.debug("<< [%d %s] %s", empire.get().id, empire.get().display_name,
          PacketDebug.getPacketDebug(packet, encodedSize))
    }
    TaskRunner.i.runTask(Runnable { player.onPacket(packet) }, Threads.BACKGROUND)
  }

  override fun onDisconnect() {
    TaskRunner.i.runTask(Runnable { player.onDisconnect() }, Threads.BACKGROUND)
    manager.onDisconnect(empire.get().id)
  }

  private val packetEncodeHandler = object : PacketEncoder.PacketHandler {
    override fun onPacket(packet: Packet, encodedSize: Int) {
      if (log.isDebugEnabled) {
        log.debug(">> [%d %s] %s", empire.get().id, empire.get().display_name,
            PacketDebug.getPacketDebug(packet, encodedSize))
      }
    }
  }

  companion object {
    private val log = Log("Connection")
  }
}