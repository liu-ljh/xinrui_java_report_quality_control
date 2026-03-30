## 3/30

### 1. 配置

- DeepSeek API端点：`deepseek.api-endpoint`
- **API密钥必须通过环境变量 `DEEPSEEK_API_KEY`设置**，确保安全。
- 服务端口：`8079`
- 日志输出至项目路径的`logs/`目录，按天和大小滚动，并分离INFO与ERROR日志，内置敏感信息过滤。

### 2. 使用示例

**请求示例** `POST /api/v1/quality-control/deepseek`

```
{
  "model": "deepseek-chat",
  "system": "你是一个专业的医疗质控专家，请对下面医疗报告进行质控，并给出修改建议",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "这里是需要被质控的医疗报告全文..."
        }
      ]
    }
  ],
  "temperature": 0.7,
  "stream": false
}
```

**响应示例**

```
{
  "code": 200,
  "msg": "success",
  "data": {
    "text": "模型返回的质控分析和建议文本..."
  }
}
```

### API接口

- `GET /api/v1/quality-control/test`- 服务测试（仅用于测试连接）
- `POST /api/v1/quality-control/deepseek`- 提交报告进行AI质控分析

