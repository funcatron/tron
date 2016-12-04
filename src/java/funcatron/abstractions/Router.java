package funcatron.abstractions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;



/**
 * The thing that routes a message
 */
public interface Router extends Function<MessageBroker.ReceivedMessage, Void> {

    static Logger logger = LoggerFactory.getLogger(Router.class);


    interface Message {
        /**
         * The reply-to header
         * @return the reply-to header
         */
        default String replyTo()  {
            return (String)  metadata().get("reply-to");
        }

        /**
         * The host header
         * @return the host header
         */
        default String host() {
            return (String)  metadata().get("x-host");
        }

        /**
         * Get the name of the reply queue to send the message to
         * @return the reply queue
         */
        default String replyQueue() {return (String) metadata().get("x-reply-queue");}


        /**
         * The uri header
         * @return the uri header
         */
        default String uri() {
            return (String) metadata().get("x-uri");
        }

        /**
         * The scheme header
         *
         * @return the scheme header
         */
        default String scheme() {
            return (String)  metadata().get("x-scheme");
        }

        /**
         * The method header
         * @return the method header
         */
        default String method() {
            String m = (String) metadata().get("x-method");
            if (null == m) {
                m = "get";
            }
            return m.toLowerCase();
        }

        /**
         * Get the port... may be String, Number, or null
         * @return get the port
         */
        default Object port() {
            return metadata().get("server-port");
        }

        /**
         * get the protocol for the request
         * @return
         */
        default String protocol() {
            return (String) metadata().get("x-server-protocol");
        }

        /**
         * Get the uri args for the request
         * @return the uri args for the request
         */
        default String args() {
            return (String) metadata().get("x-uri-args");
        }

        /**
         * The content-type header
         * @return the content-type header
         */
        default String contentType() {
            return (String) metadata().get("content-type");
        }

        /**
         * The remote address of the client
         * @return remote address
         */
        default String remoteAddr() {
            return (String) metadata().get("x-remote-addr");
        }

        /**
         * The
         * @return
         */
        Map<String, Object> metadata();
        Object body();
        byte[] rawBody();
        MessageBroker.ReceivedMessage underlyingMessage();
    }

    /**
     * Convert the message from the more generic one from the MessageBroker into
     * something that can be routed
     * @param message
     * @return
     */
    default Message brokerMessageToRouterMessage(MessageBroker.ReceivedMessage message) {
        return new Message() {

            @Override
            public Map<String, Object> metadata() {
                return message.metadata();
            }

            @Override
            public Object body() {
                return message.body();
            }

            @Override
            public byte[] rawBody() {
                return message.rawBody();
            }

            @Override
            public MessageBroker.ReceivedMessage underlyingMessage() {
                return message;
            }
        };
    }

    /**
     * Route the message. This may cause the message to be queued to the next handler (Runner)
     * or route it to the handler Func.
     *
     * @param message the Message to route
     * @return the result of the Message application or void if this Router forwards the message
     */
    Object routeMessage(Message message) throws IOException;

    /**
     *
     * @param message
     */
    default Void apply(MessageBroker.ReceivedMessage message) {
        try {
            routeMessage(brokerMessageToRouterMessage(message));
        } catch (IOException e) {
            logger.error("Failed to route the message", e);
        }

        return null;
    }
}
