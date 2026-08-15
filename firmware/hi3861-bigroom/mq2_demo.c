#include <stdio.h>
#include <unistd.h>

#include "ohos_init.h"
#include "cmsis_os2.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include "wifiiot_pwm.h"
#include "wifiiot_adc.h"
#include "wifiiot_errno.h"

#define GAS_SENSOR_CHAN_NAME WIFI_IOT_ADC_CHANNEL_5 //对应管脚PIN11

//气体报警阈值
#define GAS_THRESHOLD 600

int alarmbell = 0;
int gas = 0;
int bell = 0;

static void init(void)
{
    GpioInit();

    // 蜂鸣器引脚 设置为 PWM功能
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_5, WIFI_IOT_IO_FUNC_GPIO_5_PWM2_OUT);
    PwmInit(WIFI_IOT_PWM_PORT_PWM2);
}

void kitchen_entry(void *arg)
{
    (void)arg;

    init();
    while (1)
    {
        unsigned short data = 0; //保存读取到的燃气值
        //调用AdcRead读取值
        if (AdcRead(GAS_SENSOR_CHAN_NAME, &data, WIFI_IOT_ADC_EQU_MODEL_4, WIFI_IOT_ADC_CUR_BAIS_DEFAULT, 0) == WIFI_IOT_SUCCESS)
        {
            gas = data;
            printf("gas:%d ppm\n",gas);
            if (gas > GAS_THRESHOLD)
            {
                alarmbell = 1;
            }
            else
            {
                alarmbell = 0;
            }

            if (alarmbell||bell)
            {
                uint16_t freqDivisor = 34052;
                //                                  占空比，        频率
                PwmStart(WIFI_IOT_PWM_PORT_PWM2, freqDivisor*2/3, freqDivisor);
                sleep(1);
                bell=0;
            }
            else
            {
                PwmStop(WIFI_IOT_PWM_PORT_PWM2);
            }
            sleep(1);
        }
    }
}

void Mq2Demo(void)
{

    osThreadAttr_t attr;
    attr.name = "kitchen_task";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 4096;
    attr.priority = osPriorityNormal;

    if (osThreadNew(kitchen_entry, NULL, &attr) == NULL)
    {
        printf("[kitchen_entry] Falied to create kitchen_entry!\n");
    }
}
