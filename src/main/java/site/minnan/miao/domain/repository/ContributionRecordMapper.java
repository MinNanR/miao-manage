package site.minnan.miao.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import site.minnan.miao.domain.entity.ContributionRecord;

@Mapper
@Repository
public interface ContributionRecordMapper extends BaseMapper<ContributionRecord> {

}
