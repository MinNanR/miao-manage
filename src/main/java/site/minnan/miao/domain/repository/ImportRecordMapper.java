package site.minnan.miao.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.minnan.miao.domain.entity.ImportRecord;

import java.util.List;

@Repository
@Mapper
public interface ImportRecordMapper extends BaseMapper<ImportRecord> {

    @Select("select id from import_record where week_start_date > #{date}")
    List<Integer> getLatestThreeRecordId(@Param("date") String date);
}
