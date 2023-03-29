package site.minnan.miao.infrastructure.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.minnan.miao.application.service.RecordService;

@Component
@Slf4j
public class Scheduler {

    @Autowired
    private RecordService recordService;

    @Scheduled(cron = "0 0 8 * * 1")
    public void generateImportRecordScheduler() {
        log.info("开始生成导入记录");
        recordService.generateRecord();
        log.info("结束生成导入记录");
    }
}
