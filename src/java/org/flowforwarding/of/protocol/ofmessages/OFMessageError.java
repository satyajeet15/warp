/**
 * © 2013 FlowForwarding.Org
 * All Rights Reserved.  Use is subject to license terms.
 */
package org.flowforwarding.of.protocol.ofmessages;

import org.flowforwarding.of.protocol.ofmessages.OFMessagePacketIn.OFMessagePacketInHandler;

/**
 * @author Infoblox Inc.
 *
 */
public class OFMessageError extends OFMessage{
   
   protected Short type;
   protected Short code;
   
   protected OFMessageError () {
      type = code = (short) 65535;
   }
   
   public Short getType() {
      return type;
   }
   public void setType(Short t) {
      type = t;
   }
   public Short getCode() {
      return code;
   }
   public void setCode(Short c) {
      code = c;
   }
   
   public static class OFMessageErrorHandler extends OFMessageHandler<OFMessageError> {
      
      protected OFMessageErrorHandler () {
         message = new OFMessageError();
      }
      
      public static OFMessageErrorHandler create() {
         return new OFMessageErrorHandler();
      }

      // TODO Improvs: rewrite with commands classes
      public Short getCode() {
         return message.getCode();
      }
      public Short getType() {
         return message.getType();
      }
      public void setCode(Short c) {
         message.setCode(c);
      }
      public void setType(Short t) {
         message.setType(t);
      }
   }
}