package site.minnan.miao.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.minnan.miao.domain.entity.ContributionRecord;
import site.minnan.miao.domain.vo.ContributionVO;
import site.minnan.miao.userinterface.dto.GetContributionListDTO;

import java.util.List;

@Mapper
@Repository
public interface ContributionRecordMapper extends BaseMapper<ContributionRecord> {

    /**
     * 检索遗漏名单
     *
     * @param thisWeekId
     * @param lastWeekId
     * @return
     */
    List<String> getOmitRecord(@Param("thisWeekId") Integer thisWeekId, @Param("lastWeekId") Integer lastWeekId);

    /**
     * 查询miao贡记录
     * @param dto
     * @return
     */
    List<ContributionVO> getContributionList(GetContributionListDTO dto);

    /**
     * 计算miao贡数量
     *
     * @param dto
     * @return
     */
    Integer countContribution(GetContributionListDTO dto);

    /**
     * 查询近三周所有记录
     *
     * @param list
     * @return
     */
    List<ContributionVO> getLatestContribution(@Param("date") String date, @Param("guildId") Integer guildId);
}
