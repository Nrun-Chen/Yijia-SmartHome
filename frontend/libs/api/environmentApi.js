// 引入http请求模块
const http = require('../request');

/**
 * 获取温度数据
 * @param {number} limit - 获取的记录数
 * @returns {Promise<Object>} - 温度数据对象
 */
const getTemperatureData = async (limit = 1) => {
  try {
    const params = {
      columns: 'temperature',
      limit: limit
    };

    //console.log('请求温度数据参数:', params);
    const res = await http.get('/mqtt-data/specific-columns', params);

    // 保存新数据到本地存储
    const temperatureData = res.data;
    uni.setStorageSync('temperatureData', temperatureData);
    //console.log('温度数据已保存到本地存储');
    return temperatureData;
  } catch (error) {
    //console.error('获取温度数据异常:', error);
    // 尝试从本地存储获取旧数据
    const localData = uni.getStorageSync('temperatureData');
    return localData || null;
  }
};

/**
 * 获取湿度数据
 * @param {number} limit - 获取的记录数
 * @returns {Promise<Object>} - 湿度数据对象
 */
const getHumidityData = async (limit = 1) => {
  try {
    const params = {
      columns: 'humidity',
      limit
    };

    //console.log('请求湿度数据参数:', params);
    const res = await http.get('/mqtt-data/specific-columns', params);

    // 保存新数据到本地存储
    const humidityData = res.data;
    uni.setStorageSync('humidityData', humidityData);
    //console.log('湿度数据已保存到本地存储');
    return humidityData;
  } catch (error) {
   // console.error('获取湿度数据异常:', error);
    // 尝试从本地存储获取旧数据
    const localData = uni.getStorageSync('humidityData');
    return localData || null;
  }
};

/**
 * 获取煤气数据
 * @param {number} limit - 获取的记录数
 * @returns {Promise<Object>} - 煤气数据对象
 */
const getGasData = async (limit = 1) => {
  try {
    const params = {
      columns: 'gas',
      limit
    };

    //console.log('请求煤气数据参数:', params);
    const res = await http.get('/mqtt-data/specific-columns', params);

    // 保存新数据到本地存储
    const gasData = res.data;
    uni.setStorageSync('gasData', gasData);
    //console.log('煤气数据已保存到本地存储');
    return gasData;
  } catch (error) {
    //console.error('获取煤气数据异常:', error);
    // 尝试从本地存储获取旧数据
    const localData = uni.getStorageSync('gasData');
    return localData || null;
  }
};

// 导出所有API函数
module.exports = {
  getTemperatureData,
  getHumidityData,
  getGasData
};