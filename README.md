# socks5-netty
基于netty实现的socks5代理

## 环境变量
```bash
port=11080; auth=true; userName=xuda; passWord=123; bossThread=1; workerThread=64; connectTimeoutMillis=6000
```

## Docker
```bash
docker run -it -d --name socks5 -e port=1111 -e auth=true -e userName=test -e passWord=123123 -e bossThread=1 -e workerThread=64 -e connectTimeoutMillis=6000 -p1111:1111 -p1112:8181 smilex1/socks5:shenandoah8u332-b05-env
```
