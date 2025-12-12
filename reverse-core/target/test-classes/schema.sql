
CREATE TABLE IF NOT EXISTS `t_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(30) NOT NULL DEFAULT '0',
  `task_title` varchar(30) NOT NULL DEFAULT '0',
  `ctime` datetime DEFAULT NULL,
  `utime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into t_task (task_id, task_title, ctime, utime) values ('1111', 'test', '2018-06-05 12:00:00', '2018-06-05 12:00:00');