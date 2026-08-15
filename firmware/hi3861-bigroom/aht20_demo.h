#ifndef __AHT20_DEMO_H__
#define __AHT20_DEMO_H__

#ifdef __cplusplus
extern "C" {
#endif

// 全局变量声明
extern float temperature;
extern float humidity;

// 函数声明
void AhtDemoTask(void *arg);
void AhtDemo(void);

#ifdef __cplusplus
}
#endif

#endif /* __AHT20_DEMO_H__ */
