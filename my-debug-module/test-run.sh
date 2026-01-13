#!/bin/bash

echo "========================================"
echo "测试 Nacos 动态刷新应用"
echo "========================================"

# 检查 Nacos 是否运行
echo "1. 检查 Nacos 连接..."
if curl -s http://localhost:8848/nacos/ > /dev/null 2>&1; then
    echo "   ✓ Nacos 已运行"
else
    echo "   ✗ Nacos 未运行，请先启动 Nacos"
    exit 1
fi

# 检查配置是否存在
echo "2. 检查 Nacos 配置..."
CONFIG=$(curl -s "http://localhost:8848/nacos/v1/cs/configs?dataId=dynamic-tp-test-dtp-dev.yml&group=DEFAULT_GROUP" 2>&1)
if echo "$CONFIG" | grep -q "threadPoolName"; then
    echo "   ✓ 配置已存在"
else
    echo "   ✗ 配置不存在，请在 Nacos 控制台创建配置"
    echo "   Data ID: dynamic-tp-test-dtp-dev.yml"
    echo "   Group: DEFAULT_GROUP"
    exit 1
fi

# 编译项目
echo "3. 编译项目..."
cd "$(dirname "$0")"
mvn clean compile -q > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "   ✓ 编译成功"
else
    echo "   ✗ 编译失败"
    exit 1
fi

# 启动应用（后台运行）
echo "4. 启动应用..."
java -cp "target/classes:$(mvn dependency:build-classpath -q -DincludeScope=compile 2>/dev/null | tail -1)" \
     com.dtp.debug.NacosTestApplication > /tmp/nacos-test.log 2>&1 &
APP_PID=$!

# 等待应用启动
echo "5. 等待应用启动..."
for i in {1..30}; do
    sleep 1
    if curl -s http://localhost:8080/status > /dev/null 2>&1; then
        echo "   ✓ 应用启动成功 (PID: $APP_PID)"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "   ✗ 应用启动超时"
        echo "   查看日志: tail -50 /tmp/nacos-test.log"
        kill $APP_PID 2>/dev/null
        exit 1
    fi
done

# 测试接口
echo "6. 测试接口..."
STATUS=$(curl -s http://localhost:8080/status)
if echo "$STATUS" | grep -q "线程池名称"; then
    echo "   ✓ 接口正常"
    echo ""
    echo "$STATUS" | head -15
else
    echo "   ✗ 接口异常"
    echo "   响应: $STATUS"
fi

echo ""
echo "========================================"
echo "测试完成！"
echo "========================================"
echo "应用 PID: $APP_PID"
echo "查看日志: tail -f /tmp/nacos-test.log"
echo "停止应用: kill $APP_PID"
echo "访问状态: http://localhost:8080/status"
echo "========================================"
