package com.yulink.texas.admin.dto;

import com.yulink.texas.common.admin.dto.AdminDtoProperty;
import com.yulink.texas.common.admin.dto.DeclarationType;
import lombok.Data;

@Data
public class SampleDto {

    @AdminDtoProperty(value = "序号", modifyReadOnly = true)
    private Long id;

    @AdminDtoProperty("名称")
    private String name;

    @AdminDtoProperty("ssoId")
    private String ssoId;

    @AdminDtoProperty("手机")
    private String telNum;

    @AdminDtoProperty("邮箱")
    private String email;

    @AdminDtoProperty("类型")
    private String type;

    public static final String columnStatus = "status";
    @AdminDtoProperty(value = "状态", searchEnable = true, declarationType = DeclarationType.SELECT)
    private String status;

    @AdminDtoProperty("备注信息")
    private String info;

    @AdminDtoProperty(value = "创建时间", modifyEnable = false, createEnable = false)
    private Long createTime;

    @AdminDtoProperty(value = "更新时间", modifyEnable = false, createEnable = false)
    private Long updateTime;

}
