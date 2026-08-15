package com.cqu.smarthome.demo.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.connection-timeout:10}")
    private int connectionTimeout;

    @Value("${mqtt.keep-alive-interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.automatic-reconnect:true}")
    private boolean automaticReconnect;

    /**
     * 连接MQTT服务器配置
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);
        options.setAutomaticReconnect(automaticReconnect);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);

        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        // 设置遗嘱消息
        options.setWill("willTopic", (clientId + "与服务器断开连接").getBytes(), 0, false);
        return options;
    }

    /**
     * MQTT客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    // 消息发送通道
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 消息接收通道
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 消息发送处理器
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = 
            new MqttPahoMessageHandler(clientId + "_out", mqttClientFactory());
        messageHandler.setAsync(true);
        // 设置默认主题为配置的第二个主题
        String[] topics = topic.split(",");
        if (topics.length > 1) {
            messageHandler.setDefaultTopic(topics[1]);
        }
        return messageHandler;
    }

    // 消息接收适配器
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
        String[] topics = topic.split(",");
        MqttPahoMessageDrivenChannelAdapter adapter = 
            new MqttPahoMessageDrivenChannelAdapter(clientId + "_in", 
                mqttClientFactory(), topics[0]);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    // 消息接收流程
    @Bean
    public IntegrationFlow mqttInFlow(MqttPahoMessageDrivenChannelAdapter mqttInbound,
            com.cqu.smarthome.demo.mqtt.handler.MqttMessageHandler mqttMessageHandler) {
        return IntegrationFlows.from(mqttInbound)
                .handle(message -> {
                    String receivedTopic = (String) message.getHeaders().get("mqtt_receivedTopic");
                    String payload = (String) message.getPayload();
                    Integer qos = (Integer) message.getHeaders().get("mqtt_receivedQos");
                    Boolean retained = (Boolean) message.getHeaders().get("mqtt_receivedRetained");
                    
                    // 打印消息信息
                    System.out.println(String.format("接收消息主题: %s", receivedTopic));
                    System.out.println(String.format("接收消息QoS: %d", qos));
                    System.out.println(String.format("接收消息内容: %s", payload));
                    System.out.println(String.format("接收消息retained: %b", retained));
                    
                    // 调用自定义处理器处理消息
                    mqttMessageHandler.handleMessage(message);
                })
                .get();
    }
}
