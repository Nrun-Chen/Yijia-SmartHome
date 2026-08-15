#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"
#include "wifi_device.h"
#include "lwip/netifapi.h"
#include "lwip/api_shell.h"
#include "wifi_utils.h"
#include "mqtt_utils.h"
#include <at.h>
#include <hi_at.h>
#include "aht20_demo.h"
#include "sub_task.h"
#include "fan.h"
#include "mq2_demo.h"
#include "tr.h"
#include "senser.h"
#include "water_pump_task.h"
static void mqtt_test_thread(void *arg)
{
    (void)arg;
    Mq2Demo();
    light_task();
    AhtDemo();
    fan_task();
    water_pump_task();
    senser_connect();
    sub_task();
}

static void at_exe_mqtt_test_cmd(void)
{
    osThreadAttr_t attr;

    attr.name = "wifi_config_thread";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 4096;
    attr.priority = 36;
    //在新线程中执行函数mqtt_test_thread
    if (osThreadNew((osThreadFunc_t)mqtt_test_thread, NULL, &attr) == NULL)
    {
        printf("[LedExample] Falied to create LedTask!\n");
    }

    AT_RESPONSE_OK;
}

SYS_RUN(at_exe_mqtt_test_cmd);
