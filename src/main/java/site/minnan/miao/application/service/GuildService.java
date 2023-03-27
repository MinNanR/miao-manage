package site.minnan.miao.application.service;

import site.minnan.miao.domain.entity.Guild;
import site.minnan.miao.domain.vo.DropDownVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.userinterface.response.ResponseEntity;

public interface GuildService {

    ListQueryVO<DropDownVO> getGuildDropDown();

}
