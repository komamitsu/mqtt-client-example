package org.komamitsu.mqttclient.example;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.TimeUnit;

public class MqttClientTest
{
    static class Args
    {
        @Parameter(names = {"--endpoint"}, description = "MQTT broker endpoint")
        String endpoint;

        @Parameter(names = {"--cert-file"}, description = "Certification file")
        Path certFile;

        @Parameter(names = {"--key-file"}, description = "Private key file")
        Path keyFile;

        @Parameter(names = {"--topic"}, description = "MQTT topic")
        String topic;

        @Parameter(names = {"--message"}, description = "Message to be sent to MQTT broker")
        String message;

        @Parameter(names = {"--client-id"}, description = "Client ID")
        String clientId;

        @Parameter(names = {"--help", "-h"}, help = true)
        boolean help;
    }

    private static SSLSocketFactory createSocketFactory(Path certFile, Path keyFile)
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        MqttClientUtil.KeyStorePasswordPair keyStorePasswordPair =
                MqttClientUtil.getKeyStorePasswordPair(certFile.toString(), keyFile.toString());

        // client key and certificates are sent to server so it can authenticate us
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStorePasswordPair.keyStore, keyStorePasswordPair.keyPassword.toCharArray());

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), null, null);
        return context.getSocketFactory();
    }

    public static void main(String[] args)
            throws Exception
    {
        // Parse arguments, load a configuration file and generate Config from it
        Args commandArgs = new Args();
        JCommander commander = JCommander.newBuilder().addObject(commandArgs).build();
        commander.parse(args);

        String topic        = commandArgs.topic;
        String content      = commandArgs.message;
        int qos             = 0;
        String broker       = commandArgs.endpoint;
        String clientId     = commandArgs.clientId;

        MemoryPersistence persistence = new MemoryPersistence();

        SSLSocketFactory socketFactory = createSocketFactory(commandArgs.certFile, commandArgs.keyFile);

        try {
            final MqttAsyncClient client = new MqttAsyncClient(broker, MqttAsyncClient.generateClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("?SDK=Java&Version=1.2.0");
            connOpts.setCleanSession(true);
            connOpts.setSocketFactory(socketFactory);
            client.connect(null,new
                    IMqttActionListener()
                    {
                        @Override
                        public void onSuccess (IMqttToken asyncActionToken){
                            while (true) {
                                try {
                                    client.publish(topic, content.getBytes(), 0, false);
                                    System.out.println("Sent hello...");
                                    TimeUnit.SECONDS.sleep(5);
                                }
                                catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure (IMqttToken asyncActionToken, Throwable exception){
                            exception.printStackTrace();
                        }
                    });

            TimeUnit.SECONDS.sleep(30);

            /*
            MqttAsyncClient client = getClient(broker, topic, content);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("?SDK=Java&Version=1.2.0");
            connOpts.setCleanSession(true);
            connOpts.setSocketFactory(socketFactory);
            System.out.println("Connecting to broker: "+broker);
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(connOpts.getDebug());
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            client.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
            System.out.println("Message published");
            client.disconnect();
            System.out.println("Disconnected");
            */
            System.exit(0);
        }
        catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
