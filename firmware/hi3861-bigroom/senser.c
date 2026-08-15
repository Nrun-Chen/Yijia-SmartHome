#include <stdio.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include "wifiiot_adc.h"

unsigned short adcdata;
int isHuman = 0;
int sun = 0;
int senser_light = 0;
int senser_on = 0;

#define sun_rate 700

static void init(void)
{
    // 灯
    GpioInit();
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_3, WIFI_IOT_IO_FUNC_GPIO_3_GPIO);
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_3, WIFI_IOT_GPIO_DIR_OUT);
    // 光敏电阻

    // IoSetFunc(WIFI_IOT_IO_NAME_GPIO_9, WIFI_IOT_IO_FUNC_GPIO_9_GPIO);
    // GpioSetDir(WIFI_IOT_IO_NAME_GPIO_9, WIFI_IOT_GPIO_DIR_IN);

    // 人体红外感应
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_7, WIFI_IOT_IO_FUNC_GPIO_7_GPIO);
    GpioSetDir(WIFI_IOT_IO_NAME_GPIO_7, WIFI_IOT_GPIO_DIR_IN);
    IoSetPull(WIFI_IOT_IO_NAME_GPIO_7, WIFI_IOT_IO_PULL_UP);
}

void access_control_entry(void *arg)
{
    (void)arg;

    init();
    WifiIotGpioValue rel1 = 0;
    while (1)
    {
        //光敏电阻读取值
        AdcRead(WIFI_IOT_ADC_CHANNEL_4, &adcdata, WIFI_IOT_ADC_EQU_MODEL_4, WIFI_IOT_ADC_CUR_BAIS_DEFAULT, 0);
        printf("adc:%d\n", adcdata);

        //光敏判断逻辑
        if(adcdata>sun_rate)
        {
            sun=1;
        }
        else{
            sun=0;
        }
        
        //红外判断逻辑
        GpioGetInputVal(WIFI_IOT_IO_NAME_GPIO_7, &rel1);
        isHuman = rel1;

        if (isHuman&&sun&&senser_on)
        {
            GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_3, WIFI_IOT_GPIO_VALUE1);
            senser_light = 1;
        }
        else
        {
            GpioSetOutputVal(WIFI_IOT_IO_NAME_GPIO_3, WIFI_IOT_GPIO_VALUE0);
            senser_light = 0;
        }

        sleep(1);
    }
}

void senser_connect(void)
{

    osThreadAttr_t attr;
    attr.name = "access_control_task";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 4096;
    attr.priority = osPriorityNormal;

    if (osThreadNew(access_control_entry, NULL, &attr) == NULL)
    {
        printf("[access_control_task] Falied to create access_control_task!\n");
    }
}
