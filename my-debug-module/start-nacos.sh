#!/bin/bash

echo "========================================"
echo "启动 Nacos（Docker 方式）"
echo "========================================"

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查 Nacos 容器是否已存在
if docker ps -a | grep -q nacos; then
    echo "检测到已存在的 Nacos 容器"
    read -p "是否删除并重新创建？(y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "删除旧容器..."
        docker rm -f nacos
    else
        echo "启动现有容器..."
        docker start nacos
        echo "Nacos 已启动！"
        echo "访问地址: http://localhost:8848/nacos"
        echo "用户名/密码: nacos/nacos"
        exit 0
    fi
fi

# 启动 Nacos
echo "正在启动 Nacos..."
docker run -d \
  --name nacos \
  -p 8848:8848 \
  -p 9848:9848 \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  nacos/nacos-server:v2.2.0

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Nacos 启动成功！"
    echo "========================================"
    echo "访问地址: http://localhost:8848/nacos"
    echo "用户名/密码: nacos/nacos"
    echo ""
    echo "等待 Nacos 完全启动（约 10 秒）..."
    sleep 10
    echo "可以开始使用了！"
else
    echo "错误: Nacos 启动失败"
    exit 1
fi
