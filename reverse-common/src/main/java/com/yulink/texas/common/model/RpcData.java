package com.yulink.texas.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Need class description here...
 *
 * @Author: liupanpan
 * @Date: 2019/3/21
 * @Copyright (c) 2013, yulink.io All Rights Reserved
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcData {

    private String ip;
    private Integer port;
    private String username;
    private String password;

}
