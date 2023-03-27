package site.minnan.miao.userinterface.fascade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.minnan.miao.application.service.GuildService;
import site.minnan.miao.domain.entity.Guild;
import site.minnan.miao.domain.vo.DropDownVO;
import site.minnan.miao.domain.vo.ListQueryVO;
import site.minnan.miao.userinterface.response.ResponseEntity;

@RequestMapping("/miao-api/guild")
@RestController
public class GuildController {

    @Autowired
    private GuildService guildService;

    @PostMapping("/getGuildDropDown")
    public ResponseEntity<ListQueryVO<DropDownVO>> getGuildDropDown() {
        ListQueryVO<DropDownVO> dropDown = guildService.getGuildDropDown();
        return ResponseEntity.success(dropDown);
    }

}
