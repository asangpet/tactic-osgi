package ak.tactic.qmf;

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

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;

import org.apache.qpid.qmf2.common.QmfException;
import org.apache.qpid.qmf2.common.QmfQuery;
import org.apache.qpid.qmf2.common.QmfQueryTarget;
import org.apache.qpid.qmf2.common.SchemaClass;
import org.apache.qpid.qmf2.common.SchemaClassId;
import org.apache.qpid.qmf2.common.SchemaMethod;
import org.apache.qpid.qmf2.common.SchemaObjectClass;
import org.apache.qpid.qmf2.console.Agent;
import org.apache.qpid.qmf2.console.Console;
import org.apache.qpid.qmf2.console.QmfConsoleData;
import org.apache.qpid.qmf2.util.ConnectionHelper;

public class Test {
	private Console _console;

	public Test(String url) throws Exception {
		try {
			System.out
					.println("*** Starting Test1 synchronous Agent discovery ***");
			Connection connection = ConnectionHelper.createConnection(url,
					"{reconnect: true}");
			_console = new Console();
			_console.addConnection(connection);

			Thread.sleep(3000);

			/*
			for (Agent agent : agents) {
				agent.listValues();
			}
			*/
			
			/*
			System.out
					.println("*** Test1 testing _console.getAgent(\"broker\"): ***");
			Agent agent = _console.getAgent("broker");
			agent.listValues();

			System.out
					.println("*** Test1 testing _console.findAgent(\"broker\"): ***");
			agent = _console.findAgent("broker");
			if (agent == null) {
				System.out
						.println("*** Test1 _console.findAgent(\"broker\") returned null : Test1 failed ***");
				System.exit(1);
			} else {
				agent.listValues();
			}

			System.out
					.println("*** Test1 testing _console.findAgent(\"monkey\"): ***");
			agent = _console.findAgent("monkey");
			if (agent == null) {
				System.out
						.println("*** Test1 _console.findAgent(\"monkey\") correctly returned null ***");
			} else {
				agent.listValues();
			}*/

			System.out
					.println("*** Test1 testing _console.getObjects(\"broker\"): ***");
            // Declare the child class
            QmfQuery query;
            List<QmfConsoleData> results;
            query = new QmfQuery(QmfQueryTarget.OBJECT, new SchemaClassId("org.libvirt", "Domain"));
            
            List<Agent> agents = _console.getAgents();
            List<Agent> libvirtAgents = new ArrayList<Agent>();
            for (Agent a : agents) {
            	if (a.getProduct().equals("libvirt-qmf")) {
            		libvirtAgents.add(a);
            	}
            }
            
            for (Agent libvirtAgent : libvirtAgents) {
            	System.out.println(libvirtAgent.getName());
                List<QmfConsoleData> data = _console.getObjects("org.libvirt", "Domain");
                for (QmfConsoleData item : data) {
                	System.out.println(item.getStringValue("name")+":"+item.getStringValue("state"));
                }
                System.out.println("----Listed domains------ "+data.size());
            	
            }
            
            Agent libvirtAgent = _console.getAgent("libvirt-qmf");
            List<String> packages = libvirtAgent.getPackages();
            for (String pkgName : packages) {
            	System.out.println(pkgName);
            }
            for (SchemaClassId scId : libvirtAgent.getClasses()) {
            	List<SchemaClass> sclass = libvirtAgent.getSchema(scId);
            	SchemaClass schemaObj = sclass.get(0);
            	if (schemaObj instanceof SchemaObjectClass) {
            		SchemaObjectClass obj = (SchemaObjectClass) schemaObj;
            		List<SchemaMethod> methods = obj.getMethods();
            		for (SchemaMethod m: methods) {
            			System.out.println("p:"+scId.getPackageName()+" c:"+scId.getClassName()+" m:"+m.getName());
            		}
            	}
            	//System.out.println(sclass.get(0));
            }
		} catch (QmfException qmfe) {
			System.err.println("QmfException " + qmfe.getMessage()
					+ " caught: Test1 failed");
		}
	}
	
    public List<QmfConsoleData> evaluateDataQuery(QmfQuery query)
    {
        List<QmfConsoleData> results = new ArrayList<QmfConsoleData>();
        return results;
    }

	public static void main(String[] args) throws Exception {
		//url = ConnectionHelper.createConnectionURL("localhost");
		//Test hello = new Test("amqp://guest:guest@/test?brokerlist='tcp://10.1.1.2:49000?retries='10''");
		Test hello = new Test("10.1.1.2:49000");
	}
}
