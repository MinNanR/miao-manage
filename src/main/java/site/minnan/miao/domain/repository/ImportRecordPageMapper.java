package site.minnan.miao.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import site.minnan.miao.domain.entity.ImportRecordPage;

@Repository
@Mapper
public interface ImportRecordPageMapper extends BaseMapper<ImportRecordPage> {

}
