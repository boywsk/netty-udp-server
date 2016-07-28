# 停止服务

pid=`ps -ef | grep "com.gome.im.dispatcher.server.DispatchServer" | grep -v grep | awk '{print $2}'`

if [ -n "$pid" ]; then
  kill -9 $pid
fi

# 服务停止
echo '服务停止......'