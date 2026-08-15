// libs/request.js（或你的 ../request.js）
const deepMerge = require("../function/deepMerge");
const validate = require("../function/test");

class Request {
  // 设置全局默认配置
  setConfig(customConfig) {
    this.config = deepMerge(this.config, customConfig);
  }

  // 主要请求部分
  request(options = {}) {
    // 请求拦截
    if (this.interceptor.request && typeof this.interceptor.request === "function") {
      const intercepted = this.interceptor.request(options);
      if (intercepted === false) {
        // 取消请求：返回pending的Promise，避免进入then
        return new Promise(() => {});
      }
      // 关键：把拦截后的配置用回去
      options = intercepted || options;
    }

    // 添加Authorization头
    const token = uni.getStorageSync("token");
    if (token) {
      options.header = Object.assign({}, options.header, {
        Authorization: `Bearer ${token}`,
      });
    }

    // 默认项
    options.dataType = options.dataType || this.config.dataType;
    options.responseType = options.responseType || this.config.responseType;
    options.url = options.url || "";
    options.params = options.params || {};
    options.header = Object.assign({}, this.config.header, options.header);
    options.method = (options.method || this.config.method).toUpperCase();
    options.timeout = options.timeout || this.config.timeout;

    return new Promise((resolve, reject) => {
      // 确保在所有情况下都能隐藏loading
      const hideLoadingSafely = () => {
        try {
          uni.hideLoading();
        } catch (e) {
          console.warn('hideLoading failed:', e);
        }
        clearTimeout(this.config.timer);
        this.config.timer = null;
      };

      options.complete = (response) => {
        // 请求返回后处理loading
        hideLoadingSafely();

        // 是否需要返回原始数据
        if (this.config.originalData) {
          // 原样交给响应拦截器/调用方
          if (this.interceptor.response && typeof this.interceptor.response === "function") {
            const resInterceptors = this.interceptor.response(response);
            if (resInterceptors !== false) return resolve(resInterceptors);
            return reject(buildErrorFromResponse(response, "请求失败"));
          }
          return resolve(response);
        }

        // 非 originalData：只给数据或标准化错误
        const code = response.statusCode;
        if (code >= 200 && code < 300) {
          if (this.interceptor.response && typeof this.interceptor.response === "function") {
            const resInterceptors = this.interceptor.response(response.data);
            if (resInterceptors !== false) return resolve(resInterceptors);
            return reject(buildErrorFromResponse(response, "请求失败"));
          }
          return resolve(response.data);
        }

        // ❗ 非 2xx：构造 Error(message) 并附带 response
        return reject(buildErrorFromResponse(response, `请求失败(${code})`));
      };

      options.fail = (error) => {
        // 请求失败时也要隐藏loading
        hideLoadingSafely();
        reject(buildErrorFromResponse(error, "网络请求失败"));
      };

      // URL 处理（补全 baseUrl/前导斜杠）
      options.url = validate.url(options.url)
        ? options.url
        : this.config.baseUrl + (options.url.indexOf("/") === 0 ? options.url : "/" + options.url);

      // query 参数（你当前场景要在 POST 上拼 params，这里保持兼容）
      if (options.method === "POST" && options.params) {
        const params = new URLSearchParams();
        for (const key in options.params) {
          const v = options.params[key];
          if (v !== undefined && v !== null) params.append(key, v);
        }
        const qs = params.toString();
        if (qs) options.url += (options.url.includes("?") ? "&" : "?") + qs;
      }

      // loading
      if (this.config.showLoading && !this.config.timer) {
        this.config.timer = setTimeout(() => {
          uni.showLoading({
            title: this.config.loadingText,
            mask: this.config.loadingMask,
          });
          this.config.timer = null;
        }, this.config.loadingTime);
      }

      uni.request(options);
    });
  }

  constructor() {
    this.config = {
      baseUrl: "http://localhost:8088",
      header: {
        // 这里的 Access-Control-Allow-Origin 是响应头，放请求头没意义，但保留不影响
        "Access-Control-Allow-Origin": "*",
        "Content-Type": "application/json",
      },
      method: "POST",
      dataType: "json",
      responseType: "text",
      showLoading: true,
      loadingText: "请求中...",
      loadingTime: 800,
      timer: null,
      originalData: false,
      loadingMask: true,
      timeout: 5000,
    };

    // 拦截器
    this.interceptor = {
      request: null,
      response: null,
    };

    // get
    this.get = (url, data = {}, header = {}) => {
      return this.request({
        method: "GET",
        url,
        header,
        data,
      });
    };

    // post（axios风格：post(url, data, config)）
    this.post = (url, data = {}, config = {}) => {
      return this.request({
        url,
        method: "POST",
        data,
        ...config,
      });
    };

    this.put = (url, data = {}, header = {}) => {
      return this.request({
        url,
        method: "PUT",
        header,
        data,
      });
    };

    this.delete = (url, data = {}, header = {}) => {
      return this.request({
        url,
        method: "DELETE",
        header,
        data,
      });
    };
  }
}

/** 从 uni.request 的响应构造可读错误对象 */
function buildErrorFromResponse(response, fallback) {
  const data = response && (response.data !== undefined ? response.data : response.body);
  const code = response && (response.statusCode ?? response.status);
  const msg =
    (typeof data === "string"
      ? data
      : data?.message || data?.error || data?.msg || data?.detail || data?.title) ||
    response?.errMsg || // H5 上常见为 "request:ok"，只有在没有数据时兜底
    (code ? `${fallback}` : fallback);

  const err = new Error(msg);
  err.response = response;
  return err;
}

const request = new Request();
module.exports = request;
