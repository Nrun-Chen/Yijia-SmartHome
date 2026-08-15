<script>
// 引入http请求模块
import http from './libs/request';
// 引入后台计时器
import backgroundTimer from './libs/util/backgroundTimer';

export default {
	onLaunch: function() {
		console.log('App Launch');
		// 配置http请求的baseUrl
		http.setConfig({
			// 这里设置后端API的基础URL，根据实际情况修改
			baseUrl: 'http://localhost:8088'
		});
		
		// 启动后台计时器
		console.log('启动后台计时器');
		backgroundTimer.start();
	},
	onShow: function() {
		console.log('App Show');
		// 确保后台计时器在运行
		if (!backgroundTimer.isRunning) {
			console.log('重新启动后台计时器');
			backgroundTimer.start();
		}
	},
	onHide: function() {
		console.log('App Hide');
		// 注意：不要在这里停止计时器，让它继续在后台运行
	}
};
</script>

<style>
/* 解决头条小程序组件内引入字体不生效的问题 */
/* #ifdef MP-TOUTIAO */
@font-face {
	font-family: uniicons;
	src: url('/static/uni.ttf');
}
/* #endif */

@font-face {
  font-family: 'Inter';
  src: url('@/static/fonts/Inter-Regular.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
@font-face {
  font-family: 'ZCOOL Xiaowei'; /* 字体的自定义名字 */
  src: url('/static/fonts/ZhanKuXiaoLOGOTi-2.ttf') format('truetype'); /* 引用 ttf 文件 */
  font-weight: 400;
  font-style: normal;
  font-display: swap;
}
@font-face {
  font-family: 'Taipei Sans TC'; /* 台北黑体的自定义名字 */
  src: url('/static/fonts/Taipei-Sans-TC-Beta-Bold-2.ttf') format('truetype'); /* 引用字体文件 */
  font-weight: bold;
  font-style: normal;
  font-display: swap;
}
</style>



