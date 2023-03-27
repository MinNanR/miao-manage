package site.minnan.miao.application.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.minnan.miao.application.service.GuildService;
import site.minnan.miao.domain.entity.Guild;
import site.minnan.miao.domain.repository.GuildMapper;
import site.minnan.miao.domain.vo.DropDownVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.userinterface.response.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuildServiceImpl implements GuildService {

    @Autowired
    GuildMapper guildRepository;

    @Override
    public ListQueryVO<DropDownVO> getGuildDropDown() {
        List<Guild> guildList = guildRepository.selectList(null);
        List<DropDownVO> dropDownList =
                guildList.stream().map(e -> new DropDownVO(e.getId(), e.getName())).collect(Collectors.toList());
        return new ListQueryVO<>(dropDownList, dropDownList.size());
    }
}
