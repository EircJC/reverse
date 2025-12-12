package com.yulink.texas.admin.bean;

import com.yulink.texas.common.admin.bean.InputOption;
import com.yulink.texas.common.admin.constant.StatusEnum;
import java.util.ArrayList;
import java.util.List;

public class SampleInputOption {

    public static List<InputOption> createStatusEnum(String status) {
        List<InputOption> statusOptionList = new ArrayList<>();
        statusOptionList.add(new InputOption(StatusEnum.ENABLED.getDesc(), StatusEnum.ENABLED.getStatus(),
            StatusEnum.ENABLED.getStatus().equals(status)));
        statusOptionList.add(new InputOption(StatusEnum.DISABLED.getDesc(), StatusEnum.DISABLED.getStatus(),
            StatusEnum.DISABLED.getStatus().equals(status)));
        return statusOptionList;
    }

}
