/**
 * Copyright 2014 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowforwarding.warp.protocol.adapter

import scala.util.Try

import org.flowforwarding.warp.controller.session.{OFSessionHandler, MessageDriverFactory, OFMessage, MessageDriver}

import org.flowforwarding.warp.protocol.ofmessages.{OFMessageRef, IOFMessageProviderFactory, IOFMessageProvider}
import org.flowforwarding.warp.protocol.ofmessages.OFMessageHello.OFMessageHelloRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageSwitchConfigRequest.OFMessageSwitchConfigRequestRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageSwitchFeaturesRequest.OFMessageSwitchFeaturesRequestRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageEchoReply.OFMessageEchoReplyRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageEchoRequest.OFMessageEchoRequestRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageFlowMod.OFMessageFlowModRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageGroupMod.OFMessageGroupModRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessagePacketIn.OFMessagePacketInRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageSwitchConfig.OFMessageSwitchConfigRef
import org.flowforwarding.warp.protocol.ofmessages.OFMessageError.OFMessageErrorRef


case class JDriverMessage(ref: OFMessageRef[_]) extends OFMessage

case class IOFMessageProviderAdapter(provider: IOFMessageProvider) extends MessageDriver[JDriverMessage]{
  provider.init()

  def decodeMessage(in: Array[Byte]): Try[JDriverMessage] = Try {
    val res = if (provider.isHello(in))
                provider.parseHelloMessage(in)
              else if (provider.isPacketIn(in))
                provider.parsePacketIn(in)
              else if(provider.isConfig(in))
                provider.parseSwitchConfig(in)
              else if (provider.isError(in))
                provider.parseError(in)
              //else if (provider.isEchoRequest(in))
              //  provider.parseEchoRequest(in)
              //else if (provider.isSwitchFeatures(in))
              //  provider.parseSwitchFeatures(in)
              else throw new RuntimeException("Unrecognized message")
    JDriverMessage(res)
  }

  def encodeMessage(msg: JDriverMessage): Try[Array[Byte]] = Try {
    msg.ref match{
      case m: OFMessageHelloRef                 => provider.encodeHelloMessage()
      case m: OFMessageSwitchConfigRequestRef   => provider.encodeSwitchConfigRequest()
      case m: OFMessageSwitchFeaturesRequestRef => provider.encodeSwitchFeaturesRequest()
      case m: OFMessageEchoReplyRef             => provider.encodeEchoReply()
      case m: OFMessageEchoRequestRef           => provider.encodeEchoRequest()
      case m: OFMessageFlowModRef               => provider.encodeFlowMod(m)
      case m: OFMessageGroupModRef              => provider.encodeGroupMod(m)
      case m => throw new RuntimeException("Unable to encode such kind of message: " + m)
    }
  }

  def getDPID(in: Array[Byte]): Try[Long] = Try(provider.getDPID(in).longValue())

  val versionCode: Short = provider.getVersion
}

case class IOFMessageProviderFactoryAdapter(factory: IOFMessageProviderFactory) extends MessageDriverFactory[JDriverMessage]{
  def get(versionCode: Short): Option[MessageDriver[JDriverMessage]] =
    Try(factory.getMessageProvider(versionCode)).map(IOFMessageProviderAdapter.apply).toOption
}

abstract class OFJDriverSessionHandler(pFactory: IOFMessageProviderFactory) extends OFSessionHandler(IOFMessageProviderFactoryAdapter(pFactory)){

  private val providers = scala.collection.mutable.Map[Short, IOFMessageProvider]()

  override def connected(versionCode: Short) {
    if(!providers.contains(versionCode))
      providers(versionCode) = pFactory.getMessageProvider(versionCode)
  }

  implicit def refsToMessages(refs: Seq[OFMessageRef[_]]) = refs map JDriverMessage.apply

  protected def getHandshakeMessage(version: Short, msg: JDriverMessage): Seq[JDriverMessage] = {
    refsToMessages(Seq(OFMessageHelloRef.create, OFMessageSwitchFeaturesRequestRef.create))
  }

  protected def onReceivedMessage(version: Short, dpid: Long, msg: JDriverMessage): Seq[JDriverMessage] = {
    msg.ref match{
      case p: OFMessagePacketInRef     => packetIn(providers(version), dpid, p)
      case c: OFMessageSwitchConfigRef => switchConfig(providers(version), dpid, c)
      case e: OFMessageErrorRef        => error(providers(version), dpid, e)
    }
  }

  def packetIn(provider: IOFMessageProvider, dpid: Long, pIn: OFMessagePacketInRef): Seq[OFMessageRef[_]]
  def switchConfig(provider: IOFMessageProvider, dpid: Long, config: OFMessageSwitchConfigRef): Seq[OFMessageRef[_]]
  def error(provider: IOFMessageProvider, dpid: Long, error: OFMessageErrorRef): Seq[OFMessageRef[_]]
}