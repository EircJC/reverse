create table t_sys_role
(
    id                     bigint          not null auto_increment     comment '编号',
    role_id                varchar(64)     not null                    comment '角色ID',
    name                   varchar(256)                                comment '角色名',
    type                   varchar(3)      not null default '0'        comment '类型',
    status                 varchar(3)      not null default '0'        comment '状态',
    info                   varchar(64)                                 comment '信息',
    create_time            timestamp       default current_timestamp   comment '创建时间',
    update_time            timestamp       default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id),
    unique(role_id)
) ENGINE=InnoDB  default CHARSET=utf8 comment='系统角色表';

create table t_sys_right
(
    id                     bigint          not null auto_increment     comment '编号',
    role_id                varchar(256)    not null                    comment '角色ID',
    right_id               varchar(256)    not null                    comment '权限ID',
    type                   varchar(3)      not null default '0'        comment '类型',
    status                 varchar(3)      not null default '0'        comment '状态',
    create_time            timestamp       default current_timestamp   comment '创建时间',
    update_time            timestamp       default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id)
) ENGINE=InnoDB  default CHARSET=utf8 comment='系统权限表';

create table t_sys_menu
(
    id                     bigint          not null auto_increment     comment '编号',
    menu_no                varchar(64)     not null                    comment '菜单ID',
    menu_parent_no         varchar(64)                                 comment '父菜单ID',
    menu_order             integer                                     comment '排序索引',
    menu_name              varchar(128)                                comment '菜单名',
    menu_url               varchar(256)                                comment '菜单URL',
    menu_icon              varchar(256)                                comment '菜单图标',
    is_visible             integer                                     comment '可见标志',
    is_leaf                integer                                     comment '叶子标志',
    create_time            timestamp       default current_timestamp   comment '创建时间',
    update_time            timestamp       default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id),
    unique(menu_no)
) ENGINE=InnoDB  default CHARSET=utf8 comment='系统菜单表';

create table t_sys_btn
(
    id                     bigint          not null auto_increment     comment '编号',
    btn_no                 varchar(64)     not null                    comment '按钮ID',
    btn_name               varchar(128)                                comment '按钮名称',
    btn_class              varchar(256)                                comment '按钮类',
    btn_icon               varchar(256)                                comment '按钮图标',
    btn_script             varchar(256)                                comment '按钮脚本',
    menu_no                varchar(64)                                 comment '菜单ID',
    init_status            varchar(256)                                comment '初始状态',
    seq_no                 integer                                     comment '排序索引',
    create_time            timestamp       default current_timestamp   comment '创建时间',
    update_time            timestamp       default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id)
) ENGINE=InnoDB  default CHARSET=utf8 comment='系统按钮表';


create table t_admin_user
(
    id                 bigint            not null auto_increment       comment '编号',
    user_id            varchar(128)      not null                      comment '管理员ID',
    user_name          varchar(128)      not null                      comment '管理员名',
    password           varchar(256)      not null                      comment '密码',
    type               varchar(3)        not null default '0'          comment '类型',
    phone              varchar(50)                                     comment '电话号码',
    email              varchar(100)                                    comment '电子邮件',
    role_id            varchar(64)                                     comment '角色ID',
    status_flag        char(1)           not null default '1'          comment '状态标志',
    info               varchar(256)                                    comment '信息',
    create_time        timestamp         default current_timestamp     comment '创建时间',
    update_time        timestamp         default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id),
    unique(user_id)
) ENGINE=InnoDB  default CHARSET=utf8 comment='管理员表';

create index ind_adminuser_statusflag on t_admin_user(status_flag);

create table t_log
(
    id                 bigint            not null auto_increment       comment '编号',
    user_name          varchar(128)      not null                      comment '用户名',
    ip		             varchar(64)                                     comment 'IP地址',
    main_type          varchar(3)        not null default '0'          comment '主类型',
    type               varchar(3)        not null default '0'          comment '类型',
    info               varchar(1024)                                   comment '信息',
    create_time        timestamp         default current_timestamp     comment '创建时间',
    update_time        timestamp         default current_timestamp on update current_timestamp comment '更新时间',
    primary key(id)
) ENGINE=InnoDB  default CHARSET=utf8 comment='系统日志表';
