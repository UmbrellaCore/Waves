package com.wavesplatform.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}
import scorex.block.Block
import scorex.network.message.{Message => ScorexMessage}
import scorex.transaction.Transaction
import scorex.utils.ScorexLogging

@Sharable
class TrafficLogger(settings: TrafficLogger.Settings) extends ChannelDuplexHandler with ScorexLogging {

  import BasicMessagesRepo.specsByClasses

  override def write(ctx: ChannelHandlerContext, msg: AnyRef, promise: ChannelPromise): Unit = {
    if (!shouldIgnore(msg)) log.trace(s"${id(ctx)} <-- $msg")
    super.write(ctx, msg, promise)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    if (!shouldIgnore(msg)) log.trace(s"${id(ctx)} --> $msg")
    super.channelRead(ctx, msg)
  }

  private def shouldIgnore(msg: AnyRef): Boolean = {
    import settings.ignoreMessages

    msg match {
      case x: RawBytes => ignoreMessages(x.code)
      case _: Transaction => ignoreMessages(TransactionMessageSpec.messageCode)
      case _: BigInt | _: LocalScoreChanged => ignoreMessages(ScoreMessageSpec.messageCode)
      case _: Block | _: BlockForged => ignoreMessages(BlockMessageSpec.messageCode)
      case x: Message => ignoreMessages(specsByClasses(x.getClass).messageCode)
      // case _: Handshake => ignoreMessages(HandshakeMessageSpec.messageCode)
      case x => true
    }
  }

}

object TrafficLogger {

  case class Settings(enable: Boolean, ignoreMessages: Set[ScorexMessage.MessageCode])

}