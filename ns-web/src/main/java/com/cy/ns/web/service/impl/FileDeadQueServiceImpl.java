package com.cy.ns.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cy.ns.raft.entity.po.FilePO;
import com.cy.ns.raft.service.FileService;
import com.cy.ns.web.handler.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author Haechi
 */
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(value = "ns_file_dead_queue",autoDelete = "false"),
                exchange = @Exchange(value = "ns_file_dead_exchange")
))
@Service
@Slf4j
public class FileDeadQueServiceImpl {

    FileHandler fileHandler;

    FileService fileService;

    public FileDeadQueServiceImpl(FileHandler fileHandler, FileService fileService) {
        this.fileHandler = fileHandler;
        this.fileService = fileService;
    }
    @RabbitHandler
    public void receive(String msg) {
        log.info( "需删除文件id：{}", msg);
        QueryWrapper<FilePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq( "id", Long.valueOf( msg ) ).eq( "is_del", 1 );
        FilePO file = fileService.getOne( queryWrapper );
        try {
            if(file!=null && file.getIsDel() && fileHandler.handledDelete( file ).get()){
                log.info("file delete success");
                fileService.removeById( Long.valueOf( msg) );
            }else{
                log.error("file delete failed");
            }
        }catch ( Exception e){
            log.error( "文件删除失败，文件id：{}", msg );
        }

    }
}
