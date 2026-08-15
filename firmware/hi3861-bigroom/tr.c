#include <stdio.h>
#include <unistd.h>
#include "ohos_init.h"
#include "cmsis_os2.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include "mqtt_utils.h"
static void init_lights(void)
{
    GpioInit();
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_10, WIFI_IOT_IO_FUNC_GPIO_10_GPIO);
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_10, WIFI_IOT_GPIO_DIR_OUT);

    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_IO_FUNC_GPIO_2_GPIO);
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_GPIO_DIR_OUT);

    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_1, WIFI_IOT_IO_FUNC_GPIO_1_GPIO);
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_1, WIFI_IOT_GPIO_DIR_OUT);
}

void light_thread(void *arg)
{
    arg = arg;

    int light_pin[3] = {WIFI_IOT_IO_NAME_GPIO_10, WIFI_IOT_IO_NAME_GPIO_2, WIFI_IOT_IO_NAME_GPIO_1};//黄红绿

    init_lights();
    while (1)
    {
        GpioSetOutputVal(light_pin[0], liv_lit);
        GpioSetOutputVal(light_pin[1], kit_lit);
        GpioSetOutputVal(light_pin[2], tol_lit);
        sleep(1);
    }
}

void light_task(void)
{
    osThreadAttr_t attr;
    attr.name = "light_task";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 4096;
    attr.priority = osPriorityNormal;

    if (osThreadNew(light_thread, NULL, &attr) == NULL) {
        printf("[light_task] Failed to create light_task!\n");
    }
}