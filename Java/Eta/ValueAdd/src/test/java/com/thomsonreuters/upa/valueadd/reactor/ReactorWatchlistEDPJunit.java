///*|-----------------------------------------------------------------------------
//*|            This source code is provided under the Apache 2.0 license      --
//*|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
//*|                See the project's LICENSE.md for details.                  --
//*|           Copyright Thomson Reuters 2019. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.upa.valueadd.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.junit.Test;

import com.thomsonreuters.upa.codec.Buffer;
import com.thomsonreuters.upa.codec.Codec;
import com.thomsonreuters.upa.codec.CodecFactory;
import com.thomsonreuters.upa.codec.MsgClasses;
import com.thomsonreuters.upa.rdm.Login;
import com.thomsonreuters.upa.transport.ConnectionTypes;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.dictionary.DictionaryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.directory.DirectoryMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginMsgFactory;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRequest;
import com.thomsonreuters.upa.valueadd.domainrep.rdm.login.LoginRequestFlags;
import com.thomsonreuters.upa.valueadd.reactor.ReactorSubmitOptions;



public class ReactorWatchlistEDPJunit
{
	final int AUTH_TOKEN_EXPIRATION = 300;
	ReactorAuthTokenInfo _tokenInfo = null;	

	abstract class ReactorServiceEndpointEventCallbackTest implements ReactorServiceEndpointEventCallback
	{
		public int _count = 0;
		public ReactorServiceEndpointInfo _endpointInfo = null;  
	}
	/*
	 * Inner class to handle default callbacks. It simply stores the event to be
	 * retrieved later.
	 */
	class ReactorCallbackHandler implements DefaultMsgCallback,
	ReactorChannelEventCallback, RDMLoginMsgCallback,
	RDMDirectoryMsgCallback, RDMDictionaryMsgCallback
	{
		Selector _selector = null;
		ReactorChannelEvent _lastChannelEvent = null;
		ReactorMsgEvent _lastDefaultMsgEvent = null;
		RDMLoginMsgEvent _lastLoginMsgEvent = null;
		RDMDirectoryMsgEvent _lastDirectoryMsgEvent = null;
		RDMDictionaryMsgEvent _lastDictionaryMsgEvent = null;
		int _channelEventCount = 0;
		int _channelUpEventCount = 0;
		int _channelReadyEventCount = 0;
		int _channelDownEventCount = 0;
		int _defaultMsgEventCount = 0;
		int _loginMsgEventCount = 0;
		int _directoryMsgEventCount = 0;
		int _dictionaryMsgEventCount = 0;
		int _channelFDChangeEventCount = 0;
		int _channelOpenedEventCount = 0;

		// These are the return codes that will be returned from the callbacks.
		// they can be overrided to trigger different reactor behaviors.
		int _channelReturnCode = ReactorCallbackReturnCodes.SUCCESS;
		int _msgReturnCode = ReactorCallbackReturnCodes.SUCCESS;

		ReactorCallbackHandler(Selector selector)
		{
			_selector = selector;
		}

		ReactorChannelEvent lastChannelEvent()
		{
			ReactorChannelEvent event = _lastChannelEvent;
			_lastChannelEvent = null;
			return event;
		}

		ReactorMsgEvent lastDefaultMsgEvent()
		{
			ReactorMsgEvent event = _lastDefaultMsgEvent;
			_lastDefaultMsgEvent = null;
			return event;
		}

		RDMLoginMsgEvent lastLoginMsgEvent()
		{
			RDMLoginMsgEvent event = _lastLoginMsgEvent;
			_lastLoginMsgEvent = null;
			return event;
		}

		RDMDirectoryMsgEvent lastDirectoryMsgEvent()
		{
			RDMDirectoryMsgEvent event = _lastDirectoryMsgEvent;
			_lastDirectoryMsgEvent = null;
			return event;
		}

		RDMDictionaryMsgEvent lastDictionaryMsgEvent()
		{
			RDMDictionaryMsgEvent event = _lastDictionaryMsgEvent;
			_lastDictionaryMsgEvent = null;
			return event;
		}

		int channelEventCount()
		{
			return _channelEventCount;
		}

		int channelUpEventCount()
		{
			return _channelUpEventCount;
		}

		int channelReadyEventCount()
		{
			return _channelReadyEventCount;
		}

		int channelDownEventCount()
		{
			return _channelDownEventCount;
		}

		int defaultMsgEventCount()
		{
			return _defaultMsgEventCount;
		}

		int loginMsgEventCount()
		{
			return _loginMsgEventCount;
		}

		int directoryMsgEventCount()
		{
			return _directoryMsgEventCount;
		}

		int dictionaryMsgEventCount()
		{
			return _dictionaryMsgEventCount;
		}

		int channelFDChangeEventCount()
		{
			return _channelFDChangeEventCount;
		}

		int channelOpenedEventCount()
		{
			return _channelOpenedEventCount;
		}

		void msgReturnCode(int retCode)
		{
			_msgReturnCode = retCode;
		}

		void channelReturnCode(int retCode)
		{
			_channelReturnCode = retCode;
		}

		@Override
		public int defaultMsgCallback(ReactorMsgEvent event)
		{
			System.out.println("DEBUG: ReactorCallbackHandler.defaultMsgCallback: entered. defaultMsgEventCount="
					+ ++_defaultMsgEventCount
					+ " event="
					+ event.toString());

			_lastDefaultMsgEvent = new ReactorMsgEvent();
			TestUtil.copyMsgEvent(event, _lastDefaultMsgEvent);

			return ReactorCallbackReturnCodes.SUCCESS;
		}

		@Override
		public int reactorChannelEventCallback(ReactorChannelEvent event)
		{
			System.out.println("DEBUG: ReactorCallbackHandler.reactorChannelEventCallback: entered. channelEventCount="
					+ ++_channelEventCount
					+ " event="
					+ event.toString());

			_lastChannelEvent = event;

			ReactorChannel rc = event.reactorChannel();
			int eventType = event.eventType();

			switch (eventType)
			{
			case ReactorChannelEventTypes.CHANNEL_UP:
				_channelUpEventCount++;
				// register this new reactorChannel for OP_READ with the selector
				try
				{
					rc.channel().selectableChannel().register(_selector, SelectionKey.OP_READ, rc);
				}
				catch (ClosedChannelException e)
				{
					assertTrue("Exception occurred, " + e.getLocalizedMessage(), false);
				}
				break;
			case ReactorChannelEventTypes.CHANNEL_DOWN:
			case ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING:
				_channelDownEventCount++;
				if (rc.selectableChannel() != null)
				{
					SelectionKey key = rc.selectableChannel().keyFor(_selector);
					if (key != null)
						key.cancel();
				}
				break;
			case ReactorChannelEventTypes.CHANNEL_READY:
				_channelReadyEventCount++;
				break;
			case ReactorChannelEventTypes.FD_CHANGE:
				_channelFDChangeEventCount++;
				break;
			case ReactorChannelEventTypes.CHANNEL_OPENED:
				_channelOpenedEventCount++;
				break;
			default:
				assertTrue("This test currently doesn't handle this event", false);
			}

			return _channelReturnCode;
		}

		@Override
		public int rdmLoginMsgCallback(RDMLoginMsgEvent event)
		{
			System.out.println("DEBUG: ReactorCallbackHandler.rdmLoginMsgCallback: entered. loginMsgEventCount="
					+ ++_loginMsgEventCount
					+ " event="
					+ event.toString());

			_lastLoginMsgEvent = new RDMLoginMsgEvent();
			TestUtil.copyMsgEvent(event, _lastLoginMsgEvent);

			if (event.rdmLoginMsg() != null)
			{
				_lastLoginMsgEvent.rdmLoginMsg(LoginMsgFactory.createMsg());
				TestUtil.copyLoginMsg(event.rdmLoginMsg(), _lastLoginMsgEvent.rdmLoginMsg());
			}

			return _msgReturnCode;
		}

		@Override
		public int rdmDirectoryMsgCallback(RDMDirectoryMsgEvent event)
		{
			System.out.println("DEBUG: ReactorCallbackHandler.rdmDirectoryMsgCallback: entered. directoryMsgEventCount="
					+ ++_directoryMsgEventCount
					+ " event="
					+ event.toString());

			_lastDirectoryMsgEvent = new RDMDirectoryMsgEvent();
			TestUtil.copyMsgEvent(event, _lastDirectoryMsgEvent);

			if (event.rdmDirectoryMsg() != null)
			{
				_lastDirectoryMsgEvent.rdmDirectoryMsg(DirectoryMsgFactory.createMsg());
				TestUtil.copyDirectoryMsg(event.rdmDirectoryMsg(), _lastDirectoryMsgEvent.rdmDirectoryMsg());
			}

			return _msgReturnCode;
		}

		@Override
		public int rdmDictionaryMsgCallback(RDMDictionaryMsgEvent event)
		{
			System.out.println("DEBUG: ReactorCallbackHandler.rdmDictionaryMsgCallback: entered. dictionaryMsgEventCount="
					+ ++_dictionaryMsgEventCount
					+ " event="
					+ event.toString());

			_lastDictionaryMsgEvent = new RDMDictionaryMsgEvent();
			TestUtil.copyMsgEvent(event, _lastDictionaryMsgEvent);

			if (event.rdmDictionaryMsg() != null)
			{
				_lastDictionaryMsgEvent.rdmDictionaryMsg(DictionaryMsgFactory.createMsg());
				TestUtil.copyDictionaryMsg(event.rdmDictionaryMsg(), _lastDictionaryMsgEvent.rdmDictionaryMsg());
			}

			return _msgReturnCode;
		}
	}

	boolean checkCredentials()
	{
		if (System.getProperty("edpUserName") != null && 
				System.getProperty("edpPassword") != null && 
				System.getProperty("keyfile") != null && 
				System.getProperty("keypasswd") != null ) 	
		{
			return true;
		}
		else
		{
			System.out.println("edpUserName edpPassword keyfile and keypasswd need to be set as VM arguments to run this test.");
			System.out.println("i.e. -DedpUserName=USERNAME -DedpPassword=PASSWORD -Dkeyfile=*.jks -Dkeypasswd=PASSWORD");
			System.out.println("or with gradle i.e. ./gradlew eta:valueadd:test --tests *EDP* -PvmArgs=\"-DedpUserName=USERNAME -DedpPassword=PASSWORD -Dkeyfile=keystore.jks -Dkeypasswd=PASSWORD\"");
			System.out.println("Skipping this test");			
			return false;
		}
	}

	/* create default consumer connect options */
	static ReactorConnectOptions createDefaultConsumerConnectOptionsForSessionManagment(TestReactorComponent reactor)
	{
		ReactorConnectOptions rcOpts = ReactorFactory.createReactorConnectOptions();
		ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
		assertNotNull(rcOpts);
		assertEquals(ReactorReturnCodes.PARAMETER_OUT_OF_RANGE, connectInfo.initTimeout(0));
		assertEquals(ReactorReturnCodes.SUCCESS, connectInfo.initTimeout(10));
		connectInfo.connectOptions().connectionType(ConnectionTypes.ENCRYPTED);
		connectInfo.connectOptions().majorVersion(Codec.majorVersion());
		connectInfo.connectOptions().minorVersion(Codec.minorVersion());

		connectInfo.connectOptions().tunnelingInfo().KeystoreFile(System.getProperty("keyfile"));
		connectInfo.connectOptions().tunnelingInfo().KeystorePasswd(System.getProperty("keypasswd"));
		connectInfo.connectOptions().tunnelingInfo().objectName("");
		connectInfo.connectOptions().tunnelingInfo().KeystoreType("JKS");
		connectInfo.connectOptions().tunnelingInfo().SecurityProtocol("TLS");
		connectInfo.connectOptions().tunnelingInfo().SecurityProvider("SunJSSE");
		connectInfo.connectOptions().tunnelingInfo().KeyManagerAlgorithm("SunX509");
		connectInfo.connectOptions().tunnelingInfo().TrustManagerAlgorithm("PKIX");		
		connectInfo.connectOptions().tunnelingInfo().tunnelingType("encrypted");			
		connectInfo.connectOptions().userSpecObject(reactor);		

		connectInfo.enableSessionManagement(true);
		rcOpts.connectionList().add(connectInfo);

		rcOpts.reconnectAttemptLimit(5);
		rcOpts.reconnectMinDelay(1000);
		rcOpts.reconnectMaxDelay(1000);
		rcOpts.connectionList().get(0).connectOptions().pingTimeout(255);
		rcOpts.connectionList().get(0).initTimeout(10);		
		return rcOpts;
	}	

	@Test
	public void EDPConnectSpecificLocationTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectSpecificLocationTest <<<<<<<<<<\n");
		/* Test a queryServiceDiscovery */
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		

			ReactorErrorInfo errorInfo = null;		 

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;	  

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);
			rcOpts.connectionList().get(0).location("eu");

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	


			TestReactorEvent event = null;
			ReactorChannelEvent chnlEvent = null;
			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());			


			verifyAuthTokenEvent(consumerReactor, 10, true);

		}
		finally
		{
			consumerReactor.close();
		}		

	}	

	private int verifyAuthTokenEvent(TestReactor consumerReactor, int howLongToWait, boolean success)
	{
		TestReactorEvent event = null;
		int count = 0;
		// Consumer receives Auth Service Token Event
		System.out.print("Waiting for auth event: ");
		while (event == null)
		{
			System.out.print(count + " ");
			event = consumerReactor.pollEvent();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
			if (count > howLongToWait)
				break;
		}
		System.out.println("");		

		if (success)
		{
			assertNotNull("Did not receive AUTH_TOKEN_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.AUTH_TOKEN_EVENT, received: " + event.type(), TestReactorEventTypes.AUTH_TOKEN_EVENT, event.type());
			ReactorAuthTokenEvent authTokenEvent = (ReactorAuthTokenEvent)event.reactorEvent();
			System.out.println(authTokenEvent.reactorAuthTokenInfo());				

			_tokenInfo = authTokenEvent.reactorAuthTokenInfo();
			assertEquals(_tokenInfo.expiresIn(), AUTH_TOKEN_EXPIRATION);   	
			return ((_tokenInfo.expiresIn() / 5) * 4);
		}
		else
		{
			ReactorChannelEvent chnlEvent = null;				
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.WARNING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.WARNING, chnlEvent.eventType());				

			event = consumerReactor.pollEvent();			
			assertNotNull("Did not receive AUTH_TOKEN_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.AUTH_TOKEN_EVENT, received: " + event.type(), TestReactorEventTypes.AUTH_TOKEN_EVENT, event.type());
			ReactorAuthTokenEvent authTokenEvent = (ReactorAuthTokenEvent)event.reactorEvent();
			System.out.println(authTokenEvent.reactorAuthTokenInfo());			

			System.out.println(authTokenEvent.errorInfo().toString());
			assertTrue(authTokenEvent.errorInfo().toString().contains("{\"error\":\"access_denied\"  ,\"error_description\":\"Invalid username or password.\" }"));		
		}
		return -1;
	}


	@Test
	public void EDPConnectErrorInvalidConnectionTypeTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorInvalidConnectionTypeTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);
			// set invalid connection type
			rcOpts.connectionList().get(0).connectOptions().connectionType(ConnectionTypes.SOCKET);

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);

			// fix the connection type and run it again. 
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  
			rcOpts.connectionList().get(0).connectOptions().connectionType(ConnectionTypes.ENCRYPTED);			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);			

			consumerReactor.close();		}
		finally
		{
			consumerReactor.close();
		}	}

	@Test
	public void EDPConnectErrorInvalidLocationTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorInvalidLocationTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);
			rcOpts.connectionList().get(0).location("invalid_location");

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID); 

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  	

			// fix the location and run it again, also needs to reset the username and password, this time rest client already exists.
			consumerRole.rdmLoginRequest().userName().data(System.getProperty("edpUserName"));
			consumerRole.rdmLoginRequest().password().data(System.getProperty("edpPassword"));
			rcOpts.connectionList().get(0).location("eu");

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS); 
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);		

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			consumerRole.rdmLoginRequest().userName().data(System.getProperty("edpUserName"));
			consumerRole.rdmLoginRequest().password().data(System.getProperty("edpPassword"));
			rcOpts.connectionList().get(0).location("invalid_location");

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);			

		}
		finally
		{
			consumerReactor.close();
		}
	}	

	@Test
	public void EDPConnectErrorAddressAndSessionManagmentSpecifiedTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorAddressAndSessionManagmentSpecifiedTest <<<<<<<<<<\n");		
		assumeTrue(checkCredentials());	
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;

			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.SUCCESS );
					assertTrue(event.serviceEndpointInfo().size() > 0);

					for (int i = 0; i < event.serviceEndpointInfo().size(); i++)
					{
						assertTrue(event.serviceEndpointInfo().get(i).transport().equals("tcp"));
						assertTrue(event.serviceEndpointInfo().get(i).dataFormatList().get(0).equals("rwf"));
						if (event.serviceEndpointInfo().get(i).locationList().size() == 2)
						{
							_endpointInfo = event.serviceEndpointInfo().get(i);
						}
					}
					System.out.println(event.serviceEndpointInfo());
					_count++;
					return 0;
				}
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);
			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address("FAKE ADDRESS");
			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName("FAKE SERVICE NAME");			

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);   			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			assertTrue(callback._endpointInfo != null);
			rcOpts.connectionList().get(0).connectOptions()
			.unifiedNetworkInfo().address(callback._endpointInfo.endPoint());			
			rcOpts.connectionList().get(0).connectOptions()
			.unifiedNetworkInfo().serviceName(callback._endpointInfo.port());

			// call it again to test when rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			//call it again to test when rest reactor already created, this time fix invalid parameter
			rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);
		}
		finally
		{
			consumerReactor.close();
		}
	}	

	private void setupConsumer(Consumer consumer, boolean defaultRDMLogin)
	{

		ReactorCallbackHandler callbackHandler = null;
		Selector selector = null;		
		/* Create consumer. */
		callbackHandler = new ReactorCallbackHandler(selector);
		assertEquals(null, callbackHandler.lastChannelEvent());

		ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();
		if (defaultRDMLogin)
		{
			consumerRole.initDefaultRDMLoginRequest();
			consumerRole.initDefaultRDMDirectoryRequest();
		}	
		consumerRole.channelEventCallback(consumer);
		consumerRole.loginMsgCallback(consumer);
		consumerRole.directoryMsgCallback(consumer);
		consumerRole.dictionaryMsgCallback(consumer);
		consumerRole.defaultMsgCallback(consumer);
		consumerRole.watchlistOptions().enableWatchlist(true);
		consumerRole.watchlistOptions().channelOpenCallback(consumer);
		consumerRole.watchlistOptions().requestTimeout(3000);

		if (defaultRDMLogin)
		{
			consumerRole.rdmLoginRequest().userName().data(System.getProperty("edpUserName"));
			consumerRole.rdmLoginRequest().password().data(System.getProperty("edpPassword"));
			consumerRole.rdmLoginRequest().applyHasPassword();

			// initialize consumer role to default	
			consumerRole.rdmLoginRequest().attrib().applyHasSingleOpen();
			consumerRole.rdmLoginRequest().attrib().singleOpen(1);
			consumerRole.rdmLoginRequest().attrib().applyHasAllowSuspectData();
			consumerRole.rdmLoginRequest().attrib().allowSuspectData(1);			
		}
	}

	@Test
	public void EDPConnectTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		

			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorMsgEvent msgEvent;			
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());			

			int sleep = verifyAuthTokenEvent(consumerReactor, 10, true);
			long runtime = System.currentTimeMillis() + ((sleep - 3) * 1000);		

			consumer.testReactor().dispatch(4, 8000);

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_UP, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_UP, chnlEvent.eventType());

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive LOGIN_MSG", event);
			assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive DIRECTORY_MSG", event);
			assertEquals("Expected TestReactorEventTypes.DIRECTORY_MSG, received: " + event.type(), TestReactorEventTypes.DIRECTORY_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());		        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_READY, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_READY, chnlEvent.eventType());	        

			verifyAuthTokenRequestAndLoginReissue(consumerReactor, consumer, 2, sleep, runtime, true, false);

		}
		finally
		{
			consumerReactor.close();
		}	
	}	


	@Test
	public void EDPConnectConnectionListTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectConnectionListTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorMsgEvent msgEvent;			
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */

			// create first entry in the connection list that will fail
			ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
			assertEquals(ReactorReturnCodes.SUCCESS, connectInfo.initTimeout(10));
			connectInfo.connectOptions().connectionType(ConnectionTypes.SOCKET);			
			connectInfo.connectOptions().majorVersion(Codec.majorVersion());
			connectInfo.connectOptions().minorVersion(Codec.minorVersion());
			connectInfo.connectOptions().unifiedNetworkInfo().address("localhost");
			connectInfo.connectOptions().unifiedNetworkInfo().serviceName("14002");
			connectInfo.connectOptions().userSpecObject(consumer);		
			connectInfo.initTimeout(40);
			connectInfo.enableSessionManagement(false);		

			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);

			ReactorConnectInfo connectInfoSecond = rcOpts.connectionList().remove(0);		

			rcOpts.connectionList().add(connectInfo);
			rcOpts.connectionList().add(connectInfoSecond);

			rcOpts.connectionList().get(1).reactorAuthTokenEventCallback(consumer);				
			//		consumerRole.watchlistOptions().requestTimeout(300);		

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			for (int j = 0; j < 5; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}	        

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("localhost"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("14002"));			
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().serviceName() == null);

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());				

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());			

			for (int j = 0; j < 16; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}

			int sleep = verifyAuthTokenEvent(consumerReactor, 10, true);
			long runtime = System.currentTimeMillis() + ((sleep - 5) * 1000);	        

			for (int j = 0; j < 6; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}			

			// Consumer receives CHANNEL_UP event        
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_UP, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_UP, chnlEvent.eventType());			

			for (int j = 0; j < 6; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}				

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive LOGIN_MSG", event);
			assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass()); 

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive DIRECTORY_MSG", event);
			assertEquals("Expected TestReactorEventTypes.DIRECTORY_MSG, received: " + event.type(), TestReactorEventTypes.DIRECTORY_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());		        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_READY, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_READY, chnlEvent.eventType());	        

			verifyAuthTokenRequestAndLoginReissue(consumerReactor, consumer, 2, sleep, runtime, true, false);

		}
		finally
		{
			consumerReactor.close();
		};
	}	

	@Test
	public void EDPConnectConnectionListMaxReconnectAttemptsTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectConnectionListMaxReconnectAttemptsTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		// send correct password to get unlocked.
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			

			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;		
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */

			// create first entry in the connection list that will fail
			ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
			assertEquals(ReactorReturnCodes.SUCCESS, connectInfo.initTimeout(10));
			connectInfo.connectOptions().connectionType(ConnectionTypes.SOCKET);			
			connectInfo.connectOptions().majorVersion(Codec.majorVersion());
			connectInfo.connectOptions().minorVersion(Codec.minorVersion());
			connectInfo.connectOptions().unifiedNetworkInfo().address("localhost");
			connectInfo.connectOptions().unifiedNetworkInfo().serviceName("14002");
			connectInfo.connectOptions().userSpecObject(consumer);		
			connectInfo.initTimeout(40);
			connectInfo.enableSessionManagement(false);		

			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);

			ReactorConnectInfo connectInfoSecond = rcOpts.connectionList().remove(0);
			connectInfoSecond.location("us");
			connectInfoSecond.enableSessionManagement(true);

			rcOpts.connectionList().add(connectInfo);
			rcOpts.connectionList().add(connectInfoSecond);
			ReactorConnectInfo connectInfoThird = ReactorFactory.createReactorConnectInfo();		
			connectInfoSecond.copy(connectInfoThird);
			connectInfoThird.location("eu");		

			rcOpts.connectionList().add(connectInfoThird);

			consumerRole.rdmLoginRequest().password().data("FAKE");		

			rcOpts.connectionList().get(1).reactorAuthTokenEventCallback(consumer);
			rcOpts.connectionList().get(2).reactorAuthTokenEventCallback(consumer);	
			rcOpts.reconnectAttemptLimit(4);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			for (int j = 0; j < 15; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}	        

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("localhost"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("14002"));

			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().serviceName() == null);

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());				

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());		

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.WARNING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.WARNING, chnlEvent.eventType());			

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive AUTH_TOKEN_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.AUTH_TOKEN_EVENT, received: " + event.type(), TestReactorEventTypes.AUTH_TOKEN_EVENT, event.type());

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.WARNING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.WARNING, chnlEvent.eventType());				

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive AUTH_TOKEN_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.AUTH_TOKEN_EVENT, received: " + event.type(), TestReactorEventTypes.AUTH_TOKEN_EVENT, event.type());		

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());			

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());			

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.WARNING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.WARNING, chnlEvent.eventType());				

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive AUTH_TOKEN_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.AUTH_TOKEN_EVENT, received: " + event.type(), TestReactorEventTypes.AUTH_TOKEN_EVENT, event.type());		

			event = consumerReactor.pollEvent();		
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN, chnlEvent.eventType());			

			for (int j = 0; j < 15; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}			

			event = consumerReactor.pollEvent();		
			assertNull("No more event", event);	

		}
		finally
		{
			consumerReactor.close();
		}
	}		


	@Test
	public void EDPConnectConnectionListErrorWrongCredentialsTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectConnectionListErrorWrongCredentialsTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());

		// request service discovery with valid user name / password to prevent being locked out 
		// from EDP Gateway because of too many invalid requests.
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */

			// create first entry in the connection list that will fail
			ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
			connectInfo.connectOptions().connectionType(ConnectionTypes.SOCKET);			
			connectInfo.connectOptions().majorVersion(Codec.majorVersion());
			connectInfo.connectOptions().minorVersion(Codec.minorVersion());
			connectInfo.connectOptions().unifiedNetworkInfo().address("localhost");
			connectInfo.connectOptions().unifiedNetworkInfo().serviceName("14002");
			connectInfo.connectOptions().userSpecObject(consumer);		
			connectInfo.initTimeout(40);
			connectInfo.enableSessionManagement(false);		

			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);

			ReactorConnectInfo connectInfoSecond = rcOpts.connectionList().remove(0);		

			rcOpts.connectionList().add(connectInfo);
			rcOpts.connectionList().add(connectInfoSecond);

			rcOpts.connectionList().get(1).reactorAuthTokenEventCallback(consumer);					
			consumerRole.rdmLoginRequest().password().data("FAKE");

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			for (int j = 0; j < 5; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}	        

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("localhost"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("14002"));			
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().serviceName() == null);

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());				


			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());			

			for (int i = 0; i < 2; i++)
			{
				System.out.println(i);
				verifyAuthTokenEvent(consumerReactor, 30, false);

				for (int j = 0; j < 15; j++)
				{
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					consumer.testReactor().dispatch(-1);
				}			

				// Consumer receives CHANNEL_DOWN_RECONNECTING event
				event = consumerReactor.pollEvent();
				assertNotNull("Did not receive CHANNEL_EVENT", event);
				assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
				chnlEvent = (ReactorChannelEvent)event.reactorEvent();
				assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());	

				// Consumer receives CHANNEL_DOWN_RECONNECTING event
				event = consumerReactor.pollEvent();
				assertNotNull("Did not receive CHANNEL_EVENT", event);
				assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
				chnlEvent = (ReactorChannelEvent)event.reactorEvent();
				assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());				
			}

		}
		finally
		{
			consumerReactor.close();
		}
	}	


	@Test
	public void EDPConnectConnectionListSecondConnectionEDPInvalidCredentialsTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectConnectionListSecondConnectionEDPInvalidCredentialsTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());

		// request service discovery with valid user name / password to prevent being locked out 
		// from EDP Gateway because of too many invalid requests.
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			consumerRole.rdmLoginRequest().password().data("FAKE");			

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */

			// create first entry in the connection list that will fail
			ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
			assertEquals(ReactorReturnCodes.PARAMETER_OUT_OF_RANGE, connectInfo.initTimeout(0));
			assertEquals(ReactorReturnCodes.SUCCESS, connectInfo.initTimeout(10));
			connectInfo.connectOptions().connectionType(ConnectionTypes.SOCKET);			
			connectInfo.connectOptions().majorVersion(Codec.majorVersion());
			connectInfo.connectOptions().minorVersion(Codec.minorVersion());
			connectInfo.connectOptions().unifiedNetworkInfo().address("localhost");
			connectInfo.connectOptions().unifiedNetworkInfo().serviceName("14002");
			connectInfo.connectOptions().userSpecObject(consumer);		
			connectInfo.initTimeout(40);
			connectInfo.enableSessionManagement(false);		

			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);

			ReactorConnectInfo connectInfoSecond = rcOpts.connectionList().remove(0);		

			rcOpts.connectionList().add(connectInfo);
			rcOpts.connectionList().add(connectInfoSecond);

			rcOpts.connectionList().get(1).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			for (int j = 0; j < 5; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				consumer.testReactor().dispatch(-1);
			}	        

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("localhost"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("14002"));			
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().serviceName() == null);

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());				


			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());		

			for (int i = 0; i < 2; i++)
			{	
				System.out.println("index " + i);
				verifyAuthTokenEvent(consumerReactor, 30, false);

				for (int j = 0; j < 5; j++)
				{
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					consumer.testReactor().dispatch(-1);
				}			

				// Consumer receives CHANNEL_DOWN_RECONNECTING event for 1st connection
				event = consumerReactor.pollEvent();
				assertNotNull("Did not receive CHANNEL_EVENT", event);
				assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
				chnlEvent = (ReactorChannelEvent)event.reactorEvent();
				assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());	

				// Consumer receives CHANNEL_DOWN_RECONNECTING event for 2 connection
				event = consumerReactor.pollEvent();
				assertNotNull("Did not receive CHANNEL_EVENT", event);
				assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
				chnlEvent = (ReactorChannelEvent)event.reactorEvent();
				assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());				

			}

		}
		finally
		{

			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		

			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPConnectWatchlistDisabledLoginReIssueSendWithRefreshFlagTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledLoginReIssueSendWithRefreshFlagTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		

			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorMsgEvent msgEvent;			
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);				

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);		

			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());			


			int sleep = verifyAuthTokenEvent(consumerReactor, 10, true);
			long runtime = System.currentTimeMillis() + ((sleep - 3) * 1000);

			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			consumer.testReactor().dispatch(4, 8000);	        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_UP, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_UP, chnlEvent.eventType());

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive LOGIN_MSG", event);
			assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive DIRECTORY_MSG", event);
			assertEquals("Expected TestReactorEventTypes.DIRECTORY_MSG, received: " + event.type(), TestReactorEventTypes.DIRECTORY_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());		        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_READY, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_READY, chnlEvent.eventType());	        

			verifyAuthTokenRequestAndLoginReissue(consumerReactor, consumer, 2, sleep, runtime, true, true);

		}
		finally
		{
			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPConnectWatchlistDisabledAddressAndPortEmptyTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledAddressAndPortEmptyTest <<<<<<<<<<\n");			
		/* Test a to see if address and service name will be overwritten by service discovery when user sets them to 
		 * empty strings prior to connection */
		assumeTrue(checkCredentials());

		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;	

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);
			consumerRole.watchlistOptions().enableWatchlist(false);			

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address("");
			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName("");			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals(""));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals(""));	

		}
		finally
		{
			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPConnectWatchlistDisabledAddressAndPortSetTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledAddressAndPortSetTest <<<<<<<<<<\n");			
		/* Test a to see if address and service name will NOT be overwritten by service discovery when user sets them to 
		 * some string */
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {


			ReactorErrorInfo errorInfo = null;	

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);
			consumerRole.watchlistOptions().enableWatchlist(false);			

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address("FAKE");
			rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName("FAKE");			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			// check if the connection info came from EDP
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("FAKE"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("FAKE"));

		}
		finally
		{
			consumerReactor.close();
		}	
	}	

	@Test
	public void EDPConnectWatchlistDisabledTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorMsgEvent msgEvent;			
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);
			consumerRole.watchlistOptions().enableWatchlist(false);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);	

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	

			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int sleep = verifyAuthTokenEvent(consumerReactor, 10, true);	
			long runtime = System.currentTimeMillis() + ((sleep - 3) * 1000);

			consumer.testReactor().dispatch(4, 8000);		

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_UP, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_UP, chnlEvent.eventType());

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive LOGIN_MSG", event);
			assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive DIRECTORY_MSG", event);
			assertEquals("Expected TestReactorEventTypes.DIRECTORY_MSG, received: " + event.type(), TestReactorEventTypes.DIRECTORY_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());		        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_READY, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_READY, chnlEvent.eventType());	        

			verifyAuthTokenRequestAndLoginReissue(consumerReactor, consumer, 2, sleep, runtime, false, false);

		}
		finally
		{
			consumerReactor.close();
		}

	}

	@Test
	public void EDPConnectWatchlistDisabledConnectionListTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledConnectionListTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();	
		
		TestReactor consumerReactor = null;
		try {		
	

			ReactorErrorInfo errorInfo = null;		 

			TestReactorEvent event;
			ReactorMsgEvent msgEvent;			
			ReactorChannelEvent chnlEvent;			

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, true);
			consumerRole.watchlistOptions().enableWatchlist(false);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			// create first entry in the connection list that will fail
			ReactorConnectInfo connectInfo = ReactorFactory.createReactorConnectInfo();
			assertEquals(ReactorReturnCodes.SUCCESS, connectInfo.initTimeout(10));
			connectInfo.connectOptions().connectionType(ConnectionTypes.SOCKET);			
			connectInfo.connectOptions().majorVersion(Codec.majorVersion());
			connectInfo.connectOptions().minorVersion(Codec.minorVersion());
			connectInfo.connectOptions().unifiedNetworkInfo().address("localhost");
			connectInfo.connectOptions().unifiedNetworkInfo().serviceName("14002");
			connectInfo.connectOptions().userSpecObject(consumer);		
			connectInfo.initTimeout(40);
			connectInfo.enableSessionManagement(false);		

			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);

			ReactorConnectInfo connectInfoSecond = rcOpts.connectionList().remove(0);		

			rcOpts.connectionList().add(connectInfo);
			rcOpts.connectionList().add(connectInfoSecond);

			rcOpts.connectionList().get(1).reactorAuthTokenEventCallback(consumer);				

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);

			consumer.testReactor().dispatch(-1, 8000);			

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address().equals("localhost"));
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName().equals("14002"));			
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(1).connectOptions().unifiedNetworkInfo().serviceName() == null);

			// Consumer receives CHANNEL_DOWN_RECONNECTING event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_DOWN_RECONNECTING, chnlEvent.eventType());

			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		

			int sleep = verifyAuthTokenEvent(consumerReactor, 10, true);
			long runtime = System.currentTimeMillis() + ((sleep - 5) * 1000);		        


			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_UP, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_UP, chnlEvent.eventType());	        


			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			


			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive LOGIN_MSG", event);
			assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive DIRECTORY_MSG", event);
			assertEquals("Expected TestReactorEventTypes.DIRECTORY_MSG, received: " + event.type(), TestReactorEventTypes.DIRECTORY_MSG, event.type());
			msgEvent = (ReactorMsgEvent)event.reactorEvent();
			assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());		        

			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_READY, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_READY, chnlEvent.eventType());

			verifyAuthTokenRequestAndLoginReissue(consumerReactor, consumer, 2, sleep, runtime, false, false);

		}
		finally
		{
			consumerReactor.close();
		};		
	}	

	@Test
	public void EDPConnectWatchlistDisabledErrorNoRDMLoginTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectWatchlistDisabledErrorNoRDMLoginTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		

			/* Create reactor. */
			//TestReactor.enableReactorXmlTracing();	        
			consumerReactor = new TestReactor();

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();	

			setupConsumer(consumer, false);
			consumerRole.watchlistOptions().enableWatchlist(false);			

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			// since no RDMLoginRequest there is no user name or password, authentication will fail
			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.INVALID_USAGE);

		}
		finally
		{
			consumerReactor.close();
		};		
	}		

	private void verifyAuthTokenRequestAndLoginReissue(TestReactor consumerReactor, Consumer consumer, 
			int numberOfSequences, int sleep, long runtime, boolean loginReissue, boolean refreshFlag)
	{
		for (int i = 0; i < numberOfSequences; i++)
		{
			System.out.println("Sequence number: " + i);
			System.out.print("SLEEP: ");
			// Wait for the token management event
			// to keep the connection alive needs to dispatch every second
			// to send the ping, can't time the 
			for (int j = 0; j < sleep; j++)
			{
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (System.currentTimeMillis() > runtime)
					break;
				System.out.print(" " + j);
				consumer.testReactor().dispatch(-1);
			}

			System.out.println("");
			runtime += ((sleep - 3)* 1000);

			if (loginReissue)
			{
				verifyAuthTokenEvent(consumerReactor, 1000, true);				
			}
			else // no watchlist, so we need to send the login reissue on behalf of the consumer
			{
				//				verifyAuthTokenEvent(consumerReactor, 10, true);				

				ReactorErrorInfo errorInfo = ReactorFactory.createReactorErrorInfo();
				assertNotNull(errorInfo);

				// submit login request and make sure it succeeds
				ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();
				LoginRequest loginRequest = consumerRole.rdmLoginRequest();
				loginRequest.userNameType(Login.UserIdTypes.AUTHN_TOKEN);
				loginRequest.userName().data(_tokenInfo.accessToken());
				if (!refreshFlag)
					loginRequest.applyNoRefresh();
				assertNotNull(loginRequest);

				ReactorSubmitOptions submitOptions = ReactorFactory.createReactorSubmitOptions();
				submitOptions.clear();
				loginRequest.flags(loginRequest.flags() & ~LoginRequestFlags.HAS_PASSWORD);
				loginRequest.applyNoRefresh();
				assertEquals(ReactorReturnCodes.SUCCESS, consumer.reactorChannel().submit(loginRequest, submitOptions, errorInfo));	

				if (refreshFlag)
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					TestReactorEvent event;
					ReactorMsgEvent msgEvent;	            	
					event = consumerReactor.pollEvent();
					assertEquals("Expected TestReactorEventTypes.LOGIN_MSG, received: " + event.type(), TestReactorEventTypes.LOGIN_MSG, event.type());
					msgEvent = (ReactorMsgEvent)event.reactorEvent();
					assertEquals(MsgClasses.REFRESH, msgEvent.msg().msgClass());  	            	
				}
			}
		}
	}

	@Test
	public void EDPConnectUserSpecifiedClientIdTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectUserSpecifiedClientIdTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			consumerRole.clientId().data(System.getProperty("edpUserName"));

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);

			TestReactorEvent event = null;
			ReactorChannelEvent chnlEvent;		
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());

			verifyAuthTokenEvent(consumerReactor, 10, true);

		}
		finally
		{
			consumerReactor.close();
		}	
	}	

	@Test
	public void EDPConnectErrorInvalidClientIdTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorInvalidClientIdTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;	

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			consumerRole.clientId().data("FAKE");

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.FAILURE);		
			// unauthorized
			assertTrue(errorInfo.toString().contains("{\"error\":\"invalid_client\"  ,\"error_description\":\"clientId field is invalid.\" }"));

			// run it again with correct credentials.
			consumerRole.clientId().data(System.getProperty("edpUserName"));

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);			

			TestReactorEvent event = null;
			ReactorChannelEvent chnlEvent = null;		
			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());			

			verifyAuthTokenEvent(consumerReactor, 10, true);

		}
		finally
		{
			consumerReactor.close();
		}
	}		

	@Test
	public void EDPConnectErrorIncorrectCredentialsTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorIncorrectCredentialsTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());

		// request service discovery with valid user name / password to prevent being locked out 
		// from EDP Gateway because of too many invalid requests.
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		
		

			ReactorErrorInfo errorInfo = null;		

			/* Create reactor. */
			consumerReactor = new TestReactor();
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			consumerRole.rdmLoginRequest().userName().data("FAKE");
			consumerRole.rdmLoginRequest().password().data("FAKE");

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.FAILURE);
			// unauthorized
			assertTrue(errorInfo.toString().contains("{\"error\":\"invalid_client\"  ,\"error_description\":\"clientId field is invalid.\" }"));
			System.out.println(errorInfo);

			// run it again with correct credentials.
			consumerRole.rdmLoginRequest().userName().data(System.getProperty("edpUserName"));
			consumerRole.rdmLoginRequest().password().data(System.getProperty("edpPassword"));

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);			

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);

			TestReactorEvent event = null;
			ReactorChannelEvent chnlEvent = null;
			// Consumer receives CHANNEL_OPENED event
			event = consumerReactor.pollEvent();
			assertNotNull("Did not receive CHANNEL_EVENT", event);
			assertEquals("Expected TestReactorEventTypes.CHANNEL_EVENT, received: " + event.type(), TestReactorEventTypes.CHANNEL_EVENT, event.type());
			chnlEvent = (ReactorChannelEvent)event.reactorEvent();
			assertEquals("Expected ReactorChannelEventTypes.CHANNEL_OPENED, received: " + chnlEvent.eventType(), ReactorChannelEventTypes.CHANNEL_OPENED, chnlEvent.eventType());			

			verifyAuthTokenEvent(consumerReactor, 10, true);	

		}
		finally
		{
			consumerReactor.close();
		}		
	}		 

	@Test
	public void EDPConnectErrorInvalidTokenServiceURLTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorInvalidTokenServiceURLTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;

			ReactorOptions reactorOptions = ReactorFactory.createReactorOptions();

			Buffer url = CodecFactory.createBuffer();
			url.data("INVALID_URL");
			reactorOptions.tokenServiceURL(url);

			/* Create reactor. */
			consumerReactor = new TestReactor(reactorOptions);
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;	  

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.FAILURE);	

		}
		finally
		{
			consumerReactor.close();
		}
	}		

	@Test
	public void EDPConnectUserSpecifiedTokenServiceUrlTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectUserSpecifiedTokenServiceUrlTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;

			ReactorOptions reactorOptions = ReactorFactory.createReactorOptions();

			Buffer url = CodecFactory.createBuffer();
			url.data("https://api.refinitiv.com/auth/oauth2/beta1/token");
			reactorOptions.tokenServiceURL(url);

			/* Create reactor. */
			consumerReactor = new TestReactor(reactorOptions);
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;	  

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);			

		}
		finally
		{
			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPConnectUserSpecifiedServiceDiscoveryUrlTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectUserSpecifiedServiceDiscoveryUrlTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();		
		TestReactor consumerReactor = null;
		try {


			ReactorErrorInfo errorInfo = null;

			ReactorOptions reactorOptions = ReactorFactory.createReactorOptions();

			Buffer url = CodecFactory.createBuffer();
			url.data("https://api.refinitiv.com/streaming/pricing/v1");
			reactorOptions.serviceDiscoveryURL(url);

			/* Create reactor. */
			consumerReactor = new TestReactor(reactorOptions);
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;	  

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);			

		}
		finally
		{
			consumerReactor.close();
		};
	}	

	@Test
	public void EDPConnectErrorInvalidServiceDiscoveryURLTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPConnectErrorInvalidServiceDiscoveryURLTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {


			ReactorErrorInfo errorInfo = null;

			ReactorOptions reactorOptions = ReactorFactory.createReactorOptions();

			Buffer url = CodecFactory.createBuffer();
			url.data("INVALID_URL");
			reactorOptions.serviceDiscoveryURL(url);

			/* Create reactor. */
			consumerReactor = new TestReactor(reactorOptions);
			ReactorCallbackHandler callbackHandler = null;
			Selector selector = null;	  

			/* Create consumer. */
			callbackHandler = new ReactorCallbackHandler(selector);
			assertEquals(null, callbackHandler.lastChannelEvent());

			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);

			assertTrue(consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.FAILURE);	

		}
		finally
		{
			consumerReactor.close();
		}	
	}			

	@Test
	public void EDPQueryServiceDiscoveryErrorInvalidDataFormatAndTransportTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorInvalidDataFormatAndTransportTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {


			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(11111);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.PARAMETER_OUT_OF_RANGE );
					System.out.println(event.serviceEndpointInfo());					
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();


			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_OUT_OF_RANGE);
			// verify that callback was NOT called
			assertTrue("callback was called", callback._count == 0);
			assertTrue(errorInfo.code() == ReactorReturnCodes.PARAMETER_OUT_OF_RANGE);

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(23232);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  	     

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_OUT_OF_RANGE);
			// verify that callback was NOT called
			assertTrue("callback was called", callback._count == 0);	
			assertTrue(errorInfo.code() == ReactorReturnCodes.PARAMETER_OUT_OF_RANGE);			
		}
		finally
		{
			consumerReactor.close();
		}
	}		

	@Test
	public void EDPQueryServiceDiscoveryErrorCallbackMissingTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorCallbackMissingTest <<<<<<<<<<\n");			
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);		    

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);			    

		}
		finally
		{
			consumerReactor.close();
		}	
	}


	@Test
	public void EDPQueryServiceDiscoveryErrorErrorInfoMissingTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorErrorInfoMissingTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);			    

		}
		finally
		{
			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPQueryServiceDiscoveryErrorServiceDiscoveryOptionsMissingTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorServiceDiscoveryOptionsMissingTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {		


			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = null;

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue(consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.PARAMETER_INVALID);	    

		}
		finally
		{
			consumerReactor.close();
		}
	}

	@Test
	public void EDPQueryServiceDiscoveryErrorMismatchOfTypesTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorMismatchOfTypesTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_JSON2);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					System.out.println(event.serviceEndpointInfo());
					System.out.println(event.errorInfo());					
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.FAILURE );
					// since there is a mismatch of the dataformat and protocol the EDP will respond with 404 error Not found.
					assertTrue(event.errorInfo().toString().contains("404"));
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);

		}
		finally
		{
			consumerReactor.close();
		}
	}	

	@Test
	public void EDPQueryServiceDiscoveryJSONTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryJSONTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {


			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_WEBSOCKET);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_JSON2);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.SUCCESS );
					assertTrue(event.serviceEndpointInfo().size() > 0);

					for (int i = 0; i < event.serviceEndpointInfo().size(); i++)
					{
						assertTrue(event.serviceEndpointInfo().get(i).transport().equals("websocket"));
						assertTrue(event.serviceEndpointInfo().get(i).dataFormatList().get(0).equals("tr_json2"));
					}
					System.out.println(event.serviceEndpointInfo());					
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);			

		}
		finally
		{
			consumerReactor.close();
		}	
	}

	@Test
	public void EDPQueryServiceDiscoveryErrorPasswordTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorPasswordTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {
	

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("WRONG_USER_NAME");
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("WRONG_PASSWORD");
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.FAILURE );
					// since user name and password is incorrect we should see the error message
					assertTrue(event.errorInfo().toString().contains("{\"error\":\"invalid_client\"  ,\"error_description\":\"clientId field is invalid.\" }"));
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);

		}
		finally
		{
			consumerReactor.close();
		}	
	}

	@Test
	public void EDPQueryServiceDiscoveryErrorUserNamePasswordAndSuccessConnectTest()
	{
		
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorPasswordAndSuccessConnectTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {
		

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("WRONG USER NAME");
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("WRONG PASSWORD");
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.FAILURE );
					// since password is incorrect we should see 400 code
					assertTrue(event.errorInfo().toString().contains("{\"error\":\"access_denied\"  ,\"error_description\":\"Authentication Failed.\" }"));
					System.out.println(event.serviceEndpointInfo());
					System.out.println(event.errorInfo());
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);		

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	

		}
		finally
		{
			consumerReactor.close();
		}	
	}	

	@Test
	public void EDPQueryServiceDiscoveryErrorPasswordAndSuccessConnectTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorPasswordAndSuccessConnectTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("WRONG PASSWORD");
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.FAILURE );
					// since password is incorrect we should see access_denied message
					assertTrue(event.errorInfo().toString().contains("{\"error\":\"access_denied\"  ,\"error_description\":\"Invalid username or password.\" }"));
					System.out.println(event.serviceEndpointInfo());
					System.out.println(event.errorInfo());
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);		

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	

		}
		finally
		{
			consumerReactor.close();
		}
	}	


	@Test
	public void EDPQueryServiceDiscoveryErrorInvalidClientIdAndSuccessConnectTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryErrorInvalidClientIdAndSuccessConnectTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data("INVALID_CLIENT_ID");
				reactorServiceDiscoveryOptions.clientId(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}		

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.FAILURE );
					// since client id is incorrect we should see invalid client error message
					assertTrue(event.errorInfo().toString().contains("{\"error\":\"invalid_client\"  ,\"error_description\":\"clientId field is invalid.\" }"));
					System.out.println(event.serviceEndpointInfo());
					System.out.println(event.errorInfo());						
					_count++;
					return 0;
				}
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);
			ConsumerRole consumerRole = (ConsumerRole)consumer.reactorRole();

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that the callback was called once
			assertTrue(callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that the callback was called twice
			assertTrue(callback._count == 2);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			/*
			 * create a Client Connection.
			 */
			ReactorConnectOptions rcOpts = createDefaultConsumerConnectOptionsForSessionManagment(consumer);
			rcOpts.connectionList().get(0).reactorAuthTokenEventCallback(consumer);					

			assertTrue("Expected SUCCESS", consumerReactor._reactor.connect(rcOpts, consumerRole, errorInfo) == ReactorReturnCodes.SUCCESS);
			assertTrue(consumerReactor._countAuthTokenEventCallbackCalls == 1);

			// check that user specified connection info was not overwritten
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().address() == null);
			assertTrue(rcOpts.connectionList().get(0).connectOptions().unifiedNetworkInfo().serviceName() == null);	

		}
		finally
		{
			consumerReactor.close();
		}	
	}

	@Test
	public void EDPQueryServiceDiscoveryNoDataFormatProtocolTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryNoDataFormatProtocolTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.SUCCESS );
					assertTrue(event.serviceEndpointInfo().size() > 0);

					for (int i = 0; i < event.serviceEndpointInfo().size(); i++)
					{
						assertTrue(event.serviceEndpointInfo().get(i).transport().equals("tcp"));
						assertTrue(event.serviceEndpointInfo().get(i).dataFormatList().get(0).equals("rwf"));
					}
					System.out.println(event.serviceEndpointInfo());				
					_count++;
					return 0;
				}
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("queryServiceDiscovery did not return SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("queryServiceDiscovery did not return SUCCESS",consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 2);			

		}
		finally
		{
			consumerReactor.close();
		}		
	}	

	@Test
	public void EDPQueryServiceDiscoveryNoTransportProtocolTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryNoTransportProtocolTest <<<<<<<<<<\n");
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {

			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().code() == ReactorReturnCodes.SUCCESS );
					assertTrue(event.serviceEndpointInfo().size() > 0);

					for (int i = 0; i < event.serviceEndpointInfo().size(); i++)
					{
						assertTrue(event.serviceEndpointInfo().get(i).transport().equals("tcp"));
						assertTrue(event.serviceEndpointInfo().get(i).dataFormatList().get(0).equals("rwf"));
					}
					System.out.println(event.serviceEndpointInfo());			
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);			

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that callback was called twice
			assertTrue("callback was not called", callback._count == 2);

		}
		finally
		{
			consumerReactor.close();
		}	
	}

	private void unlockAccount()
	{
		TestReactor consumerReactor = null;
		try {
			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);		

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();   

			int i = 0;
			System.out.println("UNLOCKING ACCOUNT");
			int ret = consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo);
			
			while ( ret != ReactorReturnCodes.SUCCESS || errorInfo.code() != 0)
			{
				System.out.println("================= UNLOCK ACCOUNT: " + (i++) + " ================== \n" + errorInfo.error().toString());	
				errorInfo = ReactorFactory.createReactorErrorInfo();

				try {
					Thread.sleep(1000 * i);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ret = consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo);
			}
			System.out.println("================= ACCOUNT UNLOCKED " + i + " ================== \n" + errorInfo.error().toString());				
		}
		finally
		{
			consumerReactor.close();
		}
	}

	@Test
	public void EDPQueryServiceDiscoveryTest()
	{
		System.out.println("\n>>>>>>>>> Running EDPQueryServiceDiscoveryTest <<<<<<<<<<\n");	
		assumeTrue(checkCredentials());
		unlockAccount();
		
		TestReactor consumerReactor = null;
		try {
			ReactorErrorInfo errorInfo = null;		 
			ReactorServiceDiscoveryOptions reactorServiceDiscoveryOptions = ReactorFactory.createReactorServiceDiscoveryOptions();
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpUserName"));
				reactorServiceDiscoveryOptions.userName(buf);
			}
			{
				Buffer buf = CodecFactory.createBuffer();
				buf.data(System.getProperty("edpPassword"));
				reactorServiceDiscoveryOptions.password(buf);
			}  	

			reactorServiceDiscoveryOptions.transport(ReactorDiscoveryTransportProtocol.RD_TP_TCP);
			reactorServiceDiscoveryOptions.dataFormat(ReactorDiscoveryDataFormatProtocol.RD_DP_RWF);

			ReactorServiceEndpointEventCallbackTest callback = new ReactorServiceEndpointEventCallbackTest()
			{
				@Override
				public int reactorServiceEndpointEventCallback(ReactorServiceEndpointEvent event) {
					assertTrue(event.errorInfo().error().text(), event.errorInfo().code() == ReactorReturnCodes.SUCCESS );
					assertTrue(event.serviceEndpointInfo().size() > 0);

					for (int i = 0; i < event.serviceEndpointInfo().size(); i++)
					{
						assertTrue(event.serviceEndpointInfo().get(i).transport().equals("tcp"));
						assertTrue(event.serviceEndpointInfo().get(i).dataFormatList().get(0).equals("rwf"));
					}
					System.out.println(event.serviceEndpointInfo());				
					_count++;
					return 0;
				}     				
			};

			reactorServiceDiscoveryOptions.reactorServiceEndpointEventCallback(callback);

			/* Create reactor. */
			consumerReactor = new TestReactor();

			/* Create consumer. */
			Consumer consumer = new Consumer(consumerReactor);

			setupConsumer(consumer, true);

			errorInfo = ReactorFactory.createReactorErrorInfo();
			errorInfo.clear();
			assertNotNull(errorInfo);     

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 1);

			// do it second time to test with rest reactor already created
			errorInfo = ReactorFactory.createReactorErrorInfo();
			assertNotNull(errorInfo);  

			assertTrue("Expected SUCCESS", consumerReactor._reactor.queryServiceDiscovery(reactorServiceDiscoveryOptions, errorInfo) == ReactorReturnCodes.SUCCESS);    
			// verify that callback was called once
			assertTrue("callback was not called", callback._count == 2);

		}
		finally
		{
			consumerReactor.close();
		}		

	}	
}
