/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.qmf2.common;

// JMS Imports
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.Session;

// Misc Imports
import java.util.Enumeration;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Need the following to decode and encode amqp/list messages
import java.nio.ByteBuffer;
import org.apache.qpid.transport.codec.BBDecoder;
import org.apache.qpid.transport.codec.BBEncoder;

// QMF2 Imports
import org.apache.qpid.qmf2.common.QmfData;

/**
 * Provides static helper methods for encoding and decoding "amqp/list" and "amqp/map" ContentTypes.
 *<p> 
 * Unfortunately the encoding of amqp/map and amqp/list messages is not as useful as it might be in the
 * Qpid JMS runtime. amqp/list messages don't <i>actually</i> have a useful encoding in Qpid JMS, so we have to
 * fake it in this class by encoding/decoding java.util.List objects into a JMS BytesMessage and setting
 * the ContentType to "amqp/list".
 *<p>
 * Whilst amqp/map messages are encoded as JMS MapMessage this isn't necessarily the most useful format as
 * MapMessage does not conform to the java.util.Map interface. As QMF methods returning lists return lists
 * of java.util.Map there's a bit of an inconsistency of type that getMap() resolves.
 * 
 * @author Fraser Adams
 */
public final class AMQPMessage
{
    /**
     * Make constructor private at this class provides a set of static helper methods and doesn't need instantiated.
     */
    private AMQPMessage()
    {
    }

    /** 
     * Builds a java.util.Map from a JMS MapMessage.
     * This is really a helper method to make code more homogenous as QmfData objects are constructed from Maps
     * but JMS returns MapMessages which don't share a common interface. This method enumerates MapMessage
     * Properties and Objects and stores them in a java.util.Map.
     *
     * @param message a JMS Message
     * @return a java.util.Map containing the the properties extracted from the Message.
     * <p>
     * Note that this method copies the Message properties <b>and</b> the properties from the MapMessage Map.
     * <p>
     * This method also attempts to populate "_user_id" using the JMSXUserID property, however that's not as
     * easy as it sounds!! There's a bug in AMQMessageDelegate_0_10.getStringProperty() whereby if the property
     * is "JMSXUserID" it returns "new String(_messageProps.getUserId());" however if the client uses anonymous
     * authentication _messageProps.getUserId() returns null. In order to get around this this class unfortunately
     * has to delve inside "org.apache.qpid.client.message.AbstractJMSMessage".
     */
    public static Map<String, Object> getMap(final Message message) throws JMSException
    {
        if (message == null)
        {
            throw new MessageFormatException("Attempting to do AMQPMessage.getMap() on null Message");
        }
        else if (message instanceof MapMessage)
        {
            Map<String, Object> object = new HashMap<String, Object>();
            MapMessage msg = (MapMessage)message;
            for (Enumeration e = msg.getMapNames() ; e.hasMoreElements() ;)
            {
                String key = (String)e.nextElement();
                object.put(key, msg.getObject(key));
            }
            
            if (object.size() == 0)
            { // If there is no MapMessage content return an empty Map.
                return object;
            }

            // If there is MapMessage content include the Message properties in the returned Map.
            for (Enumeration e = msg.getPropertyNames() ; e.hasMoreElements() ;)
            {
                String prop = (String)e.nextElement();
                object.put(prop, QmfData.getString(msg.getObjectProperty(prop)));
            }

            // Should be msg.getStringProperty("JMSXUserID"). See comments above for the reason behind this evil hack.
            org.apache.qpid.client.message.AMQMessageDelegate_0_10 delegate = (org.apache.qpid.client.message.AMQMessageDelegate_0_10)(((org.apache.qpid.client.message.AbstractJMSMessage)msg).getDelegate());
            byte[] rawUserId = delegate.getMessageProperties().getUserId();
            if (rawUserId != null)
            {
                String userId = new String(rawUserId);
                object.put("_user_id", userId);
            }

            return object;
        }
        else
        {
            return null;
        }
    }

    /**
     * JMS QMF returns amqp/list types as a BytesMessage this method decodes that into a java.util.List
     * <p>
     * Taken from Gordon Sim's initial JMS QMF Example using the BBDecoder
     * <p>
     * Trivia: This block of code from Gordon Sim is the seed that spawned the whole of this Java QMF2 API
     * implementation - cheers Gordon.
     *
     * @param message amqp/list encoded JMS Message
     * @return a java.util.List decoded from Message
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(final Message message) throws JMSException
    {
        if (message == null)
        {
            throw new MessageFormatException("Attempting to do AMQPMessage.getList() on null Message");
        }
        else if (message instanceof BytesMessage)
        {
            BytesMessage msg = (BytesMessage)message;

            //only handles responses up to 2^31-1 bytes long
            byte[] data = new byte[(int) msg.getBodyLength()];
            msg.readBytes(data);
            BBDecoder decoder = new BBDecoder();
            decoder.init(ByteBuffer.wrap(data));
            return (List<T>)decoder.readList();
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates an amqp/list encoded Message out of a BytesMessage.
     * <p>
     * This is somewhat of a dirty hack that needs to be monitored as qpid versions change.
     * <p>
     * Unfortunately there's no "clean" way to encode or decode amqp/list messages via the JMS API.
     * <p>
     * Unfortunately even more hackery has to take place to set the content-type as no pure JMS API
     * property currently gets mapped to content-type, so we have to cast to AbstractJMSMessage.
     *
     * @param session used to create the JMS Message
     * @return an amqp/list encoded JMS Message
     */
    public static Message createListMessage(final Session session) throws JMSException
    {
        BytesMessage message = session.createBytesMessage();
        ((org.apache.qpid.client.message.AbstractJMSMessage)message).setContentType("amqp/list");
        return message;
    }

    /**
     * Encodes a java.util.List on an amqp/list encoded BytesMessage.
     * <p>
     * This is somewhat of a dirty hack that needs to be monitored as qpid versions change.
     * <p>
     * Unfortunately there's no "clean" way to encode or decode amqp/list messages via the JMS API.
     * <p>
     * This method uses the org.apache.qpid.transport.codec.BBEncoder writeList() method to encode
     * a List into a ByteBuffer then writes the bytes from the buffer into a JMS BytesMessage.
     *
     * @param message amqp/list encoded JMS BytesMessage
     * @param list to encode into JMS Message
     */
    @SuppressWarnings("unchecked")
    public static void setList(final Message message, final List list) throws JMSException
    {
        String type = ((org.apache.qpid.client.message.AbstractJMSMessage)message).getContentType();
        if (!type.equals("amqp/list"))
        {
            throw new MessageFormatException("Can only do setList() on amqp/list encoded Message");
        }

        if (message == null)
        {
            throw new MessageFormatException("Attempting to do AMQPMessage.setList() on null Message");
        }
        else if (message instanceof BytesMessage)
        {
            BBEncoder encoder = new BBEncoder(1024);
            encoder.writeList(list);
            ByteBuffer buf = encoder.segment();
            byte[] data = new byte[buf.limit()];
            buf.get(data);
            ((BytesMessage)message).writeBytes(data);
        }
        else
        {
            throw new MessageFormatException("Attempting to do setList() on " + message.getClass().getCanonicalName());
        }
    }
}



