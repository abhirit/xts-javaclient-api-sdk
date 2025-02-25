package com.sf.xts.api.sdk.interactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import com.sf.xts.api.sdk.main.api.InteractiveClient;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

/**
 * It provides socket handling like connect join, disconnect, error, position, order and trade
 * 
 * @author SymphonyFintech
 */
public class SocketHandler implements XTSAPIInteractiveEvents{
	private List<XTSAPIInteractiveEvents> listeners = new ArrayList<XTSAPIInteractiveEvents>();
	public static Logger logger = LoggerFactory.getLogger(SocketHandler.class);
	/**
	 * provide XTSAPIInteractiveEvents object
	 * @param xtsapiInteractiveEvents XTSAPIInteractiveEvents
	 */
	public void addListner(XTSAPIInteractiveEvents xtsapiInteractiveEvents) {
		listeners.add(xtsapiInteractiveEvents);
	}

	/**
	 * it connect socket handler
	 * @param url String
	 * @param user String
	 * @param authToken String
	 */
	public SocketHandler(String url,String user,String authToken) {
		try {

			//@TODO: Check inputs and throw exceptions 
			Socket socket = null;
			String[] transportArray={"websocket"};
			
			String queryString = "token="+authToken+"&userID="+user+"&apiType=INTERACTIVE";
			
			IO.Options options = new IO.Options();
			//make this configurable variable/property
			options.path="/socket.io";
			options.query = queryString;
			options.transports=transportArray;
			options.reconnection=true;
			options.forceNew=true;
			options.timeout=40000;
			System.out.println("queryString==="+queryString);
			System.out.println("url==="+url);
			socket = IO.socket(url, options);
			socket.on("connect", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.info("on Connect "+args[0]);
					}
					else {
						logger.info("on Connect");
					}
				}
			});
			
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.info("handshake "+args[0]);
					}
					else {
						logger.info("handshake");
					}

				}
			});

			socket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.info("connecting event "+args[0]);
					}
					else {
						logger.info("connecting event");
					}

				}
			});

			socket.on("position", new Emitter.Listener() {
				@Override
				public void call(Object... args) {

					logger.info("**position_Report**");
//					Gson gson = new Gson();
//					System.out.println("position_Report : "+args[0].toString());
//					PositionResponse response = gson.fromJson(args[0].toString(), PositionResponse.class);
//					onPosition(response);
				}
			});

			socket.on("trade", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.info("**got traded**");
					Gson gson = new Gson();
					OrderExecutionResponse response = gson.fromJson(args[0].toString(), OrderExecutionResponse.class);
//					 System.out.println("got Traded:"+args[0].toString());
					onTrade(response);
				}
			});
			
			socket.on("tradeConversion", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.info("**got tradeConversion**");
					Gson gson = new Gson();
					TradeConversionResponse response = gson.fromJson(args[0].toString(), TradeConversionResponse.class);
//					System.out.println("trade conversion:"+args[0].toString());
					onTradeConversion(response);
				}
			});
			
			socket.on("order", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.info("**order placed**");
					Gson gson = new Gson();
					OrderBookResponse response = gson.fromJson(args[0].toString(), OrderBookResponse.class);
//					System.out.println("order placed:"+args[0].toString());
					onExecutionReport(response);
					// System.out.println("  "+response.getOrderStatus());
				}
			});

			socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.error("closed!!!!!!!!!!"+ args[0]);
					startAlert();
				}
			});
			
			/*// Adding authentication headers when encountering EVENT_TRANSPORT
			socket.on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
			    @Override
			    public void call(Object... args) {
			        Transport transport = (Transport) args[0];
			        // Adding headers when EVENT_REQUEST_HEADERS is called
			        transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
			            @Override
			            public void call(Object... args) {
			                System.out.println("Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers");
			                Map<String, List<String>> mHeaders = (Map<String, List<String>>)args[0];
			                mHeaders.put("Authorization", Arrays.asList("Basic bXl1c2VyOm15cGFzczEyMw=="));
			            }
			        });
			    }
			});*/

			socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.error("error!!!!!!!!!!"+args[0]);
					startAlert();
				}
			});

			socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					logger.info("Message!!!!!!!!!!"+args[0]);

				}
			});
			socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Connect Error!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Connect Error!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Connect Timeout!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Connect Timeout!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Reconnect!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Reconnect!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Reconnect Error!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Reconnect Error!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Reconnect failed!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Reconnect failed!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.error("Reconnect attempt!!!!!!!!!! "+args[0]);
					}
					else {
						logger.error("Reconnect attempt!!!!!!!!!!");
					}
					startAlert();

				}
			});
			socket.on(Socket.EVENT_PING, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.info("ping!!!!!!!!!! "+args[0]);
					}
					else {
						logger.info("ping!!!!!!!!!!");
					}

				}
			});
			socket.on(Socket.EVENT_PONG, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					if (args != null && args.length > 0) {
						logger.info("pong!!!!!!!!!! "+args[0]);
					}
					else {
						logger.info("pong!!!!!!!!!!");
					}

				}
			});


			socket.connect();
			Thread.sleep(2000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.symphonyfintech.xts.ally.Interactive.XTSAPIInteractiveEvents#onTrade(com.symphonyfintech.xts.ally.Interactive.OrderExecutionResponse)
	 */
	@Override
	public void onTrade(OrderExecutionResponse obj) {
		for (XTSAPIInteractiveEvents hl : listeners)
			hl.onTrade(obj);
	}

	/* (non-Javadoc)
	 * @see com.symphonyfintech.xts.ally.Interactive.XTSAPIInteractiveEvents#onExecutionReport(com.symphonyfintech.xts.ally.Interactive.OrderBookResponse)
	 */
	@Override
	public void onExecutionReport(OrderBookResponse obj) {
		for (XTSAPIInteractiveEvents hl : listeners)
			hl.onExecutionReport(obj);
	}

	/* (non-Javadoc)
	 * @see com.symphonyfintech.xts.ally.Interactive.XTSAPIInteractiveEvents#onPosition(com.symphonyfintech.xts.ally.Interactive.PositionResponse)
	 */
	@Override
	public void onPosition(PositionResponse obj) {
		for (XTSAPIInteractiveEvents hl : listeners)
			hl.onPosition(obj);
	}
	
	@Override
	public void onTradeConversion(TradeConversionResponse obj) {
		for (XTSAPIInteractiveEvents hl : listeners)
			hl.onTradeConversion(obj);
	}

	@Override
	public void startAlert() {
		for (XTSAPIInteractiveEvents hl : listeners)
			hl.startAlert();
	}


}
