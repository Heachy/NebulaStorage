# NebulaStorage
This repository is used to develop object storage systems





# Raft方案

## 节点通信方案

### 服务启动

1. 一个节点启动的时候，首先打开自己的服务(Server)节点
2. 随机时间过后，如果没有消息给到自身，发起选举
3. 读取配置，获取所有channel，如果每个机子对应的channel已经构建，就直接发消息，不然就进行构建
