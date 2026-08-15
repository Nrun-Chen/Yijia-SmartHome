#include <stdio.h>
#include <unistd.h>
#include "ohos_init.h"
#include "cmsis_os2.h"

#include <unistd.h>
#include "hi_wifi_api.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"

#include "lwip/sockets.h"

#include "MQTTPacket.h"
#include "transport.h"
#include "aht20_demo.h"
#include "mq2_demo.h"
#include "fan.h"
#include "senser.h"
#include "water_pump_task.h"
#include "config.h"

int liv_lit = 0;
int kit_lit = 0;
int tol_lit = 0;
int toStop = 0;
unsigned char *payload_in;
int payloadlen_in;
// 函数声明（放在文件开头或头文件中）
void process_mqtt_fan_message(unsigned char *payload, int payload_len);
void process_mqtt_water_pump_message(unsigned char *payload, int payload_len);
void process_mqtt_liv_message(unsigned char *payload, int payload_len);
void process_mqtt_kit_message(unsigned char *payload, int payload_len);
void process_mqtt_tol_message(unsigned char *payload, int payload_len);
void process_mqtt_senser_message(unsigned char *payload, int payload_len);
void process_mqtt_bell_message(unsigned char *payload, int payload_len);

// 函数实现（解析风扇控制消息）
void process_mqtt_fan_message(unsigned char *payload, int payload_len)
{
    // 检查参数是否有效（避免空指针或空消息）
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    // 查找并解析"fan_level="字段（你之前的核心逻辑）
    char *pos = strstr((char *)payload, "fan_level="); // 强制转换为char*（因为payload是unsigned char*）
    if (pos != NULL)
    {
        int level = atoi(pos + strlen("fan_level="));
        if (level >= 0 && level < 4)
        {
            fan_level = level; // 更新全局风扇等级变量
            printf("Fan level updated to %d\n", fan_level);
        }
        else
        {
            printf("Invalid fan level: %d (must be 0-4)\n", level);
        }
    }
    else
    {
        printf("No 'fan_level=' field in message: %s\n", payload);
    }
}

// 函数实现（解析水泵控制消息）
void process_mqtt_water_pump_message(unsigned char *payload, int payload_len)
{
    // 检查参数是否有效（避免空指针或空消息）
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    // 查找并解析"water_pump_level="字段（你之前的核心逻辑）
    char *pos = strstr((char *)payload, "water_pump_level="); // 强制转换为char*（因为payload是unsigned char*）
    if (pos != NULL)
    {
        int level = atoi(pos + strlen("water_pump_level="));
        if (level >= 0 && level < 4)
        {
            water_pump_level = level; // 更新全局水泵等级变量
            printf("water_pump level updated to %d\n", water_pump_level);
        }
        else
        {
            printf("Invalid water_pump level: %d (must be 0-4)\n", level);
        }
    }
    else
    {
        printf("No 'water_pump_level=' field in message: %s\n", payload);
    }
}
//--------------------------------------------------------------------------------------------------------------------------
void process_mqtt_liv_message(unsigned char *payload, int payload_len)
{
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    char *pos3 = strstr((char *)payload, "liv_lit=");
    if (pos3 != NULL)
    {
        int level = atoi(pos3 + strlen("liv_lit="));
        if (level == 0 || level == 1)
        {
            liv_lit = level;
            printf("liv_lit updated to %d\n", liv_lit);
        }
        else
        {
            printf("Invalid liv_lit: %d (must be 0 or 1)\n", level);
        }
    }
    else
    {
        printf("No 'liv_lit=' field in message: %s\n", payload);
    }
}

void process_mqtt_kit_message(unsigned char *payload, int payload_len)
{
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    char *pos4 = strstr((char *)payload, "kit_lit=");
    if (pos4 != NULL)
    {
        int level = atoi(pos4 + strlen("kit_lit="));
        if (level == 0 || level == 1)
        {
            kit_lit = level;
            printf("kit_lit updated to %d\n", kit_lit);
        }
        else
        {
            printf("Invalid kit_lit: %d (must be 0 or 1)\n", level);
        }
    }
    else
    {
        printf("No 'kit_lit=' field in message: %s\n", payload);
    }
}

void process_mqtt_tol_message(unsigned char *payload, int payload_len)
{
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    char *pos5 = strstr((char *)payload, "tol_lit=");
    if (pos5 != NULL)
    {
        int level = atoi(pos5 + strlen("tol_lit="));
        if (level == 0 || level == 1)
        {
            tol_lit = level;
            printf("tol_lit updated to %d\n", tol_lit);
        }
        else
        {
            printf("Invalid tol_lit: %d (must be 0 or 1)\n", level);
        }
    }
    else
    {
        printf("No 'tol_lit=' field in message: %s\n", payload);
    }
}
void process_mqtt_senser_message(unsigned char *payload, int payload_len)
{
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    char *pos5 = strstr((char *)payload, "senser_lit=");
    if (pos5 != NULL)
    {
        int level = atoi(pos5 + strlen("senser_lit="));
        if (level == 0 || level == 1)
        {
            senser_on = level;
            printf("senser_lit updated to %d\n", senser_on);
        }
        else
        {
            printf("Invalid senser_lit: %d (must be 0 or 1)\n", level);
        }
    }
    else
    {
        printf("No 'senser_lit' field in message: %s\n", payload);
    }
}
void process_mqtt_bell_message(unsigned char *payload, int payload_len)
{
    if (payload == NULL || payload_len <= 0)
    {
        printf("Invalid message: payload is NULL or empty\n");
        return;
    }

    char *pos5 = strstr((char *)payload, "bell=");
    if (pos5 != NULL)
    {
        int level = atoi(pos5 + strlen("bell="));
        if (level == 0 || level == 1)
        {
            bell = level;
            printf("bell updated to %d\n", bell);
        }
        else
        {
            printf("Invalid bell: %d (must be 0 or 1)\n", level);
        }
    }
    else
    {
        printf("No 'bell' field in message: %s\n", payload);
    }
}
//--------------------------------------------------------------------------------------------------------------------------

int mqtt_connect(void)
{
    MQTTPacket_connectData data = MQTTPacket_connectData_initializer;
    int rc = 0; // 主返回码
    int mysock = 0;
    unsigned char buf[300];
    int buflen = sizeof(buf);
    int msgid = 1;
    MQTTString topicString = MQTTString_initializer;
    int req_qos = 0;
    char payload[300] = "nihao";
    int payloadlen = strlen(payload);
    int len = 0;
    char *host = MQTT_BROKER_HOST;
    int port = MQTT_BROKER_PORT;

    mysock = transport_open(host, port);
    if (mysock < 0)
        return mysock;

    printf("Sending to hostname %s port %d\n", host, port);

    // 连接MQTT服务器
    data.clientID.cstring = "hi3861_bigroom";
    data.keepAliveInterval = 20;
    data.cleansession = 1;

    len = MQTTSerialize_connect(buf, buflen, &data);
    rc = transport_sendPacketBuffer(mysock, buf, len);
    // 等待连接确认
    if (MQTTPacket_read(buf, buflen, transport_getdata) == CONNACK)
    {
        unsigned char sessionPresent, connack_rc;
        if (MQTTDeserialize_connack(&sessionPresent, &connack_rc, buf, buflen) != 1 || connack_rc != 0)
        {
            printf("Unable to connect, return code %d\n", connack_rc);
            goto exit;
        }
    }
    else
        goto exit;

    // 订阅主题
    topicString.cstring = "bigroom";
    len = MQTTSerialize_subscribe(buf, buflen, 0, msgid, 1, &topicString, &req_qos);
    rc = transport_sendPacketBuffer(mysock, buf, len);

    if (MQTTPacket_read(buf, buflen, transport_getdata) == SUBACK)
    {
        unsigned short submsgid;
        int subcount;
        int granted_qos;
        rc = MQTTDeserialize_suback(&submsgid, 1, &subcount, &granted_qos, buf, buflen);
        if (granted_qos != 0)
        {
            printf("granted qos != 0, %d\n", granted_qos);
            goto exit;
        }
    }
    else
        goto exit;

    // 循环处理消息
    while (!toStop)
    {
        if (MQTTPacket_read(buf, buflen, transport_getdata) == PUBLISH)
        {
            unsigned char dup;
            int qos;
            unsigned char retained;
            unsigned short msgid;
            int rc; // 重命名局部变量，避免遮蔽外部rc
            MQTTString receivedTopic;
            // 检查反序列化结果
            rc = MQTTDeserialize_publish(&dup, &qos, &retained, &msgid, &receivedTopic,
                                         &payload_in, &payloadlen_in, buf, buflen);
            process_mqtt_fan_message(payload_in, payloadlen_in); //-------------------------------------------------------------
            process_mqtt_water_pump_message(payload_in, payloadlen_in);
            process_mqtt_liv_message(payload_in, payloadlen_in);
            process_mqtt_kit_message(payload_in, payloadlen_in);
            process_mqtt_tol_message(payload_in, payloadlen_in);
            process_mqtt_senser_message(payload_in, payloadlen_in);
            process_mqtt_bell_message(payload_in, payloadlen_in);

            printf("message arrived %d,%s\n", payloadlen_in, payload_in);

            rc = rc;
        }
        // 发布传感器数据
        // 构造 JSON 格式的 payload
        // snprintf(payload, sizeof(payload),
        //  "{\"temperature\":%.2f,\"humidity\":%.2f,\"gasdata\":%hu,\"fan_level\":%d,\"liv_lit\":%d,\"kit_lit\":%d,\"tol_lit\":%d}", temperature, humidity, gasdata, fan_level, liv_lit, kit_lit, tol_lit);
        //      payloadlen = strlen(payload);
        //snprintf(payload, sizeof(payload),"{\"liv_lit\":%d,\"kit_lit\":%d,\"tol_lit\":%d,\"temperature\":%.2f,\"humidity\":%.2f,\"alarmbell\":%d,\"gas\":%d ppm,\"fan_level\":%d}", liv_lit, kit_lit, tol_lit, temperature, humidity, alarmbell, gas, fan_level);
        //snprintf(payload, sizeof(payload),"{\"liv_lit\":%d,\"kit_lit\":%d,\"tol_lit\":%d,\"temperature\":%.2f,\"humidity\":%.2f,\"isHuman\":%d,\"sun\":%d,\"adcdata\":%u,\"senser_light\":%d,\"fan_level\":%d}", liv_lit, kit_lit, tol_lit, temperature, humidity, isHuman, sun, adcdata, senser_light, fan_level);
        snprintf(payload, sizeof(payload),"{\"liv_lit\":%d,\"kit_lit\":%d,\"tol_lit\":%d,\"senser_on\":%d,\"fan_level\":%d,\"water_pump_level\":%d,\"bell\":%d,\"temperature\":%.2f,\"humidity\":%.2f,\"gas\":%d,\"adcdata\":%u,\"isHuman\":%d,\"sun\":%d,\"senser_light\":%d,\"alarmbell\":%d}", liv_lit, kit_lit, tol_lit, senser_on, fan_level, water_pump_level, bell, temperature, humidity, gas, adcdata, isHuman, sun, senser_light, alarmbell);
        
        
        
        
        
        
        payloadlen = strlen(payload);
        topicString.cstring = "p_bigroom";

        len = MQTTSerialize_publish(buf, buflen, 0, 0, 0, 0, topicString, (unsigned char *)payload, payloadlen);
        rc = transport_sendPacketBuffer(mysock, buf, len);
        usleep(2000 * 1000);
    }

    // 断开连接
    printf("disconnecting\n");
    len = MQTTSerialize_disconnect(buf, buflen);
    rc = transport_sendPacketBuffer(mysock, buf, len);

exit:
    transport_close(mysock);
    rc = rc;
    return 0;
}
