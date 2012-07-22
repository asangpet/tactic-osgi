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
package org.apache.qpid.qmf2.tools;

// JMS Imports
import javax.jms.Connection;

// Misc Imports
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

// QMF2 Imports
import org.apache.qpid.qmf2.common.ObjectId;
import org.apache.qpid.qmf2.common.QmfEvent;
import org.apache.qpid.qmf2.common.QmfEventListener;
import org.apache.qpid.qmf2.common.QmfException;
import org.apache.qpid.qmf2.common.WorkItem;
import org.apache.qpid.qmf2.console.Agent;
import org.apache.qpid.qmf2.console.Console;
import org.apache.qpid.qmf2.console.EventReceivedWorkItem;
import org.apache.qpid.qmf2.console.QmfConsoleData;
import org.apache.qpid.qmf2.util.ConnectionHelper;
import static org.apache.qpid.qmf2.common.WorkItem.WorkItemType.*;

/**
 * ConnectionLogger is a QMF2 class used to provide information about connections made to a broker. The information
 * provided is very similar to qpid-config - b queues but with additional connection related information.
 *
 * @author Fraser Adams
 */
public final class ConnectionLogger implements QmfEventListener
{
    private Console _console;

    /**
     * Basic constructor. Creates JMS Session, Initialises Destinations, Producers & Consumers and starts connection.
     * @param url the connection URL.
     */
    public ConnectionLogger(final String url)
    {
        try
        {
            Connection connection = ConnectionHelper.createConnection(url, "{reconnect: true}");        
            _console = new Console(this);
            _console.addConnection(connection);
            logSubscriptionInformation("Initialisation");
        }
        catch (QmfException qmfe)
        {
            System.err.println ("QmfException " + qmfe.getMessage() + " caught in ConnectionLogger constructor");
        }
    }

    /**
     * Finds a QmfConsoleData in a List of QMF Objects that matches a given ObjectID
     *
     * More or less a direct Java port of findById from qpid-config
     *
     * @param items the List of QMF Objects to search
     * @param id the ObjectId we're searching the List for
     * @return return the found object as a QmfConsoleData else return null
     */
    private QmfConsoleData findById(final List<QmfConsoleData> items, final ObjectId id)
    {
        for (QmfConsoleData item : items)
        {
            if (item.getObjectId().equals(id))
            {
                return item;
            }
        }

        return null;
    }

    /**
     * For every queue list the bindings (equivalent of qpid-config -b queues)
     *
     * More or less a direct Java port of QueueListRecurse in qpid-config, which handles qpid-config -b queues
     *
     * @param ref If ref is null list info about all queues else list info about queue referenced by ObjectID
     */
    private void logQueueInformation(final ObjectId ref)
    {
        List<QmfConsoleData> queues = _console.getObjects("org.apache.qpid.broker", "queue");
        List<QmfConsoleData> bindings = _console.getObjects("org.apache.qpid.broker", "binding");
        List<QmfConsoleData> exchanges = _console.getObjects("org.apache.qpid.broker", "exchange");

        for (QmfConsoleData queue : queues)
        {
//queue.listValues();
            ObjectId queueId = queue.getObjectId();

            if (ref == null || ref.equals(queueId))
            {
                System.out.printf("Queue '%s'\n", queue.getStringValue("name"));

System.out.println("arguments " + (Map)queue.getValue("arguments"));

                for (QmfConsoleData binding : bindings)
                {
                    ObjectId queueRef = binding.getRefValue("queueRef");

                    if (queueRef.equals(queueId))
                    {
                        ObjectId exchangeRef = binding.getRefValue("exchangeRef");
                        QmfConsoleData exchange = findById(exchanges, exchangeRef);
//exchange.listValues();
                        String exchangeName = "<unknown>";
                        if (exchange != null)
                        {
                            exchangeName = exchange.getStringValue("name");
                            if (exchangeName.equals(""))
                            {
                                exchangeName = "''";
                            }
                        }

                        String bindingKey = binding.getStringValue("bindingKey");
                        Map arguments = (Map)binding.getValue("arguments");
                        if (arguments.isEmpty())
                        {
                            System.out.printf("    bind [%s] => %s\n", bindingKey, exchangeName);
                        }
                        else
                        {
                            // If there are binding arguments then it's a headers exchange
                            System.out.printf("    bind [%s] => %s %s\n", bindingKey, exchangeName, arguments);
                        }
                    }
                }
            }
        }
    }

    /**
     * Logs audit information about each connection made to the broker
     *
     * Obtains connection, session and subscription objects and iterates in turn through these comparing
     * references to find the subscriptions association with sessions and sessions associated with
     * connections. Ultimately it then uses logQueueInformation to display the queues associated with
     * each subscription.
     *
     * @param event the Event that triggered this log, used for display purposes
     */
    private void logSubscriptionInformation(final String event)
    {
        System.out.println("\n\n**** " + event + " event occurred logging subscription information ****");

        List<QmfConsoleData> connections = _console.getObjects("org.apache.qpid.broker", "connection");
        List<QmfConsoleData> sessions = _console.getObjects("org.apache.qpid.broker", "session");
        List<QmfConsoleData> subscriptions = _console.getObjects("org.apache.qpid.broker", "subscription");

        for (QmfConsoleData connection : connections)
        {
//connection.listValues();
            ObjectId connectionId = connection.getObjectId();
            System.out.printf("\nConnection '%s'\n", connection.getStringValue("address"));

            String authIdentity = connection.getStringValue("authIdentity");
            System.out.println("authIdentity: " + authIdentity);

            String remoteProcessName = connection.getStringValue("remoteProcessName");
            System.out.println("remoteProcessName: " + remoteProcessName);

            System.out.println("createTimestamp: " + new Date(connection.getCreateTime()/1000000l));

            for (QmfConsoleData session : sessions)
            {
//session.listValues();
                ObjectId connectionRef = session.getRefValue("connectionRef");
                if (connectionRef.equals(connectionId))
                {
                    ObjectId sessionId = session.getObjectId();
                    for (QmfConsoleData subscription : subscriptions)
                    {
//subscription.listValues();
                        ObjectId sessionRef = subscription.getRefValue("sessionRef");
                        if (sessionRef.equals(sessionId))
                        {
                            ObjectId queueRef = subscription.getRefValue("queueRef");
                            logQueueInformation(queueRef);
                        }
                    }
                }
            }
        }
    }

    /**
     * Listener for QMF2 WorkItems
     *
     * If the Event type is subscribe, clientConnect or clientDisconnect an audit log is made of all
     * connections made to the broker.
     *
     * @param wi a QMF2 WorkItem object
     */
    public void onEvent(final WorkItem wi)
    {
        //System.out.println("WorkItem type: " + wi.getType());

        if (wi instanceof EventReceivedWorkItem)
        {
            EventReceivedWorkItem item = (EventReceivedWorkItem)wi;
            Agent agent = item.getAgent();
            QmfEvent event = item.getEvent();

            String className = event.getSchemaClassId().getClassName();
            System.out.println("Event: " + className);
//event.listValues();    

            if (className.equals("subscribe") ||
                className.equals("clientConnect") ||
                className.equals("clientDisconnect"))
            {
                logSubscriptionInformation(className);
            }
        }
    }

    /**
     * Runs ConnectionLogger.
     * @param args the command line arguments.
     */
    public static void main(final String[] args)
    {
        //System.out.println ("Setting log level to FATAL");
        System.setProperty("amqj.logging.level", "FATAL");

        String url = (args.length == 1) ? args[0] : "localhost";
        ConnectionLogger auditor = new ConnectionLogger(url);

        BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));
        try
        { // Blocks here until return is pressed
            System.out.println("Hit Return to exit");
            String s = commandLine.readLine();
        }
        catch (IOException e)
        {
            System.out.println ("ConnectionLogger main(): IOException: " + e.getMessage());
        }
    }
}
