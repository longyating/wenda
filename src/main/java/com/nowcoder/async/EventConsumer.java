package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by nowcoder on 2016/7/30.
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private Map<EventType, List<EventHandler>>  config = new HashMap<EventType, List<EventHandler>>();
    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 输入一个EventType 找出所有关心它的handler  存在 Map<EventType, List<EventHandler>>  config 中
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);  //把工程中实现EventHandler接口的所有的类都找出来
        if(beans != null){
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()){
                List<EventType> list = entry.getValue().getSupportEventTypes(); // 关心的所有type
                for(EventType type : list){
                    if(!config.containsKey(type)){
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }


        //开启线程 找队列的事件
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0, key);

                    //Brpop 命令 - 移出并获取列表的最后一个元素。推进的时候 lpush 是放在头部，所以取得时候要从尾巴开始取
                    //取出来后会返回两部分
                    //1）弹出元素所属于的 key
                    //2)弹出元素的值

                    for (String message : events) {
                        if (message.equals(key)) {
                            continue;
                        }

                        EventModel eventModel = JSON.parseObject(message, EventModel.class);

                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }


                        List<EventHandler> EventHandlerList= config.get(eventModel.getType());
                        for (EventHandler handler : config.get(eventModel.getType())) { //取出来event 后，发给所有相关的handler 去处理
                            handler.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
